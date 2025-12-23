package com.gita.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gita.app.data.ReflectionAngle
import com.gita.app.data.VerseEntry
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
        val userInput: String
    ) : AppState()
    object History : AppState()
    object Settings : AppState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val storage = LocalStorage(application)
    private val selectionEngine = SelectionEngine(storage)
    
    private val _appState = MutableStateFlow<AppState>(AppState.Home)
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    private val _historyEntries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val historyEntries: StateFlow<List<HistoryEntry>> = _historyEntries.asStateFlow()
    
    private val _aiApiKey = MutableStateFlow<String?>(null)
    val aiApiKey: StateFlow<String?> = _aiApiKey.asStateFlow()
    
    init {
        loadHistory()
        loadApiKey()
    }
    
    fun onUserInputSubmitted(input: String) {
        if (input.isNotBlank()) {
            _appState.value = AppState.Pause(input.trim())
        }
    }
    
    fun generateResponse(userInput: String) {
        viewModelScope.launch {
            val detectedTheme = ThemeDetector.detectTheme(userInput) 
                ?: ThemeDetector.getFallbackTheme()
            
            val verse = selectionEngine.selectVerse(
                detectedTheme.themeId,
                detectedTheme.subthemeId
            ) ?: return@launch
            
            val reflectionAngle = selectionEngine.getNextReflectionAngle(verse.id)
            val reflection = verse.reflections[reflectionAngle] ?: ""
            val anchorLine = selectionEngine.getAnchorLine(verse)
            
            val historyEntry = HistoryEntry(
                timestamp = System.currentTimeMillis(),
                userInput = userInput,
                verseId = verse.id,
                anchorLine = anchorLine
            )
            storage.addHistoryEntry(historyEntry)
            loadHistory()
            
            _appState.value = AppState.Response(
                verse = verse,
                reflection = reflection,
                anchorLine = anchorLine,
                currentAngle = reflectionAngle,
                userInput = userInput
            )
        }
    }
    
    fun getAnotherPerspective(currentVerse: VerseEntry, currentAngle: ReflectionAngle) {
        viewModelScope.launch {
            val nextAngle = selectionEngine.getNextReflectionAngle(currentVerse.id)
            val reflection = currentVerse.reflections[nextAngle] ?: ""
            
            val currentState = _appState.value
            if (currentState is AppState.Response) {
                _appState.value = currentState.copy(
                    reflection = reflection,
                    currentAngle = nextAngle
                )
            }
        }
    }
    
    fun navigateToHome() {
        _appState.value = AppState.Home
    }
    
    fun navigateToHistory() {
        _appState.value = AppState.History
    }
    
    fun navigateToSettings() {
        _appState.value = AppState.Settings
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            _historyEntries.value = storage.getHistoryEntries()
        }
    }
    
    private fun loadApiKey() {
        viewModelScope.launch {
            _aiApiKey.value = storage.getAiApiKey()
        }
    }
    
    fun saveAiApiKey(key: String?) {
        viewModelScope.launch {
            storage.setAiApiKey(key)
            _aiApiKey.value = key
        }
    }
}

