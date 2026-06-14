# Gita AI — Play Store Release Info

## Build Artifacts

| Artifact | Path | Size |
|---|---|---|
| Signed AAB (submit this) | `app/build/outputs/bundle/release/app-release.aab` | 6.9 MB |
| Signed APK (sideload/testing) | `app/build/outputs/apk/release/app-release.apk` | 5.8 MB |

---

## Keystore

| Field | Value |
|---|---|
| File | `/Users/aksharma/Projects/gita-release.jks` |
| Store password | `GitaAI@2024!` |
| Key alias | `gita` |
| Key password | `GitaAI@2024!` |
| Algorithm | RSA 2048-bit, SHA384withRSA |
| Valid from | 15 Jun 2026 |
| Valid until | 31 Oct 2053 (10,000 days) |
| SHA-1 fingerprint | `2F:47:B6:AB:D3:05:9F:EE:EA:77:17:1F:15:28:3E:4C:90:20:93:6E` |
| SHA-256 fingerprint | `F5:F3:73:A8:C8:95:2C:51:4A:67:2B:0A:6C:2A:35:F6:6C:77:D7:37:06:52:B0:17:C9:82:E0:8F:33:64:72:94` |

> **CRITICAL:** Back up `gita-release.jks` to a safe location (iCloud, password manager, external drive).
> If lost, you cannot publish updates to this app on the Play Store — ever.

---

## local.properties (never commit this file)

```
RELEASE_KEYSTORE_PATH=/Users/aksharma/Projects/gita-release.jks
RELEASE_KEYSTORE_PASSWORD=GitaAI@2024!
RELEASE_KEY_ALIAS=gita
RELEASE_KEY_PASSWORD=GitaAI@2024!
```

Verify it stays out of git:
```bash
git check-ignore -v local.properties
```

---

## App Config

| Field | Value |
|---|---|
| Application ID | `com.gitaaikrishna.app` |
| Version name | `1.0.0` |
| Version code | `1` |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Minification | R8 enabled |
| Signing | Release keystore (above) |
| Cleartext HTTP | Blocked (network_security_config.xml) |
| Backup | Disabled (prevents SharedPreferences leaking) |

---

## Rebuild Commands

```bash
# Full signed AAB for Play Store submission
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab

# Signed APK (sideloading / manual testing)
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

---

## Play Store Submission Checklist

- [ ] Back up `gita-release.jks` off this machine
- [ ] Create app on [Google Play Console](https://play.google.com/console)
- [ ] Upload `app-release.aab` to Internal Testing track first
- [ ] Fill in store listing: title, short/long description, screenshots (phone + 7-inch tablet)
- [ ] Set content rating (complete the questionnaire)
- [ ] Set pricing (Free)
- [ ] Add privacy policy URL (required — app uses Firebase Auth + internet)
- [ ] Declare data safety (Firebase Auth collects email/name; no data sold)
- [ ] Swap Razorpay test key (`rzp_test_`) for live key (`rzp_live_`) in `local.properties` before production release
- [ ] Bump `versionCode` to `2` and `versionName` to `1.0.1` for each subsequent update

---

## ProGuard / R8

Rules file: `app/proguard-rules.pro`

Covers: BuildConfig, Razorpay, Gson, OkHttp, Retrofit, Firebase, GMS, all app data/logic/network models.

---

## Security Notes

- All network traffic is HTTPS-only (enforced via `res/xml/network_security_config.xml`)
- API keys (Gemini, Razorpay, Google OAuth) are read from `local.properties` at build time — never hardcoded
- `local.properties` is in `.gitignore` and must never be committed
- Subscription state is stored in `SharedPreferences` (not backed up — `android:allowBackup="false"`)
