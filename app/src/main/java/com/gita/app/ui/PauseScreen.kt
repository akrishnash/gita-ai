package com.gita.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PauseScreen(
    userInput: String,
    onProcessProblem: () -> Unit,
    onBack: () -> Unit
) {
    // Trigger processing after delay
    LaunchedEffect(userInput) {
        delay(1800) // ~1500-2000ms delay
        onProcessProblem()
    }
    
    // Peacock-themed gradient background (same as HomeScreen)
    val peacockGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9), // Light green
            Color(0xFFF1F8E9), // Very light green
            Color(0xFFE3F2FD), // Light blue
            Color(0xFFF5F5F5)  // Soft white
        )
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pause") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F5E9).copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(peacockGradient)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Let's sit with this for a moment.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF424242),
                    lineHeight = 28.sp
                )
            }
        }
    }
}
