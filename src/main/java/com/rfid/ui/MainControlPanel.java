package com.rfid.ui;

import com.gg.reader.api.protocol.gx.EnumG;
import com.rfid.connection.ReaderConnection;
import com.rfid.reader.TagInfo;
import com.rfid.reader.TagReader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 主控制面板
 * 包含連接設定、讀取控制和標籤資料顯示
 */
public class MainControlPanel extends JPanel {

    // 連接設定元件
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JLabel connectionStatusLabel;

    // 讀取控制元件
    private JComboBox<String> protocolComboBox;
    private JButton fastReadButton;
    private JButton fullReadButton;
    private JButton stopReadButton;
    private JButton clearButton;

    // 標籤資料表格
    private JTable tagTable;
    private DefaultTableModel tableModel;
    private Map<String, Integer> epcToRowMap; // EPC 到表格行的映射

    // 連接和讀取器
    private ReaderConnection connection;
    private TagReader tagReader;

    // 狀態變更回調
    private Consumer<ReaderConnection> onConnectionChangedCallback;

    // 統計資訊
    private JLabel totalTagsLabel;
    private int totalTags = 0;

    // 斷線處理標記（防止重複觸發）
    private volatile boolean isDisconnectHandled = false;

    public MainControlPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        epcToRowMap = new HashMap<>();

