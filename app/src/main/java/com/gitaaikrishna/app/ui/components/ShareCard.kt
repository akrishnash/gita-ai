package com.gitaaikrishna.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaaikrishna.app.data.VerseEntry
import com.gitaaikrishna.app.ui.theme.IndigoLight
import com.gitaaikrishna.app.ui.theme.SaffronGold

/**
 * Square share card rendered at 360dp × 360dp (≈ 1080px at 3x density).
 * Use ShareManager to capture this as a Bitmap and share it.
 */
@Composable
fun ShareCard(verse: VerseEntry) {
    val bgColor = Color(0xFF0A0A0A)
    val gold = SaffronGold
    val indigo = IndigoLight
    val textWhite = Color(0xFFF0F0F0)
    val textMuted = Color(0xFF666666)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        // Subtle radial glow behind Om
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopCenter)
                .offset(y = 48.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            indigo.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top: ॐ ────────────────────────────────────────────
            Text(
                text = "ॐ",
                fontSize = 48.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Thin,
                color = gold
            )

            // ── Middle: Sanskrit + divider + translation ──────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = verse.sanskrit,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 17.sp,
                        lineHeight = 28.sp
                    ),
                    color = gold,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                // Thin gold divider
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(0.5.dp)
                        .background(gold.copy(alpha = 0.45f))
                )

                Text(
                    text = verse.translation,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    ),
                    color = textWhite,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Bottom: pill + watermark ──────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(gold.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Chapter ${verse.chapter}  ·  Verse ${verse.verse}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = textMuted
                    )
                }

                Text(
                    text = "गीता AI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp
                    ),
                    color = gold.copy(alpha = 0.35f)
                )
            }
        }
    }
}
