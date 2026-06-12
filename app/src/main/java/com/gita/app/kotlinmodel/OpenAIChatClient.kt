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
 * Uses GPT-4o-mini to extract key themes, emotions, intent, and THERAPEUTIC NEEDS from user input.
 * 
 * Key enhancement: Detects whether user needs empathy/comfort vs motivation/push
 * to solve the semantic mismatch problem (e.g., returning challenging verses for comfort queries).
 */
class OpenAIChatClient(
    private val apiKey: String,
    private val model: String = "gpt-4o-mini"
) {
    private val client = com.gita.app.network.NetworkModule.client

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
     * Returns a structured understanding with emotions, themes, intent, AND therapeutic needs.
     * 
     * NEW: Returns detected_need (empathy_needed vs push_needed) and ideal_tone (comforting vs challenging)
     * to enable re-ranking that matches the verse tone to user's emotional needs.
     */
    suspend fun understandQuery(query: String): QueryUnderstanding? = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            Log.w(TAG, "Empty query provided")
            return@withContext null
        }
        
        try {
            val systemPrompt = """You are an expert therapeutic advisor analyzing queries in the context of the Bhagavad Gita. Your task is to deeply understand what the user emotionally NEEDS, not just what they're saying.

## CRITICAL: DETECT THERAPEUTIC NEED

The most important part of your analysis is determining:
1. Does this person need EMPATHY (comfort, validation, understanding) or a PUSH (motivation, challenge, wake-up call)?
2. What TONE of response would be healing for them?

### Signs user needs EMPATHY (comfort-first approach):
- Expressing pain, sadness, grief, loss
- Feeling overwhelmed, exhausted, hopeless
- Seeking understanding ("nobody gets it", "I feel so alone")
- Recently experienced trauma or loss
- Sounds emotionally fragile or vulnerable
- Using words like: "can't", "giving up", "so tired", "broken", "lost"

### Signs user needs a PUSH (challenge-first approach):
- Stuck in inaction due to fear or overthinking
- Procrastinating or avoiding responsibilities
- Seeking permission or validation for action they know they should take
- Expressing frustration with themselves
- Using words like: "I know I should but...", "I'm being lazy", "I need to just do it"

## Response Format

Respond in JSON format:
{
  "emotions": ["emotion1", "emotion2"],
  "themes": ["theme1", "theme2", "theme3"],
  "intent": "brief description of what they're seeking",
  "enhanced_query": "refined version that captures spiritual/emotional essence",
  "detected_need": "empathy_needed" or "push_needed",
  "ideal_tone": "comforting" or "challenging" or "philosophical",
  "therapeutic_goal": "validation" or "motivation" or "detachment" or "perspective" or "acceptance",
  "vulnerability_level": "high" or "medium" or "low"
}

## Field Definitions

- detected_need: 
  - "empathy_needed": User needs to feel heard, understood, and validated BEFORE any guidance
  - "push_needed": User is ready for and would benefit from a direct challenge or call to action

- ideal_tone:
  - "comforting": Gentle, reassuring, validating (for vulnerable/grieving users)
  - "challenging": Direct, motivating, firm (for stuck/procrastinating users)
  - "philosophical": Reflective, perspective-shifting (for confused/questioning users)

- therapeutic_goal:
  - "validation": User needs to feel their emotions are valid and understood
  - "motivation": User needs energy and push to take action
  - "detachment": User needs to let go of attachment or control
  - "perspective": User needs to see their situation from a higher vantage point
  - "acceptance": User needs help accepting what cannot be changed

- vulnerability_level:
  - "high": User seems emotionally fragile, handle with extra care
  - "medium": User is struggling but stable
  - "low": User is seeking intellectual guidance, not in crisis

## Emotion Categories
Emotions must be from: Anxiety, Grief, Anger, Attachment, Burnout, Identity Crisis, Intellectual Doubt, Loneliness, Moral Dilemma, Pride, Result-Obsession, Fear, Jealousy, Guilt, Hopelessness, Confusion, Impatience

## Examples

Example 1: "I feel like giving up on everything"
{
  "emotions": ["Hopelessness", "Burnout"],
  "themes": ["despair", "exhaustion", "loss of meaning"],
  "intent": "seeking reason to continue",
  "enhanced_query": "I am overwhelmed by despair and seek wisdom on finding meaning when all feels futile",
  "detected_need": "empathy_needed",
  "ideal_tone": "comforting",
  "therapeutic_goal": "validation",
  "vulnerability_level": "high"
}

Example 2: "I keep procrastinating on important decisions"
{
  "emotions": ["Fear", "Confusion"],
  "themes": ["avoidance", "indecision", "self-sabotage"],
  "intent": "overcome paralysis and take action",
  "enhanced_query": "I am paralyzed by fear and seek courage to fulfill my dharma",
  "detected_need": "push_needed",
  "ideal_tone": "challenging",
  "therapeutic_goal": "motivation",
  "vulnerability_level": "low"
}"""

            val userPrompt = """User Query: "$query"

Analyze this query deeply. Most importantly:
1. What does this person EMOTIONALLY NEED right now?
2. Would comfort or challenge serve them better?
3. How vulnerable do they seem?

Provide a comprehensive therapeutic analysis."""

            val messages = listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            )

            val payload = mapOf(
                "model" to model,
                "messages" to messages,
                "temperature" to 0.5, // Lower temperature for more consistent therapeutic assessment
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
                    Log.i(TAG, "OpenAI Usage: $totalTokens tokens, cost: $$costStr")
                }
                
                val content = parsed.choices?.firstOrNull()?.message?.content ?: run {
                    Log.e(TAG, "No content in response")
                    return@withContext null
                }
                
                try {
                    val understanding = gson.fromJson(content, QueryUnderstanding::class.java)
                    Log.i(TAG, "═══════════════════════════════════════════════════════")
                    Log.i(TAG, "QUERY UNDERSTANDING RESULT")
                    Log.i(TAG, "Emotions: ${understanding.emotions}")
                    Log.i(TAG, "Detected Need: ${understanding.detected_need}")
                    Log.i(TAG, "Ideal Tone: ${understanding.ideal_tone}")
                    Log.i(TAG, "Therapeutic Goal: ${understanding.therapeutic_goal}")
                    Log.i(TAG, "Vulnerability: ${understanding.vulnerability_level}")
                    Log.i(TAG, "═══════════════════════════════════════════════════════")
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
    
    /**
     * Generates a 2-sentence "bridge" explaining why the matched verse is relevant
     * to the user's specific pain point.
     */
    suspend fun generateBridge(
        userQuery: String,
        verse: EnrichedVerse,
        detectedEmotion: String?,
        therapeuticGoal: String?
    ): String? = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """You are a compassionate spiritual counselor. Your task is to create a brief, warm "bridge" that connects a user's pain point to a Bhagavad Gita verse.

The bridge should:
1. Acknowledge their specific situation (1 sentence)
2. Explain why this verse speaks to their need (1 sentence)

Keep it personal, warm, and specific. Do NOT be preachy or generic. Maximum 2 sentences total."""

            val userPrompt = """User's concern: "$userQuery"
Detected emotion: ${detectedEmotion ?: "unspecified"}
Therapeutic goal: ${therapeuticGoal ?: "perspective"}

Verse ${verse.chapter_number}.${verse.verse_number}:
"${verse.english_translation}"

Context: ${verse.arjuna_despair_link ?: verse.modern_problem_match ?: ""}

Write a 2-sentence bridge connecting their pain to this verse's wisdom."""

            val messages = listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            )

            val payload = mapOf(
                "model" to model,
                "messages" to messages,
                "temperature" to 0.7,
                "max_tokens" to 100
            )

            val req = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(gson.toJson(payload).toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e(TAG, "Bridge generation failed: ${resp.code}")
                    return@withContext null
                }
                val body = resp.body?.string() ?: return@withContext null
                val parsed = gson.fromJson(body, ChatResponse::class.java)
                
                // Track usage
                parsed.usage?.let { usage ->
                    OpenAIUsageTracker.recordUsage(
                        model = model,
                        promptTokens = usage.prompt_tokens ?: 0,
                        completionTokens = usage.completion_tokens ?: 0,
                        totalTokens = usage.total_tokens ?: 0
                    )
                }
                
                val bridge = parsed.choices?.firstOrNull()?.message?.content?.trim()
                Log.i(TAG, "Generated bridge: $bridge")
                return@withContext bridge
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bridge generation failed", e)
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
 * Enhanced structured understanding of a user query.
 * Now includes therapeutic needs detection for re-ranking.
 */
data class QueryUnderstanding(
    val emotions: List<String>,
    val themes: List<String>,
    val intent: String,
    val enhanced_query: String,
    
    // NEW: Therapeutic needs for re-ranking
    val detected_need: String = "empathy_needed",  // "empathy_needed" or "push_needed"
    val ideal_tone: String = "philosophical",      // "comforting", "challenging", "philosophical"
    val therapeutic_goal: String = "perspective",  // "validation", "motivation", "detachment", "perspective", "acceptance"
    val vulnerability_level: String = "medium"     // "high", "medium", "low"
) {
    companion object {
        const val NEED_EMPATHY = "empathy_needed"
        const val NEED_PUSH = "push_needed"
        
        const val TONE_COMFORTING = "comforting"
        const val TONE_CHALLENGING = "challenging"
        const val TONE_PHILOSOPHICAL = "philosophical"
    }
    
    fun needsEmpathy(): Boolean = detected_need == NEED_EMPATHY
    fun needsPush(): Boolean = detected_need == NEED_PUSH
    fun isHighVulnerability(): Boolean = vulnerability_level == "high"
}
