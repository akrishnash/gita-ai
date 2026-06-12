package com.gita.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gita.app.data.VerseEntry
import com.gita.app.ui.theme.*
import com.gita.app.viewmodel.StoryCard
import kotlinx.coroutines.delay

/**
 * Response screen — displays the matched Gita verse with therapeutic context.
 * 
 * Design: Clean card-based layout with staggered fade-in animations,
 * Sanskrit verse in serif font, and subtle spiritual accents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponseScreen(
    userQuestion: String,
    verse: VerseEntry,
    reflection: String,
    anchorLine: String,
    story: StoryCard? = null,
    language: String = "en",
    isDarkMode: Boolean = true,
    onLanguageChange: (String) -> Unit = {},
    onDarkModeChange: (Boolean) -> Unit = {},
    onAnotherPerspective: () -> Unit,
    onBack: () -> Unit,
    debugInfo: com.gita.app.kotlinmodel.MatchDebugInfo? = null
) {
    var currentLanguage by remember { mutableStateOf(language) }
    var currentDarkMode by remember { mutableStateOf(isDarkMode) }
    
    // Theme-aware colors
    val bgPrimary = if (currentDarkMode) SurfaceDark else SurfaceLight
    val bgCard = if (currentDarkMode) SurfaceDarkElevated else SurfaceLightElevated
    val textPrimary = if (currentDarkMode) OnSurfaceDark else OnSurfaceLight
    val textSecondary = if (currentDarkMode) Color(0xFF9A9A9A) else Color(0xFF5A5A5A)
    val textMuted = if (currentDarkMode) OnSurfaceDarkMuted else OnSurfaceLightMuted
    val accent = if (currentDarkMode) IndigoLight else IndigoPrimary
    val gold = if (currentDarkMode) SaffronGold else SaffronDeep
    val divider = if (currentDarkMode) OutlineDark else OutlineLight
    val sanskritBg = if (currentDarkMode) Color(0xFF141210) else Color(0xFFF8F5F0)
    
    // Therapeutic response
    val bridge = debugInfo?.bridge
    val therapeuticEn = bridge ?: debugInfo?.emotionComfortingMessage ?: getEmpatheticResponse(debugInfo?.detectedEmotion)
    val therapeuticHi = debugInfo?.hindiResponse ?: getHindiEmpatheticResponse(debugInfo?.detectedEmotion)
    val therapeutic = if (currentLanguage == "hi") therapeuticHi else therapeuticEn
    
    // Staggered animation controls
    var section1Visible by remember { mutableStateOf(false) }
    var section2Visible by remember { mutableStateOf(false) }
    var section3Visible by remember { mutableStateOf(false) }
    var section4Visible by remember { mutableStateOf(false) }
    var section5Visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100); section1Visible = true
        delay(150); section2Visible = true
        delay(150); section3Visible = true
        delay(150); section4Visible = true
        delay(150); section5Visible = true
    }
    
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
                    // Back button
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
                    
                    // Title
                    Text(
                        text = "॥ गीता ॥",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 3.sp,
                            fontSize = 14.sp
                        ),
                        color = gold
                    )
                    
                    // Toggles
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (currentDarkMode) Color(0xFF1A1A1A) else Color(0xFFF0F0EB))
                                .padding(2.dp)
                        ) {
                            listOf("en" to "EN", "hi" to "हि").forEach { (code, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            if (currentLanguage == code) accent.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            currentLanguage = code
                                            onLanguageChange(code)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        label,
                                        fontSize = 11.sp,
                                        fontWeight = if (currentLanguage == code) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (currentLanguage == code) accent else textMuted
                                    )
                                }
                            }
                        }
                        
                        // Theme toggle
                        IconButton(
                            onClick = {
                                currentDarkMode = !currentDarkMode
                                onDarkModeChange(currentDarkMode)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (currentDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ═══════════════════════════════════════════════════════
            // Section 1: User's question (subtle echo)
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = section1Visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
            ) {
                if (userQuestion.isNotBlank()) {
                    Text(
                        text = "\"$userQuestion\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 22.sp
                        ),
                        color = textMuted.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
            
            // ═══════════════════════════════════════════════════════
            // Section 2: Therapeutic response card
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = section2Visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = bgCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = divider,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Emotion emoji if available
                        debugInfo?.emotionEmoji?.let { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 32.sp
                            )
                        }
                        
                        // Therapeutic message
                        Text(
                            text = therapeutic,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp,
                                letterSpacing = 0.3.sp
                            ),
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════
            // Section 3: Verse reference + Sanskrit
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = section3Visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Verse reference
                    Text(
                        text = if (currentLanguage == "hi")
                            "अध्याय ${verse.chapter}, श्लोक ${verse.verse}"
                        else
                            "CHAPTER ${verse.chapter}, VERSE ${verse.verse}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = accent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Sanskrit verse card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = sanskritBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = verse.sanskrit,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 34.sp,
                                    fontSize = 18.sp
                                ),
                                color = gold,
                                textAlign = TextAlign.Center
                            )
                            
                            if (verse.transliteration.isNotBlank()) {
                                Divider(
                                    modifier = Modifier.width(40.dp),
                                    color = divider
                                )
                                Text(
                                    text = verse.transliteration,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 22.sp
                                    ),
                                    color = textMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════
            // Section 4: Translation
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = section4Visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = divider,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == "hi") "अनुवाद" else "TRANSLATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = textMuted
                        )
                        
                        Text(
                            text = if (currentLanguage == "hi")
                                getHindiTranslation(verse)
                            else
                                verse.translation,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp
                            ),
                            color = textPrimary
                        )
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════
            // Section 5: Reflection + Actions
            // ═══════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = section5Visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Reflection
                    if (reflection.isNotBlank()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "· · ·",
                                color = textMuted.copy(alpha = 0.4f),
                                letterSpacing = 8.sp,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (currentLanguage == "hi") "विचार" else "REFLECTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = accent
                            )
                            Text(
                                text = reflection,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 26.sp,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                    
                    // Story card (if available)
                    story?.let { storyCard ->
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
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (currentLanguage == "hi") "कथा" else "STORY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = gold
                                )
                                Text(
                                    text = storyCard.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = textPrimary
                                )
                                Text(
                                    text = storyCard.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 26.sp
                                    ),
                                    color = textSecondary
                                )
                                storyCard.moralLesson?.let { moral ->
                                    Divider(color = divider)
                                    Text(
                                        text = moral,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = textMuted
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onAnotherPerspective,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    listOf(divider, divider)
                                )
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textSecondary
                            )
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (currentLanguage == "hi") "अन्य श्लोक" else "Another Verse",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = if (currentDarkMode) Color(0xFF1A1A1A) else Color.White
                            )
                        ) {
                            Text(
                                if (currentLanguage == "hi") "नया प्रश्न" else "New Question",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Therapeutic response functions
// ═══════════════════════════════════════════════════════════════

private fun getEmpatheticResponse(emotion: String?): String {
    return when (emotion?.lowercase()) {
        "anxiety" -> "I understand how overwhelming anxiety can feel. Take a deep breath with me. You're not alone in this, and these feelings will pass. Here's what Krishna shared about finding inner peace..."
        "grief" -> "I'm truly sorry for what you're going through. Grief is a testament to love, and it's okay to feel this deeply. Krishna spoke of the eternal nature of the soul to comfort those who mourn..."
        "anger" -> "Your anger is valid and understandable. It often comes from a place of being hurt or unheard. Let's see what Krishna said about transforming this powerful emotion..."
        "confusion" -> "It's perfectly okay not to have all the answers right now. Even Arjuna stood confused on the battlefield. Krishna's words brought him clarity..."
        "fear" -> "Fear is natural, and acknowledging it takes courage. You're braver than you know. Here's how Krishna helped Arjuna overcome his deepest fears..."
        "loneliness" -> "Feeling alone can be so heavy. But reaching out shows your strength. Remember, there's a presence that's always with you. Krishna promised..."
        "hopelessness" -> "When hope feels distant, please know that even the longest night ends with dawn. Your presence here is itself a spark of light. Krishna said..."
        "burnout" -> "You've been giving so much of yourself. Rest is not weakness—it's wisdom. Let's explore what Krishna taught about action without exhaustion..."
        "guilt" -> "Carrying guilt shows you have a good heart. But you deserve forgiveness too. Krishna spoke about moving forward, even after mistakes..."
        "attachment" -> "Letting go is one of life's hardest lessons. Your love is beautiful, and learning to hold gently is a journey. Krishna taught..."
        else -> "I'm here with you. Whatever you're feeling right now is valid. Let's explore together what ancient wisdom has to offer..."
    }
}

private fun getHindiEmpatheticResponse(emotion: String?): String {
    return when (emotion?.lowercase()) {
        "anxiety" -> "मैं समझता हूं कि चिंता कितनी भारी लग सकती है। मेरे साथ एक गहरी सांस लीजिए। आप अकेले नहीं हैं। देखिए कृष्ण ने शांति के बारे में क्या कहा..."
        "grief" -> "आपके दुख के लिए मुझे सच में खेद है। शोक प्रेम की निशानी है। कृष्ण ने आत्मा की अमरता के बारे में क्या कहा..."
        "anger" -> "आपका क्रोध स्वाभाविक है। यह अक्सर दर्द से आता है। देखिए कृष्ण ने इस शक्ति को कैसे बदलने की बात कही..."
        "confusion" -> "सभी उत्तर न होना ठीक है। अर्जुन भी भ्रमित थे। कृष्ण के शब्दों ने उन्हें स्पष्टता दी..."
        "fear" -> "डर स्वाभाविक है। इसे स्वीकारना साहस है। देखिए कृष्ण ने अर्जुन को कैसे निडर बनाया..."
        "loneliness" -> "अकेलापन भारी होता है। लेकिन आप वास्तव में अकेले नहीं हैं। कृष्ण ने वादा किया..."
        "hopelessness" -> "जब आशा दूर लगे, याद रखिए अंधेरे के बाद सुबह जरूर आती है। कृष्ण ने कहा..."
        "burnout" -> "आपने बहुत कुछ दिया है। आराम करना कमजोरी नहीं, बुद्धिमानी है। देखिए कृष्ण ने क्या सिखाया..."
        "guilt" -> "अपराधबोध दिखाता है कि आपका दिल अच्छा है। आप भी क्षमा के पात्र हैं। कृष्ण ने कहा..."
        "attachment" -> "छोड़ना जीवन का कठिन पाठ है। आपका प्रेम सुंदर है। कृष्ण ने सिखाया..."
        else -> "मैं आपके साथ हूं। आप जो भी महसूस कर रहे हैं, वह सही है। आइए देखें प्राचीन ज्ञान क्या कहता है..."
    }
}

private fun getHindiTranslation(verse: VerseEntry): String {
    return verse.translation
}
