package com.gita.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gita.app.ai.IntentInterpreter
import com.gita.app.data.ReflectionAngle
import com.gita.app.data.VerseEntry
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
        val subthemeId: String
    ) : AppState()
    object History : AppState()
    object Settings : AppState()
}

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
    
    private val _appState = MutableStateFlow<AppState>(AppState.Home)
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    private val _aiApiKey = MutableStateFlow<String?>(null)
    val aiApiKey: StateFlow<String?> = _aiApiKey.asStateFlow()
    
    // Store current problem and theme for alternate perspectives
    private var currentProblem: String = ""
    private var currentThemeId: String = ""
    private var currentSubthemeId: String = ""
    private var currentVerse: VerseEntry? = null
    
    init {
        // Load API key in background
        viewModelScope.launch {
            try {
                _aiApiKey.value = storage.getAiApiKey()
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
                
                // Step 1: Try AI intent extraction first
                val detectedTheme = try {
                    val apiKey = _aiApiKey.value
                    if (!apiKey.isNullOrBlank()) {
                        val intentInterpreter = IntentInterpreter(apiKey)
                        val aiIntent = intentInterpreter.extractIntent(currentProblem)
                        
                        if (aiIntent != null) {
                            Log.d("MainViewModel", "AI intent extracted: ${aiIntent.primaryTheme}/${aiIntent.subtheme}, confidence=${aiIntent.confidence}")
                            // Convert AI intent to DetectedTheme
                            DetectedTheme(
                                themeId = aiIntent.primaryTheme,
                                subthemeId = aiIntent.subtheme,
                                confidence = aiIntent.confidence
                            )
                        } else {
                            Log.d("MainViewModel", "AI intent extraction failed or low confidence, falling back to keyword matching")
                            // Fallback to keyword matching
                            ThemeDetector.detectTheme(currentProblem) ?: ThemeDetector.getFallbackTheme()
                        }
                    } else {
                        Log.d("MainViewModel", "No API key, using keyword matching")
                        // No API key, use keyword matching
                        ThemeDetector.detectTheme(currentProblem) ?: ThemeDetector.getFallbackTheme()
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Intent extraction failed, falling back", e)
                    // Fallback to keyword matching on any error
                    ThemeDetector.detectTheme(currentProblem) ?: ThemeDetector.getFallbackTheme()
                }
                
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
                
                // Step 4: Get reflection text
                val reflection = verse.reflections[reflectionAngle] 
                    ?: verse.reflections.values.firstOrNull() 
                    ?: "Reflection not available for this verse."
                
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
                    verse = verse,
                    reflection = reflection,
                    anchorLine = anchorLine,
                    currentAngle = reflectionAngle,
                    userInput = currentProblem,
                    themeId = currentThemeId,
                    subthemeId = currentSubthemeId
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
