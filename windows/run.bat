@echo off
echo ========================================
echo   ScreenShare - Wireless Display
echo ========================================
echo.

:: Check if Python is installed
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed!
    echo.
    echo Please run install_and_run.bat first
    echo to install Python automatically.
    echo.
    echo Or install Python manually from:
    echo https://www.python.org/downloads/
    echo (Make sure to check "Add Python to PATH")
    echo.
    pause
    exit /b 1
)

echo [1/3] Checking packages...
pip install mss Pillow websockets pyautogui PyQt6 --quiet 2>nul

echo [2/3] Starting ScreenShare Server...
echo.
echo ========================================
echo   Server is running!
echo   Use this IP in your Android app.
echo ========================================
echo.

python main.py

pause
