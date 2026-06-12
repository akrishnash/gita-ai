# Gita AI - Deployment Guide

This guide covers the steps required to deploy the Gita AI app to the Google Play Store or to generate a release APK for manual distribution.

## 1. Pre-requisites

1. **Android Studio** installed
2. **Java 17** installed (configured in Android Studio)
3. **OpenAI API Key** (optional but recommended for production builds)
4. A **Keystore** file for signing the release build

## 2. Generating a Keystore

If you don't have a keystore yet, generate one using the `keytool` command or Android Studio's UI:

```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gita-alias
```
Make sure to securely store the keystore file and its password.

## 3. Configuring Release Build

1. Open `app/build.gradle.kts`.
2. Ensure the `versionCode` and `versionName` are updated for the new release.
```kotlin
defaultConfig {
    versionCode = 2 // Increment for each release
    versionName = "1.1.0" // Semantic versioning
}
```

3. (Optional) Provide a default API key for the release if you wish to package one. 
*Note: Due to security concerns, the current build configuration injects an empty string `""` into the release build so no API key is hardcoded. Users can input their own key in the Settings screen to enable the AI features. Without it, the app safely falls back to local deterministic keyword matching.*

## 4. Building the Release App Bundle (AAB) or APK

You can build the app via Android Studio:
1. Go to **Build** > **Generate Signed Bundle / APK...**
2. Choose **Android App Bundle** (for Google Play) or **APK** (for direct distribution).
3. Select your keystore file, enter the password and alias.
4. Select the `release` build variant.
5. Click **Finish**.

Alternatively, via Gradle command line:

```bash
# Build APK
./gradlew assembleRelease

# Build App Bundle (AAB)
./gradlew bundleRelease
```

The output file will be located in:
`app/build/outputs/apk/release/app-release.apk`
or
`app/build/outputs/bundle/release/app-release.aab`

## 5. ProGuard / R8 Rules

The app uses R8 for shrinking and obfuscation in release mode (`isMinifyEnabled = true`).
Important data classes used by Gson are protected in `app/proguard-rules.pro`:
```proguard
-keep class com.gita.app.data.** { *; }
-keep class com.gita.app.logic.** { *; }
-keep class com.gita.app.kotlinmodel.** { *; }
-keep class com.google.gson.** { *; }
```
If you add new JSON data classes in the future, ensure their package is included in the keep rules, otherwise the app will crash in release mode.

## 6. App Store Listing Assets

Ensure you have the following assets ready before uploading to Google Play Console:
- App Icon (512x512 PNG)
- Feature Graphic (1024x500 PNG)
- 3-8 Phone Screenshots
- Privacy Policy URL (Required)
