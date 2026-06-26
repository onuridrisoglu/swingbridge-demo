package com.example.warehouse.ui.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TableExporter {

    public static void exportToCsv(JTable table, String suggestedName, Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to CSV");
        chooser.setSelectedFile(new File(suggestedName));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Headers
            int colCount = table.getColumnCount();
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < colCount; c++) {
                if (c > 0) row.append(",");
                row.append(escape(table.getColumnName(c)));
            }
            pw.println(row);

            // Rows — iterate view rows so current sort/filter is respected
            for (int r = 0; r < table.getRowCount(); r++) {
                row.setLength(0);
                for (int c = 0; c < colCount; c++) {
                    if (c > 0) row.append(",");
                    Object val = table.getValueAt(r, c);
                    row.append(escape(val != null ? val.toString() : ""));
                }
                pw.println(row);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
