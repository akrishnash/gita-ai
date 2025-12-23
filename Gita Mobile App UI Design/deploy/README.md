# Gita App Deployment Guide

This document explains how to build the web app and package it as an Android APK.

## Prerequisites

- Node.js 18+ and npm/yarn
- For Android APK: Android Studio (for Capacitor) or Android SDK (for TWA)

## Web Build

### Development

```bash
npm install
npm run dev
```

The app will run on `http://localhost:3000`

### Production Build

```bash
npm run build
```

Output will be in the `build/` directory.

### Testing Production Build Locally

```bash
npx serve -s build
```

## Android APK Packaging

### Option 1: Capacitor (Recommended)

Capacitor wraps the web app in a native container, providing better performance and native features.

#### Setup

1. Install Capacitor CLI:
```bash
npm install -g @capacitor/cli
```

2. Initialize Capacitor in the project:
```bash
npx cap init
```
- App name: `Gita`
- App ID: `com.gita.app` (or your preferred ID)
- Web dir: `build`

3. Add Android platform:
```bash
npm install @capacitor/android
npx cap add android
```

4. Build the web app:
```bash
npm run build
```

5. Sync to Android:
```bash
npx cap sync android
```

6. Open in Android Studio:
```bash
npx cap open android
```

7. In Android Studio:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or use Build → Generate Signed Bundle / APK for release

#### Configuration

Edit `capacitor.config.ts` (will be created during init):

```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.gita.app',
  appName: 'Gita',
  webDir: 'build',
  server: {
    androidScheme: 'https'
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 0,
      backgroundColor: '#FAF7F2'
    }
  }
};

export default config;
```

### Option 2: Trusted Web Activity (TWA)

TWA creates a minimal Android wrapper that loads your PWA.

#### Setup

1. Use [PWA Builder](https://www.pwabuilder.com/) or [Bubblewrap](https://github.com/GoogleChromeLabs/bubblewrap)

2. With Bubblewrap:
```bash
npm install -g @bubblewrap/cli
bubblewrap init --manifest=https://your-domain.com/manifest.json
bubblewrap build
```

3. Or manually create TWA using Android Studio with the TWA library

## Keeping App Under 100MB

### Current Size Considerations

- **No bundled AI models**: The app uses API calls only (optional)
- **Minimal dependencies**: React + Vite only
- **No heavy assets**: Text-based content only
- **gitaMap.ts**: Will contain verse data (text only, should be < 1MB)

### Size Optimization Tips

1. **Code splitting**: Vite handles this automatically
2. **Tree shaking**: Remove unused dependencies
3. **Compression**: Enable gzip/brotli on server
4. **Images**: Use WebP format if adding icons
5. **Fonts**: Use system fonts or minimal web fonts

### Expected Sizes

- Web bundle: ~200-500 KB (gzipped)
- Android APK: ~5-15 MB (uncompressed)
- With Capacitor: ~10-20 MB (includes native runtime)

## PWA Features

The app is PWA-ready with:

- ✅ `manifest.json` for installability
- ✅ Service worker for offline support
- ✅ Responsive design (mobile-first)
- ✅ Offline-first architecture (localStorage)

### Testing PWA

1. Build the app: `npm run build`
2. Serve locally: `npx serve -s build`
3. Open Chrome DevTools → Application → Service Workers
4. Test "Add to Home Screen" functionality

## Deployment Checklist

- [ ] Build production bundle: `npm run build`
- [ ] Test PWA features locally
- [ ] Verify service worker registration
- [ ] Test offline functionality
- [ ] Create app icons (192x192, 512x512)
- [ ] Update manifest.json with correct icons
- [ ] For Capacitor: Configure app ID and permissions
- [ ] For TWA: Set up digital asset links
- [ ] Test on physical Android device
- [ ] Verify app size is under 100MB

## Notes

- The app works **without backend** - all data is client-side
- AI is **optional** - app functions perfectly without API keys
- History is stored in **localStorage** (persists across sessions)
- No server-side rendering - pure client-side React app

## Troubleshooting

### Service Worker Not Registering

- Ensure you're serving over HTTPS (or localhost)
- Check browser console for errors
- Verify `sw.js` is in the `public/` directory

### Capacitor Build Fails

- Ensure Android SDK is properly installed
- Check `android/app/build.gradle` for correct configurations
- Verify `capacitor.config.ts` has correct `webDir` path

### App Size Too Large

- Run `npm run build -- --analyze` to see bundle breakdown
- Remove unused dependencies
- Check for duplicate dependencies
- Optimize images if any are added

