package com.gita.app.kotlinmodel

// Data models for enriched_gita_formatted.json

data class EnrichedVersesRoot(val verses: List<EnrichedVerse> = emptyList())

/**
 * Enhanced verse model with tone and therapeutic metadata for better matching.
 * 
 * New fields for solving semantic mismatch:
 * - tone: "comforting", "challenging", or "philosophical"
 * - therapeutic_goal: "validation", "motivation", "detachment", "perspective", "acceptance"
 */
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
    val wisdom_nugget: String? = null,
    
    // NEW: Tone and therapeutic metadata for re-ranking
    val tone: String? = null,  // "comforting", "challenging", "philosophical"
    val therapeutic_goal: String? = null  // "validation", "motivation", "detachment", "perspective", "acceptance"
) {
    companion object {
        // Valid tone values
        const val TONE_COMFORTING = "comforting"
        const val TONE_CHALLENGING = "challenging"
        const val TONE_PHILOSOPHICAL = "philosophical"
        
        // Valid therapeutic goal values
        const val GOAL_VALIDATION = "validation"
        const val GOAL_MOTIVATION = "motivation"
        const val GOAL_DETACHMENT = "detachment"
        const val GOAL_PERSPECTIVE = "perspective"
        const val GOAL_ACCEPTANCE = "acceptance"
    }
    
    /**
     * Returns the effective tone, with fallback inference based on content.
     */
    fun getEffectiveTone(): String {
        if (tone != null) return tone
        
        // Fallback: infer tone from content patterns
        val translation = english_translation.lowercase()
        return when {
            translation.contains("arise") || translation.contains("shake off") || 
            translation.contains("fight") || translation.contains("do not") -> TONE_CHALLENGING
            
            translation.contains("neither") || translation.contains("eternal") ||
            translation.contains("soul") || translation.contains("wise") -> TONE_PHILOSOPHICAL
            
            translation.contains("protect") || translation.contains("carry") ||
            translation.contains("never") && translation.contains("perish") -> TONE_COMFORTING
            
            else -> TONE_PHILOSOPHICAL // default
        }
    }
    
    /**
     * Returns the effective therapeutic goal, with fallback inference.
     */
    fun getEffectiveTherapeuticGoal(): String {
        if (therapeutic_goal != null) return therapeutic_goal
        
        // Fallback: infer from emotion category and content
        val translation = english_translation.lowercase()
        return when {
            emotion_category.contains("Hopelessness", ignoreCase = true) && 
            translation.contains("arise") -> GOAL_MOTIVATION
            
            emotion_category.contains("Grief", ignoreCase = true) -> GOAL_ACCEPTANCE
            emotion_category.contains("Anxiety", ignoreCase = true) -> GOAL_PERSPECTIVE
            emotion_category.contains("Attachment", ignoreCase = true) -> GOAL_DETACHMENT
            
            translation.contains("understand") || translation.contains("accept") -> GOAL_VALIDATION
            translation.contains("action") || translation.contains("duty") -> GOAL_MOTIVATION
            
            else -> GOAL_PERSPECTIVE // default
        }
    }
}
