package com.gita.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

@Composable
fun PauseScreen(
    userInput: String,
    onProcessProblem: () -> Unit,
    onBack: () -> Unit
) {
    // Process IMMEDIATELY - no delay
    LaunchedEffect(userInput) {
        delay(100) // Just enough for smooth transition
        onProcessProblem()
    }
    
    // Minimal dark theme
    val bgColor = Color(0xFF0A0A0A)
    val accent = Color(0xFFD4AF37)
    
    // Subtle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
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
            // Simple breathing Om
            Text(
                text = "ॐ",
                fontSize = 80.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.Serif,
                color = accent.copy(alpha = alpha),
                modifier = Modifier.scale(breatheScale)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Three dots loading
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 200)
                        ),
                        label = "dot$index"
                    )
                    Text(
                        text = "·",
                        fontSize = 24.sp,
                        color = accent.copy(alpha = dotAlpha)
                    )
                }
            }
        }
    }
}
