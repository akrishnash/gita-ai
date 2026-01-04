package com.gita.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import com.gita.app.viewmodel.StoryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponseScreen(
    userQuestion: String,
    verse: VerseEntry,
    reflection: String,
    anchorLine: String,
    story: StoryCard? = null,
    language: String = "en", // "en" or "hi"
    isDarkMode: Boolean = true,
    onLanguageChange: (String) -> Unit = {},
    onDarkModeChange: (Boolean) -> Unit = {},
    onAnotherPerspective: () -> Unit,
    onBack: () -> Unit,
    debugInfo: com.gita.app.kotlinmodel.MatchDebugInfo? = null
) {
    // Local state for toggles
    var currentLanguage by remember { mutableStateOf(language) }
    var currentDarkMode by remember { mutableStateOf(isDarkMode) }
    
    // Minimal spiritual color palette
    val bgPrimary = if (currentDarkMode) Color(0xFF0A0A0A) else Color(0xFFFCFCFA)
    val bgCard = if (currentDarkMode) Color(0xFF141414) else Color(0xFFFFFFFF)
    val textPrimary = if (currentDarkMode) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
    val textSecondary = if (currentDarkMode) Color(0xFF9A9A9A) else Color(0xFF5A5A5A)
    val textMuted = if (currentDarkMode) Color(0xFF5A5A5A) else Color(0xFFAAAAAA)
    val accent = if (currentDarkMode) Color(0xFFD4AF37) else Color(0xFF8B7355)  // Gold/Earth
    val divider = if (currentDarkMode) Color(0xFF2A2A2A) else Color(0xFFE5E5E0)
    
    // Get therapeutic response - prefer LLM bridge if available
    val bridge = debugInfo?.bridge
    val therapeuticEn = bridge ?: debugInfo?.emotionComfortingMessage ?: getEmpatheticResponse(debugInfo?.detectedEmotion)
    val therapeuticHi = debugInfo?.hindiResponse ?: getHindiEmpatheticResponse(debugInfo?.detectedEmotion)
    val therapeutic = if (currentLanguage == "hi") therapeuticHi else therapeuticEn
    
    Scaffold(
        containerColor = bgPrimary,
        topBar = {
            // Minimal Top Bar with toggles
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgCard,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    // Title
                    Text(
                        text = "॥ गीता ॥",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 2.sp
                        ),
                        color = accent
                    )
                    
                    // Toggles Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Toggle: EN / HI
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (currentDarkMode) Color(0xFF1A1A1A) else Color(0xFFF0F0EB))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (currentLanguage == "en") accent.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { 
                                        currentLanguage = "en"
                                        onLanguageChange("en")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "EN",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentLanguage == "en") FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (currentLanguage == "en") accent else textMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (currentLanguage == "hi") accent.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { 
                                        currentLanguage = "hi"
                                        onLanguageChange("hi")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "हि",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentLanguage == "hi") FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (currentLanguage == "hi") accent else textMuted
                                )
                            }
                        }
                        
                        // Dark/Light mode toggle
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (currentDarkMode) Color(0xFF1A1A1A) else Color(0xFFF0F0EB))
                                .clickable { 
                                    currentDarkMode = !currentDarkMode
                                    onDarkModeChange(currentDarkMode)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentDarkMode) "☀" else "🌙",
                                fontSize = 14.sp
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // User's Question - subtle
            if (userQuestion.isNotBlank()) {
                Text(
                    text = "\"$userQuestion\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        lineHeight = 22.sp
                    ),
                    color = textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Therapeutic Response - the heart of the app
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Om symbol as accent
                    Text(
                        text = "ॐ",
                        fontSize = 28.sp,
                        color = accent.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
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
            
            // Verse Reference - minimal
            Text(
                text = if (currentLanguage == "hi") 
                    "अध्याय ${verse.chapter}, श्लोक ${verse.verse}"
                else 
                    "Chapter ${verse.chapter}, Verse ${verse.verse}",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.sp
                ),
                color = accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Sanskrit verse - elegant
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentDarkMode) Color(0xFF1A1612) else Color(0xFFFAF8F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = verse.sanskrit,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 32.sp
                        ),
                        color = accent,
                        textAlign = TextAlign.Center
                    )
                    
                    // Transliteration
                    if (verse.transliteration?.isNotBlank() == true) {
                        Divider(
                            modifier = Modifier
                                .width(60.dp)
                                .padding(vertical = 8.dp),
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
            
            // Translation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bgCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (currentLanguage == "hi") "अनुवाद" else "Translation",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp
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
            
            // Wisdom / Reflection
            if (reflection.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "· · ·",
                        color = textMuted,
                        letterSpacing = 8.sp
                    )
                    Text(
                        text = if (currentLanguage == "hi") "विचार" else "Reflection",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp
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
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom Actions - minimal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Another verse button
                OutlinedButton(
                    onClick = onAnotherPerspective,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(accent.copy(alpha = 0.5f), accent.copy(alpha = 0.3f)))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textSecondary
                    )
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (currentLanguage == "hi") "अन्य श्लोक" else "Another Verse",
                        fontSize = 14.sp
                    )
                }
                
                // New question button
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (currentLanguage == "hi") "नया प्रश्न" else "New Question",
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Get therapeutic response based on detected emotion
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
    // For now, return English translation with a note
    // In production, you'd have actual Hindi translations in your data
    return verse.translation
}
