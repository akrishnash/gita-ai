package com.gita.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gita.app.BuildConfig
import com.gita.app.data.ReflectionAngle
import com.gita.app.data.VerseEntry
import com.gita.app.kotlinmodel.KotlinModelRepository
import com.gita.app.kotlinmodel.OpenAIUsageTracker
import com.gita.app.logic.DetectedTheme
import com.gita.app.logic.HistoryEntry
import com.gita.app.logic.LocalStorage
import com.gita.app.logic.SelectionEngine
import com.gita.app.logic.ThemeDetector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Navigation state machine for the app.
 */
sealed class AppState {
    /** Animated splash while assets load */
    object Splash : AppState()
    /** Main input screen */
    object Home : AppState()
    /** Breathing animation while processing */
    data class Pause(val userInput: String) : AppState()
    /** Verse response display */
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
    /** History list */
    object History : AppState()
    /** Settings/API key config */
    object Settings : AppState()
    /** Error with retry */
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false,
        val retryAction: (() -> Unit)? = null
    ) : AppState()
}

data class StoryCard(
    val title: String,
    val text: String,
    val moralLesson: String? = null,
    val keyThemes: List<String> = emptyList()
)

/**
 * Central ViewModel managing all app state, navigation, and data flow.
 * 
 * Lifecycle:
 * 1. Splash → load preferences + warm up model
 * 2. Home → user inputs problem
 * 3. Pause → breathing animation + processing
 * 4. Response → verse display
 * 5. Error → on API failure, with retry
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = LocalStorage(application.applicationContext)
    private val selectionEngine = SelectionEngine(storage)
    private val kotlinModelRepo = KotlinModelRepository(application.applicationContext)
    private val fastMatcher = com.gita.app.kotlinmodel.FastMatcher(application.applicationContext)
    
    private val _appState = MutableStateFlow<AppState>(AppState.Splash)
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    // Language preference
    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _aiApiKey = MutableStateFlow<String?>(null)
    val aiApiKey: StateFlow<String?> = _aiApiKey.asStateFlow()
    
    private val _usageStats = MutableStateFlow(OpenAIUsageTracker.getUsageSummary())
    val usageStats: StateFlow<OpenAIUsageTracker.UsageSummary> = _usageStats.asStateFlow()
    
    private val _historyEntries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val historyEntries: StateFlow<List<HistoryEntry>> = _historyEntries.asStateFlow()
    
    // Store current problem and theme for alternate perspectives
    private var currentProblem: String = ""
    private var currentThemeId: String = ""
    private var currentSubthemeId: String = ""
    private var currentVerse: VerseEntry? = null
    
    init {
        Log.i("MainViewModel", "═══════════════════════════════════════════════════════")
        Log.i("MainViewModel", "Gita App — Initializing")
        Log.i("MainViewModel", "═══════════════════════════════════════════════════════")
        
        // Load all saved state and warm up assets
        viewModelScope.launch {
            try {
                // Load persisted preferences
                _isDarkMode.value = storage.getDarkMode()
                _selectedLanguage.value = storage.getLanguage()
                
                // Load API key
                val saved = storage.getAiApiKey()
                val buildConfigKey = BuildConfig.OPENAI_API_KEY.takeIf { it.isNotBlank() }
                _aiApiKey.value = saved ?: buildConfigKey
                
                // Warm up KotlinModel assets (models + embeddings + verse/story data)
                kotlinModelRepo.ensureInitialized()
                
                // Brief splash display (min 1.5s for the animation)
                delay(1500)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Initialization error (non-fatal)", e)
            } finally {
                // Always proceed to Home, even if init partially fails
                _appState.value = AppState.Home
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Chat Flow
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * STEP 1: User submits a problem from HomeScreen.
     */
    fun submitProblem(problemText: String) {
        if (problemText.isNotBlank()) {
            currentProblem = problemText.trim()
            _appState.value = AppState.Pause(currentProblem)
        }
    }
    
    /**
     * STEP 2: Process the problem and generate response.
     * 
     * Pipeline priority:
     * 1. Enhanced mode (OpenAI RAG) — if API key available
     * 2. Fast mode (local keyword matching) — offline fallback
     * 3. Theme-based selection — ultimate fallback
     */
    fun processProblem() {
        viewModelScope.launch {
            try {
                val apiKey = _aiApiKey.value
                
                // ══════════════════════════════════════════════════════════════
                // ENHANCED MODE: Multi-stage re-ranking with OpenAI
                // ══════════════════════════════════════════════════════════════
                if (!apiKey.isNullOrBlank()) {
                    Log.d("MainViewModel", "Using ENHANCED multi-stage re-ranking...")
                    
                    val match = try {
                        kotlinModelRepo.match(currentProblem, apiKey)
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Enhanced matching failed", e)
                        null
                    }
                    
                    if (match != null) {
                        val v = match.verse
                        val debug = match.debugInfo
                        Log.i("MainViewModel", "✅ Enhanced match: ${v.id}")
                        
                        val reflectionBase = debug?.bridge 
                            ?: v.explanation 
                            ?: v.context
                        
                        val reflections = mapOf(
                            ReflectionAngle.PSYCHOLOGICAL to reflectionBase,
                            ReflectionAngle.ACTION to reflectionBase,
                            ReflectionAngle.DETACHMENT to reflectionBase,
                            ReflectionAngle.COMPASSION to reflectionBase,
                            ReflectionAngle.SELFTRUST to reflectionBase
                        )
                        
                        val anchorLine = v.explanation?.take(100) ?: reflectionBase.take(100)
                        
                        val verseEntry = VerseEntry(
                            id = v.id,
                            chapter = v.chapter,
                            verse = v.verse,
                            sanskrit = v.sanskrit,
                            transliteration = v.transliteration,
                            translation = v.translation,
                            context = v.context,
                            reflections = reflections,
                            anchorLines = listOf(anchorLine)
                        )
                        
                        currentThemeId = debug?.detectedEmotion ?: "unknown"
                        currentSubthemeId = debug?.therapeuticGoal ?: "perspective"
                        currentVerse = verseEntry
                        
                        val storyCard = match.story?.let {
                            StoryCard(
                                title = it.title,
                                text = it.text,
                                moralLesson = it.moral_lesson,
                                keyThemes = it.key_themes ?: emptyList()
                            )
                        }
                        
                        // Save to history
                        saveHistoryEntry(verseEntry, anchorLine)
                        
                        // Update usage stats
                        _usageStats.value = OpenAIUsageTracker.getUsageSummary()
                        
                        _appState.value = AppState.Response(
                            verse = verseEntry,
                            reflection = reflectionBase,
                            anchorLine = anchorLine,
                            currentAngle = ReflectionAngle.PSYCHOLOGICAL,
                            userInput = currentProblem,
                            themeId = currentThemeId,
                            subthemeId = currentSubthemeId,
                            story = storyCard,
                            debugInfo = debug
                        )
                        return@launch
                    }
                    
                    // If enhanced match returned null (API error), fall through to fast mode
                    Log.w("MainViewModel", "Enhanced mode returned null, falling back to fast mode")
                }
                
                // ══════════════════════════════════════════════════════════════
                // FAST MODE: Local keyword matching (no API calls)
                // ══════════════════════════════════════════════════════════════
                Log.d("MainViewModel", "Using FAST local matching...")
                
                val match = fastMatcher.match(currentProblem)
                
                if (match != null) {
                    val v = match.verse
                    Log.i("MainViewModel", "✅ Fast match: ${v.id}, emotion: ${match.emotion}")
                    
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
                    
                    val debugInfo = com.gita.app.kotlinmodel.MatchDebugInfo(
                        detectedEmotion = match.emotion.replaceFirstChar { it.uppercase() },
                        emotionEmoji = getEmotionEmoji(match.emotion),
                        emotionScore = match.confidence,
                        emotionComfortingMessage = match.therapeuticResponse.english,
                        hindiResponse = match.therapeuticResponse.hindi,
                        allEmotionScores = null
                    )
                    
                    // Save to history
                    saveHistoryEntry(verseEntry, anchorLine)
                    
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
                
                // ══════════════════════════════════════════════════════════════
                // FALLBACK: Theme-based selection
                // ══════════════════════════════════════════════════════════════
                Log.w("MainViewModel", "No match found, using theme fallback")
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
                    _appState.value = AppState.Error(
                        message = "Could not find a relevant verse. Please try rephrasing your question.",
                        isNetworkError = false,
                        retryAction = { navigateToHome() }
                    )
                    return@launch
                }
                
                currentVerse = verse
                val reflectionAngle = try {
                    selectionEngine.getNextReflectionAngle(verse.id)
                } catch (e: Exception) {
                    ReflectionAngle.PSYCHOLOGICAL
                }
                
                val baseReflection = verse.reflections[reflectionAngle] 
                    ?: verse.reflections.values.firstOrNull() 
                    ?: "Reflection not available for this verse."
                
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
                
                // Save to history
                saveHistoryEntry(verse, anchorLine)
                
                // Update usage stats
                _usageStats.value = OpenAIUsageTracker.getUsageSummary()
                
                _appState.value = AppState.Response(
                    verse = verse,
                    reflection = baseReflection,
                    anchorLine = anchorLine,
                    currentAngle = reflectionAngle,
                    userInput = currentProblem,
                    themeId = currentThemeId,
                    subthemeId = currentSubthemeId,
                    story = null
                )
                
            } catch (e: java.net.UnknownHostException) {
                Log.e("MainViewModel", "Network error", e)
                _appState.value = AppState.Error(
                    message = "Unable to connect. Please check your internet connection and try again.",
                    isNetworkError = true,
                    retryAction = {
                        _appState.value = AppState.Pause(currentProblem)
                        processProblem()
                    }
                )
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("MainViewModel", "Timeout error", e)
                _appState.value = AppState.Error(
                    message = "The request timed out. Please try again.",
                    isNetworkError = true,
                    retryAction = {
                        _appState.value = AppState.Pause(currentProblem)
                        processProblem()
                    }
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "CRITICAL: processProblem failed", e)
                _appState.value = AppState.Error(
                    message = "Something went wrong. Please try again.",
                    isNetworkError = false,
                    retryAction = { navigateToHome() }
                )
            }
        }
    }
    
    /**
     * Save a history entry for the current interaction.
     */
    private suspend fun saveHistoryEntry(verse: VerseEntry, anchorLine: String) {
        try {
            val historyEntry = HistoryEntry(
                timestamp = System.currentTimeMillis(),
                userInput = currentProblem,
                verseId = verse.id,
                anchorLine = anchorLine
            )
            storage.addHistoryEntry(historyEntry)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to save history (non-critical)", e)
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
            "identity crisis" -> "🪞"
            "intellectual doubt" -> "🤔"
            "moral dilemma" -> "⚖️"
            "pride" -> "👑"
            "result-obsession" -> "🎯"
            "jealousy" -> "💚"
            "impatience" -> "⏳"
            else -> "🙏"
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Alternate Perspective
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Rotate reflection angle for the same verse.
     */
    fun getAnotherPerspective() {
        viewModelScope.launch {
            try {
                val currentState = _appState.value
                if (currentState !is AppState.Response) {
                    return@launch
                }
                
                val verse = currentVerse ?: currentState.verse
                
                val nextAngle = try {
                    selectionEngine.getNextReflectionAngle(verse.id)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to get next angle", e)
                    currentState.currentAngle
                }
                
                val reflection = verse.reflections[nextAngle] 
                    ?: verse.reflections.values.firstOrNull() 
                    ?: currentState.reflection
                
                _appState.value = currentState.copy(
                    reflection = reflection,
                    anchorLine = currentState.anchorLine,
                    currentAngle = nextAngle
                )
                
            } catch (e: Exception) {
                Log.e("MainViewModel", "getAnotherPerspective failed", e)
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Navigation
    // ═══════════════════════════════════════════════════════════════
    
    fun navigateToHome() {
        currentProblem = ""
        currentVerse = null
        _appState.value = AppState.Home
    }
    
    fun navigateToHistory() {
        viewModelScope.launch {
            _historyEntries.value = storage.getHistoryEntries()
            _appState.value = AppState.History
        }
    }
    
    fun navigateToSettings() {
        _usageStats.value = OpenAIUsageTracker.getUsageSummary()
        _appState.value = AppState.Settings
    }
    
    fun deleteHistoryEntry(timestamp: Long) {
        viewModelScope.launch {
            storage.deleteHistoryEntry(timestamp)
            _historyEntries.value = storage.getHistoryEntries()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // User Preferences
    // ═══════════════════════════════════════════════════════════════
    
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
    
    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
        viewModelScope.launch {
            storage.setLanguage(lang)
        }
    }
    
    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        viewModelScope.launch {
            storage.setDarkMode(dark)
        }
    }
}
