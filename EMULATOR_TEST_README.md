# Emulator Testing Scripts

These scripts help you build and test the Gita AI app on Android emulators using Git Bash.

## Prerequisites

- Git Bash installed
- Android SDK installed
- At least one Android Virtual Device (AVD) created in Android Studio

## Scripts

### 1. `run-on-emulator.sh` - Full Build & Install

This script will:
- Auto-detect your Android SDK
- Check if an emulator is running
- Start an emulator if needed (uses first available AVD)
- Build the APK
- Install and launch the app

**Usage:**
```bash
# Basic usage (will start emulator if needed)
./run-on-emulator.sh

# Skip build (use existing APK)
./run-on-emulator.sh --skip-build

# Specify a specific AVD
./run-on-emulator.sh --avd-name Pixel_8

# Don't wait for emulator to fully boot
./run-on-emulator.sh --no-wait

# Uninstall existing app first
./run-on-emulator.sh --uninstall-first
```

### 2. `quick-test.sh` - Quick Install (Emulator must be running)

Use this when you already have an emulator running - it's much faster!

**Usage:**
```bash
# Build and install
./quick-test.sh

# Just install (skip build)
./quick-test.sh --skip-build

# Uninstall first, then install
./quick-test.sh --uninstall-first
```

## Examples

**First time setup:**
```bash
./run-on-emulator.sh
```
This will start your emulator, wait for it to boot, build the app, and install it.

**Quick iteration (when emulator is already running):**
```bash
./quick-test.sh --skip-build
```
This will just install the existing APK - very fast for testing UI changes!

**After making code changes:**
```bash
./quick-test.sh
```
This will rebuild and reinstall.

## Troubleshooting

**"ERROR: ANDROID_HOME not found!"**
- The script tries to auto-detect, but if it fails:
- Set ANDROID_HOME environment variable to your SDK path
- Or manually edit the script to set the path

**"ERROR: No AVDs found!"**
- Open Android Studio
- Go to Tools > Device Manager
- Create a Virtual Device

**Emulator takes too long to boot?**
- Use `--no-wait` flag to skip the wait
- Or start emulator manually from Android Studio first, then use `quick-test.sh`

**"Permission denied" when running script?**
- In Git Bash, you may need to run: `bash run-on-emulator.sh` instead of `./run-on-emulator.sh`
- Or ensure the file has execute permissions

## Notes

- The scripts automatically detect Windows-style paths (C:\Users\...) and convert them for Git Bash
- If you have multiple AVDs, it will use the first one by default
- You can run the scripts multiple times - they will replace the existing installation



