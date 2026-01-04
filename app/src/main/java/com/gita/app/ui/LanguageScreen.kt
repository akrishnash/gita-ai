package com.gita.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppLanguage(val displayName: String, val nativeName: String, val code: String) {
    ENGLISH("English", "English", "en"),
    HINDI("Hindi", "हिंदी", "hi"),
    BOTH("Both", "दोनों", "both")
}

@Composable
fun LanguageScreen(
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val bgPrimary = if (isDarkTheme) Color(0xFF0D0D0D) else Color(0xFFFAFAF8)
    val bgSecondary = if (isDarkTheme) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkTheme) Color(0xFFF5F5F5) else Color(0xFF1A1A1A)
    val textSecondary = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF4A4A4A)
    val textMuted = if (isDarkTheme) Color(0xFF707070) else Color(0xFF8A8A8A)
    val accent = if (isDarkTheme) Color(0xFFC4A77D) else Color(0xFF8B7355)
    val border = if (isDarkTheme) Color(0xFF333333) else Color(0xFFE8E8E0)
    
    var selectedLanguage by remember { mutableStateOf<AppLanguage?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Choose Language",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = textPrimary
            )
            
            Text(
                text = "भाषा चुनें",
                style = MaterialTheme.typography.titleMedium,
                color = textMuted
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Language options
            AppLanguage.values().forEach { language ->
                val isSelected = selectedLanguage == language
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedLanguage = language }
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, accent, RoundedCornerShape(16.dp))
                            } else {
                                Modifier.border(1.dp, border, RoundedCornerShape(16.dp))
                            }
                        ),
                    color = if (isSelected) accent.copy(alpha = 0.1f) else bgSecondary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) accent else textPrimary
                            )
                            Text(
                                text = language.nativeName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textMuted
                            )
                        }
                        
                        if (isSelected) {
                            Surface(
                                color = accent,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Continue button
            Button(
                onClick = {
                    selectedLanguage?.let { onLanguageSelected(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    disabledContainerColor = accent.copy(alpha = 0.3f)
                ),
                enabled = selectedLanguage != null
            ) {
                Text(
                    text = "Continue | जारी रखें",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "You can change this later in Settings",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted,
                textAlign = TextAlign.Center
            )
            Text(
                text = "आप इसे बाद में सेटिंग्स में बदल सकते हैं",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

