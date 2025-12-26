package com.gita.app.ai

import android.util.Log
import com.google.gson.Gson
import com.gita.app.kotlinmodel.OpenAIUsageTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Generates personalized reflections and translations using AI.
 * Ensures responses align with the user's query context.
 */
class ReflectionGenerator(private val apiKey: String?) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    companion object {
        private const val TAG = "ReflectionGenerator"
        private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL = "gpt-5-nano" // GPT-5 nano for reflections
    }
    
    private data class ChatCompletionResponse(
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
    
    /**
     * Generates a complete, fluent English translation (not word-by-word).
     */
    suspend fun generateCompleteTranslation(
        sanskrit: String,
        transliteration: String,
        wordByWordTranslation: String
    ): String? {
        if (apiKey.isNullOrBlank()) {
            Log.d(TAG, "No API key, returning original translation")
            return null
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val systemPrompt = """
                    You are a Sanskrit-to-English translator. Your task is to convert a word-by-word translation into a complete, fluent, natural English translation.
                    
                    Rules:
                    - Convert the word-by-word translation into a complete, whole English sentence (NOT word-by-word)
                    - Make it a natural, flowing English translation that reads like a complete thought
                    - Maintain the meaning and essence of the original Sanskrit
                    - Do NOT add interpretations or explanations
                    - Do NOT keep it word-by-word - make it a complete, coherent sentence
                    - Keep it concise (1-2 sentences maximum)
                    - Make it grammatically correct and readable
                    
                    Return ONLY the complete English translation, nothing else.
                """.trimIndent()
                
                val userPrompt = """
                    Sanskrit: $sanskrit
                    Transliteration: $transliteration
                    Word-by-word translation: $wordByWordTranslation
                    
                    Convert this into a complete, fluent English translation:
                """.trimIndent()
                
                val requestBody = buildRequestBody(systemPrompt, userPrompt)
                val request = Request.Builder()
                    .url(OPENAI_API_URL)
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    Log.e(TAG, "Translation API call failed: ${response.code} - ${response.message}")
                    return@withContext null
                }
                
                val responseBody = response.body?.string()
                if (responseBody == null) {
                    Log.e(TAG, "Empty response body for translation")
                    return@withContext null
                }
                
                val parsed = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
                
                // Track token usage and cost
                parsed.usage?.let { usage ->
                    val promptTokens = usage.prompt_tokens ?: 0
                    val completionTokens = usage.completion_tokens ?: 0
                    val totalTokens = usage.total_tokens ?: (promptTokens + completionTokens)
                    
                    // Record in session tracker
                    OpenAIUsageTracker.recordUsage(
                        model = MODEL,
                        promptTokens = promptTokens,
                        completionTokens = completionTokens,
                        totalTokens = totalTokens
                    )
                    
                    val logMessage = """
                        ═══════════════════════════════════════════════════════
                        OpenAI API Usage - GPT-4 Translation
                        Model: $MODEL
                        Prompt Tokens: $promptTokens
                        Completion Tokens: $completionTokens
                        Total Tokens: $totalTokens
                        ═══════════════════════════════════════════════════════
                    """.trimIndent()
                    
                    Log.i(TAG, logMessage)
                    println(logMessage)
                }
                
                val translation = parsed.choices?.firstOrNull()?.message?.content?.trim()
                if (translation.isNullOrBlank()) {
                    Log.e(TAG, "Empty translation content")
                    return@withContext null
                }
                Log.d(TAG, "Generated complete translation")
                return@withContext translation
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating translation", e)
                return@withContext null
            }
        }
    }
    
    /**
     * Generates a personalized reflection that aligns with the user's query.
     * For example, if user says "I am sad", the reflection should address sadness.
     */
    suspend fun generatePersonalizedReflection(
        userQuery: String,
        verseSanskrit: String,
        verseTranslation: String,
        verseContext: String,
        baseExplanation: String
    ): String? {
        if (apiKey.isNullOrBlank()) {
            Log.d(TAG, "No API key, returning base explanation")
            return null
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val systemPrompt = """
                    You are a thoughtful reflection assistant. Your task is to create a personalized reflection that connects the Bhagavad Gita verse to the user's specific situation and emotion.
                    
                    Rules:
                    - Maximum 120 words
                    - DIRECTLY address the user's specific concern or emotion (e.g., if they say "I am sad", the reflection must directly address sadness and how the verse relates to their sadness)
                    - The reflection must be consistent with and aligned to the user's question/emotion
                    - Connect the verse's wisdom specifically to their situation and emotional state
                    - Keep tone: calm, human, non-religious, non-authoritative
                    - Do NOT quote scripture or speak as Krishna
                    - Do NOT add new philosophy beyond what the verse offers
                    - Allow doubt and uncertainty
                    - No moral commands
                    - Make it feel personally relevant and directly responsive to their query
                    - If the user expresses an emotion (sad, anxious, angry, etc.), the reflection must acknowledge and address that emotion
                    
                    Return ONLY the reflection text, nothing else.
                """.trimIndent()
                
                val userQueryPreview = userQuery.take(100)
                val userPrompt = """
                    User's query: $userQuery
                    
                    Verse (Sanskrit): $verseSanskrit
                    Verse (Translation): $verseTranslation
                    Verse Context: $verseContext
                    
                    Base explanation: $baseExplanation
                    
                    Create a personalized reflection that:
                    1. DIRECTLY addresses the user's query/emotion (e.g., if they say "I am sad", address sadness)
                    2. Connects this verse specifically to their situation and emotional state
                    3. Makes the reflection consistent with and aligned to their question
                    4. Feels personally relevant and directly responsive to: $userQueryPreview
                    
                    The reflection must be consistent with the user's question - if they express sadness, address sadness; if they express anxiety, address anxiety, etc.
                """.trimIndent()
                
                val requestBody = buildRequestBody(systemPrompt, userPrompt)
                val request = Request.Builder()
                    .url(OPENAI_API_URL)
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    Log.e(TAG, "Reflection API call failed: ${response.code} - ${response.message}")
                    return@withContext null
                }
                
                val responseBody = response.body?.string()
                if (responseBody == null) {
                    Log.e(TAG, "Empty response body for reflection")
                    return@withContext null
                }
                
                val parsed = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
                
                // Track token usage and cost
                parsed.usage?.let { usage ->
                    val promptTokens = usage.prompt_tokens ?: 0
                    val completionTokens = usage.completion_tokens ?: 0
                    val totalTokens = usage.total_tokens ?: (promptTokens + completionTokens)
                    
                    // Record in session tracker
                    OpenAIUsageTracker.recordUsage(
                        model = MODEL,
                        promptTokens = promptTokens,
                        completionTokens = completionTokens,
                        totalTokens = totalTokens
                    )
                    
                    val logMessage = """
                        ═══════════════════════════════════════════════════════
                        OpenAI API Usage - Reflection Generation
                        Model: $MODEL
                        Prompt Tokens: $promptTokens
                        Completion Tokens: $completionTokens
                        Total Tokens: $totalTokens
                        ═══════════════════════════════════════════════════════
                    """.trimIndent()
                    
                    Log.i(TAG, logMessage)
                    println(logMessage)
                }
                
                val reflection = parsed.choices?.firstOrNull()?.message?.content?.trim()
                if (reflection.isNullOrBlank()) {
                    Log.e(TAG, "Empty reflection content")
                    return@withContext null
                }
                Log.d(TAG, "Generated personalized reflection")
                return@withContext reflection
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating reflection", e)
                return@withContext null
            }
        }
    }
    
    private fun buildRequestBody(systemPrompt: String, userPrompt: String): String {
        val requestBody = mapOf(
            "model" to MODEL,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            ),
            "temperature" to 0.7,
            "max_tokens" to 300
        )
        return gson.toJson(requestBody)
    }
    
}

