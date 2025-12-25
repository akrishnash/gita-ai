# Build and Install Script for Gita Android App
# This script builds the debug APK and installs it to a connected Android device

param(
    [switch]$SkipBuild = $false,
    [switch]$UninstallFirst = $false
)

$ErrorActionPreference = "Stop"

Write-Host "`n=== Gita App - Build & Install ===" -ForegroundColor Cyan
Write-Host ""

# Set Android SDK path
$env:ANDROID_HOME = "C:/Users/anurag/AppData/Local/Android/Sdk"
$adbPath = "$env:ANDROID_HOME\platform-tools\adb.exe"

# Check if ADB exists
if (-not (Test-Path $adbPath)) {
    Write-Host "❌ ADB not found at: $adbPath" -ForegroundColor Red
    Write-Host "Please check your Android SDK installation." -ForegroundColor Yellow
    exit 1
}

# Check if device is connected
Write-Host "📱 Checking for connected devices..." -ForegroundColor Yellow
$devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }

if ($devices.Count -eq 0) {
    Write-Host "❌ No Android device found!" -ForegroundColor Red
    Write-Host "Please:" -ForegroundColor Yellow
    Write-Host "  1. Connect your phone via USB" -ForegroundColor White
    Write-Host "  2. Enable USB Debugging in Developer Options" -ForegroundColor White
    Write-Host "  3. Accept the USB debugging prompt on your phone" -ForegroundColor White
    exit 1
}

Write-Host "✅ Found $($devices.Count) device(s)" -ForegroundColor Green
& $adbPath devices

# Uninstall if requested
if ($UninstallFirst) {
    Write-Host "`n🗑️  Uninstalling existing app..." -ForegroundColor Yellow
    & $adbPath uninstall com.gita.app 2>&1 | Out-Null
    Write-Host "✅ Uninstalled" -ForegroundColor Green
}

# Build APK
if (-not $SkipBuild) {
    Write-Host "`n🔨 Building APK..." -ForegroundColor Yellow
    $buildResult = & .\gradlew.bat assembleDebug --no-daemon 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`n❌ Build failed!" -ForegroundColor Red
        $buildResult | Select-Object -Last 20
        exit 1
    }
    
    Write-Host "✅ Build successful!" -ForegroundColor Green
} else {
    Write-Host "`n⏭️  Skipping build (using existing APK)" -ForegroundColor Yellow
}

# Check if APK exists
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "`n❌ APK not found at: $apkPath" -ForegroundColor Red
    Write-Host "Please build the app first." -ForegroundColor Yellow
    exit 1
}

$apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "`n📦 APK Size: $apkSize MB" -ForegroundColor Cyan

# Install APK
Write-Host "`n📲 Installing to device..." -ForegroundColor Yellow
$installResult = & $adbPath install -r $apkPath 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Installation successful!" -ForegroundColor Green
    Write-Host "`n🚀 Launching app on device..." -ForegroundColor Yellow
    
    # Automatically launch the app
    & $adbPath shell am start -n com.gita.app/.MainActivity
    $LAUNCH_RESULT = $LASTEXITCODE
    
    if ($LAUNCH_RESULT -eq 0) {
        Write-Host "✅ App launched!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Could not auto-launch. Please open the app manually." -ForegroundColor Yellow
    }
    
    Write-Host "`n🎉 App is ready to use!" -ForegroundColor Green
    Write-Host "`nYou can now:" -ForegroundColor Cyan
    Write-Host "  - Test with input like 'i am sad'" -ForegroundColor White
    Write-Host "  - Make changes and run this script again" -ForegroundColor White
} else {
    Write-Host "`n❌ Installation failed!" -ForegroundColor Red
    $installResult
    exit 1
}

Write-Host ""

