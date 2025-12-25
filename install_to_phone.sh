#!/bin/bash
# Install app to connected phone/device

SDK_DIR="/c/Users/anurag/AppData/Local/Android/Sdk"
ADB="$SDK_DIR/platform-tools/adb.exe"

echo "========================================"
echo "Install Gita to Your Phone"
echo "========================================"
echo ""

# Check for connected devices
echo "Checking for connected devices..."
echo ""

DEVICES=$("$ADB" devices 2>/dev/null | grep -E "device$" | grep -v "List of devices")

if [ -z "$DEVICES" ]; then
    echo "❌ No devices found!"
    echo ""
    echo "Make sure:"
    echo "  1. Phone is connected via USB"
    echo "  2. USB Debugging is enabled (Settings > Developer Options)"
    echo "  3. You've authorized the computer on your phone"
    echo ""
    echo "Check with: adb devices"
    exit 1
fi

echo "✅ Found device(s):"
"$ADB" devices
echo ""

# Uninstall existing app if present (to avoid signature conflicts)
echo "🗑️  Uninstalling existing app (if present)..."
"$ADB" uninstall com.gita.app 2>/dev/null || true
echo ""

# Build and install
echo "📦 Building and installing app..."
echo ""

# Use gradlew.bat on Windows (Git Bash)
if [ -f "./gradlew.bat" ]; then
    ./gradlew.bat installDebug
elif [ -f "./gradlew" ]; then
    ./gradlew installDebug
else
    echo "❌ Gradle wrapper not found!"
    echo "Please run this script from the project root directory."
    exit 1
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ App installed successfully!"
    echo ""
    echo "Launching app on your phone..."
    "$ADB" shell am start -n com.gita.app/.MainActivity
    echo ""
    echo "✅ Done! Check your phone - the app should be launching now!"
else
    echo ""
    echo "❌ Installation failed"
    echo ""
    echo "Try:"
    echo "  1. Make sure phone is unlocked"
    echo "  2. Check USB connection"
    echo "  3. Try: adb devices (to verify connection)"
    exit 1
fi

