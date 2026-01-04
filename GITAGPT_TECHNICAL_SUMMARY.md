# GitaGPT - RAG System Technical Summary for AI Review

> **Purpose**: This document provides a complete technical overview of the GitaGPT RAG-based chatbot for debugging accuracy issues. It covers data ingestion, retrieval logic, system prompts, and sample traces.

> **Last Updated**: v8 with Multi-Stage Re-ranking to solve Semantic Mismatch problem

---

## 🆕 v8 Changes: Multi-Stage Re-ranking Pipeline

### Problem Solved: Semantic Mismatch
Previously, verses like 2.3 ("Shake off this weakness!") were retrieved for comfort queries because of word overlap with "hopelessness", even though the TONE was wrong (challenging instead of comforting).

### New Architecture
```
User Query → Embedding → LLM Intent Detection → top_k=5 candidates
                                ↓
                    [detectedNeed: empathy_needed/push_needed]
                    [idealTone: comforting/challenging/philosophical]
                                ↓
                         Multi-Stage Re-ranking
                                ↓
            Score = SemanticSim + ToneBonus - MismatchPenalty
                                ↓
                    Winner Verse + LLM Bridge Synthesis
```

### Re-ranking Formula
```
FinalScore = SemanticSimilarity 
           + (ToneMatchBonus if verseTone == idealTone)        // +0.3
           - (MismatchPenalty if empathy_needed && challenging) // -0.2
           + (EmotionBonus if emotion category matches)         // +0.15
           - (VulnerabilityProtection if high && challenging)   // -0.1
```

### New Data Fields (EnrichedVerse)
```kotlin
data class EnrichedVerse(
    // ... existing fields ...
    val tone: String?,              // "comforting", "challenging", "philosophical"
    val therapeutic_goal: String?   // "validation", "motivation", "detachment", etc.
)
```

### New LLM Output (QueryUnderstanding)
```kotlin
data class QueryUnderstanding(
    // ... existing fields ...
    val detected_need: String,      // "empathy_needed" or "push_needed"
    val ideal_tone: String,         // "comforting", "challenging", "philosophical"
    val therapeutic_goal: String,   // "validation", "motivation", etc.
    val vulnerability_level: String // "high", "medium", "low"
)
```

---

