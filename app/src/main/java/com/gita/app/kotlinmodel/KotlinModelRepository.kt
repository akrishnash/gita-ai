package com.gita.app.kotlinmodel

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

/**
 * Loads KotlinModel assets and performs ML matching (Kotlin-only).
 *
 * - Verse selection uses the trained verse bi-encoder model + precomputed verse embeddings.
 * - Story selection is primarily via verse.mythology_key -> stories_expanded.json (best coherence).
 *   If no linked story is found, we fall back to a very small heuristic (no story-model right now),
 *   because story embeddings in this repo are 3072-dim and require a projection not present here.
 */
class KotlinModelRepository(private val context: Context) {
    companion object {
        private const val TAG = "KotlinModelRepository"
        private const val ASSET_BASE = "KotlinModel"
        private const val VERSE_MODEL_BIN = "$ASSET_BASE/verse_model.bin"
        private const val VERSE_EMB_JSON = "$ASSET_BASE/verse_embeddings_dict.json"
        private const val VERSES_JSON = "$ASSET_BASE/verses_expanded.json"
        private const val STORIES_JSON = "$ASSET_BASE/stories_expanded.json"
        private const val ENRICHED_GITA_JSON = "$ASSET_BASE/enriched_gita_formatted.json"
        
        // Emotion categories for matching
        private val EMOTION_CATEGORIES = listOf(
            "Anxiety", "Grief", "Anger", "Attachment", "Burnout",
            "Identity Crisis", "Intellectual Doubt", "Loneliness",
            "Moral Dilemma", "Pride", "Result-Obsession"
        )
    }

    private val gson = Gson()
    private val initMutex = Mutex()
    @Volatile private var initialized = false

    private lateinit var verseModel: TinyBiEncoderModel
    private lateinit var verseKeyIds: List<String>
    private lateinit var verseKeyEncoded: List<FloatArray> // [N][256]
    private lateinit var verseById: Map<String, ExpandedVerse>
    private lateinit var storyByKey: Map<String, ExpandedStory>
    private lateinit var enrichedVerseById: Map<String, EnrichedVerse>
    private lateinit var versesByEmotion: Map<String, List<EnrichedVerse>> // emotion_category -> verses

    suspend fun ensureInitialized() {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return
            try {
                withContext(Dispatchers.IO) {
                    Log.d(TAG, "Initializing KotlinModel assets...")
                    verseModel = TinyBiEncoderModel(context, VERSE_MODEL_BIN)

                    // Load verse data + stories (for display)
                    verseById = loadVerses()
                    storyByKey = loadStories()
                    
                    // Load enriched verses with emotion categories
                    enrichedVerseById = loadEnrichedVerses()
                    versesByEmotion = enrichedVerseById.values.groupBy { it.emotion_category }
                    Log.d(TAG, "Loaded ${enrichedVerseById.size} enriched verses with ${versesByEmotion.size} emotion categories")

                    // Load verse embeddings and pre-encode keys once
                    val verseEmbeddings = loadVerseEmbeddings()
                    if (verseEmbeddings.isEmpty()) {
                        Log.w(TAG, "Warning: No verse embeddings loaded!")
                    }
                    val ids = ArrayList<String>(verseEmbeddings.size)
                    val encoded = ArrayList<FloatArray>(verseEmbeddings.size)
                    for ((id, emb) in verseEmbeddings) {
                        ids.add(id)
                        encoded.add(verseModel.encodeKey(emb))
                    }
                    verseKeyIds = ids
                    verseKeyEncoded = encoded

                    Log.d(TAG, "Initialized successfully. verses=${verseKeyIds.size}, stories=${storyByKey.size}")
                    initialized = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize KotlinModel repository", e)
                throw e
            }
        }
    }

    suspend fun match(query: String, openAiApiKey: String): MatchResult? {
        if (query.isBlank()) {
            Log.w(TAG, "Empty query provided to match()")
            return null
        }
        
        try {
            ensureInitialized()
        } catch (e: Exception) {
            Log.e(TAG, "Model initialization failed during match", e)
            return null
        }

        if (verseKeyEncoded.isEmpty()) {
            Log.e(TAG, "No verse embeddings available for matching")
            return null
        }

        val embeddingsClient = OpenAIEmbeddingsClient(openAiApiKey)
        val chatClient = OpenAIChatClient(openAiApiKey)
        
        // STEP 1: Get embedding for user query FIRST
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "STEP 1: GETTING EMBEDDING FOR USER QUERY")
        Log.i(TAG, "Original Query: $query")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        val queryEmbedding = embeddingsClient.embed(query) ?: run {
            Log.e(TAG, "Failed to get embedding for query: ${query.take(50)}...")
            return null
        }
        
