# Build and Install Script for Gita Android App - Emulator Version
# This script builds the debug APK and installs it to an Android emulator
# It will automatically start an emulator if none is running

param(
    [switch]$SkipBuild = $false,
    [switch]$UninstallFirst = $false,
    [string]$AvdName = "",
    [switch]$WaitForBoot = $true
)

$ErrorActionPreference = "Stop"

Write-Host "`n=== Gita App - Build & Install on Emulator ===" -ForegroundColor Cyan
Write-Host ""

# Set Android SDK path (auto-detect)
$env:ANDROID_HOME = $null
$possiblePaths = @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk",
    "C:/Users/anurag/AppData/Local/Android/Sdk"
)
foreach ($path in $possiblePaths) {
    if (Test-Path "$path\platform-tools\adb.exe") {
        $env:ANDROID_HOME = $path
        break
    }
}

if ($env:ANDROID_HOME -eq $null -or $env:ANDROID_HOME -eq "") {
    Write-Host "ERROR: ANDROID_HOME not found!" -ForegroundColor Red
    Write-Host "Please set ANDROID_HOME environment variable or update the path in this script." -ForegroundColor Yellow
    exit 1
}

$adbPath = "$env:ANDROID_HOME\platform-tools\adb.exe"
$emulatorPath = "$env:ANDROID_HOME\emulator\emulator.exe"

# Check if ADB exists
if (-not (Test-Path $adbPath)) {
    Write-Host "ERROR: ADB not found at: $adbPath" -ForegroundColor Red
    Write-Host "Please check your Android SDK installation." -ForegroundColor Yellow
    exit 1
}

# Function to wait for device to be ready
function Wait-ForDevice {
    param([int]$MaxWaitSeconds = 120)
    $elapsed = 0
    Write-Host "Waiting for emulator to be ready..." -ForegroundColor Yellow
    while ($elapsed -lt $MaxWaitSeconds) {
        $devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }
        if ($devices.Count -gt 0) {
            # Check if device is fully booted
            $bootComplete = & $adbPath shell getprop sys.boot_completed 2>&1
            if ($bootComplete -eq "1") {
                Write-Host "Emulator is ready!" -ForegroundColor Green
                return $true
            }
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
    Write-Host ""
    Write-Host "WARNING: Timeout waiting for emulator to boot" -ForegroundColor Yellow
    return $false
}

# Check if device/emulator is connected
Write-Host "Checking for connected devices/emulators..." -ForegroundColor Yellow
$devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }

if ($devices.Count -eq 0) {
    Write-Host "No device/emulator found!" -ForegroundColor Red
    
    # Check if emulator executable exists
    if (-not (Test-Path $emulatorPath)) {
        Write-Host "ERROR: Emulator not found at: $emulatorPath" -ForegroundColor Red
        Write-Host "Please install Android Emulator via Android Studio SDK Manager" -ForegroundColor Yellow
        exit 1
    }
    
    # List available AVDs
    Write-Host "`nAvailable Android Virtual Devices (AVDs):" -ForegroundColor Cyan
    $avds = & $emulatorPath -list-avds
    if ($avds.Count -eq 0) {
        Write-Host "ERROR: No AVDs found! Please create one in Android Studio:" -ForegroundColor Red
        Write-Host "   1. Open Android Studio" -ForegroundColor White
        Write-Host "   2. Tools > Device Manager" -ForegroundColor White
        Write-Host "   3. Create Virtual Device" -ForegroundColor White
        exit 1
    }
    
    foreach ($avd in $avds) {
        Write-Host "   - $avd" -ForegroundColor White
    }
    
    # Select AVD to use
    if ($AvdName -eq "") {
        if ($avds -is [System.Array]) {
            $AvdName = $avds[0]
        } else {
            $AvdName = $avds
        }
        Write-Host "`nUsing first available AVD: $AvdName" -ForegroundColor Cyan
    } else {
        if ($avds -notcontains $AvdName) {
            Write-Host "ERROR: AVD '$AvdName' not found!" -ForegroundColor Red
            exit 1
        }
        Write-Host "`nStarting AVD: $AvdName" -ForegroundColor Cyan
    }
    
    # Start emulator in background
    Write-Host "`nStarting emulator (this may take a minute)..." -ForegroundColor Yellow
    Write-Host "   You can close this window if needed - emulator will continue running" -ForegroundColor Gray
    
    Start-Process -FilePath $emulatorPath -ArgumentList "-avd", $AvdName, "-no-snapshot-save" -WindowStyle Normal
    
    if ($WaitForBoot) {
        if (-not (Wait-ForDevice)) {
            Write-Host "Continuing anyway - emulator may still be booting" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Skipping boot wait (use -WaitForBoot to wait for full boot)" -ForegroundColor Yellow
        Write-Host "   Waiting 10 seconds for emulator to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
    }
} else {
    Write-Host "Found $($devices.Count) device(s)/emulator(s)" -ForegroundColor Green
    & $adbPath devices
}

# Verify device is accessible
$devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }
if ($devices.Count -eq 0) {
    Write-Host "ERROR: Still no device available!" -ForegroundColor Red
    exit 1
}

