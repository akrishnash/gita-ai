# Build Instructions

## Logic Verification ✅

The app logic has been verified:
- Input: "i am sad"
- **Will match**: Grief theme (keyword "sad" is in the grief theme keywords)
- **Will return**: Verse 2.13 about grief/loss
- **Translation**: "Just as the body passes through stages, so does life move onward."

## Building the App

### Option 1: Build in Android Studio (Recommended)

1. **Open Android Studio**
   - File → Open → Select the `app` folder or root project folder

2. **Sync Gradle**
   - Android Studio will automatically sync, or click "Sync Now" if prompted
   - Wait for Gradle sync to complete

3. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or use: `Build → Make Project` (Ctrl+F9)

4. **Find the APK**
   - Location: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Build from Command Line

If you have Android SDK and Gradle installed:

```bash
# Navigate to project root
cd "e:\gita ai"

# Build debug APK
gradle assembleDebug

# Or if using Gradle wrapper (after setup):
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Option 3: Run on Emulator/Device

1. **Connect device or start emulator**
2. **In Android Studio**: Click the green "Run" button (Shift+F10)
3. **Or from command line**: `gradle installDebug`

## Testing the App

1. Install the APK on a device/emulator
2. Open the app
3. Enter: "i am sad"
4. Click "Continue"
5. Wait for the pause screen
6. You should see:
   - **Verse**: Chapter 2, Verse 13
   - **Sanskrit**: देहिनोऽस्मिन्यथा देहे
   - **Translation**: "Just as the body passes through stages, so does life move onward."
   - **Context**: About grief and loss
   - **Reflection**: One of the 5 reflection angles
   - **Anchor Line**: "Loss is change, not erasure." or "Grief moves at its own pace."

## Requirements

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 33+
- Min SDK: 24 (Android 7.0)

## Troubleshooting

### Gradle Sync Fails
- Check internet connection (for downloading dependencies)
- File → Invalidate Caches → Invalidate and Restart

### Build Errors
- Ensure all dependencies are downloaded
- Check that Java 17 is set as project SDK
- Clean project: Build → Clean Project, then rebuild

