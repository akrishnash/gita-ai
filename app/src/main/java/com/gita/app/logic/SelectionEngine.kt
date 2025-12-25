package com.gita.app.logic

import com.gita.app.data.GitaMap
import com.gita.app.data.ReflectionAngle
import com.gita.app.data.VerseEntry
import com.gita.app.logic.LocalStorage
import kotlin.math.absoluteValue

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
        
        // Use suspend functions to avoid blocking
        val seenVerseIds = storage.getSeenVerseIdsSuspend()
        val lastVerseId = storage.getLastVerseIdSuspend()
        
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
        
        // Mark as seen and update last verse (these are suspend functions)
        try {
            storage.addSeenVerseId(selectedVerse.id)
            storage.setLastVerseId(selectedVerse.id)
        } catch (e: Exception) {
            // If storage fails, still return the verse (non-critical)
            e.printStackTrace()
        }
        
        return selectedVerse
    }
    
    /**
     * Gets the next reflection angle for a verse.
     * Rotates through available angles to provide variety.
     */
    suspend fun getNextReflectionAngle(verseId: String): ReflectionAngle {
        val lastAngle = storage.getLastReflectionAngleSuspend(verseId)
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
        try {
            storage.setLastReflectionAngle(verseId, nextAngle)
        } catch (e: Exception) {
            // If storage fails, still return the angle (non-critical)
            e.printStackTrace()
        }
        
        return nextAngle
    }
    
    /**
     * Gets an anchor line from the verse's anchor lines.
     * Rotates through anchor lines deterministically based on verse ID.
     */
    suspend fun getAnchorLine(verse: VerseEntry): String {
        return if (verse.anchorLines.isNotEmpty()) {
            // Deterministic selection based on verse ID hash
            val index = verse.id.hashCode().absoluteValue % verse.anchorLines.size
            verse.anchorLines[index]
        } else {
            ""
        }
    }
}

