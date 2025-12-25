package com.gita.app.logic

import com.gita.app.data.GitaMap
import com.gita.app.data.SubTheme
import com.gita.app.data.Theme

/**
 * Detects the most relevant theme and subtheme based on user input.
 * Uses keyword scoring to match user's problem description.
 */
object ThemeDetector {
    
    /**
     * Detects the best matching theme and subtheme for the given user input.
     * Returns null if no match is found (should use fallback).
     */
    fun detectTheme(userInput: String): DetectedTheme? {
        val normalizedInput = userInput.lowercase()
        val words = normalizedInput.split(Regex("\\s+")).filter { it.isNotBlank() }
        
        var bestMatch: Pair<SubTheme, Float>? = null
        
        // Score each subtheme
        for (theme in GitaMap.themes) {
            for (subtheme in theme.subthemes) {
                val score = calculateScore(words, subtheme.keywords)
                if (bestMatch == null || score > bestMatch.second) {
                    bestMatch = subtheme to score
                }
            }
        }
        
        // Only return if we have a reasonable match (threshold > 0)
        return bestMatch?.let { (subtheme, score) ->
            val theme = GitaMap.themes.find { it.subthemes.contains(subtheme) }
            if (theme != null && score > 0f) {
                DetectedTheme(
                    themeId = theme.id,
                    subthemeId = subtheme.id,
                    confidence = score
                )
            } else null
        }
    }
    
    /**
     * Calculates a score based on keyword matches.
     * Returns a value between 0 and 1.
     */
    private fun calculateScore(inputWords: List<String>, keywords: List<String>): Float {
        if (keywords.isEmpty()) return 0f
        
        var matches = 0
        for (keyword in keywords) {
            val normalizedKeyword = keyword.lowercase()
            // Check if keyword appears in any input word (substring match)
            if (inputWords.any { it.contains(normalizedKeyword) || normalizedKeyword.contains(it) }) {
                matches++
            }
        }
        
        return matches.toFloat() / keywords.size
    }
    
    /**
     * Gets a fallback theme/subtheme when detection fails.
     * Returns the first theme's first subtheme as a safe default.
     */
    fun getFallbackTheme(): DetectedTheme {
        val firstTheme = GitaMap.themes.firstOrNull()
        val firstSubtheme = firstTheme?.subthemes?.firstOrNull()
        
        return if (firstTheme != null && firstSubtheme != null) {
            DetectedTheme(
                themeId = firstTheme.id,
                subthemeId = firstSubtheme.id,
                confidence = 0f
            )
        } else {
            // Emergency fallback (should never happen if data is valid)
            DetectedTheme("fear", "fear_of_failure", 0f)
        }
    }
}


