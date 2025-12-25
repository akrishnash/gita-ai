package com.gita.app.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * AI Intent Interpreter
 * 
 * Takes raw user input and uses LLM to extract semantic intent.
 * Returns structured theme/subtheme classification.
 * 
 * Rules:
 * - AI NEVER selects scripture
 * - AI NEVER invents verses
 * - AI NEVER speaks as Krishna
 * - AI ONLY interprets user meaning
 */
class IntentInterpreter(private val apiKey: String?) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    companion object {
        private const val TAG = "IntentInterpreter"
        private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL = "gpt-4" // Can fallback to gpt-3.5-turbo
        private const val MIN_CONFIDENCE = 0.6f
    }
    
    /**
     * Extracts semantic intent from user input using AI.
     * Returns null if API fails or confidence is too low.
     */
    suspend fun extractIntent(userInput: String): IntentResult? {
        if (apiKey.isNullOrBlank()) {
            Log.d(TAG, "No API key provided, skipping AI intent extraction")
            return null
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val systemPrompt = buildSystemPrompt()
                val userPrompt = buildUserPrompt(userInput)
                
                val requestBody = buildRequestBody(systemPrompt, userPrompt)
                val request = Request.Builder()
                    .url(OPENAI_API_URL)
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed: ${response.code} - ${response.message}")
                    return@withContext null
                }
                
                val responseBody = response.body?.string()
                if (responseBody == null) {
                    Log.e(TAG, "Empty response body")
                    return@withContext null
                }
                
                val intentResult = parseResponse(responseBody)
                
                // Validate confidence
                if (intentResult != null && intentResult.confidence < MIN_CONFIDENCE) {
                    Log.w(TAG, "Confidence too low: ${intentResult.confidence}, falling back to keyword matching")
                    return@withContext null
                }
                
                Log.d(TAG, "AI intent extracted: theme=${intentResult?.primaryTheme}, confidence=${intentResult?.confidence}")
                return@withContext intentResult
                
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting intent", e)
                return@withContext null
            }
        }
    }
    
    private fun buildSystemPrompt(): String {
        return """
            You are a semantic interpreter.
            Do not give advice.
            Do not quote scripture.
            Do not be spiritual.
            Your task is to classify the user's problem into themes only.
            
            Available themes:
            - fear (subthemes: fear_of_failure, fear_of_loss)
            - confusion (subthemes: decision_paralysis)
            - attachment (subthemes: emotional_attachment)
            - grief (subthemes: loss)
            - duty_vs_desire (subthemes: inner_conflict)
            - exhaustion (subthemes: burnout)
            
            Return ONLY valid JSON in this exact format:
            {
              "primary_theme": "fear",
              "subtheme": "fear_of_failure",
              "secondary_themes": ["duty_vs_desire"],
              "emotional_tone": "anxiety",
              "confidence": 0.85
            }
        """.trimIndent()
    }
    
    private fun buildUserPrompt(userInput: String): String {
        return """
            Classify this problem using the provided theme list:
            [fear, confusion, attachment, grief, duty_vs_desire, exhaustion]
            
            User problem:
            $userInput
        """.trimIndent()
    }
    
    private fun buildRequestBody(systemPrompt: String, userPrompt: String): String {
        // Use Gson to properly build JSON
        val requestBody = mapOf(
            "model" to MODEL,
            "messages" to listOf(
                mapOf(
                    "role" to "system",
                    "content" to systemPrompt
                ),
                mapOf(
                    "role" to "user",
                    "content" to userPrompt
                )
            ),
            "temperature" to 0.3,
            "max_tokens" to 200,
            "response_format" to mapOf("type" to "json_object")
        )
        return gson.toJson(requestBody)
    }
    
    private fun parseResponse(responseBody: String): IntentResult? {
        return try {
            // Parse OpenAI response structure
            val jsonResponse = gson.fromJson(responseBody, OpenAIResponse::class.java)
            val content = jsonResponse.choices?.firstOrNull()?.message?.content
                ?: return null
            
            // Parse the actual intent JSON from content
            val intentJson = gson.fromJson(content, IntentJson::class.java)
            
            IntentResult(
                primaryTheme = intentJson.primary_theme ?: return null,
                subtheme = intentJson.subtheme ?: return null,
                secondaryThemes = intentJson.secondary_themes ?: emptyList(),
                emotionalTone = intentJson.emotional_tone ?: "",
                confidence = intentJson.confidence ?: 0f
            )
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse JSON response", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response", e)
            null
        }
    }
}

/**
 * Result of AI intent extraction
 */
data class IntentResult(
    val primaryTheme: String,
    val subtheme: String,
    val secondaryThemes: List<String>,
    val emotionalTone: String,
    val confidence: Float
)

/**
 * JSON structure returned by AI
 */
private data class IntentJson(
    val primary_theme: String?,
    val subtheme: String?,
    val secondary_themes: List<String>?,
    val emotional_tone: String?,
    val confidence: Float?
)

/**
 * OpenAI API response structure
 */
private data class OpenAIResponse(
    val choices: List<Choice>?
)

private data class Choice(
    val message: Message?
)

private data class Message(
    val content: String?
)

