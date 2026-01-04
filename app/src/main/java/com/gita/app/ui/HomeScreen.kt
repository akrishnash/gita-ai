package com.gita.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    isDarkMode: Boolean = true,
    language: String = "en",
    onLanguageChange: (String) -> Unit = {},
    onDarkModeChange: (Boolean) -> Unit = {},
    onSubmit: (String) -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var currentDarkMode by remember { mutableStateOf(isDarkMode) }
    var currentLanguage by remember { mutableStateOf(language) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Minimal spiritual palette
    val bgPrimary = if (currentDarkMode) Color(0xFF0A0A0A) else Color(0xFFFCFCFA)
    val cardBg = if (currentDarkMode) Color(0xFF141414) else Color(0xFFFFFFFF)
    val textPrimary = if (currentDarkMode) Color(0xFFF0F0F0) else Color(0xFF1A1A1A)
    val textSecondary = if (currentDarkMode) Color(0xFF9A9A9A) else Color(0xFF5A5A5A)
    val textMuted = if (currentDarkMode) Color(0xFF5A5A5A) else Color(0xFFAAAAAA)
    val accent = if (currentDarkMode) Color(0xFFD4AF37) else Color(0xFF8B7355)
    val inputBg = if (currentDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F5F0)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Toggle
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
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Dark/Light toggle
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
            
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Om symbol - large, elegant
            Text(
                text = "ॐ",
                fontSize = 72.sp,
                color = accent.copy(alpha = 0.3f),
                fontFamily = FontFamily.Serif
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = if (currentLanguage == "hi") "गीता" else "GITA",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = 8.sp
                ),
                color = textPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = if (currentLanguage == "hi") 
                    "आपके जीवन के प्रश्नों का उत्तर" 
                else 
                    "Wisdom for life's questions",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 1.sp
                ),
                color = textMuted
            )
            
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (currentLanguage == "hi") 
                            "अपने मन की बात कहें..." 
                        else 
                            "What's on your mind?",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 0.5.sp
                        ),
                        color = textMuted
                    )
                    
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                if (currentLanguage == "hi")
                                    "मैं उदास महसूस कर रहा हूं..."
                                else
                                    "I'm feeling anxious about...",
                                color = textMuted.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = accent.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            cursorColor = accent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (userInput.isNotBlank()) {
                                    keyboardController?.hide()
                                    onSubmit(userInput)
                                }
                            }
                        ),
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    Button(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                keyboardController?.hide()
                                onSubmit(userInput)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White,
                            disabledContainerColor = accent.copy(alpha = 0.3f)
                        ),
                        enabled = userInput.isNotBlank()
                    ) {
                        Text(
                            if (currentLanguage == "hi") "मार्गदर्शन पाएं" else "Seek Guidance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(0.4f))
            
            // Subtle footer quote
            Text(
                text = if (currentLanguage == "hi")
                    "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन"
                else
                    "You have the right to work, but never to the fruit of work",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                ),
                color = textMuted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
