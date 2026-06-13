package com.gita.app.data

import com.gita.app.network.GeminiApi
import com.gita.app.network.GeminiContent
import com.gita.app.network.GeminiPart
import com.gita.app.network.GeminiRequest

private const val SYSTEM_PROMPT = """You are a compassionate Gita guide. Given the user's problem, identify the most relevant Bhagavad Gita verse. Respond in this exact format:

CHAPTER: [number]
VERSE: [number]
SANSKRIT: [original shloka]
TRANSLATION: [English translation]
GUIDANCE: [3-4 sentences explaining how this verse applies to their situation, warm and compassionate tone]"""

class GeminiRepository(
    private val api: GeminiApi,
    private val apiKey: String
) {
    suspend fun getVerseForProblem(userProblem: String): Result<GeminiVerseResponse> {
        return try {
            val prompt = "$SYSTEM_PROMPT\n\nUser's problem: $userProblem"
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )
            val response = api.generateContent(apiKey, request)
            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: return Result.failure(Exception("Empty response from Gemini"))

            Result.success(parseResponse(text))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseResponse(text: String): GeminiVerseResponse {
        val lines = text.lines()

        fun field(key: String): String =
            lines.firstOrNull { it.startsWith("$key:") }
                ?.removePrefix("$key:")
                ?.trim()
                ?: ""

        // GUIDANCE may span multiple lines after the GUIDANCE: label
        val guidanceStart = lines.indexOfFirst { it.startsWith("GUIDANCE:") }
        val guidance = if (guidanceStart >= 0) {
            lines.subList(guidanceStart, lines.size)
                .joinToString(" ")
                .removePrefix("GUIDANCE:")
                .trim()
        } else ""

        return GeminiVerseResponse(
            chapter = field("CHAPTER").toIntOrNull() ?: 1,
            verse = field("VERSE").toIntOrNull() ?: 1,
            sanskrit = field("SANSKRIT"),
            translation = field("TRANSLATION"),
            guidance = guidance
        )
    }
}

data class GeminiVerseResponse(
    val chapter: Int,
    val verse: Int,
    val sanskrit: String,
    val translation: String,
    val guidance: String
)
