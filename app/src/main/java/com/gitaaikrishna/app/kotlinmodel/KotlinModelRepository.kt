package com.gitaaikrishna.app.kotlinmodel

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import kotlin.math.sqrt

/**
 * RAG Pipeline for GitaGPT with Multi-Stage Re-ranking.
 * 
 * Architecture:
 * 1. Query Understanding (LLM) - Detect emotions, therapeutic needs, ideal tone
 * 2. Raw Vector Search - Direct 1536-dim dot product, top_k=5
 * 3. Multi-Stage Re-ranking - Score = SemanticSim + ToneBonus - MismatchPenalty
 * 4. Bridge Synthesis - LLM generates personalized connection
 * 
 * Key improvement: Solves semantic mismatch by matching verse TONE to user's emotional NEEDS.
 */
class KotlinModelRepository(private val context: Context) {
    companion object {
        private const val TAG = "KotlinModelRepository"
        private const val ASSET_BASE = "KotlinModel"
        private const val VERSE_EMB_JSON = "$ASSET_BASE/verse_embeddings_dict.json"
        private const val VERSES_JSON = "$ASSET_BASE/verses_expanded.json"
        private const val STORIES_JSON = "$ASSET_BASE/stories_expanded.json"
        private const val ENRICHED_GITA_JSON = "$ASSET_BASE/enriched_gita_formatted.json"
        
        // Re-ranking constants
        private const val TOP_K = 5                    // Retrieve top 5 candidates
        private const val TONE_MATCH_BONUS = 0.3f      // Bonus for matching tone
        private const val TONE_MISMATCH_PENALTY = 0.2f // Penalty for empathy->challenging mismatch
        private const val EMOTION_MATCH_BONUS = 0.15f  // Bonus for matching emotion category
        
        private val EMOTION_CATEGORIES = EmotionConfig.allEmotionIds
    }

    private val gson = Gson()
    private val initMutex = Mutex()
    @Volatile private var initialized = false

    // Data stores
    private lateinit var verseKeyIds: List<String>
    private lateinit var verseEmbeddings: Map<String, FloatArray>  // Raw 1536-dim embeddings
    private lateinit var verseById: Map<String, ExpandedVerse>
    private lateinit var storyByKey: Map<String, ExpandedStory>
    private lateinit var enrichedVerseById: Map<String, EnrichedVerse>
    private lateinit var versesByEmotion: Map<String, List<EnrichedVerse>>

