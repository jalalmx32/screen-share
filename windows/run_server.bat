@echo off
echo ========================================
echo   ScreenShare - Kill Old Server & Run
echo ========================================
echo.

:: Kill any existing Python process using port 8765
echo [1/3] Checking for existing server...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8765 ^| findstr LISTENING') do (
    echo Found process %%a using port 8765
    taskkill /F /PID %%a >nul 2>&1
    echo Killed process %%a
)
timeout /t 2 /nobreak >nul

:: Check Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python not found!
    echo Please run install_and_run.bat first
    pause
    exit /b 1
)

echo [2/3] Starting ScreenShare Server...
echo ========================================
echo   Server starting on port 8765...
echo   Use this IP in your Android app.
echo ========================================
echo.

python main.py

pause
