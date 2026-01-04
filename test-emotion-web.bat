@echo off
echo ========================================
echo   Gita AI - Emotion Matching Tester
echo   Web UI (opens at http://localhost:5000)
echo ========================================
echo.

REM Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python not found. Please install Python 3.8+
    pause
    exit /b 1
)

REM Install requirements if needed
pip show flask >nul 2>&1
if errorlevel 1 (
    echo Installing required packages...
    pip install -r test_requirements.txt
)

REM Open browser after a short delay
start "" cmd /c "timeout /t 3 >nul && start http://localhost:5000"

REM Run the web server
python test_emotion_matching.py --web