    suspend fun ensureInitialized() {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return
            try {
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "Initializing KotlinModel assets (direct 1536-dim mode)...")
                    
                    // Load verse data + stories
                    verseById = loadVerses()
                    storyByKey = loadStories()
                    
                    // Load enriched verses with emotion/tone metadata
                    enrichedVerseById = loadEnrichedVerses()
                    versesByEmotion = enrichedVerseById.values.groupBy { it.emotion_category }
                    
                    // Load raw 1536-dim embeddings (NO bi-encoder projection)
                    verseEmbeddings = loadVerseEmbeddings()
                    verseKeyIds = verseEmbeddings.keys.toList()
                    
                    Log.d(TAG, "Initialized: ${verseKeyIds.size} verses, ${storyByKey.size} stories")
                    Log.d(TAG, "Embedding dimensions: 1536 (direct, no projection)")
                    initialized = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize", e)
                throw e
            }
        }
    }

    /**
     * Main matching function with multi-stage re-ranking.
     * 
     * Pipeline:
     * 1. Get query embedding (OpenAI text-embedding-3-small)
     * 2. LLM understands query → emotions, ideal_tone, detected_need
     * 3. Raw vector search → top_k=5 candidates
     * 4. Re-rank with tone matching → final winner
     * 5. Generate bridge (optional)
     */
    suspend fun match(query: String, openAiApiKey: String): MatchResult? {
        if (query.isBlank()) {
            Log.w(TAG, "Empty query")
            return null
        }
        
        try {
            ensureInitialized()
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed", e)
            return null
        }

        val embeddingsClient = OpenAIEmbeddingsClient(openAiApiKey)
        val chatClient = OpenAIChatClient(openAiApiKey)
        
        // ═══════════════════════════════════════════════════════════════════════
        // STEP 1: Get query embedding (1536-dim)
        // ═══════════════════════════════════════════════════════════════════════
        Log.i(TAG, "STEP 1: Getting query embedding...")
        val queryEmbedding = embeddingsClient.embed(query) ?: run {
            Log.e(TAG, "Failed to get embedding")
            return null
        }
        Log.i(TAG, "Query embedding: ${queryEmbedding.size} dimensions")
        
        // ═══════════════════════════════════════════════════════════════════════
        // STEP 2: LLM understands query (emotions, tone needs, therapeutic goal)
        // ═══════════════════════════════════════════════════════════════════════
        Log.i(TAG, "STEP 2: Understanding query with LLM...")
        val understanding = chatClient.understandQuery(query)
        
        val detectedNeed = understanding?.detected_need ?: "empathy_needed"
        val idealTone = understanding?.ideal_tone ?: "philosophical"
        val therapeuticGoal = understanding?.therapeutic_goal ?: "perspective"
        val detectedEmotions = understanding?.emotions ?: emptyList()
        val enhancedQuery = understanding?.enhanced_query ?: query
        val vulnerabilityLevel = understanding?.vulnerability_level ?: "medium"
        
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "QUERY UNDERSTANDING")
        Log.i(TAG, "  Detected Need: $detectedNeed")
        Log.i(TAG, "  Ideal Tone: $idealTone")
        Log.i(TAG, "  Therapeutic Goal: $therapeuticGoal")
        Log.i(TAG, "  Emotions: $detectedEmotions")
        Log.i(TAG, "  Vulnerability: $vulnerabilityLevel")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        // Get enhanced query embedding for better matching
        val enhancedEmbedding = if (enhancedQuery != query) {
            embeddingsClient.embed(enhancedQuery) ?: queryEmbedding
        } else {
            queryEmbedding
        }
        
        // ═══════════════════════════════════════════════════════════════════════
        // STEP 3: Raw vector search - Direct 1536-dim dot product, top_k=5
        // ═══════════════════════════════════════════════════════════════════════
        Log.i(TAG, "STEP 3: Raw vector search (top_k=$TOP_K)...")
        
        val semanticScores = mutableListOf<Pair<String, Float>>()
        for (verseId in verseKeyIds) {
            val verseEmb = verseEmbeddings[verseId] ?: continue
            val similarity = cosineSimilarity(enhancedEmbedding, verseEmb)
            semanticScores.add(verseId to similarity)
        }
        
        // Get top_k candidates
        val topKCandidates = semanticScores
            .sortedByDescending { it.second }
            .take(TOP_K)
        
        Log.i(TAG, "Top $TOP_K semantic matches:")
        topKCandidates.forEachIndexed { i, (id, score) ->
            val verse = enrichedVerseById[id]
            val tone = verse?.getEffectiveTone() ?: "unknown"
            Log.i(TAG, "  ${i+1}. $id (sem=${"%.4f".format(score)}, tone=$tone)")
        }
        
        // ═══════════════════════════════════════════════════════════════════════
        // STEP 4: Multi-stage re-ranking
        // Score = SemanticSim + ToneBonus - MismatchPenalty + EmotionBonus
        // ═══════════════════════════════════════════════════════════════════════
        Log.i(TAG, "STEP 4: Re-ranking with tone matching...")
        
        val rerankedScores = topKCandidates.map { (verseId, semanticScore) ->
            val verse = enrichedVerseById[verseId]
            val verseTone = verse?.getEffectiveTone() ?: EnrichedVerse.TONE_PHILOSOPHICAL
            val verseEmotion = verse?.emotion_category ?: ""
            
            var finalScore = semanticScore
            var bonuses = mutableListOf<String>()
            var penalties = mutableListOf<String>()
            
            // Tone matching bonus/penalty
            when {
                // Tone matches ideal → bonus
                verseTone == idealTone -> {
                    finalScore += TONE_MATCH_BONUS
                    bonuses.add("tone_match +$TONE_MATCH_BONUS")
                }
                
                // User needs empathy but verse is challenging → penalty
                detectedNeed == "empathy_needed" && verseTone == EnrichedVerse.TONE_CHALLENGING -> {
                    finalScore -= TONE_MISMATCH_PENALTY
                    penalties.add("empathy_vs_challenging -$TONE_MISMATCH_PENALTY")
                }
                
                // User needs push but verse is comforting → small penalty
                detectedNeed == "push_needed" && verseTone == EnrichedVerse.TONE_COMFORTING -> {
                    finalScore -= (TONE_MISMATCH_PENALTY / 2)
                    penalties.add("push_vs_comforting -${TONE_MISMATCH_PENALTY / 2}")
                }
            }
            
            // Emotion category match bonus
            if (detectedEmotions.any { it.equals(verseEmotion, ignoreCase = true) }) {
                finalScore += EMOTION_MATCH_BONUS
                bonuses.add("emotion_match +$EMOTION_MATCH_BONUS")
            }
            
            // High vulnerability protection: extra penalty for challenging tone
            if (vulnerabilityLevel == "high" && verseTone == EnrichedVerse.TONE_CHALLENGING) {
                finalScore -= 0.1f
                penalties.add("high_vulnerability_protection -0.1")
            }
            
            RerankCandidate(
                verseId = verseId,
                semanticScore = semanticScore,
                finalScore = finalScore,
                verseTone = verseTone,
                bonuses = bonuses,
                penalties = penalties
            )
        }.sortedByDescending { it.finalScore }
        
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "RE-RANKING RESULTS (User needs: $detectedNeed, Ideal tone: $idealTone)")
        rerankedScores.forEachIndexed { i, candidate ->
            val delta = candidate.finalScore - candidate.semanticScore
            val deltaStr = if (delta >= 0) "+${"%.3f".format(delta)}" else "${"%.3f".format(delta)}"
            Log.i(TAG, "  ${i+1}. ${candidate.verseId}")
            Log.i(TAG, "     Semantic: ${"%.4f".format(candidate.semanticScore)}")
            Log.i(TAG, "     Final: ${"%.4f".format(candidate.finalScore)} ($deltaStr)")
            Log.i(TAG, "     Tone: ${candidate.verseTone}")
            if (candidate.bonuses.isNotEmpty()) Log.i(TAG, "     Bonuses: ${candidate.bonuses}")
            if (candidate.penalties.isNotEmpty()) Log.i(TAG, "     Penalties: ${candidate.penalties}")
        }
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        // Get winner
        val winner = rerankedScores.firstOrNull() ?: return null
        val winnerVerse = enrichedVerseById[winner.verseId] ?: return null
        
        Log.i(TAG, "🏆 WINNER: ${winner.verseId} (score: ${"%.4f".format(winner.finalScore)})")
        
        // ═══════════════════════════════════════════════════════════════════════
        // STEP 5: Generate bridge (LLM synthesis)
        // ═══════════════════════════════════════════════════════════════════════
        Log.i(TAG, "STEP 5: Generating bridge...")
        val bridge = chatClient.generateBridge(
            userQuery = query,
            verse = winnerVerse,
            detectedEmotion = detectedEmotions.firstOrNull(),
            therapeuticGoal = therapeuticGoal
        )
        Log.i(TAG, "Bridge: ${bridge ?: "(none generated)"}")
        
        // Convert to ExpandedVerse for compatibility
        val verse = ExpandedVerse(
            id = winnerVerse.id,
            chapter = winnerVerse.chapter_number,
            verse = winnerVerse.verse_number,
            sanskrit = winnerVerse.sanskrit_text,
            transliteration = winnerVerse.transliteration,
            translation = winnerVerse.english_translation,
            context = winnerVerse.arjuna_despair_link ?: "",
            explanation = winnerVerse.wisdom_nugget,
            detailed_explanation = winnerVerse.modern_problem_match,
            relevant_for = listOf(winnerVerse.emotion_category),
            feeling = listOf(winnerVerse.emotion_category),
            mythology_key = null
        )
        
        val story = verse.mythology_key?.let { storyByKey[it] }
        
        // Get emotion metadata for UI
        val emotionData = detectedEmotions.firstOrNull()?.let { EmotionConfig.getEmotion(it) }
        
        return MatchResult(
            verse = verse,
            story = story,
            score = winner.finalScore,
            debugInfo = MatchDebugInfo(
                userInput = query,
                detectedEmotion = detectedEmotions.firstOrNull(),
                emotionScore = winner.semanticScore,
                emotionVersesCount = TOP_K,
                matchedVerseId = winner.verseId,
                matchingMethod = "multi-stage-rerank",
                allEmotionScores = null,
                enhancedQuery = enhancedQuery,
                emotionEmoji = emotionData?.emoji,
                emotionComfortingMessage = emotionData?.comfortingMessage,
                emotionIcon = emotionData?.icon,
                hindiResponse = null,
                // New re-ranking metadata
                detectedNeed = detectedNeed,
                idealTone = idealTone,
                actualTone = winner.verseTone,
                therapeuticGoal = therapeuticGoal,
                bridge = bridge,
                rerankCandidates = rerankedScores.map { 
                    "${it.verseId}:${"%.3f".format(it.finalScore)}"
                }
            )
        )
    }
    
    /**
     * Cosine similarity between two vectors.
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        
        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0) dotProduct / denominator else 0f
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // Data loading functions
    // ═══════════════════════════════════════════════════════════════════════
    
    private fun loadVerseEmbeddings(): Map<String, FloatArray> {
        return try {
            context.assets.open(VERSE_EMB_JSON).use { input ->
                val reader = InputStreamReader(input)
                val root = gson.fromJson(reader, EmbeddingsRoot::class.java)
                root.embeddings.mapValues { (_, v) -> FloatArray(v.size) { i -> v[i].toFloat() } }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load verse embeddings", e)
            emptyMap()
        }
    }
    
    private fun loadVerses(): Map<String, ExpandedVerse> {
        return try {
            context.assets.open(VERSES_JSON).use { input ->
                val reader = InputStreamReader(input)
                val root = gson.fromJson(reader, VersesRoot::class.java)
                root.verses.associateBy { it.id }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load verses", e)
            emptyMap()
        }
    }
    
    private fun loadStories(): Map<String, ExpandedStory> {
        return try {
            context.assets.open(STORIES_JSON).use { input ->
                val reader = InputStreamReader(input)
                val root = gson.fromJson(reader, StoriesRoot::class.java)
                root.stories.associateBy { it.key }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load stories", e)
            emptyMap()
        }
    }
    
    private fun loadEnrichedVerses(): Map<String, EnrichedVerse> {
        return try {
            context.assets.open(ENRICHED_GITA_JSON).use { input ->
                val reader = InputStreamReader(input)
                val type = object : TypeToken<List<EnrichedVerse>>() {}.type
                val list: List<EnrichedVerse> = gson.fromJson(reader, type)
                list.associateBy { it.id }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load enriched verses", e)
            emptyMap()
        }
    }
}

/**
 * Candidate during re-ranking phase.
 */
