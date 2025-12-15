package com.rfid.ui;

import com.rfid.connection.ReaderConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Hashtable;

/**
 * 讀取頭狀態顯示面板
 * 顯示連接狀態、設備資訊和天線功率
 */
public class StatusPanel extends JPanel {

    // 狀態指示燈
    private JPanel statusIndicator;
    private JLabel statusLabel;

    // 設備資訊
    private JLabel serialNumberValue;
    private JLabel appVersionValue;
    private JLabel baseVersionValue;
    private JLabel ipAddressValue;
    private JLabel portValue;

    // 天線功率表格
    private JTable powerTable;
    private DefaultTableModel powerTableModel;

    // 支援資訊
    private JLabel maxPowerValue;
    private JLabel minPowerValue;
    private JLabel antennaCountValue;

    public StatusPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 建立狀態面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 連接狀態區
        JPanel connectionStatusPanel = createConnectionStatusPanel();
        mainPanel.add(connectionStatusPanel);

        mainPanel.add(Box.createVerticalStrut(10));

        // 設備資訊區
        JPanel deviceInfoPanel = createDeviceInfoPanel();
        mainPanel.add(deviceInfoPanel);

        mainPanel.add(Box.createVerticalStrut(10));

        // 天線功率區
        JPanel powerPanel = createPowerPanel();
        mainPanel.add(powerPanel);

        mainPanel.add(Box.createVerticalStrut(10));

        // 設備能力區
        JPanel capabilityPanel = createCapabilityPanel();
        mainPanel.add(capabilityPanel);

        add(mainPanel, BorderLayout.NORTH);

        // 初始化為未連接狀態
        updateStatus(null);
    }

    /**
     * 建立連接狀態區
     */
    private JPanel createConnectionStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("連接狀態"));

        // 狀態指示燈
        statusIndicator = new JPanel();
        statusIndicator.setPreferredSize(new Dimension(20, 20));
        statusIndicator.setBackground(Color.RED);
        statusIndicator.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        panel.add(statusIndicator);

        statusLabel = new JLabel("未連接");
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        panel.add(statusLabel);

        return panel;
    }

    /**
     * 建立設備資訊區
     */
    private JPanel createDeviceInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("設備資訊"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // IP 位址
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("IP 位址:"), gbc);
        gbc.gridx = 1;
        ipAddressValue = new JLabel("--");
        panel.add(ipAddressValue, gbc);

        // Port
        gbc.gridx = 2;
        panel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 3;
        portValue = new JLabel("--");
        panel.add(portValue, gbc);

        // 設備序號
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("設備序號:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        serialNumberValue = new JLabel("--");
        serialNumberValue.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(serialNumberValue, gbc);

        // 應用版本
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("應用版本:"), gbc);
        gbc.gridx = 1;
        appVersionValue = new JLabel("--");
        panel.add(appVersionValue, gbc);

        // 基帶版本
        gbc.gridx = 2;
        panel.add(new JLabel("基帶版本:"), gbc);
        gbc.gridx = 3;
        baseVersionValue = new JLabel("--");
        panel.add(baseVersionValue, gbc);

        return panel;
    }

    /**
     * 建立天線功率區
     */
    private JPanel createPowerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("天線功率"));

        String[] columns = {"天線編號", "功率 (dBm)"};
        powerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        powerTable = new JTable(powerTableModel);
        powerTable.setRowHeight(25);
        powerTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        powerTable.getColumnModel().getColumn(1).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(powerTable);
        scrollPane.setPreferredSize(new Dimension(250, 120));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 建立設備能力區
     */
    private JPanel createCapabilityPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("設備能力"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 天線數量
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("天線數量:"), gbc);
        gbc.gridx = 1;
        antennaCountValue = new JLabel("--");
        panel.add(antennaCountValue, gbc);

        // 最大功率
        gbc.gridx = 2;
        panel.add(new JLabel("最大功率:"), gbc);
        gbc.gridx = 3;
        maxPowerValue = new JLabel("--");
        panel.add(maxPowerValue, gbc);

        // 最小功率
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("最小功率:"), gbc);
        gbc.gridx = 1;
        minPowerValue = new JLabel("--");
        panel.add(minPowerValue, gbc);

        return panel;
    }

    /**
     * 更新狀態顯示
     */
    public void updateStatus(ReaderConnection connection) {
        if (connection == null || !connection.isConnected()) {
            // 未連接狀態
            statusIndicator.setBackground(Color.RED);
            statusLabel.setText("未連接");

            ipAddressValue.setText("--");
            portValue.setText("--");
            serialNumberValue.setText("--");
            appVersionValue.setText("--");
            baseVersionValue.setText("--");

            maxPowerValue.setText("--");
            minPowerValue.setText("--");
            antennaCountValue.setText("--");

            powerTableModel.setRowCount(0);
        } else {
            // 已連接狀態
            statusIndicator.setBackground(new Color(0, 200, 0));
            statusLabel.setText("已連接");

            ipAddressValue.setText(connection.getIp());
            portValue.setText(String.valueOf(connection.getPort()));

            String serialNumber = connection.getSerialNumber();
            serialNumberValue.setText(serialNumber != null ? serialNumber : "--");

            String appVersion = connection.getAppVersion();
            appVersionValue.setText(appVersion != null ? appVersion : "--");

            String baseVersion = connection.getBaseVersion();
            baseVersionValue.setText(baseVersion != null ? baseVersion : "--");

            int maxPower = connection.getMaxPower();
            maxPowerValue.setText(maxPower > 0 ? maxPower + " dBm" : "--");

            int minPower = connection.getMinPower();
            minPowerValue.setText(minPower > 0 ? minPower + " dBm" : "--");

            int antennaCount = connection.getAntennaCount();
            antennaCountValue.setText(antennaCount > 0 ? String.valueOf(antennaCount) : "--");

            // 更新天線功率表格
            powerTableModel.setRowCount(0);
            Hashtable<Integer, Integer> powers = connection.getAntennaPowers();
            if (powers != null) {
                powers.forEach((antenna, power) -> {
                    powerTableModel.addRow(new Object[]{"天線 " + antenna, power + " dBm"});
                });
            }
        }
    }
}
