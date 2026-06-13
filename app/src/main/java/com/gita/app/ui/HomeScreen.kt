package com.gita.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gita.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Home screen — minimalist spiritual input interface.
 */
@Composable
fun HomeScreen(
    isDarkMode: Boolean,
    language: String,
    streak: Int = 0,
    onLanguageChange: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onSubmit: (String) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateBookmarks: () -> Unit,
    onNavigateSettings: () -> Unit,
) {
    var inputText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    // Theme-aware colors
    val bgColor = if (isDarkMode) SurfaceDark else SurfaceLight
    val cardBg = if (isDarkMode) SurfaceDarkElevated else SurfaceLightElevated
    val textPrimary = if (isDarkMode) OnSurfaceDark else OnSurfaceLight
    val textMuted = if (isDarkMode) OnSurfaceDarkMuted else OnSurfaceLightMuted
    val accent = if (isDarkMode) IndigoLight else IndigoPrimary
    val gold = if (isDarkMode) SaffronGold else SaffronDeep
    val navBg = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F2EC)
    val inputBorder = if (isDarkMode) OutlineDark else OutlineLight

    // Focus-aware animated border color
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) accent.copy(alpha = 0.55f) else inputBorder,
        animationSpec = tween(300),
        label = "borderColor"
    )

    // Breathing Om watermark
    val infiniteTransition = rememberInfiniteTransition(label = "home")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val omAlpha by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omAlpha"
    )
    val particleY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle"
    )

    // Content fade-in
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOut),
        label = "contentFade"
    )

    // Rotating placeholders
    val placeholders = listOf(
        "What's weighing on your mind?",
        "Share what troubles your heart...",
        "Ask Krishna for guidance...",
        "What keeps you awake at night?",
        "Describe your inner conflict..."
    )
    var placeholderIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            placeholderIndex = (placeholderIndex + 1) % placeholders.size
        }
    }

    // Gradient button brush
    val sendGradient = if (inputText.isNotBlank()) {
        Brush.linearGradient(
            if (isDarkMode) GitaColors.buttonGradientDark else GitaColors.buttonGradientLight
        )
    } else {
        Brush.linearGradient(listOf(accent.copy(alpha = 0.15f), accent.copy(alpha = 0.15f)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Background Om watermark
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-80).dp)
        ) {
            Text(
                text = "ॐ",
                fontSize = 240.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.Serif,
                color = gold.copy(alpha = omAlpha),
                modifier = Modifier.scale(breatheScale)
            )
        }

        // Floating decorative particles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = (120 + particleY).dp)
                .alpha(0.08f)
        ) {
            Text("✦", fontSize = 20.sp, color = accent)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 50.dp, y = (200 - particleY).dp)
                .alpha(0.06f)
        ) {
            Text("✧", fontSize = 28.sp, color = gold)
        }

        // Subtle vertical gradient overlay — top transparent → bottom 8% gold tint
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, gold.copy(alpha = 0.08f))
                    )
                )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Elegant title lockup: lotus + गीता AI in serif
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪷", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "गीता AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 3.sp,
                            fontSize = 18.sp
                        ),
                        color = textMuted
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { onLanguageChange(if (language == "en") "hi" else "en") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (language == "en") "EN" else "हि",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textMuted,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = { onDarkModeChange(!isDarkMode) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Center content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Greeting
                Text(
                    text = if (language == "hi") "नमस्ते" else "Welcome",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = 2.sp,
                        fontSize = 28.sp
                    ),
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (language == "hi") "अपने मन की बात साझा करें" else "Share what's on your mind",
                    style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.5.sp),
                    color = textMuted
                )

                // Streak pill — only shown when streak ≥ 1
                if (streak >= 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(gold.copy(alpha = 0.10f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🔥 $streak day streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = gold.copy(alpha = 0.85f),
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                // Thin gold divider between greeting and input card
                Spacer(modifier = Modifier.height(28.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Divider(
                        modifier = Modifier.fillMaxWidth(0.35f),
                        thickness = 0.5.dp,
                        color = gold.copy(alpha = 0.35f)
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))

                // Input card with focus-glow border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 200.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { isFocused = it.isFocused },
                            textStyle = TextStyle(
                                color = textPrimary,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(accent),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = placeholders[placeholderIndex],
                                            style = TextStyle(
                                                color = textMuted.copy(alpha = 0.5f),
                                                fontSize = 16.sp,
                                                lineHeight = 26.sp
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Send row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (inputText.isNotEmpty()) {
                                Text(
                                    text = "${inputText.length}",
                                    fontSize = 11.sp,
                                    color = textMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }

                            // Gradient send button with haptic feedback
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(sendGradient)
                                    .clickable(enabled = inputText.isNotBlank()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSubmit(inputText)
                                        inputText = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = "Send",
                                    tint = if (inputText.isNotBlank()) {
                                        if (isDarkMode) Color(0xFF1A1A1A) else Color.White
                                    } else {
                                        textMuted.copy(alpha = 0.3f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Pill-shaped bottom navigation bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(navBg.copy(alpha = 0.92f))
                        .border(
                            width = 0.5.dp,
                            color = inputBorder,
                            shape = RoundedCornerShape(50.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onNavigateHistory,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = "History",
                            modifier = Modifier.size(15.dp),
                            tint = textMuted.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "History",
                            fontSize = 12.sp,
                            color = textMuted.copy(alpha = 0.6f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(18.dp)
                            .background(inputBorder)
                    )

                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(18.dp)
                            .background(inputBorder)
                    )

                    TextButton(
                        onClick = onNavigateBookmarks,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Bookmark,
                            contentDescription = "Bookmarks",
                            modifier = Modifier.size(15.dp),
                            tint = textMuted.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Saved",
                            fontSize = 12.sp,
                            color = textMuted.copy(alpha = 0.6f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(18.dp)
                            .background(inputBorder)
                    )

                    TextButton(
                        onClick = onNavigateSettings,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(15.dp),
                            tint = textMuted.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Settings",
                            fontSize = 12.sp,
                            color = textMuted.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
