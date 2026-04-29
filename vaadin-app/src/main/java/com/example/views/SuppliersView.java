package com.example.views;

import com.example.swingbridge.SwingEventBridge;
import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.SupplierService;
import com.example.warehouse.shared.SupplierEditRequestEvent;
import com.example.warehouse.shared.SupplierSelectionEvent;
import com.example.warehouse.shared.SwingEventPublisher;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Route(value = "suppliers", layout = MainLayout.class)
public class SuppliersView extends VerticalLayout {

    private final SupplierService service = new SupplierService();
    private SupplierSelectionEvent selectedEvent = null;

    public SuppliersView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // --- Vaadin toolbar ---
        Button addBtn     = new Button("+ Add Supplier");
        //<theme-editor-local-classname>
        addBtn.addClassName("suppliers-view-button-1");
        Button editBtn    = new Button("Edit");
        Button deleteBtn  = new Button("Delete");
        Button refreshBtn = new Button("↺ Refresh");
        StreamResource csvResource = new StreamResource("suppliers.csv", () -> {
            StringBuilder sb = new StringBuilder("ID,Supplier Name,Contact Person,Email,Phone,Address\r\n");
            for (Supplier s : service.findAll()) {
                sb.append(csvEscape(s.getId().toString())).append(',')
                  .append(csvEscape(s.getName())).append(',')
                  .append(csvEscape(s.getContactName())).append(',')
                  .append(csvEscape(s.getEmail())).append(',')
                  .append(csvEscape(s.getPhone())).append(',')
                  .append(csvEscape(s.getAddress())).append("\r\n");
            }
            return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        });
        Button exportBtn  = new Button("⬇ Export CSV");
        Anchor exportAnchor = new Anchor(csvResource, "");
        exportAnchor.getElement().setAttribute("download", true);
        exportAnchor.add(exportBtn);

        addBtn.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.SMALL);
        addBtn.addClassName("toolbar-btn-dark");
        editBtn.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.SMALL);
        editBtn.addClassName("toolbar-btn-dark");
        deleteBtn.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.PRIMARY, ButtonVariant.SMALL);
        deleteBtn.addClassName("toolbar-btn-delete");
        refreshBtn.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
        exportBtn.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_SMALL);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addClassName("suppliers-toolbar");
        toolbar.addToStart(addBtn, editBtn, deleteBtn, refreshBtn, exportAnchor);
        toolbar.addToEnd(searchField);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setWidth("1200px");
        toolbar.getStyle().set("padding", "8px 12px").set("box-sizing", "border-box");

        // --- SwingEventBridge: table-only Swing frame ---
        SwingEventBridge tableBridge =
                new SwingEventBridge("com.example.warehouse.bridge.SuppliersTableFrame")
                        .registerPublisher(SwingEventPublisher.class, "setEventPublisher")
                        .registerPublisher(SwingEventPublisher.class, "setEditRequestPublisher")
                        .addEventListener(SupplierSelectionEvent.class, event -> {
                            boolean selected = event.id != null;
                            selectedEvent = selected ? event : null;
                            editBtn.setEnabled(selected);
                            deleteBtn.setEnabled(selected);
                        });

        // Separate statement so tableBridge is definitely assigned when the lambda captures it
        tableBridge.addEventListener(SupplierEditRequestEvent.class, event -> {
            selectedEvent = new SupplierSelectionEvent(
                    event.id, event.name, event.contactName,
                    event.email, event.phone, event.address);
            editBtn.setEnabled(true);
            deleteBtn.setEnabled(true);
            openForm("Edit Supplier", selectedEvent, tableBridge);
        });

        // Add
        addBtn.addClickListener(e -> openForm("Add Supplier", null, tableBridge));

        // Edit
        editBtn.addClickListener(e -> {
            if (selectedEvent != null) openForm("Edit Supplier", selectedEvent, tableBridge);
        });

        // Delete
        deleteBtn.addClickListener(e -> {
            if (selectedEvent == null) return;
            ConfirmDialog confirm = new ConfirmDialog();
            confirm.setHeader("Delete Supplier");
            confirm.setText("Delete '" + selectedEvent.name + "'? This cannot be undone.");
            confirm.setCancelable(true);
            confirm.setConfirmText("Delete");
            confirm.setConfirmButtonTheme("error primary");
            confirm.addConfirmListener(ev -> {
                try {
                    service.delete(Long.parseLong(selectedEvent.id));
                    selectedEvent = null;
                    editBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                    tableBridge.callSwingMethod("refreshTable");
                    notify("Supplier deleted", false);
                } catch (Exception ex) {
                    notify("Cannot delete: supplier may be referenced by products or orders.", true);
                }
            });
            confirm.open();
        });

        // Refresh
        refreshBtn.addClickListener(e -> tableBridge.callSwingMethod("refreshTable"));

        // Search
        searchField.addValueChangeListener(e -> tableBridge.callSwingMethod("setFilter", e.getValue()));

        tableBridge.setSizeFull();

        add(toolbar, tableBridge);
        setFlexGrow(1, tableBridge);
    }

    private void openForm(String title, SupplierSelectionEvent existing, SwingEventBridge tableBridge) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("420px");

        TextField nameField    = new TextField("Supplier Name");
        TextField contactField = new TextField("Contact Person");
        TextField emailField   = new TextField("Email");
        TextField phoneField   = new TextField("Phone");
        TextArea  addressField = new TextArea("Address");

        nameField.setRequired(true);
        nameField.setWidthFull();
        contactField.setWidthFull();
        emailField.setWidthFull();
        phoneField.setWidthFull();
        addressField.setWidthFull();
        addressField.setMaxHeight("80px");

        if (existing != null) {
            nameField.setValue(existing.name);
            contactField.setValue(existing.contactName);
            emailField.setValue(existing.email);
            phoneField.setValue(existing.phone);
            addressField.setValue(existing.address);
        }

        FormLayout form = new FormLayout(nameField, contactField, emailField, phoneField, addressField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        dialog.add(form);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        Button saveBtn   = new Button("Save", e -> {
            if (nameField.isEmpty()) {
                notify("Supplier name is required.", true);
                return;
            }
            if (existing == null) {
                service.save(new Supplier(
                        nameField.getValue(), contactField.getValue(),
                        emailField.getValue(), phoneField.getValue(), addressField.getValue()));
                notify("Supplier added.", false);
            } else {
                Supplier s = service.findById(Long.parseLong(existing.id));
                s.setName(nameField.getValue());
                s.setContactName(contactField.getValue());
                s.setEmail(emailField.getValue());
                s.setPhone(phoneField.getValue());
                s.setAddress(addressField.getValue());
                service.update(s);
                notify("Supplier updated.", false);
            }
            dialog.close();
            tableBridge.callSwingMethod("refreshTable");
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private static String csvEscape(String val) {
        if (val == null) return "";
        String v = val.replace("\"", "\"\"");
        return (v.contains(",") || v.contains("\"") || v.contains("\n")) ? "\"" + v + "\"" : v;
    }

    private static void notify(String message, boolean error) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(error ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
    }
}
