package com.gita.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gita.app.BuildConfig
import com.gita.app.ai.ReflectionGenerator
import com.gita.app.data.ReflectionAngle
import com.gita.app.data.VerseEntry
import com.gita.app.kotlinmodel.KotlinModelRepository
import com.gita.app.kotlinmodel.OpenAIUsageTracker
import com.gita.app.logic.DetectedTheme
import com.gita.app.logic.HistoryEntry
import com.gita.app.logic.LocalStorage
import com.gita.app.logic.SelectionEngine
import com.gita.app.logic.ThemeDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppState {
    object Home : AppState()
    data class Pause(val userInput: String) : AppState()
    data class Response(
        val verse: VerseEntry,
        val reflection: String,
        val anchorLine: String,
        val currentAngle: ReflectionAngle,
        val userInput: String,
        val themeId: String,
        val subthemeId: String,
        val story: StoryCard? = null,
        val debugInfo: com.gita.app.kotlinmodel.MatchDebugInfo? = null
    ) : AppState()
    object History : AppState()
    object Settings : AppState()
}

data class StoryCard(
    val title: String,
    val text: String,
    val moralLesson: String? = null,
    val keyThemes: List<String> = emptyList()
)

data class ResponseState(
    val verse: VerseEntry,
    val reflection: String,
    val anchor: String,
    val currentAngle: ReflectionAngle,
    val themeId: String,
    val subthemeId: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = LocalStorage(application.applicationContext)
    private val selectionEngine = SelectionEngine(storage)
    private val kotlinModelRepo = KotlinModelRepository(application.applicationContext)
    
    private val _appState = MutableStateFlow<AppState>(AppState.Home)
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    private val _aiApiKey = MutableStateFlow<String?>(null)
    val aiApiKey: StateFlow<String?> = _aiApiKey.asStateFlow()
    
    private val _usageStats = MutableStateFlow(OpenAIUsageTracker.getUsageSummary())
    val usageStats: StateFlow<OpenAIUsageTracker.UsageSummary> = _usageStats.asStateFlow()
    
    // Store current problem and theme for alternate perspectives
    private var currentProblem: String = ""
    private var currentThemeId: String = ""
    private var currentSubthemeId: String = ""
    private var currentVerse: VerseEntry? = null
    
    init {
        // Test log to verify logging is working
        Log.i("MainViewModel", "═══════════════════════════════════════════════════════")
        Log.i("MainViewModel", "Gita App - Token Usage Tracking Enabled")
        Log.i("MainViewModel", "Look for 'OpenAIEmbeddingsClient' and 'OpenAIUsageTracker' in Logcat")
        Log.i("MainViewModel", "═══════════════════════════════════════════════════════")
        println("Gita App - Token Usage Tracking Enabled. Check Logcat for OpenAI usage logs.")
        
        // Load API key in background
        viewModelScope.launch {
            try {
                val saved = storage.getAiApiKey()
                val buildConfigKey = BuildConfig.OPENAI_API_KEY.takeIf { it.isNotBlank() }
                _aiApiKey.value = saved ?: buildConfigKey
                // Warm up KotlinModel assets (models + embeddings + verse/story data)
                kotlinModelRepo.ensureInitialized()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to load API key", e)
            }
        }
    }
    
    /**
     * STEP 1: Called when user clicks Continue on HomeScreen
     */
    fun submitProblem(problemText: String) {
        if (problemText.isNotBlank()) {
            currentProblem = problemText.trim()
            _appState.value = AppState.Pause(currentProblem)
        }
    }
    
    /**
     * STEP 2: Called by PauseScreen after delay
     * Processes the problem and generates response
     */
    fun processProblem() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Processing problem: $currentProblem")

                // Get API key once for the entire function
                val apiKey = _aiApiKey.value

                // NEW: Kotlin-only ML matching using OpenAI embeddings + bundled model/embeddings.
                if (!apiKey.isNullOrBlank() && currentProblem.isNotBlank()) {
                    try {
                        Log.i("MainViewModel", "═══════════════════════════════════════════════════════")
                        Log.i("MainViewModel", "ATTEMPTING ML MATCHING")
                        Log.i("MainViewModel", "Query: $currentProblem")
                        Log.i("MainViewModel", "API Key present: ${!apiKey.isNullOrBlank()}")
                        val match = kotlinModelRepo.match(currentProblem, apiKey)
                        if (match != null) {
                            Log.i("MainViewModel", "✅ ML model match successful: verse=${match.verse.id}, score=${match.score}")
                            Log.i("MainViewModel", "═══════════════════════════════════════════════════════")
                            val v = match.verse

                            // Generate complete translation (not word-by-word)
                            val reflectionGenerator = ReflectionGenerator(apiKey)
                            val completeTranslation = reflectionGenerator.generateCompleteTranslation(
                                sanskrit = v.sanskrit,
                                transliteration = v.transliteration,
                                wordByWordTranslation = v.translation
                            ) ?: v.translation // Fallback to original if AI fails

                            val explanation = v.explanation?.trim().orEmpty()
                            val detailed = v.detailed_explanation?.trim().orEmpty()
                            val reflectionBase = when {
                                explanation.isNotBlank() -> explanation
                                detailed.isNotBlank() -> detailed
                                else -> "A moment of reflection."
                            }

                            // Generate personalized reflection that aligns with user's query
                            val personalizedReflection = reflectionGenerator.generatePersonalizedReflection(
                                userQuery = currentProblem,
                                verseSanskrit = v.sanskrit,
                                verseTranslation = completeTranslation,
                                verseContext = v.context,
                                baseExplanation = reflectionBase
                            ) ?: reflectionBase // Fallback to base if AI fails

                            val reflections = mapOf(
                                ReflectionAngle.PSYCHOLOGICAL to personalizedReflection,
                                ReflectionAngle.ACTION to personalizedReflection,
                                ReflectionAngle.DETACHMENT to personalizedReflection,
                                ReflectionAngle.COMPASSION to personalizedReflection,
                                ReflectionAngle.SELFTRUST to personalizedReflection
                            )

                            val anchorLine = when {
                                match.story?.moral_lesson?.isNotBlank() == true -> match.story.moral_lesson.trim()
                                personalizedReflection.isNotBlank() -> personalizedReflection.take(100) + if (personalizedReflection.length > 100) "..." else ""
                                else -> "A quiet perspective."
                            }

                            val verseEntry = VerseEntry(
                                id = v.id,
                                chapter = v.chapter,
                                verse = v.verse,
                                sanskrit = v.sanskrit,
                                transliteration = v.transliteration,
                                translation = completeTranslation, // Use complete translation
                                context = v.context,
                                reflections = reflections,
                                anchorLines = listOf(anchorLine)
                            )

                            currentThemeId = "semantic"
                            currentSubthemeId = "semantic"
                            currentVerse = verseEntry

                            // Save to history (best-effort)
                            try {
                                storage.addHistoryEntry(
                                    HistoryEntry(
                                        timestamp = System.currentTimeMillis(),
                                        userInput = currentProblem,
                                        verseId = verseEntry.id,
                                        anchorLine = anchorLine
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Failed to save history (ML path)", e)
                            }

                            val storyCard = match.story?.let {
                                StoryCard(
                                    title = it.title,
                                    text = it.text,
                                    moralLesson = it.moral_lesson,
                                    keyThemes = it.key_themes ?: emptyList()
                                )
                            }

                            // Log session usage stats
                            OpenAIUsageTracker.logSessionStats()
                            // Update UI stats
                            _usageStats.value = OpenAIUsageTracker.getUsageSummary()

                            // Use the same Response screen; angle rotation still works (even if texts repeat)
                            _appState.value = AppState.Response(
                                verse = verseEntry,
                                reflection = reflections[ReflectionAngle.PSYCHOLOGICAL] ?: reflectionBase,
                                anchorLine = anchorLine,
                                currentAngle = ReflectionAngle.PSYCHOLOGICAL,
                                userInput = currentProblem,
                                themeId = currentThemeId,
                                subthemeId = currentSubthemeId,
                                story = storyCard
                            )
                            return@launch
                        } else {
                            Log.w("MainViewModel", "ML matching returned null, falling back to offline")
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "❌ KotlinModel match failed, falling back to offline selection", e)
                        e.printStackTrace()
                    }
                } else {
                    Log.w("MainViewModel", "Skipping ML matching - API key: ${if (apiKey.isNullOrBlank()) "MISSING" else "present"}, Problem: ${if (currentProblem.isBlank()) "empty" else "present"}")
                }
                
                // OFFLINE FALLBACK: keyword matching + deterministic verse rotation
                val detectedTheme = ThemeDetector.detectTheme(currentProblem) ?: ThemeDetector.getFallbackTheme()
                
                currentThemeId = detectedTheme.themeId
                currentSubthemeId = detectedTheme.subthemeId
                
                Log.d("MainViewModel", "Detected theme: ${detectedTheme.themeId}, subtheme: ${detectedTheme.subthemeId}")
                
                // Step 2: Select verse
                val verse = try {
                    selectionEngine.selectVerse(detectedTheme.themeId, detectedTheme.subthemeId)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Verse selection failed", e)
                    null
                }
                
                if (verse == null) {
                    Log.e("MainViewModel", "No verse found, returning to home")
                    _appState.value = AppState.Home
                    return@launch
                }
                
                currentVerse = verse
                Log.d("MainViewModel", "Selected verse: ${verse.id}")
                
                // Step 3: Select ONE reflection angle
                val reflectionAngle = try {
                    selectionEngine.getNextReflectionAngle(verse.id)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Reflection angle failed", e)
                    ReflectionAngle.PSYCHOLOGICAL // Safe default
                }
                
                // Step 4: Get base reflection text
                val baseReflection = verse.reflections[reflectionAngle] 
                    ?: verse.reflections.values.firstOrNull() 
                    ?: "Reflection not available for this verse."
                
                // Step 4b: Generate personalized reflection if API key is available
                val reflection = if (!apiKey.isNullOrBlank()) {
                    try {
                        val reflectionGenerator = ReflectionGenerator(apiKey)
                        val personalized = reflectionGenerator.generatePersonalizedReflection(
                            userQuery = currentProblem,
                            verseSanskrit = verse.sanskrit,
                            verseTranslation = verse.translation,
                            verseContext = verse.context,
                            baseExplanation = baseReflection
                        )
                        personalized ?: baseReflection
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Failed to generate personalized reflection", e)
                        baseReflection
                    }
                } else {
                    baseReflection
                }
                
                // Step 4c: Generate complete translation if API key is available
                val completeTranslation = if (!apiKey.isNullOrBlank()) {
                    try {
                        val reflectionGenerator = ReflectionGenerator(apiKey)
                        val complete = reflectionGenerator.generateCompleteTranslation(
                            sanskrit = verse.sanskrit,
                            transliteration = verse.transliteration,
                            wordByWordTranslation = verse.translation
                        )
                        complete ?: verse.translation
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Failed to generate complete translation", e)
                        verse.translation
                    }
                } else {
                    verse.translation
                }
                
                // Update verse with complete translation
                val updatedVerse = verse.copy(translation = completeTranslation)
                currentVerse = updatedVerse
                
                // Step 5: Select ONE anchor line
                val anchorLine = try {
                    val line = selectionEngine.getAnchorLine(verse)
                    if (line.isBlank()) {
                        verse.anchorLines.firstOrNull() ?: "Anchor line not available."
                    } else {
                        line
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Anchor line failed", e)
                    verse.anchorLines.firstOrNull() ?: "Anchor line not available."
                }
                
                Log.d("MainViewModel", "Response generated successfully")
                
                // Update usage stats (in case any API calls were made)
                _usageStats.value = OpenAIUsageTracker.getUsageSummary()
                
                // Step 6: Save to history
                try {
                    val historyEntry = HistoryEntry(
                        timestamp = System.currentTimeMillis(),
                        userInput = currentProblem,
                        verseId = verse.id,
                        anchorLine = anchorLine
                    )
                    storage.addHistoryEntry(historyEntry)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to save history", e)
                    // Continue - history is non-critical
                }
                
                // Step 7: Update UI state
                _appState.value = AppState.Response(
                    verse = updatedVerse,
                    reflection = reflection,
                    anchorLine = anchorLine,
                    currentAngle = reflectionAngle,
                    userInput = currentProblem,
                    themeId = currentThemeId,
                    subthemeId = currentSubthemeId,
                    story = null
                )
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "CRITICAL: processProblem failed", e)
                e.printStackTrace()
                // Return to home screen on any error
                _appState.value = AppState.Home
            }
        }
    }
    
    /**
     * STEP 7: Alternate perspective - rotate reflection angle for SAME verse
     * OR select another verse from SAME subtheme
     */
    fun getAnotherPerspective() {
        viewModelScope.launch {
            try {
                val currentState = _appState.value
                if (currentState !is AppState.Response) {
                    return@launch
                }
                
                val verse = currentVerse ?: currentState.verse
                
                // Option: Rotate reflection angle for SAME verse
                val nextAngle = try {
                    selectionEngine.getNextReflectionAngle(verse.id)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to get next angle", e)
                    currentState.currentAngle
                }
                
                val reflection = verse.reflections[nextAngle] 
                    ?: verse.reflections.values.firstOrNull() 
                    ?: currentState.reflection
                
                // Keep same anchor line (deterministic, so same verse = same anchor)
                val anchorLine = currentState.anchorLine
                
                _appState.value = currentState.copy(
                    reflection = reflection,
                    anchorLine = anchorLine,
                    currentAngle = nextAngle
                )
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "getAnotherPerspective failed", e)
            }
        }
    }
    
    fun navigateToHome() {
        currentProblem = ""
        currentVerse = null
        _appState.value = AppState.Home
    }
    
    fun navigateToHistory() {
        _appState.value = AppState.History
    }
    
    fun navigateToSettings() {
        // Refresh usage stats when opening settings
        _usageStats.value = OpenAIUsageTracker.getUsageSummary()
        _appState.value = AppState.Settings
    }
    
    fun saveAiApiKey(key: String?) {
        viewModelScope.launch {
            try {
                storage.setAiApiKey(key)
                _aiApiKey.value = key
            } catch (e: Exception) {
                Log.e("MainViewModel", "saveAiApiKey failed", e)
            }
        }
    }
}
