package com.example.views;

import com.example.warehouse.bridge.SupplierEditRequestEvent;
import com.example.warehouse.bridge.SupplierEditRequestListener;
import com.example.warehouse.bridge.SupplierSelectedEvent;
import com.example.warehouse.bridge.SupplierSelectionListener;
import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.SupplierService;
import com.example.warehouse.ui.MainFrameBridge;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.shared.Registration;
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
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.swingbridge.SwingBridge;
import com.vaadin.swingbridge.interop.BridgeHandle;
import com.vaadin.swingbridge.interop.VaadinCallback;

import java.io.ByteArrayInputStream;

@Route(value = "suppliers", layout = MainLayout.class)
public class SuppliersView extends VerticalLayout {

    private static final String MAIN_CLASS = "com.example.warehouse.bridge.BridgeMain";

    private final SupplierService service = new SupplierService();
    private final BridgeHandle<MainFrameBridge> handle;

    private final Button editBtn;
    private final Button deleteBtn;

    private Long selectedId;
    private Registration callbackRegistration;

    public SuppliersView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // --- Vaadin toolbar ---
        Button addBtn     = new Button("+ Add Supplier");
        editBtn           = new Button("Edit");
        deleteBtn         = new Button("Delete");
        Button refreshBtn = new Button("↺ Refresh");

        Button exportBtn  = new Button("⬇ Export CSV");
        Anchor exportAnchor = new Anchor(
                DownloadHandler.fromInputStream(e -> {
                    byte[] csv = service.exportToCsv();
                    return new DownloadResponse(
                            new ByteArrayInputStream(csv), "suppliers.csv", "text/csv", csv.length);
                }),
                "");
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
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addClassName("suppliers-toolbar");
        toolbar.addToStart(addBtn, editBtn, deleteBtn, refreshBtn, exportAnchor);
        toolbar.addToEnd(searchField);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setWidth("1200px");
        toolbar.getStyle().set("padding", "8px 12px").set("box-sizing", "border-box");

        // --- SwingBridge ---
        SwingBridge bridge = new SwingBridge(MAIN_CLASS);
        handle = SwingBridge.interop(MAIN_CLASS).of(MainFrameBridge.class);
        handle.onReady(MainFrameBridge::showSuppliersTable);

        // Button actions
        addBtn.addClickListener(e -> openForm(null));
        editBtn.addClickListener(e -> { if (selectedId != null) openForm(selectedId); });
        deleteBtn.addClickListener(e -> confirmDelete());
        refreshBtn.addClickListener(e -> handle.onReady(MainFrameBridge::refreshSuppliers));
        searchField.addValueChangeListener(e -> handle.onReady(b -> b.setSupplierFilter(e.getValue())));

        add(toolbar, bridge);
        setFlexGrow(1, bridge);
    }

    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        callbackRegistration = handle.registerCallback(this);
    }

    @VaadinCallback(observerFor = SupplierSelectionListener.class)
    public void onSelectionChanged(SupplierSelectedEvent event) {
        selectedId = event.id;
        editBtn.setEnabled(selectedId != null);
        deleteBtn.setEnabled(selectedId != null);
    }

    @VaadinCallback(observerFor = SupplierEditRequestListener.class)
    public void onEditRequested(SupplierEditRequestEvent event) {
        openForm(event.id);
    }

    @Override
    protected void onDetach(DetachEvent e) {
        if (callbackRegistration != null) {
            callbackRegistration.remove();
            callbackRegistration = null;
        }
        super.onDetach(e);
    }

    private void openForm(Long existingId) {
        Supplier existing = existingId != null ? service.findById(existingId) : null;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingId == null ? "Add Supplier" : "Edit Supplier");
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
            nameField.setValue(existing.getName() != null ? existing.getName() : "");
            contactField.setValue(existing.getContactName() != null ? existing.getContactName() : "");
            emailField.setValue(existing.getEmail() != null ? existing.getEmail() : "");
            phoneField.setValue(existing.getPhone() != null ? existing.getPhone() : "");
            addressField.setValue(existing.getAddress() != null ? existing.getAddress() : "");
        }

        FormLayout form = new FormLayout(nameField, contactField, emailField, phoneField, addressField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        dialog.add(form);

        Button cancelBtn = new Button("Cancel", ev -> dialog.close());
        Button saveBtn   = new Button("Save", ev -> {
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
                existing.setName(nameField.getValue());
                existing.setContactName(contactField.getValue());
                existing.setEmail(emailField.getValue());
                existing.setPhone(phoneField.getValue());
                existing.setAddress(addressField.getValue());
                service.update(existing);
                notify("Supplier updated.", false);
            }
            dialog.close();
            handle.onReady(MainFrameBridge::refreshSuppliers);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void confirmDelete() {
        if (selectedId == null) return;
        Supplier s = service.findById(selectedId);
        if (s == null) return;

        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Delete Supplier");
        confirm.setText("Delete '" + s.getName() + "'? This cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(ev -> {
            try {
                service.delete(s.getId());
                selectedId = null;
                editBtn.setEnabled(false);
                deleteBtn.setEnabled(false);
                handle.onReady(MainFrameBridge::refreshSuppliers);
                notify("Supplier deleted.", false);
            } catch (Exception ex) {
                notify("Cannot delete: supplier may be referenced by products or orders.", true);
            }
        });
        confirm.open();
    }

    private static void notify(String message, boolean error) {
        Notification n = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(error ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
    }
}
