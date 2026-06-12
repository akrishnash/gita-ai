package com.gita.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Animated splash screen with breathing Om symbol.
 * Displayed while the app initializes model assets and loads preferences.
 */
@Composable
fun SplashScreen() {
    val bgColor = Color(0xFF0A0A0A)
    val accent = Color(0xFF9A8FD4) // Light Indigo
    val gold = Color(0xFFD4A574)   // Saffron Gold
    
    // Breathing animation for Om
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    
    val omAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omAlpha"
    )
    
    // Fade-in for title
    var titleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)
        titleVisible = true
    }
    
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOut),
        label = "titleAlpha"
    )
    
    // Glow ring animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Om symbol with glow
            Box(contentAlignment = Alignment.Center) {
                // Glow ring behind Om
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(breatheScale * 1.2f)
                        .alpha(glowAlpha)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                
                Text(
                    text = "ॐ",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = FontFamily.Serif,
                    color = gold.copy(alpha = omAlpha),
                    modifier = Modifier.scale(breatheScale)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // App title
            Text(
                text = "GITA",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = 12.sp,
                    fontSize = 32.sp
                ),
                color = Color(0xFFF0F0F0).copy(alpha = titleAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ancient wisdom, modern guidance",
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 2.sp
                ),
                color = Color(0xFF666666).copy(alpha = titleAlpha)
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Subtle loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.alpha(titleAlpha)
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 200)
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .alpha(dotAlpha)
                            .background(accent, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }
    }
}
