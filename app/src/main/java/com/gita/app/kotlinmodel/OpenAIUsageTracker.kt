package com.gita.app.kotlinmodel

import android.util.Log

/**
 * Tracks OpenAI API usage (tokens and costs) across the app session.
 * Provides cumulative statistics.
 */
object OpenAIUsageTracker {
    private const val TAG = "OpenAIUsageTracker"
    
    // Pricing per 1M tokens (as of 2024)
    private fun getCostPerMillionTokens(modelName: String): Double {
        return when {
            modelName.contains("embedding-3-small", ignoreCase = true) -> 0.02
            modelName.contains("embedding-3-large", ignoreCase = true) -> 0.13
            modelName.contains("ada-002", ignoreCase = true) -> 0.10
            modelName.contains("gpt-5-nano", ignoreCase = true) -> {
                0.10 / 1_000_000.0 // $0.10 per 1M input tokens, $0.40 per 1M output tokens (estimated pricing)
            }
            modelName.contains("gpt-4o-mini", ignoreCase = true) -> {
                0.15 / 1_000_000.0 // $0.15 per 1M input tokens, $0.60 per 1M output tokens
            }
            modelName.contains("gpt-4", ignoreCase = true) -> {
                // GPT-4 pricing (input/output)
                if (modelName.contains("turbo", ignoreCase = true)) {
                    10.0 / 1_000_000.0 // $10 per 1M input tokens, $30 per 1M output tokens
                } else {
                    30.0 / 1_000_000.0 // $30 per 1M input tokens, $60 per 1M output tokens
                }
            }
            modelName.contains("gpt-3.5", ignoreCase = true) -> {
                if (modelName.contains("turbo", ignoreCase = true)) {
                    0.5 / 1_000_000.0 // $0.50 per 1M input tokens, $1.50 per 1M output tokens
                } else {
                    1.5 / 1_000_000.0
                }
            }
            else -> 0.02 // Default to small embedding model pricing
        }
    }
    
    private data class ModelUsage(
        var promptTokens: Int = 0,
        var completionTokens: Int = 0,
        var totalTokens: Int = 0,
        var cost: Double = 0.0,
        var requestCount: Int = 0
    )
    
    private val usageByModel = mutableMapOf<String, ModelUsage>()
    
    /**
     * Records API usage for a specific model.
     */
    fun recordUsage(
        model: String,
        promptTokens: Int,
        completionTokens: Int = 0,
        totalTokens: Int? = null
    ) {
        val usage = usageByModel.getOrPut(model) { ModelUsage() }
        usage.promptTokens += promptTokens
        usage.completionTokens += completionTokens
        usage.totalTokens += (totalTokens ?: (promptTokens + completionTokens))
        usage.requestCount++
        
        // Calculate cost
        val costPerMillion = getCostPerMillionTokens(model)
        val inputCost = (promptTokens / 1_000_000.0) * costPerMillion
        
        // For chat models, output tokens cost more
        val outputCost = if (model.contains("gpt", ignoreCase = true)) {
            when {
                model.contains("gpt-5-nano", ignoreCase = true) -> {
                    (completionTokens / 1_000_000.0) * 0.40 // $0.40 per 1M output tokens (estimated)
                }
                model.contains("gpt-4o-mini", ignoreCase = true) -> {
                    (completionTokens / 1_000_000.0) * 0.60 // $0.60 per 1M output tokens
                }
                model.contains("gpt-4-turbo", ignoreCase = true) -> {
                    (completionTokens / 1_000_000.0) * 30.0 // $30 per 1M output tokens
                }
                model.contains("gpt-4", ignoreCase = true) -> {
                    (completionTokens / 1_000_000.0) * 60.0 // $60 per 1M output tokens
                }
                model.contains("gpt-3.5-turbo", ignoreCase = true) -> {
                    (completionTokens / 1_000_000.0) * 1.5 // $1.50 per 1M output tokens
                }
                else -> 0.0
            }
        } else {
            0.0 // Embeddings don't have separate output costs
        }
        
        usage.cost += (inputCost + outputCost)
    }
    
    /**
     * Logs current session statistics.
     */
    fun logSessionStats() {
        if (usageByModel.isEmpty()) {
            val msg = "No API usage recorded in this session."
            Log.i(TAG, msg)
            println(msg)
            return
        }
        
        var totalCost = 0.0
        var totalRequests = 0
        var totalTokens = 0
        
        val summary = StringBuilder()
        summary.appendLine("═══════════════════════════════════════════════════════════════════")
        summary.appendLine("OpenAI API Usage - Session Summary")
        summary.appendLine("═══════════════════════════════════════════════════════════════════")
        
        usageByModel.forEach { (model, usage) ->
            summary.appendLine("")
            summary.appendLine("Model: $model")
            summary.appendLine("  Requests: ${usage.requestCount}")
            summary.appendLine("  Prompt Tokens: ${usage.promptTokens}")
            if (usage.completionTokens > 0) {
                summary.appendLine("  Completion Tokens: ${usage.completionTokens}")
            }
            summary.appendLine("  Total Tokens: ${usage.totalTokens}")
            summary.appendLine("  Cost: $${"%.6f".format(usage.cost)}")
            
            totalCost += usage.cost
            totalRequests += usage.requestCount
            totalTokens += usage.totalTokens
        }
        
        summary.appendLine("")
        summary.appendLine("═══════════════════════════════════════════════════════════════════")
        summary.appendLine("TOTAL SESSION:")
        summary.appendLine("  Total Requests: $totalRequests")
        summary.appendLine("  Total Tokens: $totalTokens")
        summary.appendLine("  Total Cost: $${"%.6f".format(totalCost)}")
        summary.appendLine("═══════════════════════════════════════════════════════════════════")
        
        val message = summary.toString()
        // Log at INFO level (visible in Logcat)
        Log.i(TAG, message)
        // Also print to System.out (visible in adb logcat)
        println(message)
    }
    
    /**
     * Resets all usage statistics.
     */
    fun reset() {
        usageByModel.clear()
        Log.i(TAG, "Usage statistics reset.")
    }
    
    /**
     * Gets current total cost.
     */
    fun getTotalCost(): Double {
        return usageByModel.values.sumOf { it.cost }
    }
    
    /**
     * Gets usage statistics as a formatted string for display.
     */
    fun getUsageSummary(): UsageSummary {
        if (usageByModel.isEmpty()) {
            return UsageSummary(
                totalRequests = 0,
                totalTokens = 0,
                totalCost = 0.0,
                modelBreakdown = emptyList()
            )
        }
        
        var totalCost = 0.0
        var totalRequests = 0
        var totalTokens = 0
        val breakdown = mutableListOf<ModelUsageInfo>()
        
        usageByModel.forEach { (model, usage) ->
            totalCost += usage.cost
            totalRequests += usage.requestCount
            totalTokens += usage.totalTokens
            
            breakdown.add(
                ModelUsageInfo(
                    model = model,
                    requests = usage.requestCount,
                    promptTokens = usage.promptTokens,
                    completionTokens = usage.completionTokens,
                    totalTokens = usage.totalTokens,
                    cost = usage.cost
                )
            )
        }
        
        return UsageSummary(
            totalRequests = totalRequests,
            totalTokens = totalTokens,
            totalCost = totalCost,
            modelBreakdown = breakdown
        )
    }
    
    data class UsageSummary(
        val totalRequests: Int,
        val totalTokens: Int,
        val totalCost: Double,
        val modelBreakdown: List<ModelUsageInfo>
    )
    
    data class ModelUsageInfo(
        val model: String,
        val requests: Int,
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int,
        val cost: Double
    )
}

