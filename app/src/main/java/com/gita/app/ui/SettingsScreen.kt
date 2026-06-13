package com.gita.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.app.TimePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gita.app.kotlinmodel.OpenAIUsageTracker
import com.gita.app.ui.theme.*

/**
 * Settings screen — API key management, usage stats, and app info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    aiApiKey: String?,
    usageStats: OpenAIUsageTracker.UsageSummary,
    isDarkMode: Boolean = true,
    isLoggedIn: Boolean = false,
    enableDailyVerse: Boolean = true,
    dailyVerseHour: Int = 8,
    onSaveApiKey: (String?) -> Unit,
    onSignOut: () -> Unit = {},
    onDailyVerseToggle: (Boolean) -> Unit = {},
    onDailyVerseHourChange: (Int) -> Unit = {},
    onBack: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(aiApiKey ?: "") }
    val context = LocalContext.current

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
                        text = "SETTINGS",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ═══════════════════════════════════════════════════════
            // API Key Section
            // ═══════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, divider, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Key,
                            contentDescription = null,
                            tint = gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "OpenAI API Key",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = textPrimary
                        )
                    }
                    
                    Text(
                        text = "The key enables AI-powered verse matching. It's stored only on your device. Without a key, the app uses offline keyword matching.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 20.sp
                        ),
                        color = textMuted
                    )
                    
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("sk-...", color = textMuted.copy(alpha = 0.3f))
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = divider,
                            cursorColor = accent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSaveApiKey(null)
                                apiKeyInput = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(divider, divider)
                                )
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textMuted
                            )
                        ) {
                            Text("Clear", fontSize = 13.sp)
                        }
                        
                        Button(
                            onClick = {
                                onSaveApiKey(apiKeyInput.takeIf { it.isNotBlank() })
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color(0xFF1A1A1A)
                            )
                        ) {
                            Text("Save", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    // Status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (aiApiKey.isNullOrBlank()) Color(0xFF666666) else SageGreen,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Text(
                            text = if (aiApiKey.isNullOrBlank()) "Offline mode (keyword matching)" else "AI mode (OpenAI RAG pipeline)",
                            style = MaterialTheme.typography.labelSmall,
                            color = textMuted
                        )
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════
            // Daily Verse Section
            // ═══════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, divider, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Daily Verse",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = enableDailyVerse,
                            onCheckedChange = onDailyVerseToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = bgPrimary,
                                checkedTrackColor = gold
                            )
                        )
                    }

                    Text(
                        text = "Receive a Gita verse each morning to start the day with wisdom.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = textMuted
                    )

                    // Time picker row — only when enabled
                    if (enableDailyVerse) {
                        Divider(color = divider)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Remind me at",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMuted
                            )
                            val displayHour = when {
                                dailyVerseHour == 0 -> "12:00 AM"
                                dailyVerseHour < 12 -> "${dailyVerseHour}:00 AM"
                                dailyVerseHour == 12 -> "12:00 PM"
                                else -> "${dailyVerseHour - 12}:00 PM"
                            }
                            TextButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, _ -> onDailyVerseHourChange(hour) },
                                        dailyVerseHour,
                                        0,
                                        false
                                    ).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = displayHour,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = accent
                                )
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════
            // Usage Stats Section
            // ═══════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, divider, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Session Usage",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = textPrimary
                        )
                    }
                    
                    if (usageStats.totalRequests == 0) {
                        Text(
                            text = "No API calls made yet in this session.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted
                        )
                    } else {
                        // Stats grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Requests", "${usageStats.totalRequests}", textPrimary, textMuted)
                            StatItem("Tokens", "${usageStats.totalTokens}", textPrimary, textMuted)
                            StatItem("Cost", "${"%.4f".format(usageStats.totalCost)}", accent, textMuted)
                        }
                        
                        // Model breakdown
                        if (usageStats.modelBreakdown.isNotEmpty()) {
                            Divider(color = divider)
                            
                            Text(
                                text = "BY MODEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = textMuted
                            )
                            
                            usageStats.modelBreakdown.forEach { modelInfo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = modelInfo.model,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "${modelInfo.requests} req · ${modelInfo.totalTokens} tok · ${"%.4f".format(modelInfo.cost)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════
            // Sign Out
            // ═══════════════════════════════════════════════════════
            if (isLoggedIn) {
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ═══════════════════════════════════════════════════════
            // About Section
            // ═══════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Gita AI",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.sp
                    ),
                    color = textMuted.copy(alpha = 0.5f)
                )
                Text(
                    text = "Ancient wisdom, modern guidance",
                    style = MaterialTheme.typography.labelSmall,
                    color = textMuted.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color,
    labelColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
    }
}
