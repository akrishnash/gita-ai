# PowerShell script to view OpenAI API usage logs from Android device/emulator

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Gita App - OpenAI Token Usage Log Viewer" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Check if adb is available
$adbPath = "adb"
try {
    $null = & $adbPath version 2>&1
} catch {
    Write-Host "ERROR: adb not found. Please ensure Android SDK platform-tools is in your PATH." -ForegroundColor Red
    Write-Host ""
    Write-Host "You can also view logs in Android Studio:" -ForegroundColor Yellow
    Write-Host "  1. Open Android Studio" -ForegroundColor White
    Write-Host "  2. Run the app on emulator/device" -ForegroundColor White
    Write-Host "  3. Open Logcat (bottom panel)" -ForegroundColor White
    Write-Host "  4. Filter by: OpenAIEmbeddingsClient or OpenAIUsageTracker" -ForegroundColor White
    Write-Host "  5. Set log level to Info or Verbose" -ForegroundColor White
    exit 1
}

# Check for connected devices
Write-Host "Checking for connected devices..." -ForegroundColor Yellow
$devices = & $adbPath devices 2>&1 | Select-String "device$"

if ($devices.Count -eq 0) {
    Write-Host "ERROR: No device/emulator connected!" -ForegroundColor Red
    Write-Host "Please connect a device or start an emulator first." -ForegroundColor Yellow
    exit 1
}

Write-Host "Found device(s):" -ForegroundColor Green
$devices | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
Write-Host ""

# Clear previous logs
Write-Host "Clearing old logs..." -ForegroundColor Yellow
& $adbPath logcat -c 2>&1 | Out-Null

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "Now monitoring logs. Use the app and make a query." -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop." -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Monitor logs with filtering for OpenAI usage
& $adbPath logcat -s OpenAIEmbeddingsClient:I OpenAIUsageTracker:I MainViewModel:I *:S

