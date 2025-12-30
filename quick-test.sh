#!/bin/bash
# Quick test script - Just build and install if emulator/device is already running
# Faster for repeated testing

# Default options
SKIP_BUILD=false
UNINSTALL_FIRST=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -SkipBuild|--skip-build)
            SKIP_BUILD=true
            shift
            ;;
        -UninstallFirst|--uninstall-first)
            UNINSTALL_FIRST=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--skip-build] [--uninstall-first]"
            exit 1
            ;;
    esac
done

echo ""
echo "=== Gita App - Quick Test ==="
echo ""

# Auto-detect Android SDK
ANDROID_HOME=""

if [ -n "$LOCALAPPDATA" ]; then
    ANDROID_HOME="$LOCALAPPDATA/Android/Sdk"
fi

if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME/platform-tools" ]; then
    if [ -n "$USERPROFILE" ]; then
        ANDROID_HOME="$USERPROFILE/AppData/Local/Android/Sdk"
    fi
fi

# Convert Windows path to Git Bash path if needed
if [[ "$ANDROID_HOME" == *"\\"* ]]; then
    ANDROID_HOME=$(echo "$ANDROID_HOME" | sed 's|\\|/|g' | sed 's|C:|/c|')
fi

ADB_PATH="$ANDROID_HOME/platform-tools/adb"

# Convert paths for Windows Git Bash
if [[ "$ADB_PATH" == C:* ]] || [[ "$ADB_PATH" == c:* ]]; then
    ADB_PATH=$(echo "$ADB_PATH" | sed 's|^C:|/c|' | sed 's|^c:|/c|' | sed 's|\\|/|g')
fi

# Use .exe extension on Windows
if [ ! -f "$ADB_PATH" ]; then
    ADB_PATH="${ADB_PATH}.exe"
fi

if [ -z "$ANDROID_HOME" ] || [ ! -f "$ADB_PATH" ]; then
    echo "ERROR: Android SDK not found!"
    exit 1
fi

# Check for device/emulator
echo "Checking for connected device/emulator..."
DEVICES=$("$ADB_PATH" devices | grep -E "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "ERROR: No device/emulator found!"
    echo "Please start an emulator or connect a device first."
    echo "You can use: ./run-on-emulator.sh to start an emulator automatically."
    exit 1
fi

echo "Found device(s)/emulator(s)"
"$ADB_PATH" devices

# Uninstall if requested
if [ "$UNINSTALL_FIRST" = true ]; then
    echo ""
    echo "Uninstalling existing app..."
    "$ADB_PATH" uninstall com.gita.app >/dev/null 2>&1
    echo "Uninstalled"
fi

# Build if needed
if [ "$SKIP_BUILD" = false ]; then
    echo ""
    echo "Building APK..."
    if ./gradlew.bat assembleDebug --no-daemon; then
        echo "Build successful!"
    else
        echo ""
        echo "Build failed!"
        exit 1
    fi
fi

# Install
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo ""
    echo "ERROR: APK not found! Please build first."
    exit 1
fi

echo ""
echo "Installing..."
if "$ADB_PATH" install -r "$APK_PATH"; then
    echo "Installation successful!"
    
    sleep 1
    "$ADB_PATH" shell am start -n com.gita.app/.MainActivity >/dev/null 2>&1
    
    echo "App launched!"
    echo ""
    echo "Ready to test!"
else
    echo "Installation failed!"
    exit 1
fi



