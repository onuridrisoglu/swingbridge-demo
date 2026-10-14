package com.example.warehouse.ui.stock;

import com.example.warehouse.dao.StockMovementDAO;
import com.example.warehouse.model.StockMovement;
import com.example.warehouse.ui.util.Status;
import com.example.warehouse.ui.util.TableExporter;
import com.example.warehouse.ui.util.UIConstants;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Stock Movements screen — read-only audit log with movement-type colour-coding,
 * live search and CSV export. Ported from the Swing {@code StockMovementsPanel}.
 */
@TopComponent.Description(
        preferredID = "StockTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 500)
@ActionID(category = "Window", id = "com.example.warehouse.ui.stock.StockTopComponent")
@ActionReference(path = "Menu/Window", position = 3329)
@TopComponent.OpenActionRegistration(displayName = "Stock Log", preferredID = "StockTopComponent")
public final class StockTopComponent extends TopComponent {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final StockMovementDAO dao = new StockMovementDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<StockMovement> movements = new ArrayList<>();

    public StockTopComponent() {
        setName("Stock Log");
        setToolTipText("Read-only stock movement audit log");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.TOOLBAR_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);

        JButton refreshBtn = UIConstants.toolbarButton("↺ Refresh");
        refreshBtn.addActionListener(e -> refresh());

        JLabel note = new JLabel("Read-only log. Use Products → Stock Adjust to record movements.");
        note.setFont(new Font("Tahoma", Font.ITALIC, 11));
        note.setForeground(new Color(100, 110, 130));

        left.add(refreshBtn);

        JButton exportBtn = UIConstants.toolbarButton("⬇ Export CSV");
        exportBtn.addActionListener(e -> TableExporter.exportToCsv(table, "stock-movements.csv", this));
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(exportBtn);
        left.add(Box.createHorizontalStrut(12));
        left.add(note);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        right.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(18);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterTable(searchField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                filterTable(searchField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                filterTable(searchField.getText());
            }
        });
        right.add(searchField);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Date / Time", "SKU", "Product Name", "Category", "Type", "Quantity", "Reason", "Created By"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UIConstants.styleTable(table);
        table.setDefaultRenderer(Object.class, new MovementTypeRenderer());

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        int[] widths = {130, 80, 200, 130, 90, 75, 250, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        return new JScrollPane(table);
    }

    private void filterTable(String text) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text.trim()));
        }
    }

    public void refresh() {
        movements = dao.findAll();
        tableModel.setRowCount(0);
        for (StockMovement m : movements) {
            tableModel.addRow(new Object[]{
                m.getMovementDate().format(DATE_FMT),
                m.getProduct().getSku(),
                m.getProduct().getName(),
                m.getProduct().getCategory().getName(),
                m.getMovementType().toString(),
                m.getQuantity(),
                m.getReason() != null ? m.getReason() : "—",
                m.getCreatedBy() != null ? m.getCreatedBy() : "system"
            });
        }
        Status.rowCount(movements.size(), "stock movement");
    }

    private class MovementTypeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel) {
                int modelRow = t.convertRowIndexToModel(row);
                if (modelRow < movements.size()) {
                    StockMovement.MovementType type = movements.get(modelRow).getMovementType();
                    c.setBackground(switch (type) {
                        case IN -> new Color(235, 252, 235);
                        case OUT -> new Color(252, 240, 235);
                        case ADJUSTMENT -> row % 2 == 0 ? Color.WHITE : UIConstants.ROW_ALT;
                    });
                }
                if (col == 4) {
                    setFont(UIConstants.FONT_BOLD);
                    String val = v != null ? v.toString() : "";
                    setForeground(switch (val) {
                        case "In" -> UIConstants.SUCCESS;
                        case "Out" -> UIConstants.DANGER;
                        default -> UIConstants.WARNING;
                    });
                } else if (col == 5) {
                    setHorizontalAlignment(CENTER);
                    setFont(UIConstants.FONT_REGULAR);
                    setForeground(Color.BLACK);
                } else {
                    setHorizontalAlignment(LEFT);
                    setFont(UIConstants.FONT_REGULAR);
                    setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }
}
