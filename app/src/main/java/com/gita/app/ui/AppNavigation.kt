package com.gita.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gita.app.viewmodel.AppState
import com.gita.app.viewmodel.MainViewModel

/**
 * State-based navigation router. Renders the appropriate screen
 * based on the current AppState from the ViewModel.
 */
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()
    val aiApiKey by viewModel.aiApiKey.collectAsState()
    val usageStats by viewModel.usageStats.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val historyEntries by viewModel.historyEntries.collectAsState()
    
    when (val state = appState) {
        is AppState.Splash -> {
            SplashScreen()
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
                historyEntries = historyEntries,
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
        is AppState.Error -> {
            ErrorScreen(
                message = state.message,
                isNetworkError = state.isNetworkError,
                isDarkMode = isDarkMode,
                onRetry = state.retryAction,
                onHome = { viewModel.navigateToHome() }
            )
        }
    }
}
