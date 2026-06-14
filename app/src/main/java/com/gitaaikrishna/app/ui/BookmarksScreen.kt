package com.gitaaikrishna.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaaikrishna.app.data.BookmarkedVerse
import com.gitaaikrishna.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    bookmarks: List<BookmarkedVerse>,
    isDarkMode: Boolean = true,
    language: String = "en",
    onRemoveBookmark: (String) -> Unit,
    onBack: () -> Unit
) {
    val bgPrimary = if (isDarkMode) SurfaceDark else SurfaceLight
    val bgCard   = if (isDarkMode) SurfaceDarkElevated else SurfaceLightElevated
    val textPrimary = if (isDarkMode) OnSurfaceDark else OnSurfaceLight
    val textMuted   = if (isDarkMode) OnSurfaceDarkMuted else OnSurfaceLightMuted
    val accent = if (isDarkMode) IndigoLight else IndigoPrimary
    val gold   = if (isDarkMode) SaffronGold else SaffronDeep

    Scaffold(
        containerColor = bgPrimary,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = bgPrimary) {
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
                        text = if (language == "hi") "बुकमार्क" else "BOOKMARKS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 4.sp,
                            fontSize = 14.sp
                        ),
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 40.dp)
                ) {
                    Text("🔖", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == "hi") "अभी कोई सहेजा नहीं" else "No saved verses yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        ),
                        color = textMuted.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (language == "hi") "जो श्लोक आपको पसंद आएं उन्हें सहेजें" else "Bookmark a verse on the response screen to save it here",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = textMuted.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(bookmarks) { index, bm ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 50L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
                    ) {
                        BookmarkItem(
                            bm = bm,
                            bgCard = bgCard,
                            textPrimary = textPrimary,
                            textMuted = textMuted,
                            accent = accent,
                            gold = gold,
                            onRemove = { onRemoveBookmark(bm.verseId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkItem(
    bm: BookmarkedVerse,
    bgCard: Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color,
    accent: androidx.compose.ui.graphics.Color,
    gold: androidx.compose.ui.graphics.Color,
    onRemove: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd · HH:mm", Locale.getDefault()) }
    val date = remember(bm.savedAt) { Date(bm.savedAt) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgCard)
            .height(IntrinsicSize.Min)
    ) {
        // Gold accent bar on left
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(gold.copy(alpha = 0.75f))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chapter/Verse pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(gold.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = bm.chapterVerse,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = gold.copy(alpha = 0.8f)
                )
            }

            // Sanskrit — 2 lines, italic
            Text(
                text = bm.sanskrit,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 22.sp
                ),
                color = gold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Translation — 3 lines
            Text(
                text = bm.translation,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = textPrimary.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // User problem that led here
            if (bm.userProblem.isNotBlank()) {
                Text(
                    text = "\"${bm.userProblem}\"",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = textMuted.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Timestamp
            Text(
                text = dateFormat.format(date),
                style = MaterialTheme.typography.labelSmall,
                color = textMuted.copy(alpha = 0.4f)
            )
        }

        // Delete button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.Top)
                .padding(top = 6.dp, end = 4.dp)
                .size(36.dp)
        ) {
            Icon(
                Icons.Outlined.DeleteOutline,
                contentDescription = "Remove bookmark",
                tint = textMuted.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
