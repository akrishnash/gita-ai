package com.gitaaikrishna.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Gita App Theme — Spiritual, modern, immersive
 * 
 * Design philosophy:
 * - Dark mode: Deep charcoal with indigo/gold accents — meditative, immersive
 * - Light mode: Warm whites with earthy browns and sage — calm, manuscript-inspired
 * 
 * Color system:
 * - Primary: Indigo (spiritual wisdom, depth)
 * - Secondary: Sage Green (nature, healing, balance)
 * - Tertiary: Saffron Gold (divinity, warmth, tradition)
 */

// ═══════════════════════════════════════════════════════════════
// Primary — Deep Indigo (spiritual depth, wisdom)
// ═══════════════════════════════════════════════════════════════
val IndigoPrimary = Color(0xFF4A3F99)
val IndigoLight = Color(0xFF9A8FD4)
val IndigoDeep = Color(0xFF2D2478)
val IndigoPaleContainer = Color(0xFFE8E5F5)

// ═══════════════════════════════════════════════════════════════
// Secondary — Sage Green (healing, balance)
// ═══════════════════════════════════════════════════════════════
val SageGreen = Color(0xFF6BA89F)
val SageDark = Color(0xFF4D8A80)
val SageLight = Color(0xFFA7D4CC)
val SagePaleContainer = Color(0xFFDAF0EC)

// ═══════════════════════════════════════════════════════════════
// Tertiary — Saffron Gold (tradition, warmth, divinity)
// ═══════════════════════════════════════════════════════════════
val SaffronGold = Color(0xFFD4A574)
val SaffronLight = Color(0xFFF5D8A3)
val SaffronDeep = Color(0xFF8B7355)
val SaffronPaleContainer = Color(0xFFFFF3E0)

// ═══════════════════════════════════════════════════════════════
// Neutrals
// ═══════════════════════════════════════════════════════════════
val SurfaceDark = Color(0xFF0A0A0A)       // App background dark
val SurfaceDarkElevated = Color(0xFF141414) // Cards dark
val SurfaceDarkVariant = Color(0xFF1E1E1E)  // Elevated dark
val OnSurfaceDark = Color(0xFFF0F0F0)
val OnSurfaceDarkMuted = Color(0xFF888888)
val OutlineDark = Color(0xFF2A2A2A)

val SurfaceLight = Color(0xFFFCFCFA)       // App background light
val SurfaceLightElevated = Color(0xFFFFFFFF) // Cards light
val SurfaceLightVariant = Color(0xFFF0EDE6)  // Elevated light
val OnSurfaceLight = Color(0xFF1A1A1A)
val OnSurfaceLightMuted = Color(0xFF6B6B6B)
val OutlineLight = Color(0xFFE0DDD5)

// ═══════════════════════════════════════════════════════════════
// Accent / Emotion Colors
// ═══════════════════════════════════════════════════════════════
val LotusRose = Color(0xFFD47A99)          // Emotion accent
val TempleStone = Color(0xFF5D4037)        // Heritage accent

// ═══════════════════════════════════════════════════════════════
// Color Schemes
// ═══════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF1F1A3D),
    onPrimaryContainer = Color(0xFFD0CCE8),
    
    secondary = SageGreen,
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF1A3D38),
    onSecondaryContainer = SageLight,
    
    tertiary = SaffronGold,
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFF3D2E1A),
    onTertiaryContainer = SaffronLight,
    
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    
    surface = SurfaceDarkElevated,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = OnSurfaceDarkMuted,
    
    error = Color(0xFFCF6679),
    onError = Color.Black,
    
    outline = OutlineDark,
    outlineVariant = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoPaleContainer,
    onPrimaryContainer = IndigoDeep,
    
    secondary = SageDark,
    onSecondary = Color.White,
    secondaryContainer = SagePaleContainer,
    onSecondaryContainer = Color(0xFF1A3D38),
    
    tertiary = SaffronDeep,
    onTertiary = Color.White,
    tertiaryContainer = SaffronPaleContainer,
    onTertiaryContainer = Color(0xFF3E2723),
    
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    
    surface = SurfaceLightElevated,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = OnSurfaceLightMuted,
    
    error = Color(0xFFB3261E),
    onError = Color.White,
    
    outline = OutlineLight,
    outlineVariant = Color(0xFFE8E4DB)
)

/**
 * Main app theme composable.
 * 
 * @param darkTheme Whether to use dark theme. Controlled by user preference, not system.
 */
@Composable
fun GitaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Immersive: status bar matches background
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GitaTypography,
        content = content
    )
}

/**
 * Extended color tokens for spiritual UI elements.
 * Use these for custom components that go beyond Material 3 slots.
 */
object GitaColors {
    // Emotion gradients — dark mode
    val emotionGradientDark = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
    val emotionGradientLight = listOf(Color(0xFFF0EDE6), Color(0xFFE8E5F5))
    
    // Glow colors per emotion
    val anxietyGlow = Color(0xFF7E57C2)
    val griefGlow = Color(0xFF5C6BC0)
    val angerGlow = Color(0xFFEF5350)
    val attachmentGlow = Color(0xFFEC407A)
    val burnoutGlow = Color(0xFF78909C)
    val confusionGlow = Color(0xFF66BB6A)
    val fearGlow = Color(0xFFFFCA28)
    val hopelessnessGlow = Color(0xFF8D6E63)
    
    // Card backgrounds
    val cardDark = SurfaceDarkElevated
    val cardLight = SurfaceLightElevated
    
    // Sanskrit text
    val sanskritDark = SaffronGold
    val sanskritLight = TempleStone
    
    // Transliteration
    val transliterationDark = Color(0xFF888888)
    val transliterationLight = Color(0xFF795548)
    
    // Gradient for primary buttons
    val buttonGradientDark = listOf(IndigoLight, IndigoPrimary)
    val buttonGradientLight = listOf(IndigoPrimary, IndigoDeep)
    
    // Divider
    val dividerDark = OutlineDark
    val dividerLight = OutlineLight
}
