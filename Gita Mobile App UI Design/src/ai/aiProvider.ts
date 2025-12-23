// AI provider interface
// Allows pluggable AI enhancement without requiring it

import { ReflectionContent } from '../types/reflection';

export interface AIProvider {
  /**
   * Refines a reflection text using AI
   * @param problem - User's original problem input
   * @param verseContext - Context about the selected verse
   * @param baseReflection - The deterministic reflection to refine
   * @returns Refined reflection text (max 120 words) or null if unavailable
   */
  refineReflection(
    problem: string,
    verseContext: {
      sanskrit: string;
      english: string;
      context: string;
    },
    baseReflection: ReflectionContent
  ): Promise<string | null>;
}

export class AIError extends Error {
  constructor(message: string, public code?: string) {
    super(message);
    this.name = 'AIError';
  }
}

