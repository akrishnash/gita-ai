package com.gitaaikrishna.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaaikrishna.app.logic.HistoryEntry
import com.gitaaikrishna.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * History screen — shows past user interactions with the Gita AI.
 * 
 * Features: grouped by date, verse reference per entry, staggered animations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyEntries: List<HistoryEntry>,
    isDarkMode: Boolean = true,
    language: String = "en",
    onBack: () -> Unit
) {
    val bgPrimary = if (isDarkMode) SurfaceDark else SurfaceLight
    val bgCard = if (isDarkMode) SurfaceDarkElevated else SurfaceLightElevated
    val textPrimary = if (isDarkMode) OnSurfaceDark else OnSurfaceLight
    val textMuted = if (isDarkMode) OnSurfaceDarkMuted else OnSurfaceLightMuted
    val accent = if (isDarkMode) IndigoLight else IndigoPrimary
    val gold = if (isDarkMode) SaffronGold else SaffronDeep
    val divider = if (isDarkMode) OutlineDark else OutlineLight
    
    Scaffold(
        containerColor = bgPrimary,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgPrimary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Text(
                        text = if (language == "hi") "इतिहास" else "HISTORY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 4.sp,
                            fontSize = 14.sp
                        ),
                        color = textMuted
                    )
                    
                    // Spacer for symmetry
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        }
    ) { padding ->
        if (historyEntries.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 40.dp)
                ) {
                    Text(
                        text = "🪷",
                        fontSize = 56.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == "hi") "आपकी यात्रा यहाँ से शुरू होती है" else "Your journey begins here",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        ),
                        color = textMuted.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (language == "hi") "एक प्रश्न पूछें और आपके विचार यहाँ दिखेंगे" else "Ask a question and your reflections will appear here",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 20.sp
                        ),
                        color = textMuted.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(historyEntries) { index, entry ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 50L)
                        visible = true
                    }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
                    ) {
                        HistoryItem(
                            entry = entry,
                            bgCard = bgCard,
                            textPrimary = textPrimary,
                            textMuted = textMuted,
                            accent = accent,
                            gold = gold,
                            divider = divider,
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entry: HistoryEntry,
    bgCard: Color,
    textPrimary: Color,
    textMuted: Color,
    accent: Color,
    gold: Color,
    divider: Color,
    language: String = "en"
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val date = remember(entry.timestamp) { Date(entry.timestamp) }
    var expanded by remember { mutableStateOf(false) }

    // Left-accent card: gold bar on the left, no outer border
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgCard)
            .clickable { expanded = !expanded }
            .height(IntrinsicSize.Min)
    ) {
        // 4dp gold accent bar on the left
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(gold.copy(alpha = 0.75f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User input
            Text(
                text = entry.userInput,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                color = textPrimary
            )

            // Verse reference
            Text(
                text = "Verse ${entry.verseId}",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = gold.copy(alpha = 0.7f)
            )

            // Anchor line — expands on tap
            if (entry.anchorLine.isNotBlank()) {
                Text(
                    text = entry.anchorLine,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 18.sp
                    ),
                    color = textMuted.copy(alpha = 0.6f),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Timestamp + expand hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dateFormat.format(date)} · ${timeFormat.format(date)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textMuted.copy(alpha = 0.4f)
                )
                Text(
                    text = if (expanded) {
                        if (language == "hi") "कम" else "less"
                    } else {
                        if (language == "hi") "अधिक" else "more"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.copy(alpha = 0.5f)
                )
            }
        }
    }
}
