@echo off
chcp 65001 >nul
echo ==========================================
echo RFID 讀取頭控制系統 - 啟動中...
echo ==========================================

set PROJECT_DIR=%~dp0
set LIB_DIR=%PROJECT_DIR%lib
set OUT_DIR=%PROJECT_DIR%out

if not exist "%OUT_DIR%\com" (
    echo 尚未編譯，先執行編譯...
    call "%PROJECT_DIR%build.bat"
    if %errorlevel% neq 0 (
        echo 編譯失敗，無法啟動程式。
        pause
        exit /b 1
    )
)

echo 啟動應用程式...
java -cp "%OUT_DIR%;%LIB_DIR%\*" -Dfile.encoding=UTF-8 com.rfid.Main

pause
