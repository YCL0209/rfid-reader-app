#!/bin/bash

# RFID 讀取頭控制系統 - 編譯腳本
# 使用方式: ./build.sh

echo "=========================================="
echo "RFID 讀取頭控制系統 - 編譯中..."
echo "=========================================="

# 設定目錄
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src/main/java"
LIB_DIR="$PROJECT_DIR/lib"
OUT_DIR="$PROJECT_DIR/out"

# 建立輸出目錄
mkdir -p "$OUT_DIR"

# 編譯所有 Java 檔案
echo "編譯 Java 檔案..."
find "$SRC_DIR" -name "*.java" > "$OUT_DIR/sources.txt"

javac -encoding UTF-8 \
      -cp "$LIB_DIR/*" \
      -d "$OUT_DIR" \
      @"$OUT_DIR/sources.txt"

if [ $? -eq 0 ]; then
    echo "=========================================="
    echo "編譯成功！"
    echo "=========================================="
    echo ""
    echo "執行方式:"
    echo "  ./run.sh"
    echo ""
    echo "或手動執行:"
    echo "  java -cp \"$OUT_DIR:$LIB_DIR/*\" com.rfid.Main"
else
    echo "=========================================="
    echo "編譯失敗！請檢查錯誤訊息。"
    echo "=========================================="
    exit 1
fi
