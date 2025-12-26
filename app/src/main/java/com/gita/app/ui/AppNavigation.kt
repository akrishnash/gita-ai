package com.gita.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gita.app.viewmodel.AppState
import com.gita.app.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()
    val aiApiKey by viewModel.aiApiKey.collectAsState()
    
    when (val state = appState) {
        is AppState.Home -> {
            HomeScreen(
                onInputSubmitted = { input ->
                    viewModel.submitProblem(input)
                },
                onNavigateToHistory = {
                    viewModel.navigateToHistory()
                },
                onNavigateToSettings = {
                    viewModel.navigateToSettings()
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
                verse = state.verse,
                reflection = state.reflection,
                anchorLine = state.anchorLine,
                story = state.story,
                onAnotherPerspective = {
                    viewModel.getAnotherPerspective()
                },
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.History -> {
            HistoryScreen(
                historyEntries = emptyList(), // TODO: Load from ViewModel
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.Settings -> {
            SettingsScreen(
                aiApiKey = aiApiKey,
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
