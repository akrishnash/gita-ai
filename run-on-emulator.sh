#!/bin/bash
# Build and Install Script for Gita Android App - Emulator Version
# This script builds the debug APK and installs it to an Android emulator
# It will automatically start an emulator if none is running

# Default options
SKIP_BUILD=false
UNINSTALL_FIRST=false
AVD_NAME=""
WAIT_FOR_BOOT=true

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
        -AvdName|--avd-name)
            AVD_NAME="$2"
            shift 2
            ;;
        -WaitForBoot|--wait-for-boot)
            WAIT_FOR_BOOT=true
            shift
            ;;
        -NoWait|--no-wait)
            WAIT_FOR_BOOT=false
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--skip-build] [--uninstall-first] [--avd-name AVD_NAME] [--no-wait]"
            exit 1
            ;;
    esac
done

echo ""
echo "=== Gita App - Build & Install on Emulator ==="
echo ""

# Change to script directory to ensure relative paths work
cd "$(dirname "$0")"

# Auto-detect Android SDK
ANDROID_HOME=""

# Try Windows-style paths (for Git Bash on Windows)
if [ -n "$LOCALAPPDATA" ]; then
    ANDROID_HOME="$LOCALAPPDATA/Android/Sdk"
fi

if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME/platform-tools" ]; then
    if [ -n "$USERPROFILE" ]; then
        ANDROID_HOME="$USERPROFILE/AppData/Local/Android/Sdk"
    fi
fi

if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME/platform-tools" ]; then
    if [ -n "$HOME" ]; then
        ANDROID_HOME="$HOME/AppData/Local/Android/Sdk"
    fi
fi

# Try Unix-style paths
if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME/platform-tools" ]; then
    if [ -d "$HOME/.android/sdk" ]; then
        ANDROID_HOME="$HOME/.android/sdk"
    fi
fi

# Check if ANDROID_HOME is set via environment variable
if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME/platform-tools" ]; then
    if [ -n "${ANDROID_HOME}" ] && [ -d "${ANDROID_HOME}/platform-tools" ]; then
        # Use environment variable (will be converted below)
        :
    else
        echo "ERROR: ANDROID_HOME not found!"
        echo "Please set ANDROID_HOME environment variable or install Android SDK"
        exit 1
    fi
fi

