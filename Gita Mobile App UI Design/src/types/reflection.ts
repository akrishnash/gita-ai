// TypeScript interfaces for reflection data structures
// These will consume gitaMap.ts when it's provided

export type ThemeId = string;
export type SubthemeId = string;
export type VerseId = string;
export type ReflectionAngle = 'psychological' | 'action' | 'detachment' | 'compassion' | 'selfTrust';

export interface DetectedTheme {
  themeId: ThemeId;
  subthemeId: SubthemeId;
  confidence: number;
}

export interface Verse {
  id: VerseId;
  sanskrit: string;
  transliteration: string;
  english: string;
  chapter?: number;
  verse?: number;
}

export interface SelectedVerse extends Verse {
  context: string;
  availableAngles: ReflectionAngle[];
}

export interface ReflectionContent {
  angle: ReflectionAngle;
  text: string;
}

export interface AnchorLine {
  text: string;
}

export interface AlternatePerspective {
  reflection: ReflectionContent;
  anchor: AnchorLine;
}

export interface ReflectionResult {
  verse: SelectedVerse;
  reflection: ReflectionContent;
  anchor: AnchorLine;
  alternatePerspective?: AlternatePerspective;
  heading?: string; // Rotated heading like "A moment of reflection"
}

export interface ReflectionRecord {
  id: string;
  date: string;
  problem: string;
  themeId: ThemeId;
  subthemeId: SubthemeId;
  verseId: VerseId;
  reflectionAngle: ReflectionAngle;
  result: ReflectionResult;
}

// Placeholder for theme detection function signature
export interface ThemeDetector {
  detectTheme(userInput: string): DetectedTheme | null;
}

// Placeholder for selection engine function signature
export interface SelectionEngine {
  selectVerse(themeId: ThemeId, subthemeId: SubthemeId): SelectedVerse | null;
  selectReflection(verse: SelectedVerse, previousAngle?: ReflectionAngle): ReflectionContent;
  selectAnchor(verse: SelectedVerse, reflection: ReflectionContent): AnchorLine;
  selectAlternatePerspective(verse: SelectedVerse, currentReflection: ReflectionContent): AlternatePerspective | null;
  getRandomHeading(): string;
}

