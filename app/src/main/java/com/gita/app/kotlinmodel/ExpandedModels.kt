package com.gita.app.kotlinmodel

/**
 * Data models for `app/src/main/assets/KotlinModel/*.json`.
 */

data class VersesRoot(val verses: List<ExpandedVerse> = emptyList())

data class ExpandedVerse(
    val id: String,
    val chapter: Int,
    val verse: Int,
    val sanskrit: String = "",
    val transliteration: String = "",
    val translation: String = "",
    val context: String = "",
    val explanation: String? = null,
    val detailed_explanation: String? = null,
    val relevant_for: List<String>? = null,
    val feeling: List<String>? = null,
    val mythology_key: String? = null
)

data class StoriesRoot(val stories: List<ExpandedStory> = emptyList())

data class ExpandedStory(
    val key: String,
    val title: String = "",
    val text: String = "",
    val detailed_explanation: String? = null,
    val key_themes: List<String>? = null,
    val moral_lesson: String? = null
)


