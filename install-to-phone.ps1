# Install Gita App to Connected Phone (not emulator)
$ErrorActionPreference = "Stop"

Write-Host "`n=== Installing Gita App to Phone ===" -ForegroundColor Cyan
Write-Host ""

# Find ADB
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adbPath)) {
    Write-Host "❌ ADB not found at: $adbPath" -ForegroundColor Red
    Write-Host "Please check your Android SDK installation." -ForegroundColor Yellow
    exit 1
}

# Check for connected devices
Write-Host "Checking for connected devices..." -ForegroundColor Yellow
$devicesOutput = & $adbPath devices
$devices = $devicesOutput | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }

if ($devices.Count -eq 0) {
    Write-Host "❌ No devices found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please:" -ForegroundColor Yellow
    Write-Host "  1. Connect your phone via USB" -ForegroundColor White
    Write-Host "  2. Enable USB Debugging (Settings > Developer Options)" -ForegroundColor White
    Write-Host "  3. Accept the USB debugging prompt on your phone" -ForegroundColor White
    Write-Host ""
    Write-Host "Then run this script again." -ForegroundColor Yellow
    exit 1
}

# Filter out emulators, keep only physical devices
$physicalDevices = @()
foreach ($device in $devices) {
    $deviceId = ($device -split '\s+')[0]
    if ($deviceId -notmatch "^emulator-") {
        $physicalDevices += $deviceId
    }
}

if ($physicalDevices.Count -eq 0) {
    Write-Host "⚠️  Only emulator(s) found. No physical phone detected." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please connect your phone via USB and enable USB debugging." -ForegroundColor White
    exit 1
}

Write-Host "✅ Found $($physicalDevices.Count) phone(s):" -ForegroundColor Green
foreach ($device in $physicalDevices) {
    Write-Host "  - $device" -ForegroundColor White
}
Write-Host ""

# Build APK if needed
Write-Host "Building APK..." -ForegroundColor Yellow
& .\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}

# Install to first physical device
$targetDevice = $physicalDevices[0]
Write-Host ""
Write-Host "Installing to device: $targetDevice" -ForegroundColor Yellow

# Uninstall existing app first (to avoid signature conflicts)
Write-Host "Uninstalling existing app (if present)..." -ForegroundColor Yellow
& $adbPath -s $targetDevice uninstall com.gita.app 2>&1 | Out-Null

# Install new APK
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK not found at: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host "Installing APK..." -ForegroundColor Yellow
& $adbPath -s $targetDevice install -r $apkPath

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ App installed successfully on your phone!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Launching app..." -ForegroundColor Yellow
    & $adbPath -s $targetDevice shell am start -n com.gita.app/.MainActivity
    Write-Host ""
    Write-Host "✅ Done! Check your phone - the app should be launching now!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Installation failed" -ForegroundColor Red
    Write-Host ""
    Write-Host "Try:" -ForegroundColor Yellow
    Write-Host "  1. Make sure phone is unlocked" -ForegroundColor White
    Write-Host "  2. Check USB connection" -ForegroundColor White
    Write-Host "  3. Try manually: adb install $apkPath" -ForegroundColor White
    exit 1
}

