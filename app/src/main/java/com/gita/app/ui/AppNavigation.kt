package com.gita.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gita.app.viewmodel.AppState
import com.gita.app.viewmodel.MainViewModel
import com.gita.app.ui.HomeScreen
import com.gita.app.ui.PauseScreen
import com.gita.app.ui.ResponseScreen
import com.gita.app.ui.HistoryScreen
import com.gita.app.ui.SettingsScreen

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()
    
    when (val state = appState) {
        is AppState.Home -> {
            HomeScreen(
                onInputSubmitted = { input ->
                    viewModel.onUserInputSubmitted(input)
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
                onGenerateResponse = {
                    viewModel.generateResponse(state.userInput)
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
                currentAngle = state.currentAngle,
                userInput = state.userInput,
                onAnotherPerspective = {
                    viewModel.getAnotherPerspective(state.verse, state.currentAngle)
                },
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.History -> {
            HistoryScreen(
                historyEntries = viewModel.historyEntries.collectAsState().value,
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.Settings -> {
            SettingsScreen(
                aiApiKey = viewModel.aiApiKey.collectAsState().value,
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