## Table of Contents
1. [System Architecture Overview](#1-system-architecture-overview)
2. [Data Ingestion: Chunking & Embedding](#2-data-ingestion-chunking--embedding)
3. [Retrieval Logic: Vector Search Code](#3-retrieval-logic-vector-search-code)
4. [The System Prompt](#4-the-system-prompt)
5. [Sample Trace: Query → Retrieval → Response](#5-sample-trace-query--retrieval--response)
6. [Identified Accuracy Issues](#6-identified-accuracy-issues)
7. [Full Code References](#7-full-code-references)

---

## 1. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            GitaGPT RAG Pipeline                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  User Query: "I feel like giving up on everything"                          │
│       │                                                                     │
│       ▼                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ STEP 1: Query Embedding                                              │   │
│  │ Model: text-embedding-3-small (OpenAI)                               │   │
│  │ Output: 1536-dimensional vector                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│       │                                                                     │
│       ▼                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ STEP 2: Emotion Detection (Embedding Similarity)                     │   │
│  │ Compare query embedding against 17 emotion category embeddings       │   │
│  │ Output: Detected emotion (e.g., "Hopelessness") + confidence score   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│       │                                                                     │
│       ▼                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ STEP 3: Query Understanding (LLM - gpt-4o-mini)                      │   │
│  │ Extract emotions, themes, intent, and create enhanced_query          │   │
│  │ Output: JSON with enhanced query for better semantic matching        │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│       │                                                                     │
│       ▼                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ STEP 4: Semantic Matching (Bi-Encoder + Dot Product)                 │   │
│  │ Score all ~700 verses against enhanced query embedding               │   │
│  │ Apply 20% boost to verses matching detected emotion                  │   │
│  │ Return TOP 1 match only                                              │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│       │                                                                     │
│       ▼                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ STEP 5: Response Generation                                          │   │
│  │ Return matched verse with therapeutic framing                        │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Tech Stack
- **Platform**: Android (Kotlin + Jetpack Compose)
- **Embedding Model**: OpenAI `text-embedding-3-small` (1536 dimensions)
- **LLM for Query Understanding**: OpenAI `gpt-4o-mini`
- **Vector Storage**: In-memory (no external vector DB like Pinecone/Chroma)
- **Bi-Encoder**: Custom TinyBiEncoderModel (trained, stored as `verse_model.bin`)

---

## 2. Data Ingestion: Chunking & Embedding

### 2.1 Verse Data Structure

**No traditional chunking is used.** Each Bhagavad Gita verse is treated as a single semantic unit.

**Primary Data Source**: `enriched_gita_formatted.json` (~700 verses)

```json
{
  "id": "2.47",
  "chapter_number": 2,
  "verse_number": 47,
  "sanskrit_text": "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन ।",
  "transliteration": "karmaṇy evādhikāras te mā phaleṣu kadācana",
  "hindi_translation": "कर्म करने में ही तेरा अधिकार है, फलों में कभी नहीं।",
  "english_translation": "You have a right to action alone, never to its results.",
  "emotion_category": "Result-Obsession",
  "arjuna_despair_link": "Arjuna fears the consequences of action. Krishna reframes action as responsibility without attachment to outcomes.",
  "modern_problem_match": "I'm anxious about the outcome of my efforts and obsessing over results.",
  "wisdom_nugget": "Focus on what you can control - your actions - not what you cannot - the results."
}
```

### 2.2 Embedding Model Details

| Parameter | Value |
|-----------|-------|
| **Model** | `text-embedding-3-small` (OpenAI) |
| **Dimensions** | 1536 |
| **Cost** | $0.02 per 1M tokens |
| **Chunk Size** | N/A (each verse is one unit) |

### 2.3 Pre-computed Verse Embeddings

Verse embeddings are pre-computed and stored in `verse_embeddings_dict.json`:

```json
{
  "2.47": [0.0123, -0.0456, 0.0789, ...],  // 1536 floats
  "2.48": [0.0234, -0.0567, 0.0891, ...],
  // ... ~700 verses
}
```

### 2.4 Bi-Encoder Projection Layer

The raw 1536-dim embeddings are projected through a trained **TinyBiEncoderModel**:

```kotlin
// TinyBiEncoderModel.kt
companion object {
    const val INPUT_DIM = 1536   // OpenAI embedding size
    const val PROJ_DIM = 256    // Projected query dimension
    const val HIDDEN_DIM = 32   // Hidden layer for key encoding
}

// Query encoding: 1536 → 256
fun encodeQuery(x: FloatArray): FloatArray {
    require(x.size == INPUT_DIM)
    val out = FloatArray(PROJ_DIM)
    for (i in 0 until PROJ_DIM) {
        var sum = 0f
        for (j in 0 until INPUT_DIM) sum += queryProjection[i][j] * x[j]
        out[i] = sum
    }
    return out
}

// Similarity scoring: dot product
fun scoreDot(encodedQuery: FloatArray, encodedKey: FloatArray): Float {
    var dot = 0f
    for (i in 0 until PROJ_DIM) dot += encodedQuery[i] * encodedKey[i]
    return dot
}
```

---

## 3. Retrieval Logic: Vector Search Code

### 3.1 Main Matching Function

**File**: `KotlinModelRepository.kt`

```kotlin
suspend fun match(query: String, openAiApiKey: String): MatchResult? {
    ensureInitialized()
    
    val embeddingsClient = OpenAIEmbeddingsClient(openAiApiKey)
    val chatClient = OpenAIChatClient(openAiApiKey)
    
    // STEP 1: Get embedding for user query
    val queryEmbedding = embeddingsClient.embed(query) ?: return null
    
    // STEP 2: Detect emotion using embedding similarity
    val emotionDetectionResult = detectEmotionWithScores(queryEmbedding, embeddingsClient)
    val detectedEmotion = emotionDetectionResult?.emotion  // e.g., "Hopelessness"
    
    // STEP 3: Use LLM to understand query and create enhanced version
    val queryUnderstanding = chatClient.understandQuery(query)
    val enhancedQuery = queryUnderstanding?.enhanced_query ?: query
    
    // Get embedding for enhanced query
    val enhancedQueryEmbedding = embeddingsClient.embed(enhancedQuery) ?: queryEmbedding
    
    // STEP 4: Score ALL verses by semantic similarity
    val encodedQuery = verseModel.encodeQuery(enhancedQueryEmbedding)
    
    val verseScores = mutableListOf<Pair<String, Float>>()
    for (i in verseKeyIds.indices) {
        val verseId = verseKeyIds[i]
        val score = verseModel.scoreDot(encodedQuery, verseKeyEncoded[i])
        verseScores.add(verseId to score)
    }
    
    // Sort by score (highest first)
    verseScores.sortByDescending { it.second }
    
    // STEP 5: Apply emotion boost (20%) to matching verses
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
    
    // Return TOP 1 match only
    val bestMatch = boostedScores.firstOrNull()
    val bestVerseId = bestMatch?.first ?: return null
    
    val verse = enrichedVerseById[bestVerseId] ?: return null
    
    return MatchResult(
        verse = verse,
        score = bestMatch.second,
        debugInfo = MatchDebugInfo(
            detectedEmotion = detectedEmotion,
            matchingMethod = "ai-enhanced-semantic-with-emotion-boost",
            enhancedQuery = enhancedQuery
        )
    )
}
```

### 3.2 Key Retrieval Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| **top_k** | **1** | Only the single best match is returned |
| **Vector Database** | None (in-memory) | Linear scan over ~700 embeddings |
| **Similarity Metric** | Dot product | After bi-encoder projection (256-dim) |
| **Emotion Boost** | 1.2x (20%) | Verses matching detected emotion category |
| **Re-ranking** | None | No secondary re-ranking step |

### 3.3 Emotion Detection Logic

```kotlin
private suspend fun detectEmotionWithScores(
    queryEmbedding: FloatArray,
    client: OpenAIEmbeddingsClient
): EmotionDetectionResult? {
    // Get embeddings for all 17 emotion categories
    val emotionCategories = listOf(
        "Anxiety", "Grief", "Anger", "Attachment", "Burnout",
        "Identity Crisis", "Intellectual Doubt", "Loneliness",
        "Moral Dilemma", "Pride", "Result-Obsession",
        "Fear", "Jealousy", "Guilt", "Hopelessness", "Confusion", "Impatience"
    )
    
    // For each emotion, get multiple contextual phrases and average their embeddings
    val emotionScores = mutableMapOf<String, Float>()
    
    for (emotion in emotionCategories) {
        val phrases = EmotionConfig.getMultipleEmbeddingPhrasesFor(emotion)
        // e.g., for "Hopelessness": ["feeling hopeless and despairing", "lost all hope", ...]
        
        val phraseEmbeddings = phrases.mapNotNull { client.embed(it) }
        val avgEmbedding = averageEmbeddings(phraseEmbeddings)
        
        // Calculate cosine similarity with query
        val score = cosineSimilarity(queryEmbedding, avgEmbedding)
        emotionScores[emotion] = score
    }
    
    // Return highest scoring emotion
    val best = emotionScores.maxByOrNull { it.value }
    return EmotionDetectionResult(
        emotion = best?.key,
        score = best?.value,
        allScores = emotionScores
    )
}
```

---

## 4. The System Prompt

### 4.1 Query Understanding Prompt (gpt-4o-mini)

**File**: `OpenAIChatClient.kt`

```
You are an expert at understanding spiritual and emotional queries in the context of the Bhagavad Gita. Your task is to deeply analyze user queries to help find the most relevant verse from the Gita that addresses their situation.

The Bhagavad Gita covers themes like:
- Anxiety and fear (especially before major decisions or conflicts)
- Grief and loss (mourning, heartbreak, separation)
- Anger and conflict (rage, frustration, resentment)
- Attachment and detachment (clinging, possessiveness, desire)
- Burnout and exhaustion (fatigue, depletion, overwhelm)
- Identity crisis and self-doubt (purpose, meaning, authentic self)
- Intellectual and philosophical doubts (skepticism, questioning faith)
- Loneliness and isolation (abandonment, disconnection)
- Moral dilemmas and ethical conflicts (dharma, duty vs desire)
- Pride and ego (arrogance, vanity, self-importance)
- Result-obsession and attachment to outcomes (perfectionism, fear of failure)
- Fear (terror, dread, apprehension, paralysis)
- Jealousy (envy, comparison, resentment of others' success)
- Guilt (shame, remorse, self-blame, regret)
- Hopelessness (despair, nihilism, futility, giving up)
- Confusion (bewilderment, mental fog, decision paralysis)
- Impatience (restlessness, urgency, frustration with timing)
- Self-realization and spiritual growth
- Detachment from results (nishkama karma)
- Equanimity in success and failure (sthitaprajna)
- The nature of the self and consciousness
- The eternal nature of the soul (atman)
- Dealing with change and impermanence

Your analysis should:
1. Identify the PRIMARY emotion(s) - 1-2 most prominent, in decreasing order of intensity
2. Extract key themes, situations, or life circumstances
3. Understand the core intent - what guidance or wisdom they're seeking
4. Create an enhanced query that captures the essence in terms that would match Gita verses

CRITICAL: The enhanced_query should be written to maximize semantic similarity with Bhagavad Gita verses. Use spiritual, philosophical, and emotional language that aligns with how the Gita addresses these topics.

Respond in JSON format:
{
  "emotions": ["emotion1", "emotion2"],
  "themes": ["theme1", "theme2", "theme3"],
  "intent": "brief description of what they're seeking",
  "enhanced_query": "a refined, detailed version that captures the spiritual/emotional essence in Gita-relevant terms"
}

Emotions must be from this EXACT list (match exactly, case-sensitive): 
Anxiety, Grief, Anger, Attachment, Burnout, Identity Crisis, Intellectual Doubt, Loneliness, Moral Dilemma, Pride, Result-Obsession, Fear, Jealousy, Guilt, Hopelessness, Confusion, Impatience

The enhanced_query is crucial - it should be 2-3 sentences that rephrase the user's situation in a way that would semantically match relevant Gita verses. Include emotional context, the situation, and what kind of guidance is needed. Use Sanskrit/spiritual terms where appropriate (e.g., karma, dharma, moksha, atman).
```

### 4.2 Note: No LLM for Final Response Generation

Currently, the system does **NOT** use an LLM to generate the final response. It uses:
1. Pre-written therapeutic responses (per emotion category)
2. Verse data directly from the JSON (translation, wisdom_nugget, etc.)

---

## 5. Sample Trace: Query → Retrieval → Response

### 5.1 User Query
```
"I feel like giving up on everything"
```

### 5.2 Step 1: Query Embedding
```
Model: text-embedding-3-small
Input: "I feel like giving up on everything"
Output: [0.0123, -0.0456, 0.0789, ...] (1536 floats)
Tokens used: 8
Cost: $0.00000016
```

### 5.3 Step 2: Emotion Detection
```
╔═══════════════════════════════════════════════════════════╗
║                    DETECTED EMOTION                       ║
╠═══════════════════════════════════════════════════════════╣
║  User Query: I feel like giving up on everything          ║
║                                                           ║
║  🎯 CLOSEST EMOTION: Hopelessness                         ║
║  📊 Emotion Score: 0.7234                                 ║
║                                                           ║
║  All Emotion Scores (Top 5):                              ║
║    • Hopelessness: 0.7234                                 ║
║    • Burnout: 0.6891                                      ║
║    • Grief: 0.6543                                        ║
║    • Loneliness: 0.6102                                   ║
║    • Anxiety: 0.5890                                      ║
╚═══════════════════════════════════════════════════════════╝
```

### 5.4 Step 3: LLM Query Understanding
**Request to gpt-4o-mini:**
```
User Query: "I feel like giving up on everything"

Analyze this query deeply...
```

**Response:**
```json
{
  "emotions": ["Hopelessness", "Burnout"],
  "themes": ["surrender", "exhaustion", "despair", "loss of motivation"],
  "intent": "seeking reason to continue, finding meaning",
  "enhanced_query": "I am overwhelmed by despair and the weight of existence feels unbearable. I seek wisdom on why I should continue my dharma when all feels futile and my spirit is crushed by the burden of worldly struggles."
}
```

### 5.5 Step 4: Semantic Matching
```
Enhanced Query Embedding generated...

Scoring all 700 verses...

Top 5 semantic matches (before emotion boost):
  Verse 2.3:  0.8234 (Arjuna's dejection)
  Verse 2.2:  0.7891 (Krishna's rebuke)
  Verse 18.66: 0.7654 (Surrender to me)
  Verse 2.47: 0.7543 (Right to action only)
  Verse 6.5:  0.7234 (Elevate yourself by yourself)

Applying 20% emotion boost to "Hopelessness" category verses...

Top 5 after emotion boost:
  Verse 2.3:  0.9881 ✓ (boosted - matches Hopelessness)
  Verse 2.2:  0.7891
  Verse 18.66: 0.7654
  Verse 2.47: 0.7543
  Verse 6.5:  0.7234
```

### 5.6 Step 5: Final Match Result
```
╔═══════════════════════════════════════════════════════════╗
║                    FINAL MATCH RESULT                     ║
╠═══════════════════════════════════════════════════════════╣
║  Original Query: I feel like giving up on everything      ║
║  Enhanced Query: I am overwhelmed by despair...           ║
║  Matched Verse: 2.3                                       ║
║  Emotion: Hopelessness                                    ║
║  Score: 0.9881                                            ║
║  Method: ai-enhanced-semantic-with-emotion-boost          ║
╚═══════════════════════════════════════════════════════════╝
```

### 5.7 Retrieved Verse
```json
{
  "id": "2.3",
  "chapter_number": 2,
  "verse_number": 3,
  "sanskrit_text": "क्लैब्यं मा स्म गमः पार्थ नैतत्त्वय्युपपद्यते...",
  "english_translation": "O Arjuna, do not yield to this degrading impotence. It does not befit you. Shake off this petty weakness of heart and arise, O scorcher of enemies!",
  "emotion_category": "Hopelessness",
  "wisdom_nugget": "Rise above weakness and reclaim your strength."
}
```

### 5.8 Final Response to User
```
Therapeutic Response:
"When hope feels far away, please know that darkness always precedes dawn. 
You reaching out is itself a spark of hope. Krishna spoke about finding 
light in the darkest moments..."

Verse 2.3:
"O Arjuna, do not yield to this degrading impotence. It does not befit you. 
Shake off this petty weakness of heart and arise, O scorcher of enemies!"

Reflection:
"Rise above weakness and reclaim your strength."
```

---

## 6. Identified Accuracy Issues

### 6.1 The Problem with the Sample Trace

The matched verse (2.3) is **topically correct** (about despair/hopelessness) but **tonally wrong**:

| Aspect | What User Needs | What Verse 2.3 Provides |
|--------|----------------|------------------------|
| **Tone** | Comfort, empathy, gentleness | Rebuke, challenge, tough love |
| **Message** | "It's okay to feel this way" | "Don't be weak, arise!" |
| **Approach** | Validation first | Motivation/push |

**Better matches might be:**
- **2.11**: "The wise grieve neither for the living nor for the dead" (philosophical comfort)
- **18.66**: "Abandon all dharmas and surrender to Me alone; I shall liberate you" (reassurance)
- **9.22**: "Those who worship Me with devotion, I carry what they lack and preserve what they have" (support)

### 6.2 Root Causes

| Issue | Description | Impact |
|-------|-------------|--------|
| **top_k = 1** | Only returns single best semantic match | No diversity, no fallback options |
| **No tone classification** | Verses aren't tagged by tone (comforting vs. challenging) | Wrong emotional fit |
| **Binary emotion boost** | 20% boost for any verse in emotion category | Doesn't distinguish between appropriate vs. inappropriate verses within category |
| **No intent-based filtering** | "Need comfort" vs. "need motivation" treated same | Mismatched therapeutic approach |
| **No re-ranking** | Single-stage retrieval | No opportunity to filter by additional criteria |
| **Enhanced query may mislead** | LLM enhances semantically but not therapeutically | Better match ≠ better response |

### 6.3 Suggested Improvements

1. **Increase top_k to 5-10** and add re-ranking based on therapeutic intent
2. **Add tone metadata** to verses: `"tone": "comforting" | "challenging" | "philosophical"`
3. **Classify user intent**: Does user need validation, motivation, or perspective?
4. **Use LLM for final selection**: Given top-5 matches + user intent, pick best one
5. **A/B test verse responses**: Track user engagement to learn preferences

---

## 7. Full Code References

### 7.1 File Locations

```
app/src/main/java/com/gita/app/kotlinmodel/
├── KotlinModelRepository.kt    # Main matching logic
├── OpenAIEmbeddingsClient.kt   # Embedding API client
├── OpenAIChatClient.kt         # LLM for query understanding
├── TinyBiEncoderModel.kt       # Bi-encoder projection model
├── EmotionConfig.kt            # Emotion categories and phrases
├── FastMatcher.kt              # Fallback keyword-based matcher
└── EnrichedModels.kt           # Data models

app/src/main/assets/KotlinModel/
├── enriched_gita_formatted.json   # ~700 verses with metadata
├── verse_embeddings_dict.json     # Pre-computed embeddings
├── verse_model.bin                # Trained bi-encoder weights
└── stories_expanded.json          # Related mythological stories
```

### 7.2 Key Data Structures

```kotlin
// Enriched verse from JSON
data class EnrichedVerse(
    val id: String,
    val chapter_number: Int,
    val verse_number: Int,
    val sanskrit_text: String,
    val transliteration: String,
    val hindi_translation: String,
    val english_translation: String,
    val emotion_category: String,
    val arjuna_despair_link: String?,
    val modern_problem_match: String?,
    val wisdom_nugget: String?
)

// Match result returned to UI
data class MatchResult(
    val verse: ExpandedVerse,
    val story: ExpandedStory?,
    val score: Float,
    val debugInfo: MatchDebugInfo?
)

// Debug info for analysis
data class MatchDebugInfo(
    val userInput: String?,
    val detectedEmotion: String?,
    val emotionScore: Float?,
    val matchedVerseId: String?,
    val matchingMethod: String,
    val allEmotionScores: Map<String, Float>?,
    val enhancedQuery: String?,
    val emotionEmoji: String?,
    val emotionComfortingMessage: String?,
    val hindiResponse: String?
)
```

---

## 8. Questions for the Reviewer

1. **Is the bi-encoder projection layer (1536→256) hurting semantic precision?**
2. **Should we use a cross-encoder for re-ranking instead of just bi-encoder?**
3. **How can we incorporate "therapeutic intent" into the matching?**
4. **Would fine-tuning the embedding model on Gita-specific data help?**
5. **Should the final response use an LLM to synthesize verse + context?**

---

*Document generated for AI review. Last updated: January 2026*