        if (queryEmbedding.size != TinyBiEncoderModel.INPUT_DIM) {
            Log.e(TAG, "Embedding dim mismatch: ${queryEmbedding.size} (expected ${TinyBiEncoderModel.INPUT_DIM})")
            return null
        }
        
        // STEP 2: DETECT CLOSEST EMOTION USING EMBEDDINGS (BEFORE ANYTHING ELSE)
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "STEP 2: DETECTING CLOSEST EMOTION FROM EMBEDDING")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        val emotionDetectionResult = detectEmotionWithScores(queryEmbedding, embeddingsClient)
        val detectedEmotionFromEmbedding = emotionDetectionResult?.emotion
        val emotionScoreFromEmbedding = emotionDetectionResult?.score
        val allEmotionScoresFromEmbedding = emotionDetectionResult?.allScores
        
        // PRINT EMOTION TO CONSOLE PROMINENTLY
        val queryDisplay = if (query.length > 50) query.take(47) + "..." else query
        println("")
        println("╔═══════════════════════════════════════════════════════════╗")
        println("║                    DETECTED EMOTION                       ║")
        println("╠═══════════════════════════════════════════════════════════╣")
        println("║  User Query: $queryDisplay")
        println("║  ")
        if (detectedEmotionFromEmbedding != null) {
            println("║  🎯 CLOSEST EMOTION: $detectedEmotionFromEmbedding")
            emotionScoreFromEmbedding?.let { score ->
                println("║  📊 Emotion Score: ${"%.4f".format(score)}")
            }
        } else {
            println("║  ⚠️  NO EMOTION DETECTED")
        }
        println("║  ")
        allEmotionScoresFromEmbedding?.let { scores ->
            println("║  All Emotion Scores:")
            scores.toList().sortedByDescending { it.second }.take(5).forEach { (emotion, score) ->
                println("║    • $emotion: ${"%.4f".format(score)}")
            }
        }
        println("╚═══════════════════════════════════════════════════════════╝")
        println("")
        
        // Log to Logcat as well
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "🎯 DETECTED EMOTION FROM EMBEDDING:")
        Log.i(TAG, "   User Query: $query")
        Log.i(TAG, "   CLOSEST EMOTION: $detectedEmotionFromEmbedding")
        Log.i(TAG, "   Emotion Score: $emotionScoreFromEmbedding")
        if (allEmotionScoresFromEmbedding != null) {
            Log.i(TAG, "   Top 5 Emotion Scores:")
            allEmotionScoresFromEmbedding.toList().sortedByDescending { it.second }.take(5).forEach { (emotion, score) ->
                Log.i(TAG, "     • $emotion: ${"%.4f".format(score)}")
            }
        }
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        // STEP 3: Use OpenAI to understand the query better (for enhanced matching)
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "STEP 3: UNDERSTANDING QUERY WITH OPENAI")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        val queryUnderstanding = chatClient.understandQuery(query)
        val enhancedQuery = queryUnderstanding?.enhanced_query ?: query
        val aiDetectedEmotions = queryUnderstanding?.emotions ?: emptyList()
        
        Log.i(TAG, "Query Understanding Result:")
        Log.i(TAG, "  Enhanced Query: $enhancedQuery")
        Log.i(TAG, "  AI Detected Emotions: $aiDetectedEmotions")
        Log.i(TAG, "  Themes: ${queryUnderstanding?.themes}")
        Log.i(TAG, "  Intent: ${queryUnderstanding?.intent}")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        // Get embedding for enhanced query for better semantic matching
        val enhancedQueryEmbedding = embeddingsClient.embed(enhancedQuery) ?: queryEmbedding

        // STEP 4: Use embedding-based emotion as primary (already detected above)
        // Prefer embedding-based emotion, but also consider AI-detected emotions
        val detectedEmotion: String?
        val emotionScore: Float?
        val allEmotionScores: Map<String, Float>?
        
        // Use embedding-based emotion detection as primary (already done above)
        detectedEmotion = detectedEmotionFromEmbedding
        emotionScore = emotionScoreFromEmbedding
        allEmotionScores = allEmotionScoresFromEmbedding
        
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "FINAL EMOTION SELECTION")
        Log.i(TAG, "Query: $query")
        Log.i(TAG, "Enhanced Query: $enhancedQuery")
        Log.i(TAG, "Selected Emotion (from embedding): $detectedEmotion")
        Log.i(TAG, "Emotion Score: $emotionScore")
        Log.i(TAG, "AI Detected Emotions: $aiDetectedEmotions")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        
        // STEP 5: PRIMARY MATCHING - Use semantic similarity with enhanced query across ALL verses
        // This ensures we find the most semantically relevant verse, not just emotion-filtered ones
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "STEP 4: PERFORMING SEMANTIC MATCHING")
        Log.i(TAG, "Using enhanced query for semantic matching across all verses...")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        val encodedQuery = verseModel.encodeQuery(enhancedQueryEmbedding)
        
        // Score all verses by semantic similarity
        val verseScores = mutableListOf<Pair<String, Float>>()
        for (i in verseKeyIds.indices) {
            val verseId = verseKeyIds[i]
            val score = verseModel.scoreDot(encodedQuery, verseKeyEncoded[i])
            verseScores.add(verseId to score)
        }
        
        // Sort by score (highest first)
        verseScores.sortByDescending { it.second }
        
        Log.i(TAG, "Top 5 semantic matches:")
        verseScores.take(5).forEach { (verseId, score) ->
            Log.i(TAG, "  Verse $verseId: $score")
        }
        
        // Step 5: Apply emotion boost if emotion was detected
        // Boost verses that match the detected emotion by 20%
        val emotionBoost = 1.2f
        val boostedScores = if (detectedEmotion != null) {
            val emotionVerses = versesByEmotion[detectedEmotion]?.map { it.id }?.toSet() ?: emptySet()
            verseScores.map { (verseId, score) ->
                val boosted = if (emotionVerses.contains(verseId)) {
                    score * emotionBoost
                } else {
                    score
                }
                verseId to boosted
            }.sortedByDescending { it.second }
        } else {
            verseScores
        }
        
        Log.i(TAG, "Top 5 after emotion boost:")
        boostedScores.take(5).forEach { (verseId, score) ->
            val originalScore = verseScores.find { it.first == verseId }?.second ?: 0f
            val isBoosted = detectedEmotion != null && 
                (versesByEmotion[detectedEmotion]?.any { it.id == verseId } == true)
            Log.i(TAG, "  Verse $verseId: $score (original: $originalScore, boosted: $isBoosted)")
        }
        
        // Get the best matching verse
        val bestMatch = boostedScores.firstOrNull()
        val bestVerseId = bestMatch?.first
        val bestScore = bestMatch?.second ?: Float.NEGATIVE_INFINITY
        
        if (bestVerseId == null) {
            Log.e(TAG, "No verse found - this should not happen")
            return null
        }
        
        val emotionVerses = if (detectedEmotion != null) {
            versesByEmotion[detectedEmotion] ?: emptyList()
        } else {
            emptyList()
        }
        
        // Step 5: Get the verse data (prefer enriched, fallback to expanded)
        val enrichedVerse = enrichedVerseById[bestVerseId]
        val verse = if (enrichedVerse != null) {
            // Convert EnrichedVerse to ExpandedVerse
            ExpandedVerse(
                id = enrichedVerse.id,
                chapter = enrichedVerse.chapter_number,
                verse = enrichedVerse.verse_number,
                sanskrit = enrichedVerse.sanskrit_text,
                transliteration = enrichedVerse.transliteration,
                translation = enrichedVerse.english_translation,
                context = enrichedVerse.arjuna_despair_link ?: "",
                explanation = enrichedVerse.wisdom_nugget,
                detailed_explanation = enrichedVerse.modern_problem_match,
                relevant_for = listOf(enrichedVerse.emotion_category),
                feeling = listOf(enrichedVerse.emotion_category),
                mythology_key = null
            )
        } else {
            verseById[bestVerseId] ?: run {
                Log.w(TAG, "Verse $bestVerseId not found in any data source")
                return null
            }
        }

        val story = verse.mythology_key?.let { storyByKey[it] }
        
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "FINAL MATCH RESULT")
        Log.i(TAG, "Original Query: $query")
        Log.i(TAG, "Enhanced Query: $enhancedQuery")
        Log.i(TAG, "Matched Verse: $bestVerseId")
        Log.i(TAG, "Emotion: $detectedEmotion")
        Log.i(TAG, "Score: $bestScore")
        Log.i(TAG, "Method: ai-enhanced-semantic-with-emotion-boost")
        Log.i(TAG, "═══════════════════════════════════════════════════════")

        return MatchResult(
            verse = verse,
            story = story,
            score = bestScore,
            debugInfo = MatchDebugInfo(
                userInput = query,
                detectedEmotion = detectedEmotion,
                emotionScore = emotionScore,
                emotionVersesCount = emotionVerses.size,
                matchedVerseId = bestVerseId,
                matchingMethod = "ai-enhanced-semantic-with-emotion-boost",
                allEmotionScores = allEmotionScores,
                enhancedQuery = enhancedQuery
            )
        )
    }
    
    /**
     * Detects emotion from user query by comparing with emotion category embeddings.
     * Returns emotion with scores for all categories.
     */
    private suspend fun detectEmotionWithScores(
        queryEmbedding: FloatArray,
        client: OpenAIEmbeddingsClient
    ): EmotionDetectionResult? {
        return withContext(Dispatchers.IO) {
            try {
                // Create embeddings for each emotion category
                val emotionEmbeddings = mutableMapOf<String, FloatArray>()
                for (emotion in EMOTION_CATEGORIES) {
                    val emb = client.embed(emotion) ?: continue
                    if (emb.size == TinyBiEncoderModel.INPUT_DIM) {
                        emotionEmbeddings[emotion] = emb
                    }
                }
                
                if (emotionEmbeddings.isEmpty()) {
                    Log.w(TAG, "No emotion embeddings created")
                    return@withContext null
                }
                
                // Compare query embedding with each emotion embedding
                val encodedQuery = verseModel.encodeQuery(queryEmbedding)
                var bestEmotion: String? = null
                var bestScore = Float.NEGATIVE_INFINITY
                val allScores = mutableMapOf<String, Float>()
                
                for ((emotion, emb) in emotionEmbeddings) {
                    val encodedEmotion = verseModel.encodeKey(emb)
                    val score = verseModel.scoreDot(encodedQuery, encodedEmotion)
                    allScores[emotion] = score
                    if (score > bestScore) {
                        bestScore = score
                        bestEmotion = emotion
                    }
                }
                
                // Always return the best emotion (remove threshold to ensure emotion-based matching is used)
                if (bestEmotion != null) {
                    Log.i(TAG, "Emotion detected: $bestEmotion (score: $bestScore)")
                    val sortedScores = allScores.toList().sortedByDescending { it.second }
                    Log.i(TAG, "Top 3 emotion scores:")
                    sortedScores.take(3).forEach { (emotion, score) ->
                        Log.i(TAG, "  $emotion: $score")
                    }
                    return@withContext EmotionDetectionResult(
                        emotion = bestEmotion,
                        score = bestScore,
                        allScores = allScores
                    )
                }
                
                Log.w(TAG, "No emotion detected. All emotion scores: $allScores")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting emotion", e)
                null
            }
        }
    }
    
    private data class EmotionDetectionResult(
        val emotion: String,
        val score: Float,
        val allScores: Map<String, Float>
    )
    
    /**
     * Fallback: Match by semantic similarity (original method).
     */
    private fun matchBySemanticSimilarity(queryEmbedding: FloatArray): MatchResult? {
        val encodedQuery = verseModel.encodeQuery(queryEmbedding)

        var bestIdx = -1
        var bestScore = Float.NEGATIVE_INFINITY
        for (i in verseKeyEncoded.indices) {
            val s = verseModel.scoreDot(encodedQuery, verseKeyEncoded[i])
            if (s > bestScore) {
                bestScore = s
                bestIdx = i
            }
        }
        if (bestIdx < 0) {
            Log.w(TAG, "No valid match found (all scores were negative infinity)")
            return null
        }

        val verseId = verseKeyIds[bestIdx]
        val verse = verseById[verseId] ?: run {
            Log.w(TAG, "Best verse id $verseId not found in verses_expanded.json")
            return null
        }

        val story = verse.mythology_key?.let { storyByKey[it] }
        
        Log.d(TAG, "Semantic match found: verse=${verseId}, score=$bestScore, hasStory=${story != null}")

        return MatchResult(
            verse = verse,
            story = story,
            score = bestScore,
            debugInfo = null // No debug info for semantic fallback
        )
    }

    private fun loadVerses(): Map<String, ExpandedVerse> {
        return try {
            context.assets.open(VERSES_JSON).use { input ->
                val root = gson.fromJson(InputStreamReader(input, Charsets.UTF_8), VersesRoot::class.java)
                val verses = root.verses.associateBy { it.id }
                if (verses.isEmpty()) {
                    Log.w(TAG, "Warning: No verses loaded from $VERSES_JSON")
                }
                verses
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load verses from $VERSES_JSON", e)
            emptyMap()
        }
    }

    private fun loadStories(): Map<String, ExpandedStory> {
        return try {
            context.assets.open(STORIES_JSON).use { input ->
                val root = gson.fromJson(InputStreamReader(input, Charsets.UTF_8), StoriesRoot::class.java)
                val stories = root.stories.associateBy { it.key }
                if (stories.isEmpty()) {
                    Log.w(TAG, "Warning: No stories loaded from $STORIES_JSON")
                }
                stories
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load stories from $STORIES_JSON", e)
            emptyMap()
        }
    }

    /**
     * Streaming loader for verse embeddings to avoid building gigantic intermediate objects.
     * Expected format: { "model": "...", "dimension": 1536, "embeddings": { "1.28": [..], ... } }
     */
    private fun loadVerseEmbeddings(): Map<String, FloatArray> {
        val result = LinkedHashMap<String, FloatArray>(1024)

        return try {
            context.assets.open(VERSE_EMB_JSON).use { raw ->
                val reader = JsonReader(InputStreamReader(raw, Charsets.UTF_8))
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "embeddings" -> {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                val id = reader.nextName()
                                val vec = readFloatArray(reader)
                                if (vec.size == TinyBiEncoderModel.INPUT_DIM) {
                                    result[id] = vec
                                } else {
                                    Log.w(TAG, "Skipping $id embedding dim=${vec.size} (expected ${TinyBiEncoderModel.INPUT_DIM})")
                                    // still consume
                                }
                            }
                            reader.endObject()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }
            if (result.isEmpty()) {
                Log.w(TAG, "Warning: No valid verse embeddings loaded from $VERSE_EMB_JSON")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load verse embeddings from $VERSE_EMB_JSON", e)
            emptyMap()
        }
    }

    private fun loadEnrichedVerses(): Map<String, EnrichedVerse> {
        return try {
            context.assets.open(ENRICHED_GITA_JSON).use { input ->
                // The file is a direct array, not wrapped in an object
                val versesList = gson.fromJson(
                    InputStreamReader(input, Charsets.UTF_8),
                    Array<EnrichedVerse>::class.java
                )
                val verses = versesList.associateBy { it.id }
                if (verses.isEmpty()) {
                    Log.w(TAG, "Warning: No enriched verses loaded from $ENRICHED_GITA_JSON")
                } else {
                    Log.i(TAG, "✅ Loaded ${verses.size} enriched verses with emotion categories")
                }
                verses
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load enriched verses from $ENRICHED_GITA_JSON", e)
            e.printStackTrace()
            emptyMap()
        }
    }

    private fun readFloatArray(reader: JsonReader): FloatArray {
        val values = ArrayList<Float>(TinyBiEncoderModel.INPUT_DIM)
        reader.beginArray()
        while (reader.hasNext()) {
            // JsonReader returns double for numbers; convert to float
            values.add(reader.nextDouble().toFloat())
        }
        reader.endArray()
        return FloatArray(values.size) { i -> values[i] }
    }
}

data class MatchResult(
    val verse: ExpandedVerse,
    val story: ExpandedStory?,
    val score: Float,
    val debugInfo: MatchDebugInfo? = null
)

data class MatchDebugInfo(
    val userInput: String,
    val detectedEmotion: String?,
    val emotionScore: Float?,
    val emotionVersesCount: Int,
    val matchedVerseId: String,
    val matchingMethod: String, // "emotion-based" or "semantic-similarity" or "ai-enhanced-emotion-based"
    val allEmotionScores: Map<String, Float>? = null,
    val enhancedQuery: String? = null
)


