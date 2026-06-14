package com.gitaaikrishna.app.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.gitaaikrishna.app.logic.RazorpayManager
import com.gitaaikrishna.app.viewmodel.AppState
import com.gitaaikrishna.app.viewmodel.MainViewModel
import com.gitaaikrishna.app.viewmodel.UiState

/**
 * State-based navigation router. Renders the appropriate screen
 * based on the current AppState from the ViewModel.
 */
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val appState by viewModel.appState.collectAsState()
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
    val subscriptionTier by viewModel.subscriptionTier.collectAsState()
    val remainingQueries by viewModel.remainingQueries.collectAsState()
    val context = LocalContext.current

    val dayOfWeek = remember { java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) }
    val quoteOfDay = remember(dayOfWeek, selectedLanguage) {
        val quotes = if (selectedLanguage == "hi") listOf(
            "कर्म करो, फल की चिंता मत करो। — BG 2.47",
            "मन को वश में करो। — BG 6.35",
            "आत्मा न जन्म लेती है, न मरती है। — BG 2.20",
            "जो होता है, अच्छे के लिए होता है। — BG 2.14",
            "जब भी धर्म का पतन होता है, मैं आता हूं। — BG 4.7",
            "मुझ पर भरोसा रखो। — BG 18.66",
            "अपना उद्धार स्वयं करो। — BG 6.5"
        ) else listOf(
            "Do your duty without attachment to results. — BG 2.47",
            "Control your mind — that is the greatest victory. — BG 6.35",
            "The soul is never born, nor does it die. — BG 2.20",
            "Whatever happens, happens for good. — BG 2.14",
            "Whenever righteousness declines, I appear. — BG 4.7",
            "Surrender to me and I shall deliver you. — BG 18.66",
            "Uplift yourself by your own self. — BG 6.5"
        )
        quotes[(dayOfWeek - 1) % 7]
    }

    when (val state = appState) {
        is AppState.Login -> {
            LoginScreen(
                onGoogleSignIn = { viewModel.signInWithGoogle(context) },
                onGuestSignIn = { viewModel.continueAsGuest() },
                isLoading = isSigningIn,
                language = selectedLanguage
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
                remainingQueries = remainingQueries,
                subscriptionTier = subscriptionTier,
                quoteOfDay = quoteOfDay,
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
                debugInfo = state.debugInfo,
                hindiTranslation = state.hindiTranslation
            )
        }
        is AppState.Bookmarks -> {
            BookmarksScreen(
                bookmarks = bookmarks,
                isDarkMode = isDarkMode,
                language = selectedLanguage,
                onRemoveBookmark = { verseId -> viewModel.removeBookmark(verseId) },
                onBack = { viewModel.navigateToHome() }
            )
        }
        is AppState.History -> {
            HistoryScreen(
                historyEntries = historyEntries,
                isDarkMode = isDarkMode,
                language = selectedLanguage,
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.Settings -> {
            SettingsScreen(
                isDarkMode = isDarkMode,
                language = selectedLanguage,
                isLoggedIn = isLoggedIn,
                subscriptionTier = subscriptionTier,
                subscriptionExpiry = com.gitaaikrishna.app.logic.SubscriptionManager.getExpiryTimestamp(context),
                enableDailyVerse = dailyVerseEnabled,
                dailyVerseHour = dailyVerseHour,
                onSignOut = { viewModel.signOut() },
                onDailyVerseToggle = { viewModel.setDailyVerseEnabled(it) },
                onDailyVerseHourChange = { viewModel.setDailyVerseHour(it) },
                onUpgrade = { viewModel.navigateToPaywall() },
                onBack = {
                    viewModel.navigateToHome()
                }
            )
        }
        is AppState.Paywall -> {
            PaywallScreen(
                language = selectedLanguage,
                isDarkMode = isDarkMode,
                onSubscribe = { tier ->
                    val activity = context as? Activity ?: return@PaywallScreen
                    RazorpayManager.startPayment(
                        activity = activity,
                        tier = tier,
                        onSuccess = { resolvedTier, paymentId ->
                            viewModel.onPaymentSuccess(resolvedTier, paymentId)
                        },
                        onFailure = { error ->
                            viewModel.navigateToHome()
                            android.util.Log.e("Razorpay", "Payment failed: $error")
                        }
                    )
                },
                onRestorePurchase = { viewModel.restorePurchase() },
                onBack = { viewModel.navigateToHome() }
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
