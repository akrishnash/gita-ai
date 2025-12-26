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
 * Tracks token usage and costs.
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
    
    companion object {
        private const val TAG = "OpenAIEmbeddingsClient"
        
        // OpenAI pricing per 1M tokens (as of 2024)
        // text-embedding-3-small: $0.02 per 1M tokens
        // text-embedding-3-large: $0.13 per 1M tokens
        // text-embedding-ada-002: $0.10 per 1M tokens (legacy)
        private fun getCostPerMillionTokens(modelName: String): Double {
            return when {
                modelName.contains("embedding-3-small", ignoreCase = true) -> 0.02
                modelName.contains("embedding-3-large", ignoreCase = true) -> 0.13
                modelName.contains("ada-002", ignoreCase = true) -> 0.10
                else -> 0.02 // Default to small model pricing
            }
        }
        
        private fun calculateCost(tokens: Int, modelName: String): Double {
            val costPerMillion = getCostPerMillionTokens(modelName)
            return (tokens / 1_000_000.0) * costPerMillion
        }
    }

    suspend fun embed(text: String): FloatArray? = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            Log.w(TAG, "Empty text provided for embedding")
            return@withContext null
        }
        
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
                    val errorBody = resp.body?.string() ?: "No error body"
                    Log.e(TAG, "Embeddings call failed: ${resp.code} ${resp.message}. Body: ${errorBody.take(200)}")
                    return@withContext null
                }
                val body = resp.body?.string() ?: run {
                    Log.e(TAG, "Empty response body")
                    return@withContext null
                }
                val parsed = gson.fromJson(body, EmbeddingResponse::class.java)
                
                // Log token usage and cost
                parsed.usage?.let { usage ->
                    val promptTokens = usage.prompt_tokens ?: 0
                    val totalTokens = usage.total_tokens ?: promptTokens
                    val cost = calculateCost(totalTokens, model)
                    
                    // Record in session tracker
                    OpenAIUsageTracker.recordUsage(
                        model = model,
                        promptTokens = promptTokens,
                        completionTokens = 0,
                        totalTokens = totalTokens
                    )
                    
                    // Use both Log and System.out for maximum visibility
                    val costStr = "%.6f".format(cost)
                    val logMessage = """
                        ═══════════════════════════════════════════════════════
                        OpenAI API Usage - Embeddings
                        Model: $model
                        Prompt Tokens: $promptTokens
                        Total Tokens: $totalTokens
                        Cost: $$costStr
                        ═══════════════════════════════════════════════════════
                    """.trimIndent()
                    
                    // Log at INFO level (visible in Logcat)
                    Log.i(TAG, logMessage)
                    // Also print to System.out (visible in adb logcat)
                    println(logMessage)
                }
                
                val embedding = parsed.data?.firstOrNull()?.embedding ?: run {
                    Log.e(TAG, "No embedding data in response")
                    return@withContext null
                }
                if (embedding.isEmpty()) {
                    Log.e(TAG, "Empty embedding vector")
                    return@withContext null
                }
                FloatArray(embedding.size) { i -> embedding[i].toFloat() }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Embeddings call timed out", e)
            null
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: cannot reach OpenAI API", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Embeddings call failed", e)
            null
        }
    }

    private data class EmbeddingResponse(
        val data: List<EmbeddingData>?,
        val usage: Usage?
    )

    private data class EmbeddingData(
        val embedding: List<Double>
    )
    
    private data class Usage(
        val prompt_tokens: Int?,
        val total_tokens: Int?
    )
}


