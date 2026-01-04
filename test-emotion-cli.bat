@echo off
echo ========================================
echo   Gita AI - Emotion Matching Tester
echo   Command Line Interface
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
pip show openai >nul 2>&1
if errorlevel 1 (
    echo Installing required packages...
    pip install -r test_requirements.txt
)

REM Run the CLI test
python test_emotion_matching.py

pause

