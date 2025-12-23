package com.gita.app.logic

import com.gita.app.data.GitaMap
import com.gita.app.data.ReflectionAngle
import com.gita.app.data.VerseEntry
import com.gita.app.logic.LocalStorage
import kotlinx.coroutines.runBlocking

/**
 * Handles verse selection with rotation and non-repetition logic.
 * Tracks seen verses and rotates reflection angles.
 */
class SelectionEngine(private val storage: LocalStorage) {
    
    /**
     * Selects a verse for the given theme/subtheme.
     * Rotates through verses and avoids immediate repetition.
     */
    suspend fun selectVerse(themeId: String, subthemeId: String): VerseEntry? {
        val theme = GitaMap.themes.find { it.id == themeId }
        val subtheme = theme?.subthemes?.find { it.id == subthemeId }
        
        if (subtheme == null || subtheme.verses.isEmpty()) {
            return null
        }
        
        val seenVerseIds = storage.getSeenVerseIds()
        val lastVerseId = storage.getLastVerseId()
        
        // Filter out the last verse to avoid immediate repetition
        val availableVerses = if (lastVerseId != null) {
            subtheme.verses.filter { it.id != lastVerseId }
        } else {
            subtheme.verses
        }
        
        // If all verses were seen, reset and use all verses
        val versesToChooseFrom = if (availableVerses.isEmpty()) {
            subtheme.verses
        } else {
            availableVerses
        }
        
        // Select verse (simple rotation based on seen count)
        val selectedVerse = if (versesToChooseFrom.size == 1) {
            versesToChooseFrom.first()
        } else {
            val seenCount = seenVerseIds.size
            val index = seenCount % versesToChooseFrom.size
            versesToChooseFrom[index]
        }
        
        // Mark as seen and update last verse
        storage.addSeenVerseId(selectedVerse.id)
        storage.setLastVerseId(selectedVerse.id)
        
        return selectedVerse
    }
    
    /**
     * Gets the next reflection angle for a verse.
     * Rotates through available angles to provide variety.
     */
    suspend fun getNextReflectionAngle(verseId: String): ReflectionAngle {
        val lastAngle = storage.getLastReflectionAngle(verseId)
        val allAngles = ReflectionAngle.values().toList()
        
        val currentIndex = if (lastAngle != null) {
            allAngles.indexOf(lastAngle)
        } else {
            -1
        }
        
        // Get next angle (rotate)
        val nextIndex = (currentIndex + 1) % allAngles.size
        val nextAngle = allAngles[nextIndex]
        
        // Store for next time
        storage.setLastReflectionAngle(verseId, nextAngle)
        
        return nextAngle
    }
    
    /**
     * Gets a random anchor line from the verse's anchor lines.
     */
    fun getAnchorLine(verse: VerseEntry): String {
        return if (verse.anchorLines.isNotEmpty()) {
            verse.anchorLines.random()
        } else {
            ""
        }
    }
}

