#!/bin/bash
# Bash script to view OpenAI API usage logs from Android device/emulator

echo "═══════════════════════════════════════════════════════"
echo "Gita App - OpenAI Token Usage Log Viewer"
echo "═══════════════════════════════════════════════════════"
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "ERROR: adb not found. Please ensure Android SDK platform-tools is in your PATH."
    echo ""
    echo "You can also view logs in Android Studio:"
    echo "  1. Open Android Studio"
    echo "  2. Run the app on emulator/device"
    echo "  3. Open Logcat (bottom panel)"
    echo "  4. Filter by: OpenAIEmbeddingsClient or OpenAIUsageTracker"
    echo "  5. Set log level to Info or Verbose"
    exit 1
fi

# Check for connected devices
echo "Checking for connected devices..."
DEVICES=$(adb devices | grep -E "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "ERROR: No device/emulator connected!"
    echo "Please connect a device or start an emulator first."
    exit 1
fi

echo "Found device(s):"
adb devices | grep -E "device$"
echo ""

# Clear previous logs
echo "Clearing old logs..."
adb logcat -c

echo ""
echo "═══════════════════════════════════════════════════════"
echo "Now monitoring logs. Use the app and make a query."
echo "Press Ctrl+C to stop."
echo "═══════════════════════════════════════════════════════"
echo ""

# Monitor logs with filtering for OpenAI usage
adb logcat -s OpenAIEmbeddingsClient:I OpenAIUsageTracker:I MainViewModel:I *:S

