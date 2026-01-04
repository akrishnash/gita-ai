package com.gita.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gita.app.kotlinmodel.EmotionConfig
import com.gita.app.ui.theme.GitaColors
import com.gita.app.ui.theme.GitaTextStyles
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated fade-in wrapper for staggered reveal effects
 */
@Composable
fun FadeInAnimation(
    delay: Int = 0,
    duration: Int = 600,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(duration)) + 
                slideInVertically(
                    animationSpec = tween(duration),
                    initialOffsetY = { it / 4 }
                )
    ) {
        content()
    }
}

/**
 * Animated scale-in for emphasis elements
 */
@Composable
fun ScaleInAnimation(
    delay: Int = 0,
    duration: Int = 500,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(duration))
    ) {
        content()
    }
}

/**
 * Gentle pulsing animation for breathing/meditation feel
 */
@Composable
fun PulseAnimation(
    pulseFraction: Float = 0.03f,
    duration: Int = 2000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + pulseFraction,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

/**
 * Floating animation for decorative elements
 */
@Composable
fun FloatingAnimation(
    offsetY: Dp = 8.dp,
    duration: Int = 3000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = offsetY.value,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    
    Box(modifier = Modifier.offset(y = offset.dp)) {
        content()
    }
}

/**
 * Beautiful gradient background with optional animated blur orbs
 */
@Composable
fun SacredGradientBackground(
    colors: List<Color> = listOf(
        Color(0xFFFFF8E7),  // Warm parchment
        Color(0xFFFFF3E0),  // Light saffron
        Color(0xFFFFECB3),  // Golden glow
        Color(0xFFFFF8E7)   // Back to parchment
    ),
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradient = Brush.verticalGradient(colors = colors)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        content()
    }
}

/**
 * Decorative Om symbol with animation
 */
@Composable
fun AnimatedOmSymbol(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD4A12A).copy(alpha = 0.15f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "om")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omAlpha"
    )
    
    Text(
        text = "ॐ",
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 200.sp,
            fontWeight = FontWeight.Thin
        ),
        color = color.copy(alpha = alpha),
        modifier = modifier
    )
}

/**
 * Beautiful card for verse display
 */
@Composable
fun VerseCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}

/**
 * Stunning emotion display card with glassmorphism, animated glow, and particle effects
 */
