package com.gita.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.gita.app.data.VerseEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponseScreen(
    verse: VerseEntry,
    reflection: String,
    anchorLine: String,
    onAnotherPerspective: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Response") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Home")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                OutlinedButton(
                    onClick = onAnotherPerspective,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Another way to see this")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Verse reference
            Text(
                text = "Chapter ${verse.chapter}, Verse ${verse.verse}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Verse card: Sanskrit, Transliteration, Translation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Sanskrit
                    Text(
                        text = verse.sanskrit,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    
                    // Transliteration
                    Text(
                        text = verse.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    
                    // English translation
                    Text(
                        text = verse.translation,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Context paragraph
            Text(
                text = "Context",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = verse.context,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Reflection paragraph
            Text(
                text = "Reflection",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = reflection,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Anchor line (highlighted subtly)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f) // Subtle highlight
                )
            ) {
                Text(
                    text = anchorLine,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
        }
    }
}
