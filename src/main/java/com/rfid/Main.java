package com.rfid;

import com.rfid.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * RFID 讀取頭控制系統
 * 應用程式入口
 */
public class Main {

    public static void main(String[] args) {
        // 設置系統外觀
        try {
            // 嘗試使用系統預設外觀
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("無法設置系統外觀: " + e.getMessage());
        }

        // 設置中文字體
        setChineseFont();

        // 在 EDT 執行緒中啟動 UI
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                System.out.println("RFID 讀取頭控制系統已啟動");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "啟動失敗: " + e.getMessage(),
                    "錯誤",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * 設置中文字體
     */
    private static void setChineseFont() {
        // 嘗試使用常見的中文字體
        String[] fontNames = {"Microsoft JhengHei", "微軟正黑體", "PingFang TC", "Heiti TC", "Dialog"};
        Font chineseFont = null;

        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.PLAIN, 12);
            if (font.canDisplay('中')) {
                chineseFont = font;
                break;
            }
        }

        if (chineseFont != null) {
            // 設置所有 Swing 元件使用中文字體
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) {
                    Font oldFont = (Font) value;
                    Font newFont = new Font(chineseFont.getFamily(), oldFont.getStyle(), oldFont.getSize());
                    UIManager.put(key, newFont);
                }
            }
        }
    }
}
