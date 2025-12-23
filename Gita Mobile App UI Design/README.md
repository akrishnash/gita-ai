# Gita - A Quiet Place to Think

A minimal, philosophical reflection app based on the Bhagavad Gita. This app helps users find perspective on personal problems through curated verses, contextual stories, and thoughtful reflections.

## Features

- **Deterministic by default**: Works perfectly without AI or backend
- **Offline-first**: All data stored locally via localStorage
- **Optional AI enhancement**: Pluggable AI refinement (requires API key)
- **Non-repetitive**: Smart rotation of verses and reflection angles
- **PWA-ready**: Installable as a web app
- **Android APK support**: Can be packaged using Capacitor or TWA

## Architecture

### Core Flow

1. User enters a personal problem
2. Theme detection matches input to themes/subthemes
3. Selection engine picks a verse (with rotation to avoid repetition)
4. Reflection and anchor line are selected
5. Optional AI refinement (if enabled)
6. Response displayed with alternate perspective option

### File Structure

```
src/
 ├─ data/
 │   └─ gitaMap.ts          # Verse data (to be provided)
 ├─ types/
 │   └─ reflection.ts       # TypeScript interfaces
 ├─ utils/
 │   ├─ themeDetector.ts    # Theme detection logic
 │   ├─ selectionEngine.ts  # Verse/reflection selection
 │   ├─ storage.ts          # localStorage utilities
 │   └─ aiConfig.ts         # AI provider configuration
 ├─ ai/
 │   ├─ aiProvider.ts       # AI interface
 │   ├─ nullProvider.ts     # Default no-op provider
 │   └─ openAIProvider.ts    # OpenAI implementation
 ├─ components/
 │   ├─ HomeScreen.tsx
 │   ├─ PauseScreen.tsx
 │   ├─ ResponseScreen.tsx
 │   ├─ HistoryScreen.tsx
 │   ├─ SettingsScreen.tsx
 │   └─ SilentModeScreen.tsx
 └─ App.tsx                 # Main app component
```

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

The app will run on `http://localhost:3000`

### Production Build

```bash
npm run build
```

Output will be in the `build/` directory.

## Data File

The app requires `src/data/gitaMap.ts` to function. This file contains:

- Theme definitions (fear, confusion, attachment, grief, etc.)
- Subthemes for each theme
- Verse clusters for each subtheme
- Reflection angles (psychological, action, detachment, compassion, selfTrust)
- Anchor lines
- Context paragraphs

**This file will be provided separately and should NOT be modified.**

## Configuration

### AI Enhancement (Optional)

1. Go to Settings in the app
2. Enter your OpenAI API key
3. Key is stored locally (never leaves your device)
4. AI will refine reflections (max 120 words)
5. AI never chooses verses or invents content

### Disabling AI

Simply clear the API key in Settings. The app works perfectly without it.

## Deployment

See `/deploy/README.md` for detailed deployment instructions, including:

- Web deployment
- Android APK packaging (Capacitor)
- PWA setup
- Size optimization

## Design Principles

- **Restrained**: Minimal UI, intentional whitespace
- **Non-authoritative**: No preaching, allows doubt
- **Human**: Calm, reflective tone
- **Non-repetitive**: Smart rotation prevents seeing the same content
- **Offline-first**: Works without internet
- **Privacy-focused**: All data stays on device

## Technology Stack

- **React 18** with TypeScript
- **Vite** for build tooling
- **Tailwind CSS** for styling
- **localStorage** for persistence
- **PWA** capabilities (manifest + service worker)

## License

[Your License Here]

## Notes

- The app is designed to feel intelligent and reflective without requiring AI
- All verse selections are deterministic based on theme detection
- AI only refines existing reflections, never creates new content
- History is limited to 100 reflections (oldest are removed)
- App size is kept under 100MB (no bundled models)
