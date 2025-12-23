// localStorage utilities for persistence
// Handles history, seen verses, and settings

import { ReflectionRecord } from '../types/reflection';

const STORAGE_KEYS = {
  HISTORY: 'gita_reflection_history',
  SEEN_VERSES: 'gita_seen_verses',
  LAST_REFLECTION_ANGLE: 'gita_last_reflection_angle',
  AI_API_KEY: 'gita_ai_api_key',
  AI_ENABLED: 'gita_ai_enabled',
} as const;

export interface SeenVerse {
  verseId: string;
  themeId: string;
  subthemeId: string;
  lastUsed: string; // ISO date string
  timesUsed: number;
}

export interface StorageHistory {
  reflections: ReflectionRecord[];
  lastUpdated: string;
}

// History management
export function saveReflectionToHistory(reflection: ReflectionRecord): void {
  try {
    const existing = getReflectionHistory();
    existing.reflections.unshift(reflection);
    // Keep only last 100 reflections
    if (existing.reflections.length > 100) {
      existing.reflections = existing.reflections.slice(0, 100);
    }
    existing.lastUpdated = new Date().toISOString();
    localStorage.setItem(STORAGE_KEYS.HISTORY, JSON.stringify(existing));
  } catch (error) {
    console.error('Failed to save reflection to history:', error);
  }
}

export function getReflectionHistory(): StorageHistory {
  try {
    const stored = localStorage.getItem(STORAGE_KEYS.HISTORY);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (error) {
    console.error('Failed to read reflection history:', error);
  }
  return { reflections: [], lastUpdated: new Date().toISOString() };
}

export function clearReflectionHistory(): void {
  try {
    localStorage.removeItem(STORAGE_KEYS.HISTORY);
  } catch (error) {
    console.error('Failed to clear reflection history:', error);
  }
}

// Seen verses tracking (for rotation)
export function markVerseAsSeen(verseId: string, themeId: string, subthemeId: string): void {
  try {
    const seen = getSeenVerses();
    const existing = seen.find(v => v.verseId === verseId);
    if (existing) {
      existing.timesUsed += 1;
      existing.lastUsed = new Date().toISOString();
    } else {
      seen.push({
        verseId,
        themeId,
        subthemeId,
        lastUsed: new Date().toISOString(),
        timesUsed: 1,
      });
    }
    localStorage.setItem(STORAGE_KEYS.SEEN_VERSES, JSON.stringify(seen));
  } catch (error) {
    console.error('Failed to mark verse as seen:', error);
  }
}

export function getSeenVerses(): SeenVerse[] {
  try {
    const stored = localStorage.getItem(STORAGE_KEYS.SEEN_VERSES);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (error) {
    console.error('Failed to read seen verses:', error);
  }
  return [];
}

export function getLastReflectionAngle(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEYS.LAST_REFLECTION_ANGLE);
  } catch (error) {
    console.error('Failed to read last reflection angle:', error);
    return null;
  }
}

export function setLastReflectionAngle(angle: string): void {
  try {
    localStorage.setItem(STORAGE_KEYS.LAST_REFLECTION_ANGLE, angle);
  } catch (error) {
    console.error('Failed to save last reflection angle:', error);
  }
}

// AI settings
export function getAIApiKey(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEYS.AI_API_KEY);
  } catch (error) {
    console.error('Failed to read AI API key:', error);
    return null;
  }
}

export function setAIApiKey(key: string | null): void {
  try {
    if (key) {
      localStorage.setItem(STORAGE_KEYS.AI_API_KEY, key);
      localStorage.setItem(STORAGE_KEYS.AI_ENABLED, 'true');
    } else {
      localStorage.removeItem(STORAGE_KEYS.AI_API_KEY);
      localStorage.setItem(STORAGE_KEYS.AI_ENABLED, 'false');
    }
  } catch (error) {
    console.error('Failed to save AI API key:', error);
  }
}

export function isAIEnabled(): boolean {
  try {
    const enabled = localStorage.getItem(STORAGE_KEYS.AI_ENABLED);
    const hasKey = !!getAIApiKey();
    return enabled === 'true' && hasKey;
  } catch (error) {
    console.error('Failed to check AI enabled status:', error);
    return false;
  }
}

