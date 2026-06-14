package com.gitaaikrishna.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.gitaaikrishna.app.ui.theme.IndigoLight
import com.gitaaikrishna.app.ui.theme.SaffronGold
import com.gitaaikrishna.app.ui.theme.SurfaceDark
import kotlinx.coroutines.delay

/**
 * Meditative pause/loading screen with breathing animation.
 * Shows while the RAG pipeline processes the user's query.
 * 
 * Features:
 * - Breathing Om symbol
 * - Rotating wisdom quotes
 * - Subtle glow ring
 * - Three-dot loading indicator
 */
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
    
    val bgColor = SurfaceDark
    val gold = SaffronGold
    val accent = IndigoLight
    
    // Wisdom quotes that rotate during loading
    val quotes = listOf(
        "\"Yoga is the journey of the self,\nthrough the self, to the self.\"",
        "\"The mind is restless, but it can be\nstilled through practice and detachment.\"",
        "\"You have the right to work,\nbut never to its fruits.\"",
        "\"When meditation is mastered,\nthe mind is unwavering.\"",
        "\"There is nothing lost or wasted\nin this life.\"",
        "\"Change is the law of the universe.\""
    )
    
    var quoteIndex by remember { mutableStateOf((0..quotes.lastIndex).random()) }
    var quoteAlpha by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        delay(800) // Wait a beat before showing quote
        quoteAlpha = 1f
    }
    
    // Rotate quotes every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            quoteAlpha = 0f
            delay(400)
            quoteIndex = (quoteIndex + 1) % quotes.size
            quoteAlpha = 1f
        }
    }
    
    val animatedQuoteAlpha by animateFloatAsState(
        targetValue = quoteAlpha,
        animationSpec = tween(400, easing = EaseInOutSine),
        label = "quoteAlpha"
    )
    
    // Breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    
    val omAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omAlpha"
    )
    
    // Glow ring
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.18f,
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            // Om with glow ring
            Box(contentAlignment = Alignment.Center) {
                // Glow ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(breatheScale * 1.15f)
                        .alpha(glowAlpha)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                Text(
                    text = "ॐ",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = FontFamily.Serif,
                    color = gold.copy(alpha = omAlpha),
                    modifier = Modifier.scale(breatheScale)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Wisdom quote
            Text(
                text = quotes[quoteIndex],
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF666666).copy(alpha = animatedQuoteAlpha),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                letterSpacing = 0.3.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            .size(6.dp)
                            .alpha(dotAlpha)
                            .background(accent, CircleShape)
                    )
                }
            }
        }
    }
}
