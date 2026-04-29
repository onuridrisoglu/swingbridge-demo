package com.example.warehouse.bridge;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.seed.DataSeeder;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.*;
import java.awt.*;

public class BridgeUtil {

    public static void init() {
        applyLookAndFeel();
        PersistenceUtil.init();
        DataSeeder.seedIfEmpty();
    }

    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusBase",      new Color(31, 73, 125));
            UIManager.put("nimbusBlueGrey",  new Color(90, 108, 135));
            UIManager.put("control",         new Color(228, 234, 242));
            UIManager.put("Table.font",          UIConstants.FONT_REGULAR);
            UIManager.put("TableHeader.font",     UIConstants.FONT_BOLD);
            UIManager.put("Table.background",     Color.WHITE);
            UIManager.put("Table.alternateRowColor", UIConstants.ROW_ALT);
            UIManager.put("Label.font",   UIConstants.FONT_REGULAR);
            UIManager.put("Button.font",  UIConstants.FONT_REGULAR);
            UIManager.put("ComboBox.font",UIConstants.FONT_REGULAR);
            UIManager.put("TextField.font", UIConstants.FONT_REGULAR);
            UIManager.put("TextArea.font",  UIConstants.FONT_REGULAR);
            UIManager.put("TabbedPane.font", UIConstants.FONT_BOLD);
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }
    }
}
