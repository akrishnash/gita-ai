// Selection engine for verses and reflections
// TODO: This will consume gitaMap.ts when provided
// Handles rotation, non-repetition, and reflection angle selection

import {
  ThemeId,
  SubthemeId,
  SelectedVerse,
  ReflectionContent,
  AnchorLine,
  AlternatePerspective,
  ReflectionAngle,
} from '../types/reflection';
import {
  getSeenVerses,
  markVerseAsSeen,
  getLastReflectionAngle,
  setLastReflectionAngle,
} from './storage';

// Reflection angle rotation order
const REFLECTION_ANGLES: ReflectionAngle[] = [
  'psychological',
  'action',
  'detachment',
  'compassion',
  'selfTrust',
];

// Rotated heading options
const HEADINGS = [
  'A moment of reflection',
  'A thought worth sitting with',
  'A quiet perspective',
  'Something to consider',
  'A pause for thought',
];

/**
 * Selects a verse from the given theme/subtheme
 * Implements rotation to avoid repetition
 * 
 * TODO: This will query gitaMap.ts for actual verse clusters
 * 
 * @param themeId - Detected theme ID
 * @param subthemeId - Detected subtheme ID
 * @returns Selected verse or null if none available
 */
export function selectVerse(themeId: ThemeId, subthemeId: SubthemeId): SelectedVerse | null {
  // PLACEHOLDER: This will query gitaMap.ts for verse clusters
  // The implementation will:
  // 1. Get verse cluster for themeId/subthemeId from gitaMap.ts
  // 2. Check seen verses in localStorage
  // 3. Select least recently used verse
  // 4. If all verses used, reset cycle and start over
  // 5. Mark selected verse as seen
  
  const seenVerses = getSeenVerses();
  const subthemeVerses = seenVerses.filter(v => v.subthemeId === subthemeId);
  
  // TODO: Replace with actual verse data from gitaMap.ts
  // For now, return null to indicate no data available
  // When gitaMap.ts is provided, this will:
  // - Load verse cluster from gitaMap[themeId].subthemes[subthemeId].verses
  // - Filter out recently used verses
  // - Select and return a SelectedVerse
  
  return null;
}

/**
 * Selects a reflection angle and generates reflection content
 * Rotates angles to provide variety for the same verse
 * 
 * TODO: This will use reflection angles from gitaMap.ts verse data
 * 
 * @param verse - Selected verse
 * @param previousAngle - Previously used angle (to avoid immediate repetition)
 * @returns Reflection content
 */
export function selectReflection(
  verse: SelectedVerse,
  previousAngle?: ReflectionAngle
): ReflectionContent {
  // PLACEHOLDER: This will use reflection data from gitaMap.ts
  // The implementation will:
  // 1. Get available angles from verse.availableAngles
  // 2. Filter out previousAngle if provided
  // 3. Select next angle in rotation
  // 4. Return reflection text for that angle from verse data
  
  const lastAngle = getLastReflectionAngle() as ReflectionAngle | null;
  const availableAngles = verse.availableAngles || REFLECTION_ANGLES;
  
  // Find next angle in rotation
  let nextAngle: ReflectionAngle;
  if (previousAngle && availableAngles.includes(previousAngle)) {
    const currentIndex = availableAngles.indexOf(previousAngle);
    nextAngle = availableAngles[(currentIndex + 1) % availableAngles.length];
  } else if (lastAngle && availableAngles.includes(lastAngle)) {
    const currentIndex = availableAngles.indexOf(lastAngle);
    nextAngle = availableAngles[(currentIndex + 1) % availableAngles.length];
  } else {
    nextAngle = availableAngles[0];
  }
  
  setLastReflectionAngle(nextAngle);
  
  // TODO: Return actual reflection text from verse data
  // For now, return placeholder structure
  return {
    angle: nextAngle,
    text: '', // Will be populated from gitaMap.ts
  };
}

/**
 * Selects an anchor line for the reflection
 * 
 * TODO: This will use anchor lines from gitaMap.ts verse data
 * 
 * @param verse - Selected verse
 * @param reflection - Selected reflection content
 * @returns Anchor line
 */
export function selectAnchor(
  verse: SelectedVerse,
  reflection: ReflectionContent
): AnchorLine {
  // PLACEHOLDER: This will use anchor data from gitaMap.ts
  // The implementation will:
  // 1. Get anchor options from verse data (may vary by reflection angle)
  // 2. Rotate through available anchors
  // 3. Return selected anchor
  
  // TODO: Return actual anchor from verse data
  return {
    text: '', // Will be populated from gitaMap.ts
  };
}

/**
 * Selects an alternate perspective (different reflection angle)
 * 
 * TODO: This will use alternate reflection data from gitaMap.ts
 * 
 * @param verse - Selected verse
 * @param currentReflection - Currently displayed reflection
 * @returns Alternate perspective or null if none available
 */
export function selectAlternatePerspective(
  verse: SelectedVerse,
  currentReflection: ReflectionContent
): AlternatePerspective | null {
  // PLACEHOLDER: This will use alternate reflection data from gitaMap.ts
  // The implementation will:
  // 1. Get available angles excluding currentReflection.angle
  // 2. Select a different angle
  // 3. Return reflection + anchor for that angle
  
  const availableAngles = verse.availableAngles || REFLECTION_ANGLES;
  const alternateAngles = availableAngles.filter(a => a !== currentReflection.angle);
  
  if (alternateAngles.length === 0) {
    return null;
  }
  
  // Select first alternate angle
  const alternateAngle = alternateAngles[0];
  
  // TODO: Return actual alternate reflection from verse data
  return {
    reflection: {
      angle: alternateAngle,
      text: '', // Will be populated from gitaMap.ts
    },
    anchor: {
      text: '', // Will be populated from gitaMap.ts
    },
  };
}

/**
 * Gets a random heading for the reflection section
 * 
 * @returns Random heading text
 */
export function getRandomHeading(): string {
  return HEADINGS[Math.floor(Math.random() * HEADINGS.length)];
}

