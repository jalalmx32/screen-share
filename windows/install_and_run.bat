@echo off
echo ========================================
echo   ScreenShare - Auto Setup
echo ========================================
echo.
echo This will install Python and all needed
echo packages automatically.
echo.

:: Check if Python is already installed
python --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Python is already installed!
    goto :install_packages
)

echo [1/4] Downloading Python 3.11...
echo Please wait, downloading from python.org...

:: Download Python installer
powershell -Command "Invoke-WebRequest -Uri 'https://www.python.org/ftp/python/3.11.9/python-3.11.9-amd64.exe' -OutFile '%TEMP%\python_installer.exe'"

if not exist "%TEMP%\python_installer.exe" (
    echo [ERROR] Download failed!
    echo Please install Python manually from:
    echo https://www.python.org/downloads/
    pause
    exit /b 1
)

echo [2/4] Installing Python (this may take a minute)...
echo Installing silently, please wait...

:: Install Python with PATH enabled
"%TEMP%\python_installer.exe" /quiet InstallAllUsers=1 PrependPath=1 Include_test=0

:: Wait for installation
timeout /t 30 /nobreak >nul

:: Refresh PATH
set PATH=%PATH%;C:\Python311;C:\Python311\Scripts

:: Verify installation
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Python installed but not in PATH yet.
    echo Please RESTART this script after installation.
    echo.
    del "%TEMP%\python_installer.exe" >nul 2>&1
    pause
    exit /b 1
)

echo [OK] Python installed successfully!
del "%TEMP%\python_installer.exe" >nul 2>&1

:install_packages
echo.
echo [3/4] Installing required packages...
pip install mss Pillow websockets pyautogui PyQt6 --quiet

echo.
echo [4/4] Starting ScreenShare Server...
echo ========================================
echo.

:: Run the server
python main.py

pause
