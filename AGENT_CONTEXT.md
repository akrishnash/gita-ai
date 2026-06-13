# Gita AI — Agent Context & Change Log

This document is written for AI agents picking up this project. It captures every significant change made across multiple sessions, the current architecture, active constraints, and known gotchas.

---

## Current State (as of June 2026)

A production-ready Android app built with Kotlin + Jetpack Compose (MVVM). It takes a user's emotional problem, matches it to a Bhagavad Gita verse using a multi-stage AI pipeline, and displays therapeutic guidance.

**Package:** `com.gita.app`
**Min SDK:** 26 · **Target SDK:** 34
**Build tool:** Gradle 8.4 with Kotlin DSL (`build.gradle.kts`)
**Compose BOM:** `2024.01.00` → resolves Material3 to `1.1.2`

---

## Hard Constraints (never violate these)

1. **API keys must never be hardcoded.** `GEMINI_API_KEY`, `OPENAI_API_KEY`, and `GOOGLE_WEB_CLIENT_ID` are read from `local.properties` via `buildConfigField`. Release builds always emit `""` for all three. Keys live only in `local.properties` (gitignored).

2. **Material3 is pinned at 1.1.2** (via the BOM). `HorizontalDivider` does not exist in this version — use `Divider` everywhere. Do not bump the BOM without auditing all usages.

3. **Java 17 required.** `gradle.properties` sets:
   ```
   org.gradle.java.home=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
   ```
   The system Java may be 25+. Never remove this line.

4. **`GitaMap.kt` has been deleted.** `ThemeDetector` and `SelectionEngine` are stubs that return `null`. Do not re-add `GitaMap.kt` or try to import it.

5. **`CircularProgressIndicator` crashes on Material3 1.1.2** due to a `KeyframesSpec.at()` method missing from animation-core at this BOM version. Use manual pulsing-dot animations with `infiniteTransition.animateFloat` instead.

---

## Architecture

### State Machine (`AppState` sealed class — `MainViewModel.kt`)

```
Splash → Login → Home → Pause → Response → Home
                  ↓              ↓
               History       (same verse)
               Bookmarks
               Settings
               Error
```

All screens are driven by `AppState`. Navigation happens by calling ViewModel methods that change `_appState`. **There is no Jetpack Navigation** — `AppNavigation.kt` is a single `when (appState)` block.

### ViewModel (`MainViewModel.kt`)

Single `AndroidViewModel`. All UI state is `StateFlow`. Key flows:

| Flow | Type | Purpose |
|------|------|---------|
| `appState` | `AppState` | Navigation state machine |
| `uiState` | `UiState` | Loading/Success/Error for Gemini fetch |
| `aiApiKey` | `String?` | OpenAI key from DataStore |
| `isDarkMode` | `Boolean` | Theme preference |
| `selectedLanguage` | `String` | "en" or "hi" |
| `historyEntries` | `List<HistoryEntry>` | Session history |
| `isLoggedIn` | `Boolean` | Firebase Auth state |
| `isSigningIn` | `Boolean` | Google sign-in in progress |
| `dailyVerseEnabled` | `Boolean` | WorkManager schedule toggle |
| `dailyVerseHour` | `Int` | Hour for daily notification |
| `streak` | `Int` | Current daily streak count |
| `bookmarks` | `List<BookmarkedVerse>` | Saved verse bookmarks |
| `isCurrentVerseBookmarked` | `Boolean` | Bookmark state for current verse |

### Persistence

- **DataStore (Preferences):** dark mode, language, API key, history, daily verse prefs, bookmarks JSON
- **SharedPreferences:** streak data only (`StreakManager` uses `"streak_prefs"`)
- **Firebase Auth:** sign-in state (persisted by Firebase SDK)

---

## AI Pipeline (processProblem in MainViewModel)

Priority order — falls through to next if current stage fails:

1. **Gemini Flash** — REST via Retrofit (`NetworkModule.geminiRepository`). Uses `GEMINI_API_KEY` from BuildConfig. On 429 or any error, logs and falls through.
2. **OpenAI RAG** — `KotlinModelRepository.match()`. Uses `OPENAI_API_KEY`. Multi-stage re-ranking with semantic embeddings. Returns `MatchResult` with verse + story + debug info.
3. **FastMatcher** — Local keyword matching, fully offline. `FastMatcher.match()` from `kotlinmodel/`.
4. **ThemeDetector fallback** — Returns a hardcoded `DetectedTheme`. `SelectionEngine.selectVerse()` returns `null` (stub), so this shows an error screen.

---

## File Map — Key Files

### Logic Layer (`app/src/main/java/com/gita/app/logic/`)