data class RerankCandidate(
    val verseId: String,
    val semanticScore: Float,
    val finalScore: Float,
    val verseTone: String,
    val bonuses: List<String> = emptyList(),
    val penalties: List<String> = emptyList()
)

/**
 * Match result returned to UI.
 */
data class MatchResult(
    val verse: ExpandedVerse,
    val story: ExpandedStory?,
    val score: Float,
    val debugInfo: MatchDebugInfo? = null
)

/**
 * Debug info for analysis and UI.
 */
data class MatchDebugInfo(
    val userInput: String? = null,
    val detectedEmotion: String?,
    val emotionScore: Float?,
    val emotionVersesCount: Int = 0,
    val matchedVerseId: String? = null,
    val matchingMethod: String = "multi-stage-rerank",
    val allEmotionScores: Map<String, Float>? = null,
    val enhancedQuery: String? = null,
    val emotionEmoji: String? = null,
    val emotionComfortingMessage: String? = null,
    val emotionIcon: String? = null,
    val hindiResponse: String? = null,
    
    // NEW: Re-ranking metadata
    val detectedNeed: String? = null,        // "empathy_needed" or "push_needed"
    val idealTone: String? = null,           // "comforting", "challenging", "philosophical"
    val actualTone: String? = null,          // Tone of matched verse
    val therapeuticGoal: String? = null,     // "validation", "motivation", etc.
    val bridge: String? = null,              // LLM-generated bridge text
    val rerankCandidates: List<String>? = null  // Top candidates with scores
)

// Note: ExpandedVerse and ExpandedStory are defined in ExpandedModels.kt
