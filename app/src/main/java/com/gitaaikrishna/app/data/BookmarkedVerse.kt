package com.gitaaikrishna.app.data

data class BookmarkedVerse(
    val verseId: String,
    val sanskrit: String,
    val translation: String,
    val chapterVerse: String,
    val userProblem: String,
    val savedAt: Long
)
