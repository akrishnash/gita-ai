package com.gita.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gita.app.ui.theme.*

@Composable
fun LoginScreen(
    onGoogleSignIn: () -> Unit,
    onGuestSignIn: () -> Unit,
    isLoading: Boolean = false
) {
    val gold = SaffronGold
    val textPrimary = OnSurfaceDark
    val textMuted = OnSurfaceDarkMuted

    val infiniteTransition = rememberInfiniteTransition(label = "loginOm")
    val omAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omBreath"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Om symbol — breathing
            Text(
                text = "ॐ",
                fontSize = 80.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.Serif,
                color = gold.copy(alpha = omAlpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "गीता AI",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 6.sp
                ),
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Ancient wisdom for modern struggles",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.5.sp
                ),
                color = textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Google Sign-In button
            Button(
                onClick = onGoogleSignIn,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F),
                    disabledContainerColor = Color.White.copy(alpha = 0.6f),
                    disabledContentColor = Color(0xFF1F1F1F).copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isLoading) {
                    // Pulsing dots — avoids CircularProgressIndicator's animation-core version mismatch
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(3) { i ->
                            val dotAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.2f,
                                targetValue = 0.9f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse,
                                    initialStartOffset = StartOffset(i * 160)
                                ),
                                label = "dot$i"
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .alpha(dotAlpha)
                                    .background(Color(0xFF4285F4), CircleShape)
                            )
                        }
                    }
                } else {
                    // Google "G" colored text mark
                    Text(
                        text = "G",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F1F1F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Guest option
            TextButton(
                onClick = onGuestSignIn,
                enabled = !isLoading
            ) {
                Text(
                    text = "Continue as Guest",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textMuted.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
