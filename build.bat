@echo off
chcp 65001 >nul
echo ==========================================
echo RFID 讀取頭控制系統 - 編譯中...
echo ==========================================

set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src\main\java
set LIB_DIR=%PROJECT_DIR%lib
set OUT_DIR=%PROJECT_DIR%out

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo 編譯 Java 檔案...
dir /s /b "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

javac -encoding UTF-8 -cp "%LIB_DIR%\*" -d "%OUT_DIR%" @"%OUT_DIR%\sources.txt"

if %errorlevel% equ 0 (
    echo ==========================================
    echo 編譯成功！
    echo ==========================================
    echo.
    echo 執行方式: run.bat
) else (
    echo ==========================================
    echo 編譯失敗！請檢查錯誤訊息。
    echo ==========================================
)

pause
