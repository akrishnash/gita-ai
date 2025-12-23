// AI configuration utility
// Manages AI provider initialization based on settings

import { AIProvider } from '../ai/aiProvider';
import { NullProvider } from '../ai/nullProvider';
import { OpenAIProvider } from '../ai/openAIProvider';
import { getAIApiKey, isAIEnabled } from './storage';

let cachedProvider: AIProvider | null = null;

/**
 * Gets the active AI provider
 * Returns NullProvider if AI is disabled or unavailable
 */
export function getAIProvider(): AIProvider {
  // Return cached provider if available and still valid
  if (cachedProvider && isAIEnabled()) {
    return cachedProvider;
  }

  // Check if AI is enabled
  if (!isAIEnabled()) {
    cachedProvider = new NullProvider();
    return cachedProvider;
  }

  // Try to initialize OpenAI provider
  const apiKey = getAIApiKey();
  if (apiKey) {
    try {
      cachedProvider = new OpenAIProvider(apiKey);
      return cachedProvider;
    } catch (error) {
      console.error('Failed to initialize AI provider:', error);
      cachedProvider = new NullProvider();
      return cachedProvider;
    }
  }

  // Fallback to null provider
  cachedProvider = new NullProvider();
  return cachedProvider;
}

/**
 * Resets the cached provider
 * Call this when AI settings change
 */
export function resetAIProvider(): void {
  cachedProvider = null;
}

