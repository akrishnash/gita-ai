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
 * OpenAI Chat API client for understanding user queries.
 * Uses GPT-4o-mini to extract key themes, emotions, and intent from user input.
 */
class OpenAIChatClient(
    private val apiKey: String,
    private val model: String = "gpt-4o-mini"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    
    companion object {
        private const val TAG = "OpenAIChatClient"
        
        // GPT-4o-mini pricing per 1M tokens (as of 2024)
        private const val INPUT_COST_PER_MILLION = 0.15
        private const val OUTPUT_COST_PER_MILLION = 0.60
        
        private fun calculateCost(inputTokens: Int, outputTokens: Int): Double {
            val inputCost = (inputTokens / 1_000_000.0) * INPUT_COST_PER_MILLION
            val outputCost = (outputTokens / 1_000_000.0) * OUTPUT_COST_PER_MILLION
            return inputCost + outputCost
        }
    }

    /**
     * Understands user query and extracts key information for verse matching.
     * Returns a structured understanding with emotions, themes, and intent.
     */
    suspend fun understandQuery(query: String): QueryUnderstanding? = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            Log.w(TAG, "Empty query provided")
            return@withContext null
        }
        
        try {
            val systemPrompt = """You are an expert at understanding spiritual and emotional queries in the context of the Bhagavad Gita. Your task is to deeply analyze user queries to help find the most relevant verse from the Gita that addresses their situation.

The Bhagavad Gita covers themes like:
- Anxiety and fear (especially before major decisions or conflicts)
- Grief and loss
- Anger and conflict
- Attachment and detachment
- Burnout and exhaustion
- Identity crisis and self-doubt
- Intellectual and philosophical doubts
- Loneliness and isolation
- Moral dilemmas and ethical conflicts
- Pride and ego
- Result-obsession and attachment to outcomes
- Duty and responsibility (dharma)
- Self-realization and spiritual growth
- Detachment from results
- Equanimity in success and failure
- The nature of the self and consciousness
- The eternal nature of the soul
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

Emotions must be from this exact list (match exactly): Anxiety, Grief, Anger, Attachment, Burnout, Identity Crisis, Intellectual Doubt, Loneliness, Moral Dilemma, Pride, Result-Obsession

The enhanced_query is crucial - it should be 2-3 sentences that rephrase the user's situation in a way that would semantically match relevant Gita verses. Include emotional context, the situation, and what kind of guidance is needed."""

            val userPrompt = """User Query: "$query"

Analyze this query deeply. Consider:
- What is the user really feeling? (emotional state)
- What situation are they facing? (life circumstances)
- What kind of guidance or wisdom would help them? (intent)
- How would this be expressed in the context of spiritual/philosophical teachings?

Provide a comprehensive analysis that will help match this query to the most relevant Bhagavad Gita verse."""

            val messages = listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            )

            val payload = mapOf(
                "model" to model,
                "messages" to messages,
                "temperature" to 0.7,
                "response_format" to mapOf("type" to "json_object")
            )

            val req = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(gson.toJson(payload).toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val errorBody = resp.body?.string() ?: "No error body"
                    Log.e(TAG, "Chat API call failed: ${resp.code} ${resp.message}. Body: ${errorBody.take(200)}")
                    return@withContext null
                }
                val body = resp.body?.string() ?: run {
                    Log.e(TAG, "Empty response body")
                    return@withContext null
                }
                val parsed = gson.fromJson(body, ChatResponse::class.java)
                
                // Log token usage and cost
                parsed.usage?.let { usage ->
                    val promptTokens = usage.prompt_tokens ?: 0
                    val completionTokens = usage.completion_tokens ?: 0
                    val totalTokens = usage.total_tokens ?: (promptTokens + completionTokens)
                    val cost = calculateCost(promptTokens, completionTokens)
                    
                    // Record in session tracker
                    OpenAIUsageTracker.recordUsage(
                        model = model,
                        promptTokens = promptTokens,
                        completionTokens = completionTokens,
                        totalTokens = totalTokens
                    )
                    
                    val costStr = "%.6f".format(cost)
                    val logMessage = """
                        ═══════════════════════════════════════════════════════
                        OpenAI API Usage - Chat Completion
                        Model: $model
                        Prompt Tokens: $promptTokens
                        Completion Tokens: $completionTokens
                        Total Tokens: $totalTokens
                        Cost: $$costStr
                        ═══════════════════════════════════════════════════════
                    """.trimIndent()
                    
                    Log.i(TAG, logMessage)
                    println(logMessage)
                }
                
                val content = parsed.choices?.firstOrNull()?.message?.content ?: run {
                    Log.e(TAG, "No content in response")
                    return@withContext null
                }
                
                try {
                    val understanding = gson.fromJson(content, QueryUnderstanding::class.java)
                    Log.i(TAG, "Query understanding: emotions=${understanding.emotions}, themes=${understanding.themes}")
                    return@withContext understanding
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse understanding JSON", e)
                    Log.e(TAG, "Response content: $content")
                    return@withContext null
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Chat API call timed out", e)
            null
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: cannot reach OpenAI API", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Chat API call failed", e)
            e.printStackTrace()
            null
        }
    }

    private data class ChatResponse(
        val choices: List<Choice>?,
        val usage: Usage?
    )

    private data class Choice(
        val message: Message?
    )

    private data class Message(
        val content: String?
    )
    
    private data class Usage(
        val prompt_tokens: Int?,
        val completion_tokens: Int?,
        val total_tokens: Int?
    )
}

/**
 * Structured understanding of a user query.
 */
data class QueryUnderstanding(
    val emotions: List<String>,
    val themes: List<String>,
    val intent: String,
    val enhanced_query: String
)

