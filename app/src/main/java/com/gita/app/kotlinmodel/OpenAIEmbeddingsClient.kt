package com.gita.app.kotlinmodel

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Minimal OpenAI embeddings client (Kotlin-only).
 * Uses the embeddings endpoint and returns a 1536-dim vector for text-embedding-3-small.
 */
class OpenAIEmbeddingsClient(
    private val apiKey: String,
    private val model: String = "text-embedding-3-small"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun embed(text: String): FloatArray? = withContext(Dispatchers.IO) {
        try {
            val payload = gson.toJson(
                mapOf(
                    "model" to model,
                    "input" to text
                )
            )

            val req = Request.Builder()
                .url("https://api.openai.com/v1/embeddings")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e("OpenAIEmbeddingsClient", "Embeddings call failed: ${resp.code} ${resp.message}")
                    return@withContext null
                }
                val body = resp.body?.string() ?: return@withContext null
                val parsed = gson.fromJson(body, EmbeddingResponse::class.java)
                val embedding = parsed.data?.firstOrNull()?.embedding ?: return@withContext null
                FloatArray(embedding.size) { i -> embedding[i].toFloat() }
            }
        } catch (e: Exception) {
            Log.e("OpenAIEmbeddingsClient", "Embeddings call crashed", e)
            null
        }
    }

    private data class EmbeddingResponse(
        val data: List<EmbeddingData>?
    )

    private data class EmbeddingData(
        val embedding: List<Double>
    )
}