# Convert Windows path to Git Bash path if needed
# Handle all drive letters (A-Z)
if [[ "$ANDROID_HOME" == *"\\"* ]] || [[ "$ANDROID_HOME" =~ ^[A-Za-z]: ]]; then
    ANDROID_HOME=$(echo "$ANDROID_HOME" | sed 's|\\|/|g')
    # Convert drive letters to lowercase Git Bash format (C: -> /c, E: -> /e, etc.)
    if [[ "$ANDROID_HOME" =~ ^([A-Za-z]): ]]; then
        DRIVE_LETTER=$(echo "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')
        ANDROID_HOME=$(echo "$ANDROID_HOME" | sed "s|^${BASH_REMATCH[1]}:|/${DRIVE_LETTER}|")
    fi
fi

ADB_PATH="$ANDROID_HOME/platform-tools/adb"
EMULATOR_PATH="$ANDROID_HOME/emulator/emulator"

# Convert paths for Windows Git Bash (handle all drive letters)
if [[ "$ADB_PATH" =~ ^([A-Za-z]): ]] || [[ "$ADB_PATH" == *"\\"* ]]; then
    ADB_PATH=$(echo "$ADB_PATH" | sed 's|\\|/|g')
    if [[ "$ADB_PATH" =~ ^([A-Za-z]): ]]; then
        DRIVE_LETTER=$(echo "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')
        ADB_PATH=$(echo "$ADB_PATH" | sed "s|^${BASH_REMATCH[1]}:|/${DRIVE_LETTER}|")
    fi
fi
if [[ "$EMULATOR_PATH" =~ ^([A-Za-z]): ]] || [[ "$EMULATOR_PATH" == *"\\"* ]]; then
    EMULATOR_PATH=$(echo "$EMULATOR_PATH" | sed 's|\\|/|g')
    if [[ "$EMULATOR_PATH" =~ ^([A-Za-z]): ]]; then
        DRIVE_LETTER=$(echo "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')
        EMULATOR_PATH=$(echo "$EMULATOR_PATH" | sed "s|^${BASH_REMATCH[1]}:|/${DRIVE_LETTER}|")
    fi
fi

# Check if ADB exists
if [ ! -f "$ADB_PATH" ] && [ ! -f "$ADB_PATH.exe" ]; then
    echo "ERROR: ADB not found at: $ADB_PATH"
    echo "Please check your Android SDK installation."
    exit 1
fi

# Use .exe extension on Windows
if [ ! -f "$ADB_PATH" ]; then
    ADB_PATH="${ADB_PATH}.exe"
fi
if [ ! -f "$EMULATOR_PATH" ]; then
    EMULATOR_PATH="${EMULATOR_PATH}.exe"
fi

# Convert Git Bash path back to Windows format for Gradle/ANDROID_HOME
# Gradle needs Windows paths in local.properties, not Git Bash paths
WINDOWS_ANDROID_HOME="$ANDROID_HOME"
# Convert Git Bash paths (e.g., /e/path -> E:/path)
if [[ "$ANDROID_HOME" =~ ^/([a-z])/ ]]; then
    DRIVE_UPPER=$(echo "${BASH_REMATCH[1]}" | tr '[:lower:]' '[:upper:]')
    WINDOWS_ANDROID_HOME=$(echo "$ANDROID_HOME" | sed "s|^/${BASH_REMATCH[1]}/|${DRIVE_UPPER}:/|" | sed 's|/|/|g')
elif [[ "$ANDROID_HOME" =~ ^([A-Za-z]): ]]; then
    # Already in Windows format, just ensure forward slashes
    WINDOWS_ANDROID_HOME=$(echo "$ANDROID_HOME" | sed 's|\\|/|g')
fi

# Ensure local.properties has Windows path format (Gradle reads this)
if [ -f "local.properties" ]; then
    # Read current value
    CURRENT_SDK=$(grep "^sdk.dir=" local.properties 2>/dev/null | cut -d'=' -f2 | head -1)
    # Normalize current value (remove backslashes, ensure forward slashes)
    CURRENT_SDK_NORM=$(echo "$CURRENT_SDK" | sed 's|\\|/|g' | sed 's|/$||')
    WINDOWS_SDK_NORM=$(echo "$WINDOWS_ANDROID_HOME" | sed 's|\\|/|g' | sed 's|/$||')
    
    # Update if different
    if [ "$CURRENT_SDK_NORM" != "$WINDOWS_SDK_NORM" ]; then
        echo "sdk.dir=$WINDOWS_ANDROID_HOME" > local.properties
        echo "Updated local.properties with SDK path: $WINDOWS_ANDROID_HOME"
    fi
else
    # Create local.properties if it doesn't exist
    echo "sdk.dir=$WINDOWS_ANDROID_HOME" > local.properties
    echo "Created local.properties with SDK path: $WINDOWS_ANDROID_HOME"
fi

# Note: Gradle will read SDK location from local.properties
# We keep ANDROID_HOME in Git Bash format for script's internal use (ADB_PATH, EMULATOR_PATH)

# Function to wait for device to be ready
wait_for_device() {
    local max_wait=120
    local elapsed=0
    echo "Waiting for emulator to be ready..."
    
    while [ $elapsed -lt $max_wait ]; do
        local devices=$("$ADB_PATH" devices | grep -E "device$" | wc -l)
        if [ "$devices" -gt 0 ]; then
            local boot_complete=$("$ADB_PATH" shell getprop sys.boot_completed 2>/dev/null)
            if [ "$boot_complete" = "1" ]; then
                echo "Emulator is ready!"
                return 0
            fi
        fi
        sleep 2
        elapsed=$((elapsed + 2))
        echo -n "."
    done
    
    echo ""
    echo "WARNING: Timeout waiting for emulator to boot"
    return 1
}

# Check if device/emulator is connected
echo "Checking for connected devices/emulators..."
DEVICES=$("$ADB_PATH" devices | grep -E "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "No device/emulator found!"
    
    # Check if emulator executable exists
    if [ ! -f "$EMULATOR_PATH" ]; then
        echo "ERROR: Emulator not found at: $EMULATOR_PATH"
        echo "Please install Android Emulator via Android Studio SDK Manager"
        exit 1
    fi
    
    # List available AVDs
    echo ""
    echo "Available Android Virtual Devices (AVDs):"
    AVDS=$("$EMULATOR_PATH" -list-avds)
    
    if [ -z "$AVDS" ]; then
        echo "ERROR: No AVDs found! Please create one in Android Studio:"
        echo "   1. Open Android Studio"
        echo "   2. Tools > Device Manager"
        echo "   3. Create Virtual Device"
        exit 1
    fi
    
    echo "$AVDS" | while read -r avd; do
        echo "   - $avd"
    done
    
    # Select AVD to use
    if [ -z "$AVD_NAME" ]; then
        AVD_NAME=$(echo "$AVDS" | head -n 1)
        echo ""
        echo "Using first available AVD: $AVD_NAME"
    else
        if ! echo "$AVDS" | grep -q "^${AVD_NAME}$"; then
            echo "ERROR: AVD '$AVD_NAME' not found!"
            exit 1
        fi
        echo ""
        echo "Starting AVD: $AVD_NAME"
    fi
    
    # Start emulator in background
    echo ""
    echo "Starting emulator (this may take a minute)..."
    echo "   You can close this window if needed - emulator will continue running"
    
    # Start emulator (Windows)
    if command -v cmd.exe >/dev/null 2>&1; then
        # Convert Git Bash path back to Windows path for cmd.exe
        WIN_EMULATOR_PATH="$EMULATOR_PATH"
        # Handle Git Bash paths (e.g., /e/path -> E:\path)
        if [[ "$EMULATOR_PATH" =~ ^/([a-z])/ ]]; then
            DRIVE_UPPER=$(echo "${BASH_REMATCH[1]}" | tr '[:lower:]' '[:upper:]')
            WIN_EMULATOR_PATH=$(echo "$EMULATOR_PATH" | sed "s|^/${BASH_REMATCH[1]}/|${DRIVE_UPPER}:\\\\|" | sed 's|/|\\|g')
        else
            # Already in Windows format or Unix format, just convert slashes
            WIN_EMULATOR_PATH=$(echo "$EMULATOR_PATH" | sed 's|/|\\|g')
        fi
        cmd.exe //c start "" "$WIN_EMULATOR_PATH" -avd "$AVD_NAME" -no-snapshot-save
    else
        "$EMULATOR_PATH" -avd "$AVD_NAME" -no-snapshot-save &
    fi
    
    if [ "$WAIT_FOR_BOOT" = true ]; then
        if ! wait_for_device; then
            echo "Continuing anyway - emulator may still be booting"
        fi
    else
        echo "Skipping boot wait"
        echo "   Waiting 10 seconds for emulator to start..."
        sleep 10
    fi
else
    echo "Found device(s)/emulator(s)"
    "$ADB_PATH" devices
fi

# Verify device is accessible
DEVICES=$("$ADB_PATH" devices | grep -E "device$" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "ERROR: Still no device available!"
    exit 1
fi

# Uninstall if requested
if [ "$UNINSTALL_FIRST" = true ]; then
    echo ""
    echo "Uninstalling existing app..."
    "$ADB_PATH" uninstall com.gita.app >/dev/null 2>&1
    echo "Uninstalled"
fi

# Build APK
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
else
    echo ""
    echo "Skipping build (using existing APK)"
fi

# Check if APK exists
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo ""
    echo "ERROR: APK not found at: $APK_PATH"
    echo "Please build the app first (remove --skip-build flag)."
    exit 1
fi

# Get APK size (cross-platform)
# Try du first (works on most systems including Git Bash)
if command -v du >/dev/null 2>&1; then
    APK_SIZE=$(du -h "$APK_PATH" 2>/dev/null | cut -f1)
    if [ -z "$APK_SIZE" ]; then
        # If du -h doesn't work, try du without -h and convert manually
        APK_BYTES=$(du "$APK_PATH" 2>/dev/null | cut -f1)
        if [ -n "$APK_BYTES" ] && [ "$APK_BYTES" -gt 0 ]; then
            # Convert KB to MB if needed
            if [ "$APK_BYTES" -gt 1024 ]; then
                APK_SIZE="$((APK_BYTES / 1024))MB"
            else
                APK_SIZE="${APK_BYTES}KB"
            fi
        else
            APK_SIZE="unknown"
        fi
    fi
else
    # Fallback: use stat if available
    APK_BYTES=$(stat -c%s "$APK_PATH" 2>/dev/null || stat -f%z "$APK_PATH" 2>/dev/null || echo "0")
    if [ "$APK_BYTES" != "0" ] && [ -n "$APK_BYTES" ]; then
        if [ "$APK_BYTES" -gt 1048576 ]; then
            APK_SIZE="$((APK_BYTES / 1048576))MB"
        elif [ "$APK_BYTES" -gt 1024 ]; then
            APK_SIZE="$((APK_BYTES / 1024))KB"
        else
            APK_SIZE="${APK_BYTES}B"
        fi
    else
        APK_SIZE="unknown"
    fi
fi
echo ""
echo "APK Size: $APK_SIZE"

# Install APK
echo ""
echo "Installing to emulator..."
if "$ADB_PATH" install -r "$APK_PATH"; then
    echo ""
    echo "Installation successful!"
    echo ""
    echo "Launching app on emulator..."
    
    sleep 1
    "$ADB_PATH" shell am start -n com.gita.app/.MainActivity >/dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "App launched!"
    else
        echo "WARNING: Could not auto-launch. Please open the app manually on the emulator."
    fi
    
    echo ""
    echo "App is ready to test on emulator!"
    echo ""
    echo "You can now:"
    echo "  - Test the ML model matching with queries like 'I feel anxious' or 'How to deal with stress'"
    echo "  - Test the offline fallback by disconnecting internet"
    echo "  - Check Settings to configure OpenAI API key"
    echo "  - View History of past queries"
    echo ""
    echo "Tips:"
    echo "  - To run faster next time, use: ./run-on-emulator.sh --skip-build"
    echo "  - To specify an AVD: ./run-on-emulator.sh --avd-name Pixel_8"
else
    echo ""
    echo "Installation failed!"
    exit 1
fi

echo ""

