# Gita - Android App

A native Android app built with Kotlin and Jetpack Compose that provides philosophical reflections based on the Bhagavad Gita.

## Features

- **Offline-first**: Works completely offline, no internet required
- **Theme Detection**: Automatically detects relevant themes from user input using keyword matching
- **Verse Rotation**: Intelligently rotates through verses to avoid repetition
- **Multiple Perspectives**: Each verse can be viewed from different reflection angles
- **History**: Tracks your reflection journey
- **Material 3**: Modern, clean UI with light and dark mode support

## Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Jetpack Compose**: Modern declarative UI
- **DataStore**: Persistent local storage
- **Kotlin Coroutines**: Asynchronous operations

## Project Structure

```
app/src/main/java/com/gita/app/
├── data/           # Data models and Gita verse data
├── logic/          # Business logic (theme detection, selection engine, storage)
├── ui/             # Compose UI screens
├── viewmodel/      # ViewModels for state management
└── MainActivity.kt # App entry point
```

## Building

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on an Android device or emulator (API 24+)

## Requirements

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 33+
- Min SDK: 24 (Android 7.0)

## Data

The app uses curated, real Bhagavad Gita verses from `GitaMap.kt`. All verses, translations, and reflections are preserved exactly as provided - no modifications to the source material.

## Future Enhancements

- Optional AI integration for enhanced reflections (requires API key)
- Additional themes and verses
- Export/import history

## License

This project contains translations and reflections of the Bhagavad Gita. The philosophical content is in the public domain.

