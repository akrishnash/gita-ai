package com.gitaaikrishna.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.app.TimePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitaaikrishna.app.logic.SubscriptionTier
import com.gitaaikrishna.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Settings screen — subscription, daily verse, and account.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean = true,
    language: String = "en",
    isLoggedIn: Boolean = false,
    subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    subscriptionExpiry: Long = 0L,
    enableDailyVerse: Boolean = true,
    dailyVerseHour: Int = 8,
    onSignOut: () -> Unit = {},
    onDailyVerseToggle: (Boolean) -> Unit = {},
    onDailyVerseHourChange: (Int) -> Unit = {},
    onUpgrade: () -> Unit = {},
    onBack: () -> Unit
) {
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
                        text = if (language == "hi") "सेटिंग्स" else "SETTINGS",
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
            // Subscription Section
            // ═══════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, divider, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (subscriptionTier == SubscriptionTier.FREE) {
                            Text(
                                text = if (language == "hi") "मुफ़्त योजना · 3 प्रश्न/दिन" else "Free Plan · 3 queries/day",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = textPrimary
                            )
                            Text(
                                text = if (language == "hi") "उन्नति करें और अधिक पाएं" else "Upgrade for more daily queries",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMuted
                            )
                        } else {
                            val tierLabel = when (subscriptionTier) {
                                SubscriptionTier.SADHAK -> "साधक"
                                SubscriptionTier.GYANI -> "ज्ञानी"
                                else -> ""
                            }
                            val expiryStr = if (subscriptionExpiry > 0L) {
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(subscriptionExpiry))
                            } else ""
                            Text(
                                text = "$tierLabel Plan",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = textPrimary
                            )
                            if (expiryStr.isNotBlank()) {
                                Text(
                                    text = if (language == "hi") "सक्रिय · $expiryStr तक" else "Active until $expiryStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SageGreen
                                )
                            }
                        }
                    }
                    if (subscriptionTier == SubscriptionTier.FREE) {
                        TextButton(
                            onClick = onUpgrade,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == "hi") "उन्नति →" else "Upgrade →",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = gold
                            )
                        }
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
                            text = if (language == "hi") "दैनिक श्लोक" else "Daily Verse",
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
                        text = if (language == "hi") "हर सुबह एक गीता श्लोक पाएं और दिन की शुरुआत ज्ञान से करें।"
                               else "Receive a Gita verse each morning to start the day with wisdom.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = textMuted
                    )

                    if (enableDailyVerse) {
                        Divider(color = divider)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == "hi") "मुझे याद दिलाएं" else "Remind me at",
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
            // Sign Out
            // ═══════════════════════════════════════════════════════
            if (isLoggedIn) {
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (language == "hi") "साइन आउट" else "Sign Out",
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
