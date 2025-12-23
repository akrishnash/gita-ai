# Important Notes

## LocalStorage Location

Due to disk space constraints during development, `LocalStorage.kt` is currently located in the `logic` package instead of a separate `storage` package. The file is at:

```
app/src/main/java/com/gita/app/logic/LocalStorage.kt
```

If you want to move it to a proper `storage` package:
1. Create the directory: `app/src/main/java/com/gita/app/storage/`
2. Move `LocalStorage.kt` to that directory
3. Update the package declaration in `LocalStorage.kt` to `package com.gita.app.storage`
4. Update imports in `SelectionEngine.kt` and `MainViewModel.kt` to use `com.gita.app.storage.LocalStorage`

## Build Requirements

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 33+
- Min SDK: 24

## Project Status

✅ All core functionality implemented
✅ MVVM architecture
✅ Offline-first design
✅ Theme detection
✅ Verse selection with rotation
✅ All UI screens
✅ History tracking
✅ Settings screen (AI API key placeholder)

## Next Steps

1. Test the app on a device/emulator
2. Move LocalStorage to proper package if desired
3. Add more verses to GitaMap.kt if needed
4. Implement AI integration (optional, requires API key)