| File | Purpose |
|------|---------|
| `LocalStorage.kt` | DataStore wrapper. All suspend. Includes history, bookmarks, API key, prefs, daily verse. Bookmark methods: `saveBookmark`, `getBookmarks`, `removeBookmark`, `isBookmarked`. Uses Gson JSON lists. |
| `AuthManager.kt` | Firebase Auth + Credential Manager sign-in. `signInWithGoogle(context, webClientId)` returns `Result<FirebaseUser>`. Uses `kotlinx.coroutines.tasks.await` (NOT `.result`). |
| `ThemeDetector.kt` | **STUB.** Always returns `null` / fallback. No GitaMap dependency. |
| `SelectionEngine.kt` | **PARTIAL STUB.** `selectVerse()` returns `null`. `getNextReflectionAngle()` and `getAnchorLine()` still work. |
| `ShareManager.kt` | Renders `ShareCard` composable off-screen via `ComposeView` attached to decorView at `-1080px` translationX. `Density(3f)` override produces 1080px bitmap. FileProvider URI → `ACTION_SEND image/png`. |
| `DailyVerseWorker.kt` | `CoroutineWorker`. 10 hardcoded BG verses. Creates `daily_verse` notification channel. Checks `POST_NOTIFICATIONS` on API 33+. |
| `DailyVerseScheduler.kt` | WorkManager wrapper. `scheduleDailyVerse(context, hourOfDay)` sets `PeriodicWorkRequest` with 24h interval and computed initial delay. `cancelDailyVerse`, `isScheduled`. |
| `StreakManager.kt` | SharedPreferences-based streak. `recordActivity(context)` → increments if consecutive day, resets if gap > 1. `getCurrentStreak` returns 0 if last activity was more than yesterday. Uses `java.time.LocalDate`. |

### Data Layer (`app/src/main/java/com/gita/app/data/`)

| File | Purpose |
|------|---------|
| `BookmarkedVerse.kt` | Data class: `verseId, sanskrit, translation, chapterVerse, userProblem, savedAt: Long` |
| `VerseEntry.kt` | Core verse model used throughout the pipeline |
| `ReflectionAngle.kt` | Enum: PSYCHOLOGICAL, ACTION, DETACHMENT, COMPASSION, SELFTRUST |

### UI Layer (`app/src/main/java/com/gita/app/ui/`)

| File | Key Params / Notes |
|------|--------------------|
| `AppNavigation.kt` | Master router. Collects all ViewModel StateFlows. Handles all AppState branches including Bookmarks. |
| `HomeScreen.kt` | `streak: Int`, `onNavigateBookmarks`. Shows 🔥 streak pill when streak ≥ 1. Bottom pill nav: History · Saved · Settings. |
| `ResponseScreen.kt` | `isBookmarked: Boolean`, `onToggleBookmark`. Bookmark icon button (gold filled when saved) sits between "Another Verse" and Share. |
| `BookmarksScreen.kt` | Gold left-accent cards. Shows chapterVerse pill, Sanskrit (2 lines, italic), translation (3 lines), user problem quote, timestamp. Delete button. Empty state: 🔖 + text. |
| `LoginScreen.kt` | Breathing ॐ animation, Google sign-in pill with pulsing-dots loading (NOT CircularProgressIndicator), "Continue as Guest". |
| `SettingsScreen.kt` | OpenAI API key entry, daily verse Switch + TimePickerDialog, session usage stats, Sign Out (when logged in). |
| `ShareCard.kt` | Off-screen composable for verse image card: dark bg, indigo radial glow, gold Sanskrit, divider, translation, watermark. |
| `SplashScreen.kt` | Spring-animated ॐ. Routes to Login or Home after 1.5s. |
| `HistoryScreen.kt` | Left-accent cards with user problem, verse ref, anchor line. `IntrinsicSize.Min` for bar height. |
| `PauseScreen.kt` | Breathing animation while problem processes. |
| `ErrorScreen.kt` | Network/generic error with retry. |

### Components

| File | Purpose |
|------|---------|
| `ui/components/ShareCard.kt` | The card composable used by ShareManager for off-screen render |

### Theme (`app/src/main/java/com/gita/app/ui/theme/`)

Colors are defined in `Color.kt`. Key named colors used everywhere:
- `SurfaceDark` / `SurfaceLight` — page backgrounds
- `SurfaceDarkElevated` / `SurfaceLightElevated` — card backgrounds
- `OnSurfaceDark` / `OnSurfaceLight` — primary text
- `OnSurfaceDarkMuted` / `OnSurfaceLightMuted` — secondary text
- `IndigoPrimary` / `IndigoLight` — accent (light mode / dark mode)
- `SaffronGold` / `SaffronDeep` — gold accent (dark / light)
- `OutlineDark` / `OutlineLight` — borders/dividers
- `SageGreen` — success indicator dot

