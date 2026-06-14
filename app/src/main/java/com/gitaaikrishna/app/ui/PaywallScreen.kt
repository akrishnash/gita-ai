package com.gitaaikrishna.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaaikrishna.app.logic.SubscriptionTier
import com.gitaaikrishna.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    language: String = "en",
    isDarkMode: Boolean = true,
    onSubscribe: (SubscriptionTier) -> Unit,
    onRestorePurchase: () -> Unit,
    onBack: () -> Unit
) {
    val bgPrimary = if (isDarkMode) SurfaceDark else SurfaceLight
    val bgCard = if (isDarkMode) SurfaceDarkElevated else SurfaceLightElevated
    val textPrimary = if (isDarkMode) OnSurfaceDark else OnSurfaceLight
    val textMuted = if (isDarkMode) OnSurfaceDarkMuted else OnSurfaceLightMuted
    val accent = if (isDarkMode) IndigoLight else IndigoPrimary
    val gold = if (isDarkMode) SaffronGold else SaffronDeep
    val divider = if (isDarkMode) OutlineDark else OutlineLight

    val infiniteTransition = rememberInfiniteTransition(label = "paywallOm")
    val omAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "omBreath"
    )

    Scaffold(
        containerColor = bgPrimary,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgPrimary,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (language == "hi") "उन्नति" else "Upgrade",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 3.sp,
                            fontSize = 14.sp
                        ),
                        color = gold
                    )
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ॐ glow
            Text(
                text = "ॐ",
                fontSize = 64.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.Serif,
                color = gold.copy(alpha = omAlpha)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (language == "hi") "असीमित ज्ञान अनलॉक करें" else "Unlock unlimited wisdom",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.5.sp,
                    fontSize = 20.sp
                ),
                color = textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (language == "hi") "मुफ़्त में 3 प्रश्न/दिन, और अधिक के लिए उन्नति करें"
                       else "Free gives you 3 queries/day — upgrade for more",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Two plan cards ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SADHAK — outlined
                PlanCard(
                    modifier = Modifier.weight(1f),
                    titleHindi = "साधक",
                    titleEn = "Seeker",
                    price = "₹99/माह",
                    features = listOf("15 प्रश्न/दिन", "श्लोक इतिहास", "बुकमार्क"),
                    featuresEn = listOf("15 queries/day", "Verse history", "Bookmarks"),
                    language = language,
                    isBestValue = false,
                    bgCard = bgCard,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    gold = gold,
                    accent = accent,
                    divider = divider,
                    isDarkMode = isDarkMode,
                    onClick = { onSubscribe(SubscriptionTier.SADHAK) }
                )

                // GYANI — filled + badge
                PlanCard(
                    modifier = Modifier.weight(1f),
                    titleHindi = "ज्ञानी",
                    titleEn = "Wise One",
                    price = "₹249/माह",
                    features = listOf("असीमित प्रश्न", "साधक सुविधाएं", "ऑडियो (जल्द)"),
                    featuresEn = listOf("Unlimited queries", "All Sadhak features", "Audio (soon)"),
                    language = language,
                    isBestValue = true,
                    bgCard = bgCard,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    gold = gold,
                    accent = accent,
                    divider = divider,
                    isDarkMode = isDarkMode,
                    onClick = { onSubscribe(SubscriptionTier.GYANI) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with lotus
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Divider(modifier = Modifier.weight(1f), color = divider)
                Text("✿", fontSize = 14.sp, color = gold.copy(alpha = 0.3f))
                Divider(modifier = Modifier.weight(1f), color = divider)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onRestorePurchase) {
                Text(
                    text = if (language == "hi") "खरीद पुनः प्राप्त करें" else "Restore Purchase",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted.copy(alpha = 0.6f)
                )
            }

            TextButton(onClick = onBack) {
                Text(
                    text = if (language == "hi") "मुफ़्त जारी रखें (3/दिन)" else "Continue with Free (3/day)",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted.copy(alpha = 0.45f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanCard(
    modifier: Modifier = Modifier,
    titleHindi: String,
    titleEn: String,
    price: String,
    features: List<String>,
    featuresEn: List<String>,
    language: String,
    isBestValue: Boolean,
    bgCard: Color,
    textPrimary: Color,
    textMuted: Color,
    gold: Color,
    accent: Color,
    divider: Color,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isBestValue) {
        if (isDarkMode) Color(0xFF12122A) else Color(0xFFEEEEFF)
    } else {
        bgCard
    }
    val borderColor = if (isBestValue) accent.copy(alpha = 0.5f) else divider

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(width = if (isBestValue) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = titleHindi,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                color = gold
            )
            Text(
                text = titleEn,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp
                ),
                color = textMuted
            )

            Divider(color = if (isBestValue) accent.copy(alpha = 0.2f) else divider)

            // Features
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val displayFeatures = if (language == "hi") features else featuresEn
                displayFeatures.forEach { feature ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 11.sp,
                            color = if (isBestValue) accent else gold.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp),
                            color = textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Price
            Text(
                text = price,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = textPrimary
            )

            // Subscribe button
            val btnGradient = if (isBestValue)
                Brush.linearGradient(
                    if (isDarkMode) GitaColors.buttonGradientDark else GitaColors.buttonGradientLight
                )
            else
                Brush.linearGradient(listOf(divider, divider))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = if (isBestValue) {
                        if (isDarkMode) Color(0xFF1A1A1A) else Color.White
                    } else textMuted
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(btnGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == "hi") "चुनें" else "Choose",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBestValue) {
                            if (isDarkMode) Color(0xFF1A1A1A) else Color.White
                        } else textMuted
                    )
                }
            }
        }

        // "BEST VALUE" badge — top-right on GYANI card
        if (isBestValue) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-10).dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(accent)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (language == "hi") "सर्वोत्तम" else "BEST VALUE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color(0xFF1A1A1A) else Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
