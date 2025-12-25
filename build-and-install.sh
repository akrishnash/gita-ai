#!/bin/bash
# Build and Install Script for Gita Android App
# This script builds the debug APK and installs it to a connected Android device
# Works in Git Bash on Windows

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================"
echo "Gita Android - Build and Install"
echo "========================================${NC}"
echo ""

# Change to script directory
cd "$(dirname "$0")"

# Parse arguments
SKIP_BUILD=false
UNINSTALL_FIRST=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --uninstall-first)
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

# Try to get SDK location from local.properties
SDK_DIR=""
if [ -f "local.properties" ]; then
    SDK_DIR=$(grep "sdk.dir" local.properties | cut -d'=' -f2 | sed 's/\\/\\\\/g' | sed 's/^C:/\/c/' | sed 's/^E:/\/e/' | sed 's/^D:/\/d/' | sed 's/^F:/\/f/')
    # Convert Windows path to Git Bash path format
    SDK_DIR=$(echo "$SDK_DIR" | sed 's/\\/\//g')
fi

# If not found in local.properties, try common Windows locations
if [ -z "$SDK_DIR" ] || [ ! -d "$SDK_DIR" ]; then
    # Try common Windows SDK locations (Git Bash path format)
    if [ -d "/c/Users/$USER/AppData/Local/Android/Sdk" ]; then
        SDK_DIR="/c/Users/$USER/AppData/Local/Android/Sdk"
    elif [ -d "/e/Users/$USER/AppData/Local/Android/Sdk" ]; then
        SDK_DIR="/e/Users/$USER/AppData/Local/Android/Sdk"
    elif [ -d "/c/Users/anurag/AppData/Local/Android/Sdk" ]; then
        SDK_DIR="/c/Users/anurag/AppData/Local/Android/Sdk"
    elif [ -d "/e/Users/anurag/AppData/Local/Android/Sdk" ]; then
        SDK_DIR="/e/Users/anurag/AppData/Local/Android/Sdk"
    fi
fi

# If still not found, try ANDROID_HOME
if [ -z "$SDK_DIR" ] || [ ! -d "$SDK_DIR" ]; then
    if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
        SDK_DIR="$ANDROID_HOME"
        # Convert to Git Bash path if needed
        SDK_DIR=$(echo "$SDK_DIR" | sed 's/^C:/\/c/' | sed 's/^E:/\/e/' | sed 's/^D:/\/d/' | sed 's/^F:/\/f/' | sed 's/\\/\//g')
    fi
fi

if [ -z "$SDK_DIR" ] || [ ! -d "$SDK_DIR" ]; then
    echo -e "${RED}❌ Android SDK not found!${NC}"
    echo "Please:"
    echo "  1. Set sdk.dir in local.properties, OR"
    echo "  2. Set ANDROID_HOME environment variable"
    exit 1
fi

echo -e "${GREEN}✅ Android SDK found: $SDK_DIR${NC}"

# Set ADB path (use .exe for Windows)
ADB="$SDK_DIR/platform-tools/adb.exe"
if [ ! -f "$ADB" ]; then
    # Try without .exe (for Linux/Mac)
    ADB="$SDK_DIR/platform-tools/adb"
fi

if [ ! -f "$ADB" ]; then
    echo -e "${RED}❌ ADB not found at: $SDK_DIR/platform-tools/${NC}"
    exit 1
fi

# Add Android SDK tools to PATH
export PATH="$SDK_DIR/platform-tools:$SDK_DIR/emulator:$SDK_DIR/tools:$SDK_DIR/tools/bin:$PATH"

# Convert Git Bash path back to Windows format for Gradle/ANDROID_HOME
# Gradle needs Windows paths, not Git Bash paths
WINDOWS_SDK_DIR=$(echo "$SDK_DIR" | sed 's|^/c/|C:/|' | sed 's|^/e/|E:/|' | sed 's|^/d/|D:/|' | sed 's|^/f/|F:/|' | sed 's|/|/|g')
export ANDROID_HOME="$WINDOWS_SDK_DIR"

# Ensure local.properties has Windows path format (Gradle reads this)
if [ -f "local.properties" ]; then
    # Read current value
    CURRENT_SDK=$(grep "^sdk.dir=" local.properties | cut -d'=' -f2 | head -1)
    # Normalize current value (remove backslashes, ensure forward slashes)
    CURRENT_SDK_NORM=$(echo "$CURRENT_SDK" | sed 's|\\|/|g')
    WINDOWS_SDK_NORM=$(echo "$WINDOWS_SDK_DIR" | sed 's|\\|/|g')
    
    # Update if different
    if [ "$CURRENT_SDK_NORM" != "$WINDOWS_SDK_NORM" ]; then
        echo "sdk.dir=$WINDOWS_SDK_DIR" > local.properties
        echo -e "${YELLOW}Updated local.properties with SDK path${NC}"
    fi
else
    # Create local.properties if it doesn't exist
    echo "sdk.dir=$WINDOWS_SDK_DIR" > local.properties
    echo -e "${YELLOW}Created local.properties with SDK path${NC}"
fi

echo -e "${CYAN}ANDROID_HOME=$ANDROID_HOME${NC}"

# Check if device is connected
echo ""
echo -e "${YELLOW}📱 Checking for connected Android device...${NC}"
DEVICES=$("$ADB" devices 2>/dev/null || echo "")
echo ""
echo "Connected devices:"
echo "$DEVICES"
echo ""

