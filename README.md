# RFID 讀取頭控制系統

基於原廠 Java API 開發的 RFID 讀取頭控制應用程式。

## 功能特點

- 支援 TCP/IP 連接 RFID 讀取頭
- 支援多種標籤協議：EPC (6C)、ISO 6B、國標 GB
- 讀取 EPC、TID、用戶數據區
- 繁體中文介面
- 即時顯示標籤資料和設備狀態

## 系統需求

- Java JDK 1.8 或更高版本
- Windows 作業系統

## 快速開始

### 1. 安裝 Java

下載並安裝 Java JDK：https://adoptium.net/

確認安裝成功：
```cmd
java -version
```

### 2. 編譯專案

雙擊 `build.bat` 或在命令列執行：
```cmd
build.bat
```

### 3. 執行程式

雙擊 `run.bat` 或在命令列執行：
```cmd
run.bat
```

## 使用說明

1. 輸入 RFID 讀取頭的 IP 位址（預設 `192.168.1.168:8160`）
2. 點擊「連接」按鈕
3. 選擇標籤協議（EPC 6C / ISO 6B / 國標 GB）
4. 勾選要讀取的資料（TID、用戶區）
5. 點擊「開始讀取」
6. 標籤資料會顯示在表格中
7. 點擊「停止讀取」結束

## 專案結構

```
rfid-reader-app/
├── src/main/java/com/rfid/
│   ├── Main.java                 # 應用程式入口
│   ├── connection/
│   │   └── ReaderConnection.java # 連接管理
│   ├── reader/
│   │   ├── TagReader.java        # 標籤讀取
│   │   └── TagInfo.java          # 標籤資料模型
│   └── ui/
│       ├── MainFrame.java        # 主視窗
│       ├── MainControlPanel.java # 主控制面板
│       └── StatusPanel.java      # 狀態面板
├── lib/
│   └── reader.jar                # 原廠 API
├── build.bat                     # Windows 編譯腳本
└── run.bat                       # Windows 執行腳本
```

## 介面說明

### 主控制介面
- IP/Port 輸入欄位
- 連接/斷開按鈕
- 開始讀取/停止讀取按鈕
- 標籤協議選擇
- 標籤資料表格

### 設備狀態介面
- 連接狀態指示燈
- 設備序號、版本資訊
- 天線功率設定
- 設備能力資訊

## 授權

本專案使用原廠提供的 reader.jar API 進行開發。
