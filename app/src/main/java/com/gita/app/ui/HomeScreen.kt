package com.gita.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gita.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Home screen — minimalist spiritual input interface.
 * 
 * Design: Dark immersive background with breathing Om symbol,
 * a prominent text input card, and subtle floating particles.
 */
@Composable
fun HomeScreen(
    isDarkMode: Boolean,
    language: String,
    onLanguageChange: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onSubmit: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    // Theme-aware colors
    val bgColor = if (isDarkMode) SurfaceDark else SurfaceLight
    val cardBg = if (isDarkMode) SurfaceDarkElevated else SurfaceLightElevated
    val textPrimary = if (isDarkMode) OnSurfaceDark else OnSurfaceLight
    val textMuted = if (isDarkMode) OnSurfaceDarkMuted else OnSurfaceLightMuted
    val accent = if (isDarkMode) IndigoLight else IndigoPrimary
    val gold = if (isDarkMode) SaffronGold else SaffronDeep
    val inputBorder = if (isDarkMode) OutlineDark else OutlineLight
    
    // Breathing Om animation
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
    
    // Subtle particle float
    val particleY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle"
    )
    
    // Title fade-in
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
    
    // Placeholder rotation
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
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App title
                Text(
                    text = "GITA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 6.sp,
                        fontSize = 16.sp
                    ),
                    color = textMuted
                )
                
                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Language toggle
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
                    
                    // Dark mode toggle
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
                    text = if (language == "hi") 
                        "अपने मन की बात साझा करें" 
                    else 
                        "Share what's on your mind",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = textMuted
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Input card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(
                            width = 1.dp,
                            color = inputBorder,
                            shape = RoundedCornerShape(20.dp)
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
                                .focusRequester(focusRequester),
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
                        
                        // Send button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Character count
                            if (inputText.isNotEmpty()) {
                                Text(
                                    text = "${inputText.length}",
                                    fontSize = 11.sp,
                                    color = textMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                            
                            // Send FAB
                            FilledIconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        onSubmit(inputText)
                                        inputText = ""
                                    }
                                },
                                enabled = inputText.isNotBlank(),
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (inputText.isNotBlank()) accent else accent.copy(alpha = 0.2f),
                                    contentColor = if (isDarkMode) Color(0xFF1A1A1A) else Color.White,
                                    disabledContainerColor = accent.copy(alpha = 0.1f),
                                    disabledContentColor = textMuted.copy(alpha = 0.3f)
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = "Send",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Bottom nav hints
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { /* Navigate to history via parent */ },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = textMuted.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "History",
                        fontSize = 12.sp,
                        color = textMuted.copy(alpha = 0.5f)
                    )
                }
                
                Text(
                    " · ",
                    color = textMuted.copy(alpha = 0.2f),
                    fontSize = 14.sp
                )
                
                TextButton(
                    onClick = { /* Navigate to settings via parent */ },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = textMuted.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Settings",
                        fontSize = 12.sp,
                        color = textMuted.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
