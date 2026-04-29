package com.example.warehouse.bridge;

import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.SupplierService;
import com.example.warehouse.shared.SupplierEditRequestEvent;
import com.example.warehouse.shared.SupplierSelectionEvent;
import com.example.warehouse.shared.SwingEventPublisher;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Table-only view of the supplier list — the toolbar lives on the Vaadin side.
 *
 * SwingBridge starts this frame via {@link #main}. The Vaadin side registers a
 * {@link SwingEventPublisher} by calling {@link #setEventPublisher}. Row
 * selection fires a {@link SupplierSelectionEvent} so the Vaadin toolbar can
 * enable its Edit/Delete buttons. The Vaadin toolbar calls
 * {@link #openAddDialog}, {@link #openEditDialog}, {@link #deleteSelected}, and
 * {@link #refreshTable} via {@code SwingEventBridge.callSwingMethod()}.
 */
public class SuppliersTableFrame extends JFrame {

    private static final String[] COLUMNS =
            {"ID", "Supplier Name", "Contact Person", "Email", "Phone", "Address"};

    private final SupplierService service = new SupplierService();
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Supplier> suppliers = new ArrayList<>();
    private SwingEventPublisher selectionPublisher;
    private SwingEventPublisher editRequestPublisher;

    // -----------------------------------------------------------------------
    // Entry point used by SwingBridge
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        BridgeUtil.init();
        SwingUtilities.invokeLater(SuppliersTableFrame::new);
    }

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public SuppliersTableFrame() {
        setUndecorated(true);
        setSize(1200, 650);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        UIConstants.styleTable(table);
        table.setDefaultRenderer(Object.class, new AlternatingRenderer());

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(280);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (selectionPublisher == null) return;
            if (viewRow >= 0) {
                Supplier s = suppliers.get(table.convertRowIndexToModel(viewRow));
                selectionPublisher.publish(new SupplierSelectionEvent(
                        String.valueOf(s.getId()),
                        s.getName(),
                        s.getContactName() != null ? s.getContactName() : "",
                        s.getEmail()       != null ? s.getEmail()       : "",
                        s.getPhone()       != null ? s.getPhone()       : "",
                        s.getAddress()     != null ? s.getAddress()     : ""));
            } else {
                selectionPublisher.publish(new SupplierSelectionEvent(null, null, null, null, null, null));
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && editRequestPublisher != null) {
                    int viewRow = table.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
                        table.setRowSelectionInterval(viewRow, viewRow);
                        Supplier s = suppliers.get(table.convertRowIndexToModel(viewRow));
                        editRequestPublisher.publish(new SupplierEditRequestEvent(
                                String.valueOf(s.getId()),
                                s.getName(),
                                s.getContactName() != null ? s.getContactName() : "",
                                s.getEmail()       != null ? s.getEmail()       : "",
                                s.getPhone()       != null ? s.getPhone()       : "",
                                s.getAddress()     != null ? s.getAddress()     : ""));
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        setVisible(true);
        refreshTable();
    }

    // -----------------------------------------------------------------------
    // Bridge API — called by the Vaadin side via SwingEventBridge
    // -----------------------------------------------------------------------

    public void setEventPublisher(SwingEventPublisher publisher) {
        this.selectionPublisher = publisher;
    }

    public void setEditRequestPublisher(SwingEventPublisher publisher) {
        this.editRequestPublisher = publisher;
    }

    public void setFilter(String text) {
        if (text == null || text.trim().isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text.trim()));
    }

    public void refreshTable() {
        suppliers = service.findAll();
        tableModel.setRowCount(0);
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                    s.getId(), s.getName(), s.getContactName(),
                    s.getEmail(), s.getPhone(), s.getAddress()
            });
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static class AlternatingRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.ROW_ALT);
            return c;
        }
    }
}
