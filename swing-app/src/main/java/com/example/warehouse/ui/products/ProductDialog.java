package com.example.warehouse.ui.products;

import com.example.warehouse.dao.CategoryDAO;
import com.example.warehouse.model.Category;
import com.example.warehouse.model.Product;
import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.ProductService;
import com.example.warehouse.service.SupplierService;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductDialog extends JDialog {

    private final Product existing;
    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierService supplierService = new SupplierService();

    private boolean saved = false;

    private JTextField skuField;
    private JTextField nameField;
    private JComboBox<Category> categoryBox;
    private JComboBox<Supplier> supplierBox;
    private JTextArea descriptionArea;
    private JTextField priceField;
    private JSpinner quantitySpinner;
    private JSpinner reorderSpinner;
    private JTextField unitField;

    public ProductDialog(Frame parent, Product product) {
        super(parent, product == null ? "Add New Product" : "Edit Product — " + product.getSku(), true);
        this.existing = product;

        setSize(480, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        buildUI();
        if (existing != null) populateFields();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        content.add(buildTitleBar(), BorderLayout.NORTH);
        content.add(buildForm(), BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    private JLabel buildTitleBar() {
        String title = existing == null ? "New Product" : "Edit Product";
        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_TITLE);
        lbl.setForeground(UIConstants.PRIMARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(UIConstants.sectionBorder("Product Details"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        skuField   = new JTextField(12);
        nameField  = new JTextField(24);
        priceField = new JTextField(10);
        unitField  = new JTextField(8);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        reorderSpinner  = new JSpinner(new SpinnerNumberModel(5, 0, 999999, 1));
        descriptionArea = new JTextArea(3, 24);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(UIConstants.FONT_REGULAR);

        List<Category> categories = categoryDAO.findAll();
        categoryBox = new JComboBox<>(categories.toArray(new Category[0]));

        List<Supplier> suppliers = supplierService.findAll();
        Supplier[] supplierArr = new Supplier[suppliers.size() + 1];
        supplierArr[0] = null;
        for (int i = 0; i < suppliers.size(); i++) supplierArr[i + 1] = suppliers.get(i);
        supplierBox = new JComboBox<>(supplierArr);
        supplierBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean focus) {
                super.getListCellRendererComponent(l, v, i, sel, focus);
                setText(v == null ? "— None —" : v.toString());
                return this;
            }
        });

        applyFont(skuField, nameField, priceField, unitField, categoryBox, supplierBox);
        applyFont(quantitySpinner, reorderSpinner);

        int row = 0;
        addRow(form, c, row++, "SKU: *",         skuField);
        addRow(form, c, row++, "Name: *",         nameField);
        addRow(form, c, row++, "Category: *",     categoryBox);
        addRow(form, c, row++, "Supplier:",       supplierBox);
        addRow(form, c, row++, "Unit Price ($):*",priceField);
        addRow(form, c, row++, "Quantity:",       quantitySpinner);
        addRow(form, c, row++, "Reorder Level:",  reorderSpinner);
        addRow(form, c, row++, "Unit:",           unitField);

        c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.NORTHEAST;
        form.add(UIConstants.fieldLabel("Description:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        form.add(new JScrollPane(descriptionArea), c);

        return form;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, Component field) {
        c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        form.add(UIConstants.fieldLabel(label), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1.0;
        form.add(field, c);
    }

    private void applyFont(JComponent... components) {
        for (JComponent comp : components) comp.setFont(UIConstants.FONT_REGULAR);
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 225)));

        JButton saveBtn   = UIConstants.primaryButton("Save");
        JButton cancelBtn = UIConstants.toolbarButton("Cancel");

        saveBtn.addActionListener(e -> doSave());
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(cancelBtn);
        return panel;
    }

    private void populateFields() {
        skuField.setText(existing.getSku());
        nameField.setText(existing.getName());
        priceField.setText(existing.getUnitPrice().toPlainString());
        quantitySpinner.setValue(existing.getQuantity());
        reorderSpinner.setValue(existing.getReorderLevel());
        unitField.setText(existing.getUnit() != null ? existing.getUnit() : "");
        descriptionArea.setText(existing.getDescription() != null ? existing.getDescription() : "");

        for (int i = 0; i < categoryBox.getItemCount(); i++) {
            if (categoryBox.getItemAt(i).getId().equals(existing.getCategory().getId())) {
                categoryBox.setSelectedIndex(i);
                break;
            }
        }

        if (existing.getSupplier() != null) {
            for (int i = 0; i < supplierBox.getItemCount(); i++) {
                Supplier s = supplierBox.getItemAt(i);
                if (s != null && s.getId().equals(existing.getSupplier().getId())) {
                    supplierBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void doSave() {
        String sku  = skuField.getText().trim();
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();

        if (sku.isEmpty() || name.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "SKU, Name, and Unit Price are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Unit Price must be a valid positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product p = existing != null ? existing : new Product();
        p.setSku(sku);
        p.setName(name);
        p.setCategory((Category) categoryBox.getSelectedItem());
        p.setSupplier((Supplier) supplierBox.getSelectedItem());
        p.setUnitPrice(price);
        p.setQuantity((Integer) quantitySpinner.getValue());
        p.setReorderLevel((Integer) reorderSpinner.getValue());
        p.setUnit(unitField.getText().trim());
        p.setDescription(descriptionArea.getText().trim());

        try {
            if (existing == null) productService.save(p);
            else productService.update(p);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save product:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
