// OpenAI provider for optional AI enhancement
// Only used when API key is configured

import { AIProvider, AIError } from './aiProvider';
import { ReflectionContent } from '../types/reflection';

export class OpenAIProvider implements AIProvider {
  private apiKey: string;
  private baseUrl: string;

  constructor(apiKey: string, baseUrl: string = 'https://api.openai.com/v1') {
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
  }

  async refineReflection(
    problem: string,
    verseContext: {
      sanskrit: string;
      english: string;
      context: string;
    },
    baseReflection: ReflectionContent
  ): Promise<string | null> {
    try {
      const response = await fetch(`${this.baseUrl}/chat/completions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`,
        },
        body: JSON.stringify({
          model: 'gpt-4',
          messages: [
            {
              role: 'system',
              content: `You are a thoughtful reflection assistant. Your task is to refine a philosophical reflection text.

Rules:
- Maximum 120 words
- Keep the same tone: calm, human, non-religious, non-authoritative
- Do NOT add new philosophy or verses
- Do NOT speak as Krishna or any authority
- Only refine the existing reflection for clarity and impact
- Allow doubt and uncertainty
- No moral commands`,
            },
            {
              role: 'user',
              content: `User's problem: ${problem}

Verse context:
Sanskrit: ${verseContext.sanskrit}
English: ${verseContext.english}
Context: ${verseContext.context}

Base reflection to refine:
${baseReflection.text}

Refine this reflection while keeping the same angle (${baseReflection.angle}). Make it clearer and more impactful, but do not change the core message or add new philosophy.`,
            },
          ],
          temperature: 0.7,
          max_tokens: 200,
        }),
      });

      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new AIError(
          error.error?.message || `API error: ${response.statusText}`,
          error.error?.code || 'api_error'
        );
      }

      const data = await response.json();
      const refinedText = data.choices?.[0]?.message?.content?.trim();

      if (!refinedText) {
        return null;
      }

      // Ensure word limit
      const words = refinedText.split(/\s+/);
      if (words.length > 120) {
        return words.slice(0, 120).join(' ') + '...';
      }

      return refinedText;
    } catch (error) {
      if (error instanceof AIError) {
        throw error;
      }
      throw new AIError(
        error instanceof Error ? error.message : 'Unknown error occurred',
        'network_error'
      );
    }
  }
}

