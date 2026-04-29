package com.example.warehouse;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.seed.DataSeeder;
import com.example.warehouse.ui.LoginFrame;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        applyLookAndFeel();

        // Init DB before any UI
        PersistenceUtil.init();
        DataSeeder.seedIfEmpty();

        SwingUtilities.invokeLater(LoginFrame::new);
    }

    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            // Enterprise blue overrides
            UIManager.put("nimbusBase",      new Color(31, 73, 125));
            UIManager.put("nimbusBlueGrey",  new Color(90, 108, 135));
            UIManager.put("control",         new Color(228, 234, 242));

            // Tables
            UIManager.put("Table.font",          UIConstants.FONT_REGULAR);
            UIManager.put("TableHeader.font",     UIConstants.FONT_BOLD);
            UIManager.put("Table.background",     Color.WHITE);
            UIManager.put("Table.alternateRowColor", UIConstants.ROW_ALT);

            // Labels & buttons
            UIManager.put("Label.font",   UIConstants.FONT_REGULAR);
            UIManager.put("Button.font",  UIConstants.FONT_REGULAR);
            UIManager.put("ComboBox.font",UIConstants.FONT_REGULAR);
            UIManager.put("TextField.font", UIConstants.FONT_REGULAR);
            UIManager.put("TextArea.font",  UIConstants.FONT_REGULAR);
            UIManager.put("TabbedPane.font", UIConstants.FONT_BOLD);

        } catch (Exception e) {
            // Fall back to system L&F
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }
}
