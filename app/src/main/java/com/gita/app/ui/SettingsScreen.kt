package com.gita.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    aiApiKey: String?,
    onSaveApiKey: (String?) -> Unit,
    onBack: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(aiApiKey ?: "") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "AI API Key (Optional)",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Enter an API key to enable AI-enhanced reflections. Leave empty to use offline mode only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                placeholder = { Text("sk-...") },
                singleLine = true
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                
                Button(
                    onClick = {
                        onSaveApiKey(apiKeyInput.takeIf { it.isNotBlank() })
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text(
                text = "Note: AI features are not yet implemented. This setting prepares the app for future AI integration.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

