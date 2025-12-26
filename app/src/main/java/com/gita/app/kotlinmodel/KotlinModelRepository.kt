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
    }

    private val gson = Gson()
    private val initMutex = Mutex()
    @Volatile private var initialized = false

    private lateinit var verseModel: TinyBiEncoderModel
    private lateinit var verseKeyIds: List<String>
    private lateinit var verseKeyEncoded: List<FloatArray> // [N][256]
    private lateinit var verseById: Map<String, ExpandedVerse>
    private lateinit var storyByKey: Map<String, ExpandedStory>

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

        val client = OpenAIEmbeddingsClient(openAiApiKey)
        val embedding = client.embed(query) ?: run {
            Log.w(TAG, "Failed to get embedding for query: ${query.take(50)}...")
            return null
        }
        
        if (embedding.size != TinyBiEncoderModel.INPUT_DIM) {
            Log.e(TAG, "Embedding dim mismatch: ${embedding.size} (expected ${TinyBiEncoderModel.INPUT_DIM})")
            return null
        }

        val encodedQuery = verseModel.encodeQuery(embedding)

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
        
        Log.d(TAG, "Match found: verse=${verseId}, score=$bestScore, hasStory=${story != null}")

        return MatchResult(
            verse = verse,
            story = story,
            score = bestScore
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
    val score: Float
)


