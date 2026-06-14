package com.gitaaikrishna.app.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// ── Request ──────────────────────────────────────────────────────────────────

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

// ── Response ─────────────────────────────────────────────────────────────────

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

// ── Interface ─────────────────────────────────────────────────────────────────

interface GeminiApi {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
