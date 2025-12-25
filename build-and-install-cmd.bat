@echo off
REM Build and Install Script for Windows CMD
REM This is a wrapper that calls the PowerShell script

echo ========================================
echo Gita Android - Build and Install
echo ========================================
echo.

REM Check if PowerShell is available
where powershell >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: PowerShell is not available
    echo Please use Git Bash or install PowerShell
    pause
    exit /b 1
)

REM Check arguments
set SKIP_BUILD=
set UNINSTALL_FIRST=

:parse_args
if "%1"=="" goto :run_script
if /i "%1"=="--skip-build" set SKIP_BUILD=-SkipBuild
if /i "%1"=="--uninstall-first" set UNINSTALL_FIRST=-UninstallFirst
shift
goto :parse_args

:run_script
REM Run the PowerShell script
powershell.exe -ExecutionPolicy Bypass -File "%~dp0build-and-install.ps1" %SKIP_BUILD% %UNINSTALL_FIRST%

pause

