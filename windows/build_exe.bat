@echo off
echo ========================================
echo   ScreenShare - Build EXE
echo ========================================
echo.

python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found!
    echo Install Python 3.10+ from python.org
    pause
    exit /b 1
)

echo [1/4] Creating virtual environment...
python -m venv venv
call venv\Scripts\activate.bat

echo [2/4] Installing packages...
pip install -r requirements.txt
pip install PyQt6
pip install pyinstaller

echo [3/4] Building EXE...
pyinstaller ^
    --name ScreenShare ^
    --windowed ^
    --onefile ^
    --add-data "src;src" ^
    --hidden-import=mss ^
    --hidden-import=mss.tools ^
    --hidden-import=websockets ^
    --hidden-import=PIL ^
    --hidden-import=PyQt6 ^
    main.py

echo [4/4] Done!
echo.
echo EXE created: dist\ScreenShare.exe
echo.
explorer dist
pause