**Do not add new top-level color definitions without checking `Color.kt` first.** Do not change the color token names — every screen uses them.

---

## Build & Install

```bash
# Build
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Stream logs
adb logcat -s MainViewModel GitaApp AuthManager ShareManager

# If install fails with INSTALL_FAILED_UPDATE_INCOMPATIBLE
adb uninstall com.gita.app

# If install fails with INSTALL_FAILED_VERIFICATION_FAILURE
adb shell settings put global verifier_verify_adb_installs 0
```

---

## AndroidManifest Entries (non-obvious)

- `POST_NOTIFICATIONS` — required for daily verse on API 33+
- `RECEIVE_BOOT_COMPLETED` — WorkManager auto-reschedule
- `FileProvider` authority: `com.gita.app.fileprovider` → `res/xml/file_paths.xml` (cache-path)
- WorkManager `RescheduleReceiver` has `tools:replace="android:enabled"` to avoid manifest merge conflict

---

## Gradle — Key Dependencies

```kotlin
// Compose BOM (pins Material3 to 1.1.2 — do not change without audit)
implementation(platform("androidx.compose:compose-bom:2024.01.00"))

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-auth-ktx")

// Credentials (Google Sign-In)
implementation("androidx.credentials:credentials:1.2.0")
implementation("androidx.credentials:credentials-play-services-auth:1.2.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

// Retrofit + Gson (Gemini REST)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

`build.gradle.kts` needs `import java.util.Properties` at the top (Kotlin DSL requirement).

---

## Known Issues / Gotchas

| Issue | Root Cause | Fix Applied |
|-------|-----------|-------------|
| `CircularProgressIndicator` crash | `KeyframesSpec.at()` missing in animation-core bundled with BOM 2024.01.00 | Replaced with manual pulsing dots using `infiniteTransition.animateFloat` |
| `IllegalStateException: Task is not yet complete` | Called `.result` on Firebase `Task` synchronously | Use `kotlinx.coroutines.tasks.await` everywhere |
| `HorizontalDivider` unresolved | Added in Material3 1.2.0, not available in 1.1.2 | Use `Divider` instead |
| `java.util.Properties` unresolved in build.gradle.kts | Kotlin DSL requires explicit import | Added `import java.util.Properties` at top of build.gradle.kts |
| Install fails with INSTALL_FAILED_UPDATE_INCOMPATIBLE | APK signature changed from previous install | `adb uninstall com.gita.app` first |

---

## Features Implemented (in order)

1. **Visual design upgrade** — Lotus+serif title, gradient send button with haptic, focus-glow input border, gold dividers, pill bottom nav, Sanskrit verse improvements, verse reference pill, lotus divider, quote marks on translation, spring-animated splash, left-accent history cards, empty states.

2. **Gemini Flash API** — Retrofit REST integration. `GEMINI_API_KEY` from `local.properties` via BuildConfig. Deleted `GitaMap.kt`. Added `UiState` sealed class with loading overlay (pulsing ॐ) on ResponseScreen only. Falls back to OpenAI → FastMatcher on 429 or error.

3. **Google Sign-In** — Firebase Auth + Credential Manager. `AuthManager.kt`, `LoginScreen.kt`, `AppState.Login`. `GOOGLE_WEB_CLIENT_ID` from `local.properties`. Settings screen has Sign Out button. Splash routes to Login if not authenticated.

4. **Share verse as image card** — Off-screen `ComposeView` render at 3× density → 1080px bitmap → FileProvider → `ACTION_SEND image/png`. `ShareCard.kt` composable, `ShareManager.kt`. Share button on ResponseScreen.

5. **Daily verse push notification** — WorkManager `PeriodicWorkRequest` (24h). `DailyVerseWorker.kt`, `DailyVerseScheduler.kt`. Settings card with Switch + `TimePickerDialog`. 10 hardcoded verses in the worker.

6. **Daily streak tracker** — `StreakManager.kt` (SharedPreferences). Records on every successful verse match. 🔥 pill on HomeScreen (shown when streak ≥ 1). Loaded on init, refreshed in `saveHistoryEntry`.

7. **Verse bookmarking** — `BookmarkedVerse.kt` data class. `LocalStorage` bookmark methods (JSON list in DataStore). Bookmark icon button on ResponseScreen (gold when saved). `BookmarksScreen.kt`. "Saved" nav button in HomeScreen pill nav. `AppState.Bookmarks` route.
