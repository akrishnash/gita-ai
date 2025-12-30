package com.gita.app.kotlinmodel

// Data models for enriched_gita_formatted.json

data class EnrichedVersesRoot(val verses: List<EnrichedVerse> = emptyList())

data class EnrichedVerse(
    val id: String,
    val chapter_number: Int,
    val verse_number: Int,
    val sanskrit_text: String = "",
    val transliteration: String = "",
    val hindi_translation: String = "",
    val english_translation: String = "",
    val emotion_category: String = "",
    val arjuna_despair_link: String? = null,
    val modern_problem_match: String? = null,
    val wisdom_nugget: String? = null
)

