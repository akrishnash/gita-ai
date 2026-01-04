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
    object LanguageSelection : AppState()
    object Login : AppState()
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
    private val fastMatcher = com.gita.app.kotlinmodel.FastMatcher(application.applicationContext)
    
    private val _appState = MutableStateFlow<AppState>(AppState.LanguageSelection)
    
    // Language preference
    private val _selectedLanguage = MutableStateFlow<String>("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow<Boolean>(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
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
                Log.d("MainViewModel", "FAST Processing: $currentProblem")
                
                // Use FAST local matching - no API calls!
                val match = fastMatcher.match(currentProblem)
                
                if (match != null) {
                    val v = match.verse
                    Log.i("MainViewModel", "✅ Fast match: ${v.id}, emotion: ${match.emotion}")
                    
                    // Use therapeutic response as the reflection
                    val reflectionBase = match.therapeuticResponse.english
                    
                    val reflections = mapOf(
                        ReflectionAngle.PSYCHOLOGICAL to reflectionBase,
                        ReflectionAngle.ACTION to reflectionBase,
                        ReflectionAngle.DETACHMENT to reflectionBase,
                        ReflectionAngle.COMPASSION to reflectionBase,
                        ReflectionAngle.SELFTRUST to reflectionBase
                    )
                    
                    val anchorLine = v.wisdom_nugget?.trim() 
                        ?: reflectionBase.take(100) + if (reflectionBase.length > 100) "..." else ""
                    
                    val verseEntry = VerseEntry(
                        id = v.id,
                        chapter = v.chapter_number,
                        verse = v.verse_number,
                        sanskrit = v.sanskrit_text,
                        transliteration = v.transliteration,
                        translation = v.english_translation,
                        context = v.modern_problem_match ?: "",
                        reflections = reflections,
                        anchorLines = listOf(anchorLine)
                    )
                    
                    currentThemeId = match.emotion
                    currentSubthemeId = match.emotion
                    currentVerse = verseEntry
                    
                    // Store therapeutic response for UI
                    val debugInfo = com.gita.app.kotlinmodel.MatchDebugInfo(
                        detectedEmotion = match.emotion.replaceFirstChar { it.uppercase() },
                        emotionEmoji = getEmotionEmoji(match.emotion),
                        emotionScore = match.confidence,
                        emotionComfortingMessage = match.therapeuticResponse.english,
                        hindiResponse = match.therapeuticResponse.hindi,
                        allEmotionScores = null
                    )
                    
                    _appState.value = AppState.Response(
                        verse = verseEntry,
                        reflection = reflectionBase,
                        anchorLine = anchorLine,
                        currentAngle = ReflectionAngle.PSYCHOLOGICAL,
                        userInput = currentProblem,
                        themeId = currentThemeId,
                        subthemeId = currentSubthemeId,
                        story = null,
                        debugInfo = debugInfo
                    )
                    return@launch
                }
                
                // Fallback if no match
                Log.w("MainViewModel", "No fast match, using fallback")
                val detectedTheme = ThemeDetector.detectTheme(currentProblem) ?: ThemeDetector.getFallbackTheme()
                currentThemeId = detectedTheme.themeId
                currentSubthemeId = detectedTheme.subthemeId
                
                val verse = try {
                    selectionEngine.selectVerse(detectedTheme.themeId, detectedTheme.subthemeId)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Verse selection failed", e)
                    null
                }
                
                if (verse == null) {
                    _appState.value = AppState.Home
                    return@launch
                }
                
                currentVerse = verse
                val reflectionAngle = try {
                    selectionEngine.getNextReflectionAngle(verse.id)
                } catch (e: Exception) {
                    ReflectionAngle.PSYCHOLOGICAL
                }
                
                // Step 4: Get base reflection text
                val baseReflection = verse.reflections[reflectionAngle] 
                    ?: verse.reflections.values.firstOrNull() 
                    ?: "Reflection not available for this verse."
                
                // Use existing reflection for speed (skip AI calls)
                val reflection = baseReflection
                
                // Use existing translation for speed
                val completeTranslation = verse.translation
                
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
    
    private fun getEmotionEmoji(emotion: String): String {
        return when (emotion.lowercase()) {
            "anxiety" -> "😰"
            "grief" -> "😢"
            "anger" -> "😤"
            "confusion" -> "😕"
            "fear" -> "😨"
            "loneliness" -> "😔"
            "hopelessness" -> "😞"
            "burnout" -> "😩"
            "guilt" -> "😣"
            "attachment" -> "💔"
            else -> "🙏"
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
    
    /**
     * Called after successful login or guest mode
     */
    fun onLoginComplete() {
        _appState.value = AppState.Home
    }
    
    /**
     * Check if user was previously logged in
     */
    fun checkLoginState(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            _appState.value = AppState.Home
        }
    }
    
    /**
     * Called when language is selected
     */
    fun onLanguageSelected(languageCode: String) {
        _selectedLanguage.value = languageCode
        _appState.value = AppState.Login
    }
    
    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }
    
    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }
}
