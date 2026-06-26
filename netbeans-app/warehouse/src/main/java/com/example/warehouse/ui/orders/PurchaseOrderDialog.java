package com.example.warehouse.ui.orders;

import com.example.warehouse.model.*;
import com.example.warehouse.service.ProductService;
import com.example.warehouse.service.PurchaseOrderService;
import com.example.warehouse.service.SupplierService;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PurchaseOrderDialog extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PurchaseOrder existing;
    private final PurchaseOrderService orderService = new PurchaseOrderService();
    private final SupplierService supplierService = new SupplierService();
    private final ProductService productService = new ProductService();
    private boolean saved = false;

    private JComboBox<Supplier> supplierBox;
    private JTextField orderDateField;
    private JTextField expectedDateField;
    private JComboBox<PurchaseOrder.OrderStatus> statusBox;
    private JTextArea notesArea;

    private DefaultTableModel itemTableModel;
    private JTable itemTable;
    private final List<PurchaseOrderItem> items = new ArrayList<>();
    private JLabel totalLabel;
    private boolean updatingTotal = false;

    public PurchaseOrderDialog(Frame parent, PurchaseOrder order) {
        super(parent, order == null ? "New Purchase Order" : "Purchase Order — PO-" + String.format("%04d", order.getId()), true);
        this.existing = order;
        setSize(700, 580);
        setLocationRelativeTo(parent);
        setResizable(true);
        buildUI();
        if (existing != null) populateFields();
        else orderDateField.setText(LocalDate.now().format(DATE_FMT));
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel(existing == null ? "New Purchase Order" : "Edit Purchase Order");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);

        content.add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildHeaderPanel(), buildItemsPanel());
        split.setDividerLocation(180);
        split.setResizeWeight(0.35);
        split.setBorder(null);
        content.add(split, BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(UIConstants.sectionBorder("Order Details"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        List<Supplier> suppliers = supplierService.findAll();
        supplierBox = new JComboBox<>(suppliers.toArray(new Supplier[0]));
        supplierBox.setFont(UIConstants.FONT_REGULAR);

        orderDateField    = new JTextField(10);
        expectedDateField = new JTextField(10);
        orderDateField.setFont(UIConstants.FONT_REGULAR);
        expectedDateField.setFont(UIConstants.FONT_REGULAR);

        statusBox = new JComboBox<>(PurchaseOrder.OrderStatus.values());
        statusBox.setFont(UIConstants.FONT_REGULAR);

        notesArea = new JTextArea(3, 30);
        notesArea.setFont(UIConstants.FONT_REGULAR);
        notesArea.setLineWrap(true);

        int row = 0;
        addRow(panel, c, row++, "Supplier: *",      supplierBox);
        addRow(panel, c, row++, "Order Date: *",    orderDateField);
        addRow(panel, c, row++, "Expected Date:",   expectedDateField);
        addRow(panel, c, row++, "Status:",          statusBox);

        c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.NORTHEAST; c.weightx = 0;
        panel.add(UIConstants.fieldLabel("Notes:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1.0;
        panel.add(new JScrollPane(notesArea), c);

        return panel;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, Component field) {
        c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        form.add(UIConstants.fieldLabel(label), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1.0;
        form.add(field, c);
    }

    private JPanel buildItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(UIConstants.sectionBorder("Line Items"));

        // Items toolbar
        JPanel itemToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        itemToolbar.setBackground(UIConstants.TOOLBAR_BG);

        JButton addItemBtn = UIConstants.toolbarButton("+ Add Item");
        JButton removeItemBtn = UIConstants.toolbarButton("Remove");

        addItemBtn.addActionListener(e -> addItem());
        removeItemBtn.addActionListener(e -> removeItem());
        itemToolbar.add(addItemBtn);
        itemToolbar.add(removeItemBtn);

        // Items table
        String[] cols = {"Product SKU", "Product Name", "Quantity", "Unit Price ($)", "Line Total"};
        itemTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3;
            }
        };

        itemTable = new JTable(itemTableModel);
        UIConstants.styleTable(itemTable);

        itemTableModel.addTableModelListener(e -> updateTotal());

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(UIConstants.FONT_HEADER);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 8));

        panel.add(itemToolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void addItem() {
        List<Product> products = productService.findAll();
        Product[] productArr = products.toArray(new Product[0]);
        Product selected = (Product) JOptionPane.showInputDialog(this,
                "Select product to add:", "Add Line Item",
                JOptionPane.PLAIN_MESSAGE, null, productArr,
                productArr.length > 0 ? productArr[0] : null);
        if (selected == null) return;

        PurchaseOrderItem item = new PurchaseOrderItem(selected, 1, selected.getUnitPrice());
        items.add(item);
        itemTableModel.addRow(new Object[]{
            selected.getSku(), selected.getName(), 1,
            selected.getUnitPrice().toPlainString(), selected.getUnitPrice().toPlainString()
        });
    }

    private void removeItem() {
        int row = itemTable.getSelectedRow();
        if (row < 0) return;
        items.remove(row);
        itemTableModel.removeRow(row);
        updateTotal();
    }

    private void updateTotal() {
        if (updatingTotal) return;
        updatingTotal = true;
        try {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < itemTableModel.getRowCount(); i++) {
                try {
                    Object qtyObj   = itemTableModel.getValueAt(i, 2);
                    Object priceObj = itemTableModel.getValueAt(i, 3);
                    int qty          = Integer.parseInt(qtyObj.toString());
                    BigDecimal price = new BigDecimal(priceObj.toString());
                    BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
                    itemTableModel.setValueAt(lineTotal.toPlainString(), i, 4);
                    total = total.add(lineTotal);
                } catch (Exception ignored) {}
            }
            NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);
            totalLabel.setText("Order Total: " + fmt.format(total));
        } finally {
            updatingTotal = false;
        }
    }

    private void populateFields() {
        for (int i = 0; i < supplierBox.getItemCount(); i++) {
            if (supplierBox.getItemAt(i).getId().equals(existing.getSupplier().getId())) {
                supplierBox.setSelectedIndex(i);
                break;
            }
        }
        orderDateField.setText(existing.getOrderDate().format(DATE_FMT));
        expectedDateField.setText(existing.getExpectedDate() != null ? existing.getExpectedDate().format(DATE_FMT) : "");
        statusBox.setSelectedItem(existing.getStatus());
        notesArea.setText(existing.getNotes() != null ? existing.getNotes() : "");

        items.clear();
        for (PurchaseOrderItem item : existing.getItems()) {
            items.add(item);
            itemTableModel.addRow(new Object[]{
                item.getProduct().getSku(), item.getProduct().getName(),
                item.getQuantity(), item.getUnitPrice().toPlainString(),
                item.getLineTotal().toPlainString()
            });
        }
        updateTotal();
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 225)));

        JButton saveBtn   = UIConstants.primaryButton("Save Order");
        JButton cancelBtn = UIConstants.toolbarButton("Cancel");

        saveBtn.addActionListener(e -> doSave());
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(cancelBtn);
        return panel;
    }

    private void doSave() {
        String dateStr = orderDateField.getText().trim();
        if (supplierBox.getSelectedItem() == null || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier and Order Date are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate orderDate;
        try {
            orderDate = LocalDate.parse(dateStr, DATE_FMT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Order date must be in format yyyy-MM-dd.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate expectedDate = null;
        String expStr = expectedDateField.getText().trim();
        if (!expStr.isEmpty()) {
            try {
                expectedDate = LocalDate.parse(expStr, DATE_FMT);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Expected date must be in format yyyy-MM-dd.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Sync item quantities/prices from table
        for (int i = 0; i < items.size() && i < itemTableModel.getRowCount(); i++) {
            try {
                items.get(i).setQuantity(Integer.parseInt(itemTableModel.getValueAt(i, 2).toString()));
                items.get(i).setUnitPrice(new BigDecimal(itemTableModel.getValueAt(i, 3).toString()));
            } catch (Exception ignored) {}
        }

        PurchaseOrder order = existing != null ? existing : new PurchaseOrder();
        order.setSupplier((Supplier) supplierBox.getSelectedItem());
        order.setOrderDate(orderDate);
        order.setExpectedDate(expectedDate);
        order.setStatus((PurchaseOrder.OrderStatus) statusBox.getSelectedItem());
        order.setNotes(notesArea.getText().trim());

        if (existing == null) {
            for (PurchaseOrderItem item : items) order.addItem(item);
            try {
                orderService.save(order);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save order:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            try {
                orderService.update(order);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update order:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
}
