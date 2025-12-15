package com.rfid.reader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RFID 標籤資料模型
 * 用於儲存讀取到的標籤資訊
 */
public class TagInfo {

    /** 標籤類型枚舉 */
    public enum TagType {
        EPC_6C("EPC (6C)"),
        ISO_6B("ISO 6B"),
        GB("國標 GB"),
        GJB("國軍標 GJB");

        private final String displayName;

        TagType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private String epc;           // EPC 碼
    private String tid;           // TID 碼
    private String userData;      // 用戶數據區
    private int rssi;             // 信號強度
    private int antennaId;        // 天線編號
    private LocalDateTime readTime; // 讀取時間
    private TagType tagType;      // 標籤類型
    private int readCount;        // 讀取次數

    public TagInfo() {
        this.readTime = LocalDateTime.now();
        this.readCount = 1;
    }

    public TagInfo(String epc, String tid, String userData, int rssi, int antennaId, TagType tagType) {
        this.epc = epc;
        this.tid = tid;
        this.userData = userData;
        this.rssi = rssi;
        this.antennaId = antennaId;
        this.tagType = tagType;
        this.readTime = LocalDateTime.now();
        this.readCount = 1;
    }

    // Getters and Setters
    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getAntennaId() {
        return antennaId;
    }

    public void setAntennaId(int antennaId) {
        this.antennaId = antennaId;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public String getReadTimeString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return readTime.format(formatter);
    }

    public TagType getTagType() {
        return tagType;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
    }

    public String getTagTypeDisplay() {
        return tagType != null ? tagType.getDisplayName() : "";
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public void incrementReadCount() {
        this.readCount++;
        this.readTime = LocalDateTime.now();
    }

    /** 用於表格顯示的資料陣列 */
    public Object[] toTableRow() {
        return new Object[] {
            epc != null ? epc : "",
            tid != null ? tid : "",
            userData != null ? userData : "",
            rssi,
            antennaId,
            getReadTimeString(),
            getTagTypeDisplay(),
            readCount
        };
    }

    @Override
    public String toString() {
        return "TagInfo{" +
                "epc='" + epc + '\'' +
                ", tid='" + tid + '\'' +
                ", userData='" + userData + '\'' +
                ", rssi=" + rssi +
                ", antennaId=" + antennaId +
                ", readTime=" + getReadTimeString() +
                ", tagType=" + getTagTypeDisplay() +
                ", readCount=" + readCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagInfo tagInfo = (TagInfo) o;
        return epc != null && epc.equals(tagInfo.epc);
    }

    @Override
    public int hashCode() {
        return epc != null ? epc.hashCode() : 0;
    }
}
