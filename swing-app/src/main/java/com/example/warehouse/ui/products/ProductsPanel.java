package com.example.warehouse.ui.products;

import com.example.warehouse.model.Product;
import com.example.warehouse.model.StockMovement;
import com.example.warehouse.service.ProductService;
import com.example.warehouse.ui.util.StatusBar;
import com.example.warehouse.ui.util.TableExporter;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductsPanel extends JPanel {

    private final ProductService service = new ProductService();
    private final StatusBar statusBar;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Product> products = new ArrayList<>();

    private JButton editBtn;
    private JButton deleteBtn;
    private JButton stockBtn;

    public ProductsPanel(StatusBar statusBar) {
        this.statusBar = statusBar;
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

        JButton addBtn = UIConstants.toolbarButton("+ Add Product");
        addBtn.setBackground(new Color(230, 245, 230));
        editBtn   = UIConstants.toolbarButton("Edit");
        deleteBtn = UIConstants.toolbarButton("Delete");
        stockBtn  = UIConstants.toolbarButton("± Stock Adjust");
        JButton refreshBtn = UIConstants.toolbarButton("↺ Refresh");

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        stockBtn.setEnabled(false);

        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        stockBtn.addActionListener(e -> openStockAdjustDialog());
        refreshBtn.addActionListener(e -> refresh());

        left.add(addBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(editBtn);
        left.add(deleteBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(stockBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(refreshBtn);

        JButton exportBtn = UIConstants.toolbarButton("⬇ Export CSV");
        exportBtn.addActionListener(e -> TableExporter.exportToCsv(table, "products.csv", this));
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(exportBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        right.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(18);
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterTable(searchField.getText()); }
            public void removeUpdate(DocumentEvent e)  { filterTable(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { filterTable(searchField.getText()); }
        });
        right.add(searchField);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"SKU", "Product Name", "Category", "Supplier", "On Hand", "Reorder", "Unit Price", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 4 || col == 5) return Integer.class;
                if (col == 6) return BigDecimal.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        UIConstants.styleTable(table);
        table.setDefaultRenderer(Object.class, new ProductRowRenderer());
        table.setDefaultRenderer(Integer.class, new ProductRowRenderer());
        table.setDefaultRenderer(BigDecimal.class, new ProductRowRenderer());

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Column widths
        int[] widths = {80, 200, 130, 180, 75, 75, 90, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRowCount() > 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
            stockBtn.setEnabled(sel);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });

        // Right-click context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem editItem   = new JMenuItem("Edit Product");
        JMenuItem deleteItem = new JMenuItem("Delete Product");
        JMenuItem stockItem  = new JMenuItem("Adjust Stock");
        editItem.addActionListener(e -> openEditDialog());
        deleteItem.addActionListener(e -> deleteSelected());
        stockItem.addActionListener(e -> openStockAdjustDialog());
        popup.add(editItem);
        popup.add(deleteItem);
        popup.addSeparator();
        popup.add(stockItem);
        table.setComponentPopupMenu(popup);

        return new JScrollPane(table);
    }

    private void filterTable(String text) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text.trim()));
        }
        statusBar.setMessage(table.getRowCount() + " of " + products.size() + " products shown");
    }

    public void refresh() {
        products = service.findAll();
        tableModel.setRowCount(0);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getSku(),
                p.getName(),
                p.getCategory().getName(),
                p.getSupplier() != null ? p.getSupplier().getName() : "—",
                p.getQuantity(),
                p.getReorderLevel(),
                p.getUnitPrice(),
                p.isLowStock() ? (p.getQuantity() == 0 ? "OUT OF STOCK" : "Low Stock") : "OK"
            });
        }
        statusBar.setRowCount(products.size(), "product");
    }

    private Product getSelectedProduct() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return products.get(modelRow);
    }

    private void openAddDialog() {
        ProductDialog dlg = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void openEditDialog() {
        Product p = getSelectedProduct();
        if (p == null) return;
        ProductDialog dlg = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), p);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void deleteSelected() {
        Product p = getSelectedProduct();
        if (p == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete product '" + p.getName() + "' (" + p.getSku() + ")?\n\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(p.getId());
                refresh();
                statusBar.setMessage("Product '" + p.getName() + "' deleted.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete: product may be referenced by orders or stock movements.",
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openStockAdjustDialog() {
        Product p = getSelectedProduct();
        if (p == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Stock Adjustment — " + p.getName(), true);
        dlg.setSize(380, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; content.add(UIConstants.fieldLabel("Product:"), c);
        c.gridx = 1; JLabel pName = new JLabel(p.getName()); pName.setFont(UIConstants.FONT_REGULAR); content.add(pName, c);

        c.gridx = 0; c.gridy = 1; content.add(UIConstants.fieldLabel("Current Qty:"), c);
        c.gridx = 1; JLabel curQty = new JLabel(String.valueOf(p.getQuantity())); curQty.setFont(UIConstants.FONT_BOLD); content.add(curQty, c);

        c.gridx = 0; c.gridy = 2; content.add(UIConstants.fieldLabel("Movement Type:"), c);
        c.gridx = 1;
        JComboBox<StockMovement.MovementType> typeBox = new JComboBox<>(StockMovement.MovementType.values());
        typeBox.setFont(UIConstants.FONT_REGULAR);
        content.add(typeBox, c);

        c.gridx = 0; c.gridy = 3; content.add(UIConstants.fieldLabel("Quantity:"), c);
        c.gridx = 1;
        SpinnerNumberModel spinModel = new SpinnerNumberModel(1, 0, 99999, 1);
        JSpinner qtySpinner = new JSpinner(spinModel);
        qtySpinner.setFont(UIConstants.FONT_REGULAR);
        content.add(qtySpinner, c);

        c.gridx = 0; c.gridy = 4; content.add(UIConstants.fieldLabel("Reason:"), c);
        c.gridx = 1;
        JTextField reasonField = new JTextField(20);
        reasonField.setFont(UIConstants.FONT_REGULAR);
        content.add(reasonField, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton applyBtn = UIConstants.primaryButton("Apply");
        JButton cancelBtn = UIConstants.toolbarButton("Cancel");
        applyBtn.addActionListener(e -> {
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please enter a reason.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            service.adjustStock(p, (StockMovement.MovementType) typeBox.getSelectedItem(),
                    (Integer) qtySpinner.getValue(), reason);
            refresh();
            dlg.dispose();
        });
        cancelBtn.addActionListener(e -> dlg.dispose());
        btnPanel.add(applyBtn);
        btnPanel.add(cancelBtn);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; content.add(btnPanel, c);
        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    // Row renderer: colors rows by stock status
    private class ProductRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel) {
                int modelRow = t.convertRowIndexToModel(row);
                if (modelRow < products.size()) {
                    Product p = products.get(modelRow);
                    if (p.getQuantity() == 0) {
                        c.setBackground(UIConstants.CRITICAL_BG);
                    } else if (p.isLowStock()) {
                        c.setBackground(UIConstants.LOW_STOCK_BG);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.ROW_ALT);
                    }
                }
                // Right-align numeric columns
                if (v instanceof Number) setHorizontalAlignment(CENTER);
                else setHorizontalAlignment(LEFT);

                // Color the Status column
                if (col == 7) {
                    String status = v != null ? v.toString() : "";
                    if ("OUT OF STOCK".equals(status)) setForeground(UIConstants.DANGER);
                    else if ("Low Stock".equals(status)) setForeground(UIConstants.WARNING);
                    else { setForeground(UIConstants.SUCCESS); }
                    setFont(UIConstants.FONT_BOLD);
                } else {
                    setForeground(Color.BLACK);
                    setFont(UIConstants.FONT_REGULAR);
                }
            }
            return c;
        }
    }
}
