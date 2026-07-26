@echo off
REM ScreenShare Build Script for Windows
REM Run this to install dependencies and create executable

echo ========================================
echo   ScreenShare - Build Script
echo ========================================
echo.

REM Check Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found! Please install Python 3.10+
    echo Download from: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo [1/4] Creating virtual environment...
python -m venv venv
call venv\Scripts\activate.bat

echo [2/4] Installing dependencies...
pip install -r requirements.txt
pip install pyinstaller

echo [3/4] Building executable...
pyinstaller ^
    --name ScreenShare ^
    --windowed ^
    --onefile ^
    --icon=assets\icon.ico ^
    --add-data "src;src" ^
    --hidden-import=mss ^
    --hidden-import=websockets ^
    --hidden-import=PIL ^
    main.py

echo [4/4] Done!
echo.
echo Executable created: dist\ScreenShare.exe
echo.
pause
