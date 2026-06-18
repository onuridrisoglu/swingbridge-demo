package com.example.warehouse.ui.suppliers;

import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.SupplierService;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.*;
import java.awt.*;

public class SupplierDialog extends JDialog {

    private final Supplier existing;
    private final SupplierService service = new SupplierService();
    private boolean saved = false;

    private JTextField nameField;
    private JTextField contactField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;

    public SupplierDialog(Frame parent, Supplier supplier) {
        super(parent, supplier == null ? "Add New Supplier" : "Edit Supplier — " + supplier.getName(), true);
        this.existing = supplier;
        setSize(450, 370);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        if (existing != null) populateFields();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel(existing == null ? "New Supplier" : "Edit Supplier");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);

        content.add(title, BorderLayout.NORTH);
        content.add(buildForm(), BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(UIConstants.sectionBorder("Supplier Information"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        nameField    = new JTextField(24);
        contactField = new JTextField(24);
        emailField   = new JTextField(24);
        phoneField   = new JTextField(16);
        addressArea  = new JTextArea(3, 24);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        for (JComponent f : new JComponent[]{nameField, contactField, emailField, phoneField, addressArea}) {
            f.setFont(UIConstants.FONT_REGULAR);
        }

        addRow(form, c, 0, "Company Name: *", nameField);
        addRow(form, c, 1, "Contact Person:", contactField);
        addRow(form, c, 2, "Email:",          emailField);
        addRow(form, c, 3, "Phone:",          phoneField);

        c.gridx = 0; c.gridy = 4; c.anchor = GridBagConstraints.NORTHEAST; c.weightx = 0;
        form.add(UIConstants.fieldLabel("Address:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1.0;
        form.add(new JScrollPane(addressArea), c);

        return form;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, Component field) {
        c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        form.add(UIConstants.fieldLabel(label), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1.0;
        form.add(field, c);
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
        nameField.setText(existing.getName());
        contactField.setText(existing.getContactName() != null ? existing.getContactName() : "");
        emailField.setText(existing.getEmail() != null ? existing.getEmail() : "");
        phoneField.setText(existing.getPhone() != null ? existing.getPhone() : "");
        addressArea.setText(existing.getAddress() != null ? existing.getAddress() : "");
    }

    private void doSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Company name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Supplier s = existing != null ? existing : new Supplier();
        s.setName(name);
        s.setContactName(contactField.getText().trim());
        s.setEmail(emailField.getText().trim());
        s.setPhone(phoneField.getText().trim());
        s.setAddress(addressArea.getText().trim());
        try {
            if (existing == null) service.save(s);
            else service.update(s);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
