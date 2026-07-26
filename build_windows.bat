@echo off
REM ============================================
REM   ScreenShare - Windows Build Script
REM   این اسکریپت روی ویندوز اجرا کنید
REM ============================================

echo.
echo ========================================
echo   ScreenShare - ساخت فایل اجرایی
echo ========================================
echo.

REM Check Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python پیدا نشد!
    echo لطفاً Python 3.10+ رو نصب کنید:
    echo https://www.python.org/downloads/
    pause
    exit /b 1
)

echo [1/5] ایجاد محیط مجازی...
python -m venv venv
call venv\Scripts\activate.bat

echo [2/5] نصب پکیج‌ها...
pip install -r requirements.txt
pip install PyQt6
pip install pyinstaller

echo [3/5] ساخت فایل اجرایی...
pyinstaller ^
    --name ScreenShare ^
    --windowed ^
    --onefile ^
    --add-data "src;src" ^
    --hidden-import=mss ^
    --hidden-import=mss.tools ^
    --hidden-import=websockets ^
    --hidden-import=PIL ^
    --hidden-import=PIL.Image ^
    --hidden-import=PyQt6 ^
    --hidden-import=PyQt6.QtWidgets ^
    --hidden-import=PyQt6.QtCore ^
    --hidden-import=PyQt6.QtGui ^
    main.py

echo [4/5] پاکسازی...
rmdir /s /q build 2>nul
del /q *.spec 2>nul

echo [5/5] تمام شد!
echo.
echo ========================================
echo   فایل اجرایی آماده است:
echo   dist\ScreenShare.exe
echo ========================================
echo.

REM Open the dist folder
explorer dist

pause