        // 建立頂部控制面板
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 建立中間的標籤資料表格
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // 建立底部統計面板
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // 初始化連接物件
        connection = new ReaderConnection();
        setupConnectionCallbacks();
    }

    /**
     * 建立頂部控制面板
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // 連接設定區
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("連接設定"));

        connectionPanel.add(new JLabel("IP 位址:"));
        ipField = new JTextField("192.168.1.168", 12);
        connectionPanel.add(ipField);

        connectionPanel.add(new JLabel("Port:"));
        portField = new JTextField("8160", 5);
        connectionPanel.add(portField);

        connectButton = new JButton("連接");
        connectButton.addActionListener(e -> connect());
        connectionPanel.add(connectButton);

        disconnectButton = new JButton("斷開");
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnect());
        connectionPanel.add(disconnectButton);

        connectionStatusLabel = new JLabel("未連接");
        connectionStatusLabel.setForeground(Color.RED);
        connectionPanel.add(connectionStatusLabel);

        panel.add(connectionPanel);

        // 讀取控制區
        JPanel readControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        readControlPanel.setBorder(BorderFactory.createTitledBorder("讀取控制"));

        readControlPanel.add(new JLabel("標籤協議:"));
        protocolComboBox = new JComboBox<>(new String[]{"EPC (6C)", "ISO 6B", "國標 GB"});
        readControlPanel.add(protocolComboBox);

        fastReadButton = new JButton("快速盤存(EPC)");
        fastReadButton.setEnabled(false);
        fastReadButton.addActionListener(e -> startReading(false, false));
        readControlPanel.add(fastReadButton);

        fullReadButton = new JButton("完整讀取(EPC+TID+UserData)");
        fullReadButton.setEnabled(false);
        fullReadButton.addActionListener(e -> startReading(true, true));
        readControlPanel.add(fullReadButton);

        stopReadButton = new JButton("停止讀取");
        stopReadButton.setEnabled(false);
        stopReadButton.addActionListener(e -> stopReading());
        readControlPanel.add(stopReadButton);

        clearButton = new JButton("清除資料");
        clearButton.addActionListener(e -> clearTable());
        readControlPanel.add(clearButton);

        panel.add(readControlPanel);

        return panel;
    }

    /**
     * 建立標籤資料表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("標籤資料"));

        // 表格欄位
        String[] columns = {"EPC", "TID", "用戶數據", "RSSI", "天線", "讀取時間", "標籤類型", "次數"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tagTable = new JTable(tableModel);
        tagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagTable.getTableHeader().setReorderingAllowed(false);

        // 設置欄位寬度
        tagTable.getColumnModel().getColumn(0).setPreferredWidth(200); // EPC
        tagTable.getColumnModel().getColumn(1).setPreferredWidth(200); // TID
        tagTable.getColumnModel().getColumn(2).setPreferredWidth(150); // 用戶數據
        tagTable.getColumnModel().getColumn(3).setPreferredWidth(50);  // RSSI
        tagTable.getColumnModel().getColumn(4).setPreferredWidth(40);  // 天線
        tagTable.getColumnModel().getColumn(5).setPreferredWidth(130); // 讀取時間
        tagTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // 標籤類型
        tagTable.getColumnModel().getColumn(7).setPreferredWidth(40);  // 次數

        JScrollPane scrollPane = new JScrollPane(tagTable);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 建立底部統計面板
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        totalTagsLabel = new JLabel("總標籤數: 0");
        totalTagsLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        panel.add(totalTagsLabel);

        return panel;
    }

    /**
     * 設置連接回調
     */
    private void setupConnectionCallbacks() {
        connection.setOnDisconnectedCallback(readerName -> {
            // 防止重複處理斷線事件
            if (!isDisconnectHandled) {
                isDisconnectHandled = true;
                SwingUtilities.invokeLater(() -> {
                    updateConnectionStatus(false);
                    connectionStatusLabel.setText("連接已斷開");
                    connectionStatusLabel.setForeground(Color.ORANGE);
                    // 不再彈窗，只更新狀態和輸出日誌
                    System.out.println("連接已斷開: " + readerName);
                });
            }
        });

        connection.setOnLogCallback(msg -> {
            System.out.println(msg);
        });
    }

    /**
     * 連接讀取頭
     */
    private void connect() {
        String ip = ipField.getText().trim();
        String portStr = portField.getText().trim();

        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請輸入 IP 位址", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port 格式錯誤", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 禁用連接按鈕，顯示連接中狀態
        connectButton.setEnabled(false);
        connectionStatusLabel.setText("連接中...");
        connectionStatusLabel.setForeground(Color.ORANGE);

        // 在背景執行緒中連接
        new Thread(() -> {
            boolean success = connection.connect(ip, port, 3000);

            SwingUtilities.invokeLater(() -> {
                if (success) {
                    // 重置斷線處理標記
                    isDisconnectHandled = false;
                    updateConnectionStatus(true);

                    // 建立標籤讀取器
                    tagReader = new TagReader(connection);
                    tagReader.setupEventHandlers();
                    tagReader.setOnTagReadCallback(this::onTagRead);
                    tagReader.setOnReadOverCallback(() -> {
                        SwingUtilities.invokeLater(() -> {
                            fastReadButton.setEnabled(true);
                            fullReadButton.setEnabled(true);
                            stopReadButton.setEnabled(false);
                        });
                    });

                    // 通知狀態面板
                    if (onConnectionChangedCallback != null) {
                        onConnectionChangedCallback.accept(connection);
                    }

                    JOptionPane.showMessageDialog(this, "連接成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    updateConnectionStatus(false);
                    JOptionPane.showMessageDialog(this, "連接失敗，請檢查 IP 和 Port", "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    /**
     * 斷開連接
     */
    private void disconnect() {
        if (tagReader != null && tagReader.isReading()) {
            tagReader.stopRead();
        }

        connection.disconnect();
        updateConnectionStatus(false);

        if (onConnectionChangedCallback != null) {
            onConnectionChangedCallback.accept(null);
        }
    }

    /**
     * 開始讀取
     * @param readTid 是否讀取 TID
     * @param readUserData 是否讀取用戶數據區
     */
    private void startReading(boolean readTid, boolean readUserData) {
        if (tagReader == null) return;

        int protocolIndex = protocolComboBox.getSelectedIndex();

        boolean success = false;

        // 天線設定：使用天線 1
        long antennaEnable = EnumG.AntennaNo_1;

        switch (protocolIndex) {
            case 0: // EPC 6C
                success = tagReader.startReadEpc(antennaEnable, TagReader.ReadMode.CONTINUOUS, readTid, readUserData);
                break;
            case 1: // ISO 6B
                success = tagReader.startRead6b(antennaEnable, TagReader.ReadMode.CONTINUOUS);
                break;
            case 2: // GB
                success = tagReader.startReadGb(antennaEnable, TagReader.ReadMode.CONTINUOUS, readTid, readUserData);
                break;
        }

        if (success) {
            fastReadButton.setEnabled(false);
            fullReadButton.setEnabled(false);
            stopReadButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "啟動讀取失敗", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 停止讀取
     */
    private void stopReading() {
        if (tagReader != null) {
            tagReader.stopRead();
            fastReadButton.setEnabled(true);
            fullReadButton.setEnabled(true);
            stopReadButton.setEnabled(false);
        }
    }

    /**
     * 標籤讀取回調
     */
    private void onTagRead(TagInfo tagInfo) {
        SwingUtilities.invokeLater(() -> {
            String epc = tagInfo.getEpc();

            if (epc != null && epcToRowMap.containsKey(epc)) {
                // 更新已存在的標籤
                int row = epcToRowMap.get(epc);
                int currentCount = (int) tableModel.getValueAt(row, 7);
                tableModel.setValueAt(currentCount + 1, row, 7);
                tableModel.setValueAt(tagInfo.getReadTimeString(), row, 5);
                tableModel.setValueAt(tagInfo.getRssi(), row, 3);
            } else {
                // 新增標籤
                tableModel.addRow(tagInfo.toTableRow());
                if (epc != null) {
                    epcToRowMap.put(epc, tableModel.getRowCount() - 1);
                }
                totalTags++;
                totalTagsLabel.setText("總標籤數: " + totalTags);
            }
        });
    }

    /**
     * 清除表格資料
     */
    private void clearTable() {
        tableModel.setRowCount(0);
        epcToRowMap.clear();
        totalTags = 0;
        totalTagsLabel.setText("總標籤數: 0");
    }

    /**
     * 更新連接狀態顯示
     */
    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            connectionStatusLabel.setText("已連接");
            connectionStatusLabel.setForeground(new Color(0, 128, 0));
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            fastReadButton.setEnabled(true);
            fullReadButton.setEnabled(true);
            ipField.setEnabled(false);
            portField.setEnabled(false);
        } else {
            connectionStatusLabel.setText("未連接");
            connectionStatusLabel.setForeground(Color.RED);
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            fastReadButton.setEnabled(false);
            fullReadButton.setEnabled(false);
            stopReadButton.setEnabled(false);
            ipField.setEnabled(true);
            portField.setEnabled(true);
        }
    }

    /**
     * 設置連接狀態變更回調
     */
    public void setOnConnectionChangedCallback(Consumer<ReaderConnection> callback) {
        this.onConnectionChangedCallback = callback;
    }

    /**
     * 取得連接物件
     */
    public ReaderConnection getConnection() {
        return connection;
    }
}
