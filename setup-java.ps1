# Quick script to help find and set up Java for Gradle

Write-Host "=== Java Setup for Gradle ===" -ForegroundColor Cyan
Write-Host ""

# Check current Java
Write-Host "Current Java version:" -ForegroundColor Yellow
java -version 2>&1 | Select-Object -First 3
Write-Host ""

# Check JAVA_HOME
if ($env:JAVA_HOME) {
    Write-Host "Current JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Yellow
} else {
    Write-Host "JAVA_HOME is not set" -ForegroundColor Yellow
}
Write-Host ""

# Search for Java installations
Write-Host "Searching for Java installations..." -ForegroundColor Cyan

$foundJava = $false

# Check Android Studio locations
$studioPaths = @(
    "$env:LOCALAPPDATA\Android\android-studio\jbr",
    "$env:PROGRAMFILES\Android\Android Studio\jbr",
    "${env:ProgramFiles(x86)}\Android\Android Studio\jbr"
)

foreach ($path in $studioPaths) {
    if (Test-Path $path) {
        $javaExe = Join-Path $path "bin\java.exe"
        if (Test-Path $javaExe) {
            Write-Host "✓ Found Android Studio JDK: $path" -ForegroundColor Green
            $version = & $javaExe -version 2>&1 | Select-Object -First 1
            Write-Host "  Version: $version" -ForegroundColor Gray
            
            Write-Host ""
            Write-Host "To use this JDK for this session, run:" -ForegroundColor Cyan
            Write-Host "  `$env:JAVA_HOME = '$path'" -ForegroundColor White
            Write-Host ""
            Write-Host "Or add this to gradle.properties:" -ForegroundColor Cyan
            $gradlePath = $path -replace '\\', '\\'
            Write-Host "  org.gradle.java.home=$gradlePath" -ForegroundColor White
            $foundJava = $true
        }
    }
}

# Check common Java installation locations
$javaPaths = @(
    "C:\Program Files\Java",
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\Microsoft",
    "$env:PROGRAMFILES\Java",
    "$env:PROGRAMFILES\Eclipse Adoptium"
)

foreach ($basePath in $javaPaths) {
    if (Test-Path $basePath) {
        $jdkDirs = Get-ChildItem $basePath -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "jdk|java|temurin" -and $_.Name -notmatch "jre" }
        foreach ($jdkDir in $jdkDirs) {
            $javaExe = Join-Path $jdkDir.FullName "bin\java.exe"
            if (Test-Path $javaExe) {
                $version = & $javaExe -version 2>&1 | Select-Object -First 1
                if ($version -match "version \"(1[1-9]|[2-9][0-9])") {
                    Write-Host "✓ Found JDK: $($jdkDir.FullName)" -ForegroundColor Green
                    Write-Host "  Version: $version" -ForegroundColor Gray
                    
                    Write-Host ""
                    Write-Host "To use this JDK for this session, run:" -ForegroundColor Cyan
                    Write-Host "  `$env:JAVA_HOME = '$($jdkDir.FullName)'" -ForegroundColor White
                    $foundJava = $true
                }
            }
        }
    }
}

if (-not $foundJava) {
    Write-Host ""
    Write-Host "⚠ No Java 11+ found!" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please install Java 17 (recommended) or Java 11+:" -ForegroundColor Cyan
    Write-Host "  1. Visit: https://adoptium.net/" -ForegroundColor White
    Write-Host "  2. Download JDK 17 LTS for Windows x64" -ForegroundColor White
    Write-Host "  3. Install it" -ForegroundColor White
    Write-Host "  4. Run this script again" -ForegroundColor White
    Write-Host ""
    Write-Host "Or if you have Android Studio installed, make sure it's in one of these locations:" -ForegroundColor Cyan
    foreach ($p in $studioPaths) {
        Write-Host "  - $p" -ForegroundColor Gray
    }
}

Write-Host ""


