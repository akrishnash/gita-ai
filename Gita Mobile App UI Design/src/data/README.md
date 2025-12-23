# Data Directory

This directory contains the verse data for the Gita app.

## gitaMap.ts

This file will be provided separately and contains:

- Theme definitions (fear, confusion, attachment, grief, duty_vs_desire, exhaustion)
- Subthemes for each theme (3-4 per theme)
- Verse clusters for each subtheme (5-10 verses per cluster)
- Each verse includes:
  - Sanskrit text
  - Transliteration
  - English translation
  - Context paragraph
  - Multiple reflection angles (psychological, action, detachment, compassion, selfTrust)
  - Multiple anchor lines

**IMPORTANT**: This file should NOT be modified. It is the source of truth for all verse content.

## Expected Structure

The file should export a structure that matches the interfaces defined in `src/types/reflection.ts`:

```typescript
export const gitaMap: {
  [themeId: string]: {
    subthemes: {
      [subthemeId: string]: {
        verses: SelectedVerse[];
      };
    };
  };
} = {
  // ... theme data
};
```

