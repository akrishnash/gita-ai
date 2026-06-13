package com.gita.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.gita.app.viewmodel.AppState
import com.gita.app.viewmodel.MainViewModel
import com.gita.app.viewmodel.UiState

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
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()
    val dailyVerseEnabled by viewModel.dailyVerseEnabled.collectAsState()
    val dailyVerseHour by viewModel.dailyVerseHour.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isCurrentVerseBookmarked by viewModel.isCurrentVerseBookmarked.collectAsState()
    val context = LocalContext.current

    when (val state = appState) {
        is AppState.Login -> {
            LoginScreen(
                onGoogleSignIn = { viewModel.signInWithGoogle(context) },
                onGuestSignIn = { viewModel.continueAsGuest() },
                isLoading = isSigningIn
            )
        }
        is AppState.Splash -> {
            SplashScreen()
        }
        is AppState.Home -> {
            HomeScreen(
                isDarkMode = isDarkMode,
                language = selectedLanguage,
                streak = streak,
                onLanguageChange = { lang ->
                    viewModel.setLanguage(lang)
                },
                onDarkModeChange = { dark ->
                    viewModel.setDarkMode(dark)
                },
                onSubmit = { input ->
                    viewModel.submitProblem(input)
                },
                onNavigateHistory = { viewModel.navigateToHistory() },
                onNavigateBookmarks = { viewModel.navigateToBookmarks() },
                onNavigateSettings = { viewModel.navigateToSettings() },
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
                uiState = uiState,
                isBookmarked = isCurrentVerseBookmarked,
                onToggleBookmark = {
                    val s = appState
                    if (s is AppState.Response) {
                        viewModel.toggleBookmark(s.verse, s.verse.translation, s.userInput)
                    }
                },
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
        is AppState.Bookmarks -> {
            BookmarksScreen(
                bookmarks = bookmarks,
                isDarkMode = isDarkMode,
                onRemoveBookmark = { verseId -> viewModel.removeBookmark(verseId) },
                onBack = { viewModel.navigateToHome() }
            )
        }
        is AppState.History -> {
            HistoryScreen(
                historyEntries = historyEntries,
                isDarkMode = isDarkMode,
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.Settings -> {
            SettingsScreen(
                aiApiKey = aiApiKey,
                usageStats = usageStats,
                isDarkMode = isDarkMode,
                isLoggedIn = isLoggedIn,
                enableDailyVerse = dailyVerseEnabled,
                dailyVerseHour = dailyVerseHour,
                onSaveApiKey = { key ->
                    viewModel.saveAiApiKey(key)
                },
                onSignOut = { viewModel.signOut() },
                onDailyVerseToggle = { viewModel.setDailyVerseEnabled(it) },
                onDailyVerseHourChange = { viewModel.setDailyVerseHour(it) },
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
