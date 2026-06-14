package com.gitaaikrishna.app.data

import com.gitaaikrishna.app.network.GeminiApi
import com.gitaaikrishna.app.network.GeminiContent
import com.gitaaikrishna.app.network.GeminiPart
import com.gitaaikrishna.app.network.GeminiRequest

private const val SYSTEM_PROMPT_EN = """You are a compassionate Gita guide. Given the user's problem, identify the most relevant Bhagavad Gita verse. Respond in this exact format:

CHAPTER: [number]
VERSE: [number]
SANSKRIT: [original shloka]
TRANSLATION: [English translation]
HINDI_TRANSLATION: [same translation in Hindi]
GUIDANCE: [3-4 sentences explaining how this verse applies to their situation, warm and compassionate tone]"""

private const val SYSTEM_PROMPT_HI = """आप एक करुणामय गीता मार्गदर्शक हैं। उपयोगकर्ता की समस्या के आधार पर सबसे प्रासंगिक भगवद गीता श्लोक चुनें।
इस सटीक प्रारूप में उत्तर दें:

CHAPTER: [number]
VERSE: [number]
SANSKRIT: [original shloka]
TRANSLATION: [हिंदी अनुवाद]
HINDI_TRANSLATION: [हिंदी अनुवाद]
GUIDANCE: [3-4 वाक्यों में बताएं कि यह श्लोक उनकी स्थिति पर कैसे लागू होता है, गर्म और करुणामय स्वर में]"""

class GeminiRepository(
    private val api: GeminiApi,
    private val apiKey: String
) {
    suspend fun getVerseForProblem(userProblem: String, language: String = "en"): Result<GeminiVerseResponse> {
        return try {
            val systemPrompt = if (language == "hi") SYSTEM_PROMPT_HI else SYSTEM_PROMPT_EN
            val prompt = "$systemPrompt\n\nUser's problem: $userProblem"
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

            Result.success(parseResponse(text, language))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseResponse(text: String, language: String): GeminiVerseResponse {
        val lines = text.lines()

        fun field(key: String): String =
            lines.firstOrNull { it.startsWith("$key:") }
                ?.removePrefix("$key:")
                ?.trim()
                ?: ""

        // Multiline field: collect from label line until next ALL_CAPS_KEY: line or end
        fun multiLineField(key: String): String {
            val startIdx = lines.indexOfFirst { it.startsWith("$key:") }
            if (startIdx < 0) return ""
            val firstLine = lines[startIdx].removePrefix("$key:").trim()
            val rest = lines.drop(startIdx + 1)
                .takeWhile { !it.matches(Regex("[A-Z_]+:.*")) }
            return (listOf(firstLine) + rest).joinToString(" ").trim()
        }

        val guidance = multiLineField("GUIDANCE")
        val hindiTranslation = field("HINDI_TRANSLATION")
        // When language is Hindi, guidance is already in Hindi
        val hindiGuidance = if (language == "hi") guidance else ""

        return GeminiVerseResponse(
            chapter = field("CHAPTER").toIntOrNull() ?: 1,
            verse = field("VERSE").toIntOrNull() ?: 1,
            sanskrit = field("SANSKRIT"),
            translation = field("TRANSLATION"),
            guidance = guidance,
            hindiTranslation = hindiTranslation,
            hindiGuidance = hindiGuidance
        )
    }
}

data class GeminiVerseResponse(
    val chapter: Int,
    val verse: Int,
    val sanskrit: String,
    val translation: String,
    val guidance: String,
    val hindiTranslation: String = "",
    val hindiGuidance: String = ""
)
