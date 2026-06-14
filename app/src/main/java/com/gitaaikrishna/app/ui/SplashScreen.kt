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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaaikrishna.app.ui.theme.IndigoLight
import com.gitaaikrishna.app.ui.theme.SaffronGold
import com.gitaaikrishna.app.ui.theme.SurfaceDark
import kotlinx.coroutines.delay

/**
 * Animated splash screen.
 * Om symbol enters with an overshoot spring, then "गीता AI" fades in below.
 */
@Composable
fun SplashScreen() {
    val bgColor = SurfaceDark
    val accent = IndigoLight
    val gold = SaffronGold

    // One-shot spring scale for Om (0.5 → 1.0 with overshoot)
    var omVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        omVisible = true
    }
    val omScale by animateFloatAsState(
        targetValue = if (omVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "omScale"
    )

    // Title fades in ~600ms after Om appears
    var titleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(900)
        titleVisible = true
    }
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOut),
        label = "titleAlpha"
    )

    // Ambient glow ring breathing (infinite, subtle)
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val omAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omAlpha"
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
            // Om with ambient glow ring + spring entrance
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(omScale * 1.15f)
                        .alpha(glowAlpha)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(accent.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                Text(
                    text = "ॐ",
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = FontFamily.Serif,
                    color = gold.copy(alpha = omAlpha),
                    modifier = Modifier.scale(omScale)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // "गीता AI" — serif, letter-spaced, fades in 600ms after Om
            Text(
                text = "गीता AI",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 6.sp,
                    fontSize = 28.sp
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

            // Staggered loading dots
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
                            .background(accent, CircleShape)
                    )
                }
            }
        }
    }
}
