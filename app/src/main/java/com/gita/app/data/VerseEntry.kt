package com.gita.app.data

data class VerseEntry(
    val id: String,
    val chapter: Int,
    val verse: Int,
    val sanskrit: String,
    val transliteration: String,
    val translation: String,
    val context: String,
    val reflections: Map<ReflectionAngle, String>,
    val anchorLines: List<String>
)


