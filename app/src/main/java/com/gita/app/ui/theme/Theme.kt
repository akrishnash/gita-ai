package com.gita.app.ui.theme

import android.app.Activity
import android.os.Build
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
 * Gita App Theme - Inspired by ancient manuscripts, sacred texts, and the divine
 * 
 * A warm, earthy palette with rich accents that evokes:
 * - Saffron robes of wisdom
 * - Ancient palm leaf manuscripts  
 * - Temple architecture and sacred geometry
 * - The lotus emerging from muddy waters
 */

// Primary Colors - Warm Saffron/Gold palette (represents wisdom, renunciation)
val SaffronGold = Color(0xFFD4A12A)
val DeepSaffron = Color(0xFFFF8F00)
val BurntSaffron = Color(0xFFEF6C00)

// Secondary Colors - Sacred Earth tones
val TempleStone = Color(0xFF5D4037)
val AncientParchment = Color(0xFFFFF8E7)
val SacredClay = Color(0xFF8D6E63)

// Accent Colors - Lotus & Peacock inspired
val LotusPink = Color(0xFFE91E63)
val LotusDeep = Color(0xFFC2185B)
val PeacockTeal = Color(0xFF00897B)
val PeacockBlue = Color(0xFF0288D1)

// Neutral Colors
val DarkCharcoal = Color(0xFF1A1A1A)
val WarmWhite = Color(0xFFFFFBF5)
val SoftCream = Color(0xFFFAF8F5)
val MutedGold = Color(0xFFBFA76A)

// Spiritual accent
val DivinePurple = Color(0xFF5E35B1)
val TwilightIndigo = Color(0xFF303F9F)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronGold,
    onPrimary = DarkCharcoal,
    primaryContainer = Color(0xFF3D2A00),
    onPrimaryContainer = Color(0xFFFFE082),
    
    secondary = PeacockTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFA7FFEB),
    
    tertiary = LotusPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF5D0033),
    onTertiaryContainer = Color(0xFFFFD9E3),
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E8E8),
    
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFCACACA),
    
    error = Color(0xFFCF6679),
    onError = Color.Black,
    
    outline = Color(0xFF505050),
    outlineVariant = Color(0xFF383838)
)

private val LightColorScheme = lightColorScheme(
    primary = TempleStone,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF3E2723),
    
    secondary = PeacockTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00251A),
    
    tertiary = DivinePurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE7F6),
    onTertiaryContainer = Color(0xFF311B92),
    
    background = WarmWhite,
    onBackground = Color(0xFF1C1917),
    
    surface = SoftCream,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFF5F0E6),
    onSurfaceVariant = Color(0xFF49454F),
    
    error = Color(0xFFB3261E),
    onError = Color.White,
    
    outline = Color(0xFFBFB5A0),
    outlineVariant = Color(0xFFD9D0C0)
)

@Composable
fun GitaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color for consistent branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use surface color for status bar (more subtle)
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GitaTypography,
        content = content
    )
}

/**
 * Extension colors for emotion-based theming
 */
object GitaColors {
    // Emotion gradients - starting colors
    val anxietyGradientStart = Color(0xFFEDE7F6)
    val griefGradientStart = Color(0xFFE8EAF6)
    val angerGradientStart = Color(0xFFFFEBEE)
    val attachmentGradientStart = Color(0xFFFCE4EC)
    val burnoutGradientStart = Color(0xFFECEFF1)
    
    // Warm accents
    val warmGold = SaffronGold
    val sacredRed = Color(0xFFC62828)
    val peacefulBlue = PeacockBlue
    val forestGreen = Color(0xFF2E7D32)
    val wisdomPurple = DivinePurple
    
    // Card backgrounds
    val cardElevated = Color(0xFFFFFDF8)
    val cardSubtle = Color(0xFFF9F6F0)
    
    // Sanskrit/Devanagari text
    val sanskritText = Color(0xFF5D4037)
    val transliterationText = Color(0xFF795548)
}
