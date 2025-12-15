package com.rfid.connection;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTcpDisconnected;
import com.gg.reader.api.protocol.gx.*;

import java.util.Hashtable;
import java.util.function.Consumer;

/**
 * RFID 讀取頭連接管理類
 * 負責 TCP 連接、斷開、心跳檢測等功能
 */
public class ReaderConnection {

    private GClient client;
    private String ip;
    private int port;
    private boolean connected;
    private Consumer<String> onDisconnectedCallback;
    private Consumer<String> onLogCallback;

    // 設備資訊
    private String serialNumber;
    private String appVersion;
    private String baseVersion;
    private int maxPower;
    private int minPower;
    private int antennaCount;
    private Hashtable<Integer, Integer> antennaPowers;

    public ReaderConnection() {
        this.connected = false;
        this.port = 8160; // 預設端口
    }

    /**
     * 建立 TCP 連接
     * @param ip 讀取頭 IP 位址
     * @param port 端口號
     * @param timeout 連接超時時間（毫秒）
     * @return 是否連接成功
     */
    public boolean connect(String ip, int port, int timeout) {
        this.ip = ip;
        this.port = port;

        try {
            client = new GClient();
            String connectionString = ip + ":" + port;

            log("正在連接到 " + connectionString + "...");

            if (client.openTcp(connectionString, timeout)) {
                // 設置心跳檢測
                client.setSendHeartBeat(true);

                // 訂閱斷線事件
                client.onDisconnected = new HandlerTcpDisconnected() {
                    @Override
                    public void log(String readerName) {
                        connected = false;
                        if (onDisconnectedCallback != null) {
                            onDisconnectedCallback.accept(readerName);
                        }
                        ReaderConnection.this.log("連接已斷開: " + readerName);
                    }
                };

                connected = true;
                log("連接成功！");

                // 查詢設備資訊
                queryDeviceInfo();

                return true;
            } else {
                log("連接失敗！");
                return false;
            }
        } catch (Exception e) {
            log("連接錯誤: " + e.getMessage());
            return false;
        }
    }

    /**
     * 使用預設端口連接
     */
    public boolean connect(String ip, int timeout) {
        return connect(ip, 8160, timeout);
    }

    /**
     * 斷開連接
     */
    public void disconnect() {
        if (client != null) {
            try {
                // 先發送停止指令
                stop();
                // 關閉連接
                client.close();
                log("已斷開連接");
            } catch (Exception e) {
                log("斷開連接錯誤: " + e.getMessage());
            } finally {
                connected = false;
                client = null;
            }
        }
    }

    /**
     * 發送停止指令
     */
    public boolean stop() {
        if (client == null || !connected) {
            return false;
        }

        try {
            MsgBaseStop stopMsg = new MsgBaseStop();
            client.sendSynMsg(stopMsg);
            return stopMsg.getRtCode() == 0;
        } catch (Exception e) {
            log("停止指令錯誤: " + e.getMessage());
            return false;
        }
    }

    /**
     * 查詢設備資訊
     */
    private void queryDeviceInfo() {
        if (client == null || !connected) {
            return;
        }

        try {
            // 查詢讀寫器資訊
            MsgAppGetReaderInfo infoMsg = new MsgAppGetReaderInfo();
            client.sendSynMsg(infoMsg);
            if (infoMsg.getRtCode() == 0) {
                serialNumber = infoMsg.getReaderSerialNumber();
                appVersion = infoMsg.getAppVersions();
                log("設備序號: " + serialNumber);
                log("應用版本: " + appVersion);
            }

            // 查詢基帶版本
            MsgAppGetBaseVersion baseVersionMsg = new MsgAppGetBaseVersion();
            client.sendSynMsg(baseVersionMsg);
            if (baseVersionMsg.getRtCode() == 0) {
                baseVersion = baseVersionMsg.getBaseVersions();
                log("基帶版本: " + baseVersion);
            }

            // 查詢設備能力
            MsgBaseGetCapabilities capMsg = new MsgBaseGetCapabilities();
            client.sendSynMsg(capMsg);
            if (capMsg.getRtCode() == 0) {
                maxPower = capMsg.getMaxPower();
                minPower = capMsg.getMinPower();
                antennaCount = capMsg.getAntennaCount();
                log("天線數量: " + antennaCount);
                log("功率範圍: " + minPower + " ~ " + maxPower + " dBm");
            }

            // 查詢天線功率
            MsgBaseGetPower powerMsg = new MsgBaseGetPower();
            client.sendSynMsg(powerMsg);
            if (powerMsg.getRtCode() == 0) {
                antennaPowers = powerMsg.getDicPower();
                if (antennaPowers != null) {
                    antennaPowers.forEach((ant, power) ->
                        log("天線 " + ant + " 功率: " + power + " dBm")
                    );
                }
            }

        } catch (Exception e) {
            log("查詢設備資訊錯誤: " + e.getMessage());
        }
    }

    /**
     * 設置天線功率
     */
    public boolean setAntennaPower(int antennaId, int power) {
        if (client == null || !connected) {
            return false;
        }

        try {
            Hashtable<Integer, Integer> powers = new Hashtable<>();
            powers.put(antennaId, power);

            MsgBaseSetPower powerMsg = new MsgBaseSetPower();
            powerMsg.setDicPower(powers);
            client.sendSynMsg(powerMsg);

            if (powerMsg.getRtCode() == 0) {
                log("設置天線 " + antennaId + " 功率為 " + power + " dBm 成功");
                // 更新本地快取
                if (antennaPowers == null) {
                    antennaPowers = new Hashtable<>();
                }
                antennaPowers.put(antennaId, power);
                return true;
            } else {
                log("設置功率失敗: " + powerMsg.getRtMsg());
                return false;
            }
        } catch (Exception e) {
            log("設置功率錯誤: " + e.getMessage());
            return false;
        }
    }

    private void log(String message) {
        System.out.println("[ReaderConnection] " + message);
        if (onLogCallback != null) {
            onLogCallback.accept(message);
        }
    }

    // Getters
    public GClient getClient() {
        return client;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getMinPower() {
        return minPower;
    }

    public int getAntennaCount() {
        return antennaCount;
    }

    public Hashtable<Integer, Integer> getAntennaPowers() {
        return antennaPowers;
    }

    // Setters for callbacks
    public void setOnDisconnectedCallback(Consumer<String> callback) {
        this.onDisconnectedCallback = callback;
    }

    public void setOnLogCallback(Consumer<String> callback) {
        this.onLogCallback = callback;
    }
}
