#!/bin/bash
# View emotion detection logs from Gita app

echo "═══════════════════════════════════════════════════════"
echo "Gita App - Emotion Detection Log Viewer"
echo "═══════════════════════════════════════════════════════"
echo ""

# Find ADB
ADB_PATH=""
if [ -n "$ANDROID_HOME" ]; then
    ADB_PATH="$ANDROID_HOME/platform-tools/adb"
elif [ -d "$HOME/Library/Android/sdk/platform-tools" ]; then
    ADB_PATH="$HOME/Library/Android/sdk/platform-tools/adb"
elif [ -d "$LOCALAPPDATA/Android/Sdk/platform-tools" ]; then
    ADB_PATH="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
elif [ -d "/c/Users/$USER/AppData/Local/Android/Sdk/platform-tools" ]; then
    ADB_PATH="/c/Users/$USER/AppData/Local/Android/Sdk/platform-tools/adb.exe"
else
    # Try to find adb in PATH
    if command -v adb &> /dev/null; then
        ADB_PATH="adb"
    else
        echo "ERROR: adb not found!"
        echo ""
        echo "Please set ANDROID_HOME or ensure adb is in your PATH."
        echo "Or specify ADB path manually:"
        echo "  export ADB_PATH=/path/to/adb"
        exit 1
    fi
fi

# Use custom ADB path if set
if [ -n "$ADB_PATH" ] && [ "$ADB_PATH" != "adb" ]; then
    ADB="$ADB_PATH"
else
    ADB="adb"
fi

# Check for connected devices
echo "Checking for connected devices..."
DEVICES=$($ADB devices 2>/dev/null | grep -E "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "ERROR: No device/emulator connected!"
    echo "Please connect a device or start an emulator first."
    exit 1
fi

echo "✅ Found device(s):"
$ADB devices | grep -E "device$"
echo ""

# Ask if user wants to clear logs
read -p "Clear previous logs? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Clearing old logs..."
    $ADB logcat -c
    echo "✅ Logs cleared"
    echo ""
fi

echo "═══════════════════════════════════════════════════════"
echo "Monitoring emotion detection logs..."
echo "Use the app and enter a query to see detected emotions."
echo "Press Ctrl+C to stop."
echo "═══════════════════════════════════════════════════════"
echo ""

# Monitor logs - filter for emotion detection and matching
$ADB logcat -s KotlinModelRepository:I MainViewModel:I *:S | grep -E "(DETECTED EMOTION|CLOSEST EMOTION|Emotion:|emotion|KotlinModelRepository|MainViewModel)" --color=always

