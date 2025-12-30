@echo off
REM View emotion detection logs from Gita app (Windows)

echo ========================================
echo Gita App - Emotion Detection Log Viewer
echo ========================================
echo.

REM Find ADB
set ADB_PATH=
if defined ANDROID_HOME (
    set ADB_PATH=%ANDROID_HOME%\platform-tools\adb.exe
) else if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
) else (
    where adb >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        set ADB_PATH=adb
    ) else (
        echo ERROR: adb not found!
        echo.
        echo Please set ANDROID_HOME or ensure adb is in your PATH.
        pause
        exit /b 1
    )
)

REM Check for connected devices
echo Checking for connected devices...
"%ADB_PATH%" devices | findstr "device$" >nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No device/emulator connected!
    echo Please connect a device or start an emulator first.
    pause
    exit /b 1
)

echo Found device(s):
"%ADB_PATH%" devices | findstr "device$"
echo.

REM Ask if user wants to clear logs
set /p CLEAR_LOGS="Clear previous logs? (y/n): "
if /i "%CLEAR_LOGS%"=="y" (
    echo Clearing old logs...
    "%ADB_PATH%" logcat -c
    echo Logs cleared
    echo.
)

echo ========================================
echo Monitoring emotion detection logs...
echo Use the app and enter a query to see detected emotions.
echo Press Ctrl+C to stop.
echo ========================================
echo.

REM Monitor logs - filter for emotion detection
"%ADB_PATH%" logcat -s KotlinModelRepository:I MainViewModel:I *:S

pause

