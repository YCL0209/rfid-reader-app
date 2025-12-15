package com.rfid.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 主視窗框架
 * 整合主控制面板和狀態面板
 */
public class MainFrame extends JFrame {

    private MainControlPanel controlPanel;
    private StatusPanel statusPanel;
    private JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("RFID 讀取頭控制系統");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // 設置視窗圖示（如果有的話）
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 使用預設外觀
        }

        // 建立介面
        initComponents();

        // 處理視窗關閉事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    /**
     * 初始化元件
     */
    private void initComponents() {
        // 建立標籤頁面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Dialog", Font.PLAIN, 14));

        // 建立主控制面板
        controlPanel = new MainControlPanel();

        // 建立狀態面板
        statusPanel = new StatusPanel();

        // 設置連接狀態變更回調
        controlPanel.setOnConnectionChangedCallback(connection -> {
            statusPanel.updateStatus(connection);
        });

        // 添加標籤頁
        tabbedPane.addTab("主控制", createTabIcon(new Color(52, 152, 219)), controlPanel, "連接和讀取標籤");
        tabbedPane.addTab("設備狀態", createTabIcon(new Color(46, 204, 113)), statusPanel, "查看讀取頭狀態資訊");

        // 添加到主視窗
        add(tabbedPane, BorderLayout.CENTER);

        // 底部狀態列
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * 建立標籤頁圖示
     */
    private Icon createTabIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(x, y, 12, 12);
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }

    /**
     * 建立底部狀態列
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel versionLabel = new JLabel("RFID 讀取頭控制系統 v1.0.0");
        versionLabel.setForeground(Color.GRAY);
        panel.add(versionLabel, BorderLayout.WEST);

        JLabel copyrightLabel = new JLabel("基於原廠 Java API 開發");
        copyrightLabel.setForeground(Color.GRAY);
        panel.add(copyrightLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * 處理視窗關閉
     */
    private void handleWindowClosing() {
        // 確認是否關閉
        int result = JOptionPane.showConfirmDialog(
            this,
            "確定要關閉應用程式嗎？\n如果正在讀取標籤，將會自動停止。",
            "確認關閉",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            // 斷開連接
            if (controlPanel != null && controlPanel.getConnection() != null) {
                controlPanel.getConnection().disconnect();
            }
            // 關閉視窗
            dispose();
            System.exit(0);
        }
    }

    /**
     * 取得主控制面板
     */
    public MainControlPanel getControlPanel() {
        return controlPanel;
    }

    /**
     * 取得狀態面板
     */
    public StatusPanel getStatusPanel() {
        return statusPanel;
    }
}
