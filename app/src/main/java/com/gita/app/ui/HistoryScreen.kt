package com.gita.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gita.app.logic.HistoryEntry
import com.gita.app.ui.theme.*
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
    onBack: () -> Unit
) {
    val bgPrimary = SurfaceDark
    val bgCard = SurfaceDarkElevated
    val textPrimary = OnSurfaceDark
    val textMuted = OnSurfaceDarkMuted
    val accent = IndigoLight
    val gold = SaffronGold
    val divider = OutlineDark
    
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
                        text = "HISTORY",
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .alpha(0.3f),
                        tint = textMuted
                    )
                    Text(
                        text = "No conversations yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMuted.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Your spiritual journey begins\nwith a single question",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 20.sp
                        ),
                        color = textMuted.copy(alpha = 0.3f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                            divider = divider
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
    divider: Color
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val date = remember(entry.timestamp) { Date(entry.timestamp) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, divider, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User input
            Text(
                text = entry.userInput,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = textPrimary
            )
            
            // Verse reference
            Text(
                text = "Verse ${entry.verseId}",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp
                ),
                color = gold.copy(alpha = 0.7f)
            )
            
            // Anchor line
            if (entry.anchorLine.isNotBlank()) {
                Text(
                    text = entry.anchorLine,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 18.sp
                    ),
                    color = textMuted.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Timestamp
            Text(
                text = "${dateFormat.format(date)} · ${timeFormat.format(date)}",
                style = MaterialTheme.typography.labelSmall,
                color = textMuted.copy(alpha = 0.4f)
            )
        }
    }
}
