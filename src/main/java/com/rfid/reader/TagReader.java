package com.rfid.reader;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.handler.*;
import com.gg.reader.api.protocol.gx.*;
import com.rfid.connection.ReaderConnection;

import java.util.function.Consumer;

/**
 * RFID 標籤讀取器類
 * 負責標籤讀取操作和事件處理
 */
public class TagReader {

    /** 讀取模式枚舉 */
    public enum ReadMode {
        SINGLE(0),      // 單次讀取
        CONTINUOUS(1);  // 連續讀取

        private final int value;

        ReadMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /** 標籤協議枚舉 */
    public enum TagProtocol {
        EPC_6C,
        ISO_6B,
        GB,
        GJB
    }

    private ReaderConnection connection;
    private boolean isReading;
    private TagProtocol currentProtocol;

    // 回調函數
    private Consumer<TagInfo> onTagReadCallback;
    private Runnable onReadOverCallback;
    private Consumer<String> onLogCallback;

    public TagReader(ReaderConnection connection) {
        this.connection = connection;
        this.isReading = false;
        this.currentProtocol = TagProtocol.EPC_6C;
    }

    /**
     * 設置標籤讀取事件回調
     */
    public void setupEventHandlers() {
        GClient client = connection.getClient();
        if (client == null) {
            return;
        }

        // EPC 6C 標籤讀取事件
        client.onTagEpcLog = new HandlerTagEpcLog() {
            @Override
            public void log(String readerName, LogBaseEpcInfo info) {
                if (info != null && info.getResult() == 0) {
                    TagInfo tagInfo = new TagInfo();
                    tagInfo.setEpc(info.getEpc());
                    tagInfo.setTid(info.getTid());
                    tagInfo.setUserData(info.getUserdata());
                    tagInfo.setRssi(info.getRssi());
                    tagInfo.setAntennaId(info.getAntId());
                    tagInfo.setTagType(TagInfo.TagType.EPC_6C);

                    if (onTagReadCallback != null) {
                        onTagReadCallback.accept(tagInfo);
                    }
                    TagReader.this.log("讀取到 EPC 標籤: " + info.getEpc());
                }
            }
        };

        // EPC 讀取結束事件
        client.onTagEpcOver = new HandlerTagEpcOver() {
            @Override
            public void log(String readerName, LogBaseEpcOver info) {
                TagReader.this.log("EPC 讀取結束");
                if (onReadOverCallback != null) {
                    onReadOverCallback.run();
                }
            }
        };

        // ISO 6B 標籤讀取事件
        client.onTag6bLog = new HandlerTag6bLog() {
            @Override
            public void log(String readerName, LogBase6bInfo info) {
                if (info != null && info.getResult() == 0) {
                    TagInfo tagInfo = new TagInfo();
                    tagInfo.setTid(info.getTid());
                    tagInfo.setUserData(info.getUserdata());
                    tagInfo.setRssi(info.getRssi());
                    tagInfo.setAntennaId(info.getAntId());
                    tagInfo.setTagType(TagInfo.TagType.ISO_6B);

                    if (onTagReadCallback != null) {
                        onTagReadCallback.accept(tagInfo);
                    }
                    TagReader.this.log("讀取到 6B 標籤: " + info.getTid());
                }
            }
        };

        // 6B 讀取結束事件
        client.onTag6bOver = new HandlerTag6bOver() {
            @Override
            public void log(String readerName, LogBase6bOver info) {
                TagReader.this.log("6B 讀取結束");
                if (onReadOverCallback != null) {
                    onReadOverCallback.run();
                }
            }
        };

        // GB 國標標籤讀取事件
        client.onTagGbLog = new HandlerTagGbLog() {
            @Override
            public void log(String readerName, LogBaseGbInfo info) {
                if (info != null && info.getResult() == 0) {
                    TagInfo tagInfo = new TagInfo();
                    tagInfo.setEpc(info.getEpc());
                    tagInfo.setTid(info.getTid());
                    tagInfo.setUserData(info.getUserdata());
                    tagInfo.setRssi(info.getRssi());
                    tagInfo.setAntennaId(info.getAntId());
                    tagInfo.setTagType(TagInfo.TagType.GB);

                    if (onTagReadCallback != null) {
                        onTagReadCallback.accept(tagInfo);
                    }
                    TagReader.this.log("讀取到 GB 標籤: " + info.getEpc());
                }
            }
        };

        // GB 讀取結束事件
        client.onTagGbOver = new HandlerTagGbOver() {
            @Override
            public void log(String readerName, LogBaseGbOver info) {
                TagReader.this.log("GB 讀取結束");
                if (onReadOverCallback != null) {
                    onReadOverCallback.run();
                }
            }
        };

        // GJB 國軍標標籤讀取事件
        client.onTagGJbLog = new HandlerTagGJbLog() {
            @Override
            public void log(String readerName, LogBaseGJbInfo info) {
                if (info != null && info.getResult() == 0) {
                    TagInfo tagInfo = new TagInfo();
                    tagInfo.setEpc(info.getEpc());
                    tagInfo.setTid(info.getTid());
                    tagInfo.setUserData(info.getUserdata());
                    tagInfo.setRssi(info.getRssi());
                    tagInfo.setAntennaId(info.getAntId());
                    tagInfo.setTagType(TagInfo.TagType.GJB);

                    if (onTagReadCallback != null) {
                        onTagReadCallback.accept(tagInfo);
                    }
                    TagReader.this.log("讀取到 GJB 標籤: " + info.getEpc());
                }
            }
        };

        // GJB 讀取結束事件
        client.onTagGJbOver = new HandlerTagGJbOver() {
            @Override
            public void log(String readerName, LogBaseGJbOver info) {
                TagReader.this.log("GJB 讀取結束");
                if (onReadOverCallback != null) {
                    onReadOverCallback.run();
                }
            }
        };

        log("事件處理器設置完成");
    }

