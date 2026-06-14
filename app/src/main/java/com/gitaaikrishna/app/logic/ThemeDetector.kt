package com.gitaaikrishna.app.logic

/**
 * Theme detection — retained as a compile-time stub.
 * The offline verse map (GitaMap) has been removed; detection always returns null,
 * causing the pipeline to fall through to the fast matcher.
 */
object ThemeDetector {

    fun detectTheme(userInput: String): DetectedTheme? = null

    fun getFallbackTheme(): DetectedTheme =
        DetectedTheme(themeId = "fear", subthemeId = "fear_of_failure", confidence = 0f)
}




