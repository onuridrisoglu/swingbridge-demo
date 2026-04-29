package com.example.warehouse.ui.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class UIConstants {

    public static final Color PRIMARY      = new Color(31, 73, 125);
    public static final Color PRIMARY_DARK = new Color(17, 47, 88);
    public static final Color ACCENT       = new Color(70, 130, 180);
    public static final Color ROW_ALT      = new Color(240, 246, 252);
    public static final Color LOW_STOCK_BG = new Color(255, 243, 205);
    public static final Color CRITICAL_BG  = new Color(255, 220, 220);
    public static final Color SUCCESS      = new Color(0, 128, 0);
    public static final Color WARNING      = new Color(180, 100, 0);
    public static final Color DANGER       = new Color(180, 30, 30);
    public static final Color TOOLBAR_BG   = new Color(236, 240, 245);

    public static final Font FONT_REGULAR  = new Font("Tahoma", Font.PLAIN, 12);
    public static final Font FONT_BOLD     = new Font("Tahoma", Font.BOLD, 12);
    public static final Font FONT_HEADER   = new Font("Tahoma", Font.BOLD, 13);
    public static final Font FONT_TITLE    = new Font("Tahoma", Font.BOLD, 15);
    public static final Font FONT_KPI_VAL  = new Font("Tahoma", Font.BOLD, 22);
    public static final Font FONT_KPI_LBL  = new Font("Tahoma", Font.PLAIN, 10);
    public static final Font FONT_MONO     = new Font("Courier New", Font.PLAIN, 12);

    public static final int ROW_HEIGHT = 22;
    public static final int HEADER_HEIGHT = 26;

    public static Border sectionBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(190, 200, 215)),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_BOLD,
                PRIMARY);
    }

    public static Border emptyBorder(int v, int h) {
        return BorderFactory.createEmptyBorder(v, h, v, h);
    }

    public static JButton toolbarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(3, 10, 3, 10));
        return btn;
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(5, 16, 5, 16));
        btn.setOpaque(true);
        return btn;
    }

    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        return lbl;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(ROW_HEIGHT);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(210, 220, 235));
        table.getTableHeader().setForeground(PRIMARY_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, HEADER_HEIGHT));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private UIConstants() {}
}
