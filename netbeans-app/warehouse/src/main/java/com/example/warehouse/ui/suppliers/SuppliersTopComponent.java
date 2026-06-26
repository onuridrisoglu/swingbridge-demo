package com.example.warehouse.ui.suppliers;

import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.SupplierService;
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
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Suppliers screen — single-selection table with add/edit/delete, live search and
 * CSV export. Ported from the Swing {@code SuppliersPanel} (the SwingBridge
 * interop hooks are intentionally omitted from this standalone RCP edition).
 */
@TopComponent.Description(
        preferredID = "SuppliersTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = true, position = 300)
@ActionID(category = "Window", id = "com.example.warehouse.ui.suppliers.SuppliersTopComponent")
@ActionReference(path = "Menu/Window", position = 3315)
@TopComponent.OpenActionRegistration(displayName = "Suppliers", preferredID = "SuppliersTopComponent")
public final class SuppliersTopComponent extends TopComponent {

    private final SupplierService service = new SupplierService();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Supplier> suppliers = new ArrayList<>();

    private JButton editBtn;
    private JButton deleteBtn;

    public SuppliersTopComponent() {
        setName("Suppliers");
        setToolTipText("Supplier directory");

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

        JButton addBtn = UIConstants.toolbarButton("+ Add Supplier");
        editBtn = UIConstants.toolbarButton("Edit");
        deleteBtn = UIConstants.toolbarButton("Delete");
        JButton refreshBtn = UIConstants.toolbarButton("↺ Refresh");

        addBtn.setBackground(new Color(230, 245, 230));
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> refresh());

        left.add(addBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(editBtn);
        left.add(deleteBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(refreshBtn);

        JButton exportBtn = UIConstants.toolbarButton("⬇ Export CSV");
        exportBtn.addActionListener(e -> TableExporter.exportToCsv(table, "suppliers.csv", this));
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(exportBtn);

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
        String[] cols = {"ID", "Supplier Name", "Contact Person", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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
            if (e.getValueIsAdjusting()) {
                return;
            }
            boolean sel = table.getSelectedRowCount() > 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });

        JPopupMenu popup = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Supplier");
        JMenuItem delItem = new JMenuItem("Delete Supplier");
        editItem.addActionListener(e -> openEditDialog());
        delItem.addActionListener(e -> deleteSelected());
        popup.add(editItem);
        popup.add(delItem);
        table.setComponentPopupMenu(popup);

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
        suppliers = service.findAll();
        tableModel.setRowCount(0);
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getContactName(),
                s.getEmail(), s.getPhone(), s.getAddress()
            });
        }
        Status.rowCount(suppliers.size(), "supplier");
    }

    private Supplier getSelectedSupplier() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return suppliers.get(modelRow);
    }

    private Frame mainFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void openAddDialog() {
        SupplierDialog dlg = new SupplierDialog(mainFrame(), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
        }
    }

    private void openEditDialog() {
        Supplier s = getSelectedSupplier();
        if (s == null) {
            return;
        }
        SupplierDialog dlg = new SupplierDialog(mainFrame(), s);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
        }
    }

    private void deleteSelected() {
        Supplier s = getSelectedSupplier();
        if (s == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete supplier '" + s.getName() + "'?\n\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(s.getId());
                refresh();
                Status.message("Supplier '" + s.getName() + "' deleted.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete: supplier may be referenced by products or orders.",
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class AlternatingRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.ROW_ALT);
            }
            return c;
        }
    }
}
