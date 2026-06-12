package com.gita.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Graceful error screen with spiritual design language.
 * Shows network errors with retry option, or general errors with return-to-home.
 */
@Composable
fun ErrorScreen(
    message: String,
    isNetworkError: Boolean = false,
    isDarkMode: Boolean = true,
    onRetry: (() -> Unit)? = null,
    onHome: () -> Unit
) {
    val bgPrimary = if (isDarkMode) Color(0xFF0A0A0A) else Color(0xFFFCFCFA)
    val cardBg = if (isDarkMode) Color(0xFF141414) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkMode) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
    val textMuted = if (isDarkMode) Color(0xFF5A5A5A) else Color(0xFFAAAAAA)
    val accent = if (isDarkMode) Color(0xFF9A8FD4) else Color(0xFF4A3F99) // Indigo
    val errorAccent = if (isDarkMode) Color(0xFFD4A574) else Color(0xFF8B7355)
    
    // Gentle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "error")
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Icon(
                imageVector = if (isNetworkError) Icons.Outlined.CloudOff else Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .alpha(breatheAlpha),
                tint = errorAccent
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Calming title
            Text(
                text = if (isNetworkError) "Connection lost" else "A moment of pause",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = textPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Error message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 24.sp
                ),
                color = textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calming quote
            Text(
                text = "\"In the midst of difficulty lies opportunity.\"",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 0.5.sp
                ),
                color = textMuted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onRetry != null) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Try Again",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(
                                textMuted.copy(alpha = 0.3f),
                                textMuted.copy(alpha = 0.2f)
                            )
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textMuted
                    )
                ) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Return Home",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
