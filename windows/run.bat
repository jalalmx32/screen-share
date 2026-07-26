@echo off
echo ========================================
echo   ScreenShare - Wireless Display
echo ========================================
echo.

if not exist "venv" (
    echo [1/3] Installing Python packages...
    python -m venv venv
    call venv\Scripts\activate.bat
    pip install -r requirements.txt
    pip install PyQt6
) else (
    call venv\Scripts\activate.bat
)

echo [2/3] Starting ScreenShare...
echo.
echo ========================================
echo   App is running!
echo   Check system tray for connection info
echo ========================================
echo.

python main.py

pause