    /**
     * 開始讀取 EPC 標籤
     */
    public boolean startReadEpc(int antennaEnable, ReadMode mode, boolean readTid, boolean readUserData) {
        if (!checkConnection()) return false;

        try {
            currentProtocol = TagProtocol.EPC_6C;

            MsgBaseInventoryEpc msg = new MsgBaseInventoryEpc();
            msg.setAntennaEnable(antennaEnable);
            msg.setInventoryMode(mode.getValue());

            // 設置讀取 TID
            if (readTid) {
                ParamEpcReadTid tidParam = new ParamEpcReadTid();
                tidParam.setMode(0); // 自適應模式
                tidParam.setLen(6);  // 6 個字
                msg.setReadTid(tidParam);
            }

            // 設置讀取用戶數據
            if (readUserData) {
                ParamEpcReadUserdata userParam = new ParamEpcReadUserdata();
                userParam.setStart(0);
                userParam.setLen(4); // 4 個字
                msg.setReadUserdata(userParam);
            }

            connection.getClient().sendSynMsg(msg);

            if (msg.getRtCode() == 0) {
                isReading = true;
                log("開始讀取 EPC 標籤");
                return true;
            } else {
                log("啟動讀取失敗: " + msg.getRtMsg());
                return false;
            }
        } catch (Exception e) {
            log("讀取錯誤: " + e.getMessage());
            return false;
        }
    }

    /**
     * 開始讀取 6B 標籤
     */
    public boolean startRead6b(int antennaEnable, ReadMode mode) {
        if (!checkConnection()) return false;

        try {
            currentProtocol = TagProtocol.ISO_6B;

            MsgBaseInventory6b msg = new MsgBaseInventory6b();
            msg.setAntennaEnable(antennaEnable);
            msg.setInventoryMode(mode.getValue());
            msg.setArea(1); // 讀取 TID + 用戶數據

            // 設置用戶數據讀取參數
            Param6bReadUserdata userParam = new Param6bReadUserdata();
            userParam.setStart(0);
            userParam.setLen(8); // 8 bytes
            msg.setReadUserdata(userParam);

            connection.getClient().sendSynMsg(msg);

            if (msg.getRtCode() == 0) {
                isReading = true;
                log("開始讀取 6B 標籤");
                return true;
            } else {
                log("啟動讀取失敗: " + msg.getRtMsg());
                return false;
            }
        } catch (Exception e) {
            log("讀取錯誤: " + e.getMessage());
            return false;
        }
    }

    /**
     * 開始讀取 GB 國標標籤
     */
    public boolean startReadGb(int antennaEnable, ReadMode mode, boolean readTid, boolean readUserData) {
        if (!checkConnection()) return false;

        try {
            currentProtocol = TagProtocol.GB;

            MsgBaseInventoryGb msg = new MsgBaseInventoryGb();
            msg.setAntennaEnable(antennaEnable);
            msg.setInventoryMode(mode.getValue());

            // 設置讀取 TID（標籤信息區）
            if (readTid) {
                ParamEpcReadTid tidParam = new ParamEpcReadTid();
                tidParam.setMode(0);
                tidParam.setLen(6);
                msg.setReadTid(tidParam);
            }

            // 設置讀取用戶數據
            if (readUserData) {
                ParamGbReadUserdata userParam = new ParamGbReadUserdata();
                userParam.setChildArea(0x30); // 用戶子區 0
                userParam.setStart(0);
                userParam.setLen(4);
                msg.setReadUserdata(userParam);
            }

            connection.getClient().sendSynMsg(msg);

            if (msg.getRtCode() == 0) {
                isReading = true;
                log("開始讀取 GB 標籤");
                return true;
            } else {
                log("啟動讀取失敗: " + msg.getRtMsg());
                return false;
            }
        } catch (Exception e) {
            log("讀取錯誤: " + e.getMessage());
            return false;
        }
    }

    /**
     * 停止讀取
     */
    public boolean stopRead() {
        if (connection == null || !connection.isConnected()) {
            return false;
        }

        boolean result = connection.stop();
        if (result) {
            isReading = false;
            log("已停止讀取");
        }
        return result;
    }

    /**
     * 檢查連接狀態
     */
    private boolean checkConnection() {
        if (connection == null || !connection.isConnected()) {
            log("錯誤：未連接讀取頭");
            return false;
        }
        return true;
    }

    private void log(String message) {
        System.out.println("[TagReader] " + message);
        if (onLogCallback != null) {
            onLogCallback.accept(message);
        }
    }

    // Getters
    public boolean isReading() {
        return isReading;
    }

    public TagProtocol getCurrentProtocol() {
        return currentProtocol;
    }

    // Setters for callbacks
    public void setOnTagReadCallback(Consumer<TagInfo> callback) {
        this.onTagReadCallback = callback;
    }

    public void setOnReadOverCallback(Runnable callback) {
        this.onReadOverCallback = callback;
    }

    public void setOnLogCallback(Consumer<String> callback) {
        this.onLogCallback = callback;
    }
}
