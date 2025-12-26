# Quick test script - Just build and install if emulator/device is already running
# Faster for repeated testing

param(
    [switch]$SkipBuild = $false,
    [switch]$UninstallFirst = $false
)

$ErrorActionPreference = "Stop"

Write-Host "`n=== Gita App - Quick Test ===" -ForegroundColor Cyan
Write-Host ""

# Auto-detect Android SDK
$env:ANDROID_HOME = $null
$possiblePaths = @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
)
foreach ($path in $possiblePaths) {
    if (Test-Path "$path\platform-tools\adb.exe") {
        $env:ANDROID_HOME = $path
        break
    }
}

if ($env:ANDROID_HOME -eq $null) {
    Write-Host "ERROR: Android SDK not found!" -ForegroundColor Red
    exit 1
}

$adbPath = "$env:ANDROID_HOME\platform-tools\adb.exe"

# Check for device/emulator
Write-Host "Checking for connected device/emulator..." -ForegroundColor Yellow
$devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }

if ($devices.Count -eq 0) {
    Write-Host "ERROR: No device/emulator found!" -ForegroundColor Red
    Write-Host "Please start an emulator or connect a device first." -ForegroundColor Yellow
    Write-Host "You can use: .\run-on-emulator.ps1 to start an emulator automatically." -ForegroundColor Yellow
    exit 1
}

Write-Host "Found device(s)/emulator(s)" -ForegroundColor Green
& $adbPath devices

# Uninstall if requested
if ($UninstallFirst) {
    Write-Host "`nUninstalling existing app..." -ForegroundColor Yellow
    & $adbPath uninstall com.gita.app 2>&1 | Out-Null
    Write-Host "Uninstalled" -ForegroundColor Green
}

# Build if needed
if (-not $SkipBuild) {
    Write-Host "`nBuilding APK..." -ForegroundColor Yellow
    $buildResult = & .\gradlew.bat assembleDebug --no-daemon 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`nBuild failed!" -ForegroundColor Red
        $buildResult | Select-Object -Last 20
        exit 1
    }
    
    Write-Host "Build successful!" -ForegroundColor Green
}

# Install
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "`nERROR: APK not found! Please build first." -ForegroundColor Red
    exit 1
}

Write-Host "`nInstalling..." -ForegroundColor Yellow
& $adbPath install -r $apkPath 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "Installation successful!" -ForegroundColor Green
    
    Start-Sleep -Seconds 1
    & $adbPath shell am start -n com.gita.app/.MainActivity 2>&1 | Out-Null
    
    Write-Host "App launched!" -ForegroundColor Green
    Write-Host "`nReady to test!" -ForegroundColor Cyan
} else {
    Write-Host "Installation failed!" -ForegroundColor Red
    exit 1
}