@Composable
fun EmotionCard(
    emotionId: String?,
    emotionEmoji: String?,
    emotionScore: Float?,
    comfortingMessage: String?,
    modifier: Modifier = Modifier
) {
    val emotionData = emotionId?.let { EmotionConfig.getEmotion(it) }
    val gradientColors = emotionId?.let { EmotionConfig.getGradientColors(it) }
        ?: listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
    val glowColor = emotionId?.let { EmotionConfig.getGlowColor(it) } ?: Color(0xFF7E57C2)
    val sanskritName = emotionId?.let { EmotionConfig.getSanskritName(it) }
    val healingMantra = emotionId?.let { EmotionConfig.getHealingMantra(it) }
    
    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "emotionGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    
    // Floating particles animation
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particles"
    )
    
    val gradient = Brush.verticalGradient(
        colors = gradientColors.map { it.copy(alpha = 0.9f) }
    )
    
    // Glassmorphism overlay gradient
    val glassOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.1f),
            Color.Transparent
        )
    )
    
    ScaleInAnimation(delay = 200) {
        Box(modifier = modifier.fillMaxWidth()) {
            // Glow effect behind card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(glowScale)
                    .alpha(glowAlpha * 0.5f)
                    .blur(20.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                            radius = 400f
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradient)
                ) {
                    // Glass overlay for depth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(glassOverlay)
                    )
                    
                    // Floating decorative elements
                    Box(
                        modifier = Modifier
                            .offset(x = (-20).dp, y = particleOffset.dp)
                            .alpha(0.15f)
                    ) {
                        Text(
                            text = "✦",
                            fontSize = 24.sp,
                            color = glowColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (30 - particleOffset).dp)
                            .alpha(0.12f)
                    ) {
                        Text(
                            text = "✧",
                            fontSize = 32.sp,
                            color = glowColor
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        // Emoji with enhanced pulse and glow
                        Box(contentAlignment = Alignment.Center) {
                            // Subtle glow ring
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(glowScale)
                                    .alpha(glowAlpha * 0.3f)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(glowColor.copy(alpha = 0.5f), Color.Transparent)
                                        ),
                                        shape = CircleShape
                                    )
                            )
                            PulseAnimation(pulseFraction = 0.05f) {
                                Text(
                                    text = emotionEmoji ?: emotionData?.emoji ?: "🔮",
                                    fontSize = 56.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Emotion name with optional Sanskrit
                        Text(
                            text = emotionData?.displayName ?: emotionId ?: "Unknown",
                            style = GitaTextStyles.emotionDisplay.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center
                        )
                        
                        // Sanskrit name
                        if (!sanskritName.isNullOrBlank()) {
                            Text(
                                text = sanskritName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Serif
                                ),
                                color = Color(0xFF5D4037).copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        // Enhanced confidence indicator
                        emotionScore?.let { score ->
                            val percentage = ((score + 5f) / 20f * 100f).coerceIn(0f, 100f)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Confidence bar with animation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percentage / 100f)
                                        .fillMaxHeight()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(glowColor.copy(alpha = 0.7f), glowColor)
                                            ),
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                            
                            Text(
                                text = "Resonance: ${"%.0f".format(percentage)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF555555),
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        
                        // Decorative divider
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.alpha(0.6f)
                        ) {
                            Divider(modifier = Modifier.width(30.dp), color = glowColor.copy(alpha = 0.5f))
                            Text(
                                text = " ॐ ",
                                fontSize = 14.sp,
                                color = glowColor
                            )
                            Divider(modifier = Modifier.width(30.dp), color = glowColor.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Comforting message
                        val message = comfortingMessage ?: emotionData?.comfortingMessage
                        if (!message.isNullOrBlank()) {
                            Text(
                                text = "\"$message\"",
                                style = GitaTextStyles.comfortingMessage.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color(0xFF3A3A3A),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        
                        // Healing mantra
                        if (!healingMantra.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = glowColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = healingMantra,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF5D4037),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wisdom nugget / anchor line card
 */
@Composable
fun WisdomCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFD4A12A).copy(alpha = 0.1f),
                            Color.Transparent,
                            Color(0xFFD4A12A).copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "✦",
                    fontSize = 16.sp,
                    color = Color(0xFFD4A12A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = text,
                    style = GitaTextStyles.wisdomNugget,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF3E2723)
                )
                Text(
                    text = "✦",
                    fontSize = 16.sp,
                    color = Color(0xFFD4A12A),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Beautiful primary button
 */
@Composable
fun SacredButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(Color(0xFF5D4037), Color(0xFF795548))
        } else {
            listOf(Color(0xFFBDBDBD), Color(0xFFE0E0E0))
        }
    )
    
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(16.dp))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
        }
    }
}

/**
 * Loading dots animation
 */
@Composable
fun LoadingDots(
    color: Color = Color(0xFF5D4037),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val delay = index * 200
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1000
                        0.3f at 0
                        1f at 500
                        0.3f at 1000
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(delay)
                ),
                label = "dotAlpha$index"
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .alpha(alpha)
                    .background(color, CircleShape)
            )
        }
    }
}

/**
 * Animated emotion radar/spider chart showing top emotion scores
 */