# Uninstall if requested
if ($UninstallFirst) {
    Write-Host "`nUninstalling existing app..." -ForegroundColor Yellow
    & $adbPath uninstall com.gita.app 2>&1 | Out-Null
    Write-Host "Uninstalled" -ForegroundColor Green
}

# Build APK
if (-not $SkipBuild) {
    Write-Host "`nBuilding APK..." -ForegroundColor Yellow
    $buildResult = & .\gradlew.bat assembleDebug --no-daemon 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`nBuild failed!" -ForegroundColor Red
        $buildResult | Select-Object -Last 30
        exit 1
    }
    
    Write-Host "Build successful!" -ForegroundColor Green
} else {
    Write-Host "`nSkipping build (using existing APK)" -ForegroundColor Yellow
}

# Check if APK exists
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "`nERROR: APK not found at: $apkPath" -ForegroundColor Red
    Write-Host "Please build the app first (remove -SkipBuild flag)." -ForegroundColor Yellow
    exit 1
}

$apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "`nAPK Size: $apkSize MB" -ForegroundColor Cyan

# Install APK
Write-Host "`nInstalling to emulator..." -ForegroundColor Yellow
$installResult = & $adbPath install -r $apkPath 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nInstallation successful!" -ForegroundColor Green
    Write-Host "`nLaunching app on emulator..." -ForegroundColor Yellow
    
    # Small delay to ensure installation completed
    Start-Sleep -Seconds 1
    
    # Automatically launch the app
    & $adbPath shell am start -n com.gita.app/.MainActivity 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "App launched!" -ForegroundColor Green
    } else {
        Write-Host "WARNING: Could not auto-launch. Please open the app manually on the emulator." -ForegroundColor Yellow
    }
    
    Write-Host "`nApp is ready to test on emulator!" -ForegroundColor Green
    Write-Host "`nYou can now:" -ForegroundColor Cyan
    Write-Host "  - Test the ML model matching with queries like 'I feel anxious' or 'How to deal with stress'" -ForegroundColor White
    Write-Host "  - Test the offline fallback by disconnecting internet" -ForegroundColor White
    Write-Host "  - Check Settings to configure OpenAI API key" -ForegroundColor White
    Write-Host "  - View History of past queries" -ForegroundColor White
    Write-Host "`nTips:" -ForegroundColor Cyan
    Write-Host "  - To run faster next time, use: .\run-on-emulator.ps1 -SkipBuild" -ForegroundColor White
    Write-Host "  - To specify an AVD: .\run-on-emulator.ps1 -AvdName Pixel_5_API_33" -ForegroundColor White
} else {
    Write-Host "`nInstallation failed!" -ForegroundColor Red
    $installResult
    exit 1
}

Write-Host ""
