package com.gita.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gita.app.viewmodel.AppState
import com.gita.app.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()
    val aiApiKey by viewModel.aiApiKey.collectAsState()
    val usageStats by viewModel.usageStats.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    when (val state = appState) {
        is AppState.LanguageSelection -> {
            // Skip language selection - go directly to Home
            // Language can be changed from toggles in Home/Response screens
            viewModel.onLoginComplete()
        }
        is AppState.Login -> {
            // Skip login - go directly to Home
            viewModel.onLoginComplete()
        }
        is AppState.Home -> {
            HomeScreen(
                isDarkMode = isDarkMode,
                language = selectedLanguage,
                onLanguageChange = { lang ->
                    viewModel.setLanguage(lang)
                },
                onDarkModeChange = { dark ->
                    viewModel.setDarkMode(dark)
                },
                onSubmit = { input ->
                    viewModel.submitProblem(input)
                }
            )
        }
        is AppState.Pause -> {
            PauseScreen(
                userInput = state.userInput,
                onProcessProblem = {
                    viewModel.processProblem()
                },
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.Response -> {
            ResponseScreen(
                userQuestion = state.userInput,
                verse = state.verse,
                reflection = state.reflection,
                anchorLine = state.anchorLine,
                story = state.story,
                language = selectedLanguage,
                isDarkMode = isDarkMode,
                onLanguageChange = { lang ->
                    viewModel.setLanguage(lang)
                },
                onDarkModeChange = { dark ->
                    viewModel.setDarkMode(dark)
                },
                onAnotherPerspective = {
                    viewModel.getAnotherPerspective()
                },
                onBack = {
                    viewModel.navigateToHome()
                },
                debugInfo = state.debugInfo
            )
        }
        is AppState.History -> {
            HistoryScreen(
                historyEntries = emptyList(),
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.Settings -> {
            SettingsScreen(
                aiApiKey = aiApiKey,
                usageStats = usageStats,
                onSaveApiKey = { key ->
                    viewModel.saveAiApiKey(key)
                },
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
    }
}
