@echo off
REM Quick build and install script for Gita Android App
REM Double-click this file or run from command line

powershell.exe -ExecutionPolicy Bypass -File "%~dp0build-and-install.ps1" %*

pause

