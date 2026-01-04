# Gita AI - Emotion Matching Tester
# PowerShell script to run CLI, Web UI, or Batch tests

param(
    [Parameter(Position=0)]
    [ValidateSet("cli", "web", "batch")]
    [string]$Mode = "cli"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Gita AI - Emotion Matching Tester" -ForegroundColor Yellow
Write-Host "   Mode: $Mode" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Python
try {
    $pythonVersion = python --version 2>&1
    Write-Host "Found Python: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Python not found. Please install Python 3.8+" -ForegroundColor Red
    exit 1
}

# Check for OPENAI_API_KEY
if (-not $env:OPENAI_API_KEY) {
    # Try loading from .env file
    if (Test-Path ".env") {
        Get-Content ".env" | ForEach-Object {
            if ($_ -match "^OPENAI_API_KEY=(.+)$") {
                $env:OPENAI_API_KEY = $matches[1]
            }
        }
    }
    
    if (-not $env:OPENAI_API_KEY) {
        Write-Host "WARNING: OPENAI_API_KEY not found in environment or .env file" -ForegroundColor Yellow
        $apiKey = Read-Host "Enter your OpenAI API key"
        $env:OPENAI_API_KEY = $apiKey
    }
}

# Install requirements
Write-Host "Checking dependencies..." -ForegroundColor Cyan
pip install -q openai python-dotenv numpy flask

# Run based on mode
switch ($Mode) {
    "cli" {
        Write-Host "`nStarting CLI mode..." -ForegroundColor Green
        python test_emotion_matching.py
    }
    "web" {
        Write-Host "`nStarting Web UI at http://localhost:5000" -ForegroundColor Green
        Start-Process "http://localhost:5000"
        python test_emotion_matching.py --web
    }
    "batch" {
        Write-Host "`nRunning batch tests..." -ForegroundColor Green
        python test_emotion_matching.py --batch
    }
}

