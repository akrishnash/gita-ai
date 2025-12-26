package com.gita.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.gita.app.kotlinmodel.OpenAIUsageTracker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    aiApiKey: String?,
    usageStats: OpenAIUsageTracker.UsageSummary,
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
                text = "OpenAI API Key",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "This app uses OpenAI embeddings to match your query to a verse and a related story. The key is stored only on your device.",
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
                text = "If you clear the key, the app falls back to the offline deterministic mode.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Token Usage Section
            Text(
                text = "API Usage",
                style = MaterialTheme.typography.titleMedium
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (usageStats.totalRequests == 0) {
                        Text(
                            text = "No API calls made yet in this session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Total Summary
                        Text(
                            text = "Session Total",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Requests",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${usageStats.totalRequests}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column {
                                Text(
                                    text = "Total Tokens",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${usageStats.totalTokens}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column {
                                Text(
                                    text = "Cost",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${"%.6f".format(usageStats.totalCost)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Model Breakdown
                        if (usageStats.modelBreakdown.isNotEmpty()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Text(
                                text = "By Model",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            usageStats.modelBreakdown.forEach { modelInfo ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = modelInfo.model,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${modelInfo.requests} req • ${modelInfo.totalTokens} tokens",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$${"%.6f".format(modelInfo.cost)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