# Check if any device is in "device" state (ready)
if ! echo "$DEVICES" | grep -q "device$"; then
    echo -e "${YELLOW}⚠️  No ready device or emulator found!${NC}"
    echo ""
    
    # Check if device is offline or unauthorized
    if echo "$DEVICES" | grep -q "offline"; then
        echo "Device is offline. Waiting for emulator to boot..."
        echo "Please wait a few more seconds for the emulator to fully boot."
    elif echo "$DEVICES" | grep -q "unauthorized"; then
        echo "Device is unauthorized. Please accept the USB debugging prompt on the device."
    elif echo "$DEVICES" | grep -q "emulator"; then
        echo "Emulator detected but not ready yet. Waiting..."
        echo "Please wait for the emulator to fully boot (check the emulator window)."
    else
        echo "Please start an emulator or connect a device:"
        echo "  - Connect your phone via USB"
        echo "  - Enable USB Debugging in Developer Options"
        echo "  - Accept the USB debugging prompt on your phone"
    fi
    
    echo ""
    echo "Try running this script again once a device is ready."
    exit 1
fi

echo -e "${GREEN}✅ Device found!${NC}"

# Uninstall if requested
if [ "$UNINSTALL_FIRST" = true ]; then
    echo ""
    echo -e "${YELLOW}🗑️  Uninstalling existing app...${NC}"
    "$ADB" uninstall com.gita.app 2>/dev/null || true
    echo -e "${GREEN}✅ Uninstalled${NC}"
fi

# Build APK
if [ "$SKIP_BUILD" = false ]; then
    echo ""
    echo -e "${YELLOW}🔨 Building debug APK...${NC}"
    
    # Ensure local.properties exists and is correct before building
    if [ -f "local.properties" ]; then
        echo -e "${CYAN}local.properties content:${NC}"
        cat local.properties
        echo ""
    fi
    
    # Export ANDROID_HOME for this session
    export ANDROID_HOME="$WINDOWS_SDK_DIR"
    
    # Set ANDROID_HOME for the build process
    export ANDROID_HOME="$WINDOWS_SDK_DIR"
    
    # Verify local.properties one more time
    if [ -f "local.properties" ]; then
        # Ensure it has the correct format (forward slashes, no backslashes)
        SDK_PROP=$(echo "$WINDOWS_SDK_DIR" | sed 's|\\|/|g')
        echo "sdk.dir=$SDK_PROP" > local.properties
    fi
    
    if [ -f "./gradlew" ]; then
        ANDROID_HOME="$WINDOWS_SDK_DIR" ./gradlew assembleDebug
    elif [ -f "./gradlew.bat" ]; then
        # On Windows Git Bash, use cmd.exe to run gradlew.bat with environment
        # Convert path separators for Windows CMD
        WIN_SDK_CMD=$(echo "$WINDOWS_SDK_DIR" | sed 's|/|\\|g')
        # Run build in background and capture output
        cmd.exe /c "set ANDROID_HOME=$WIN_SDK_CMD && gradlew.bat assembleDebug" 2>&1
        BUILD_RESULT=$?
    else
        echo -e "${YELLOW}Gradle wrapper not found, using system gradle...${NC}"
        ANDROID_HOME="$WINDOWS_SDK_DIR" gradle assembleDebug
        BUILD_RESULT=$?
    fi
    
    if [ ${BUILD_RESULT:-$?} -ne 0 ]; then
        echo ""
        echo -e "${RED}❌ BUILD FAILED! Check errors above.${NC}"
        exit 1
    fi
    
    echo ""
    echo -e "${GREEN}✅ Build successful!${NC}"
else
    echo ""
    echo -e "${YELLOW}⏭️  Skipping build (using existing APK)${NC}"
fi

# Check if APK exists
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo -e "\n${RED}❌ APK not found at: $APK_PATH${NC}"
    echo "Please build the app first."
    exit 1
fi

APK_SIZE=$(du -h "$APK_PATH" 2>/dev/null | cut -f1 || echo "unknown")
echo ""
echo -e "${CYAN}📦 APK Size: $APK_SIZE${NC}"

# Install APK using adb (more reliable in Git Bash)
echo ""
echo -e "${YELLOW}📲 Installing APK on device...${NC}"

# Use adb install directly (more reliable than gradlew installDebug in Git Bash)
echo "Installing $APK_PATH..."
"$ADB" install -r "$APK_PATH"
INSTALL_RESULT=$?

# If adb install fails, try gradlew installDebug as fallback
if [ $INSTALL_RESULT -ne 0 ]; then
    echo -e "${YELLOW}Trying gradlew installDebug as fallback...${NC}"
    if [ -f "./gradlew" ]; then
        ANDROID_HOME="$WINDOWS_SDK_DIR" ./gradlew installDebug
        INSTALL_RESULT=$?
    elif [ -f "./gradlew.bat" ]; then
        WIN_SDK_CMD=$(echo "$WINDOWS_SDK_DIR" | sed 's|/|\\|g')
        cmd.exe /c "set ANDROID_HOME=$WIN_SDK_CMD && gradlew.bat installDebug" 2>&1
        INSTALL_RESULT=$?
    fi
fi

if [ $INSTALL_RESULT -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================"
    echo "SUCCESS! App installed and ready to launch"
    echo "========================================${NC}"
    echo ""
    
    # Automatically launch the app
    echo -e "${YELLOW}🚀 Launching app on device...${NC}"
    "$ADB" shell am start -n com.gita.app/.MainActivity
    LAUNCH_RESULT=$?
    
    if [ $LAUNCH_RESULT -eq 0 ]; then
        echo -e "${GREEN}✅ App launched!${NC}"
    else
        echo -e "${YELLOW}⚠️  Could not auto-launch. Please open the app manually.${NC}"
    fi
    
    echo ""
    echo -e "${CYAN}You can now:${NC}"
    echo "  - Test with input like 'i am sad'"
    echo "  - Make changes and run this script again"
    echo ""
else
    echo ""
    echo -e "${RED}❌ INSTALLATION FAILED! Check errors above.${NC}"
    exit 1
fi
