#!/bin/bash

# RFID 讀取頭控制系統 - 執行腳本
# 使用方式: ./run.sh

echo "=========================================="
echo "RFID 讀取頭控制系統 - 啟動中..."
echo "=========================================="

# 設定目錄
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$PROJECT_DIR/lib"
OUT_DIR="$PROJECT_DIR/out"

# 檢查是否已編譯
if [ ! -d "$OUT_DIR" ] || [ -z "$(ls -A $OUT_DIR 2>/dev/null)" ]; then
    echo "尚未編譯，先執行編譯..."
    "$PROJECT_DIR/build.sh"
    if [ $? -ne 0 ]; then
        echo "編譯失敗，無法啟動程式。"
        exit 1
    fi
fi

# 執行程式
echo "啟動應用程式..."
java -cp "$OUT_DIR:$LIB_DIR/*" \
     -Dfile.encoding=UTF-8 \
     com.rfid.Main