@Composable
fun EmotionRadarChart(
    emotionScores: Map<String, Float>,
    primaryEmotion: String?,
    modifier: Modifier = Modifier
) {
    if (emotionScores.isEmpty()) return
    
    // Get top 5 emotions
    val topEmotions = emotionScores.toList()
        .sortedByDescending { it.second }
        .take(5)
    
    // Animation for drawing
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = EaseOutQuart),
        label = "radarProgress"
    )
    
    // Pulsing animation for primary emotion
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarPulse"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Emotional Resonance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5D4037)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Radar chart canvas
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 20
                    val numAxes = topEmotions.size
                    val angleStep = (2 * Math.PI / numAxes).toFloat()
                    
                    // Draw concentric circles (grid)
                    for (i in 1..4) {
                        val gridRadius = radius * i / 4
                        drawCircle(
                            color = Color(0xFFE0E0E0),
                            radius = gridRadius,
                            center = center,
                            style = Stroke(width = 1f)
                        )
                    }
                    
                    // Draw axes
                    for (i in topEmotions.indices) {
                        val angle = (i * angleStep) - (Math.PI / 2).toFloat()
                        val endX = center.x + radius * cos(angle)
                        val endY = center.y + radius * sin(angle)
                        
                        drawLine(
                            color = Color(0xFFBDBDBD),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 1f
                        )
                    }
                    
                    // Draw filled polygon for scores
                    val path = Path()
                    val glowPath = Path()
                    
                    topEmotions.forEachIndexed { index, (emotionId, score) ->
                        // Normalize score to 0-1 range
                        val normalizedScore = ((score + 5f) / 20f).coerceIn(0.1f, 1f)
                        val animatedScore = normalizedScore * animatedProgress
                        
                        val angle = (index * angleStep) - (Math.PI / 2).toFloat()
                        val x = center.x + radius * animatedScore * cos(angle)
                        val y = center.y + radius * animatedScore * sin(angle)
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                            glowPath.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            glowPath.lineTo(x, y)
                        }
                    }
                    
                    path.close()
                    glowPath.close()
                    
                    // Draw glow effect
                    val primaryColor = primaryEmotion?.let { 
                        EmotionConfig.getGlowColor(it) 
                    } ?: Color(0xFF7E57C2)
                    
                    drawPath(
                        path = glowPath,
                        color = primaryColor.copy(alpha = pulseAlpha * 0.3f)
                    )
                    
                    // Draw filled area
                    drawPath(
                        path = path,
                        color = primaryColor.copy(alpha = 0.4f)
                    )
                    
                    // Draw outline
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 2.5f)
                    )
                    
                    // Draw dots at each point
                    topEmotions.forEachIndexed { index, (emotionId, score) ->
                        val normalizedScore = ((score + 5f) / 20f).coerceIn(0.1f, 1f)
                        val animatedScore = normalizedScore * animatedProgress
                        
                        val angle = (index * angleStep) - (Math.PI / 2).toFloat()
                        val x = center.x + radius * animatedScore * cos(angle)
                        val y = center.y + radius * animatedScore * sin(angle)
                        
                        val dotColor = if (emotionId == primaryEmotion) {
                            primaryColor
                        } else {
                            Color(0xFF9E9E9E)
                        }
                        
                        // Outer glow for dots
                        drawCircle(
                            color = dotColor.copy(alpha = 0.3f),
                            radius = 8f,
                            center = Offset(x, y)
                        )
                        
                        // Inner dot
                        drawCircle(
                            color = dotColor,
                            radius = 5f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Legend
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topEmotions.forEach { (emotionId, score) ->
                    val emotionData = EmotionConfig.getEmotion(emotionId)
                    val normalizedScore = ((score + 5f) / 20f * 100f).coerceIn(0f, 100f)
                    val isPrimary = emotionId == primaryEmotion
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = emotionData?.emoji ?: "🔮",
                                fontSize = if (isPrimary) 18.sp else 14.sp
                            )
                            Text(
                                text = emotionData?.displayName ?: emotionId,
                                style = if (isPrimary) {
                                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                } else {
                                    MaterialTheme.typography.bodySmall
                                },
                                color = if (isPrimary) Color(0xFF3E2723) else Color(0xFF757575)
                            )
                        }
                        Text(
                            text = "${"%.0f".format(normalizedScore)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPrimary) Color(0xFF5D4037) else Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated breathing circle for meditation/calm effect
 */
@Composable
fun BreathingCircle(
    color: Color = Color(0xFFD4A12A),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathAlpha"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale * 1.2f)
                .alpha(alpha * 0.3f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        
        // Middle ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .alpha(alpha * 0.6f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        
        // Inner circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(scale * 0.8f)
                .background(color.copy(alpha = alpha), CircleShape)
        )
    }
}

/**
 * Shimmer loading effect for cards
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    val shimmerGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE0E0E0),
            Color(0xFFF5F5F5),
            Color(0xFFE0E0E0)
        ),
        start = Offset(shimmerOffset * 500, 0f),
        end = Offset((shimmerOffset + 1) * 500, 0f)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerGradient)
        )
    }
}

/**
 * Elegant quote display with decorative elements
 */
@Composable
fun QuoteDisplay(
    quote: String,
    attribution: String? = null,
    accentColor: Color = Color(0xFFD4A12A),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Opening quote mark
        Text(
            text = "❝",
            fontSize = 32.sp,
            color = accentColor.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = quote,
            style = GitaTextStyles.wisdomNugget,
            textAlign = TextAlign.Center,
            color = Color(0xFF3E2723)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Closing quote mark
        Text(
            text = "❞",
            fontSize = 32.sp,
            color = accentColor.copy(alpha = 0.6f)
        )
        
        if (!attribution.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "— $attribution",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Color(0xFF8D6E63)
            )
        }
    }
}

