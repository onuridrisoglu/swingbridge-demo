package com.example.suppliers;

import com.example.warehouse.bridge.SupplierEditRequestEvent;
import com.example.warehouse.bridge.SupplierEditRequestListener;
import com.example.warehouse.bridge.SupplierSelectedEvent;
import com.example.warehouse.bridge.SupplierSelectionListener;
import com.example.warehouse.ui.MainFrameBridge;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import com.vaadin.swingbridge.SwingBridge;
import com.vaadin.swingbridge.interop.BridgeHandle;
import com.vaadin.swingbridge.interop.VaadinCallback;

import java.util.function.Consumer;

/**
 * The Swing suppliers table as a standalone Flow component, small enough to
 * export as a web component and embed in a React view.
 *
 * <p>This is the whole of the Flow surface for the Suppliers screen. Everything
 * else — toolbar, dialogs, CRUD — is React in {@code views/suppliers.tsx}, which
 * embeds this through {@link SupplierBridgeExporter}. That makes it an
 * <em>embedded Vaadin application</em> running in its own {@code UI}, and two
 * things about that are worth knowing because they were not obvious:
 *
 * <ul>
 *   <li>Push still carries the bridge's frames — no {@code @Push} on the
 *   exporter is needed on top of the one on {@code Application}.</li>
 *   <li>{@code SwingBridge.interop} resolves the session's <em>existing</em>
 *   Swing app rather than booting a second one, so the embedded UI does not
 *   double the Swing footprint.</li>
 * </ul>
 *
 * <p>It deliberately knows nothing about the surrounding UI: it exposes two
 * callbacks and two commands, and the exporter maps those onto DOM events and
 * properties.
 */
public class SupplierBridgePanel extends Div {

    private static final String MAIN_CLASS = "com.example.warehouse.bridge.BridgeMain";

    private final BridgeHandle<MainFrameBridge> handle;
    private Registration callbackRegistration;

    /** Set by the exporter; receives the selected id, or null on deselection. */
    private Consumer<Long> selectionListener = id -> { };

    /** Set by the exporter; fires when Swing asks for a supplier to be edited. */
    private Consumer<Long> editRequestListener = id -> { };

    /** Must be public and no-arg: the exporter instantiates it reflectively. */
    public SupplierBridgePanel() {
        setSizeFull();

        SwingBridge bridge = new SwingBridge(MAIN_CLASS);
        bridge.setSizeFull();
        add(bridge);

        handle = SwingBridge.interop(MAIN_CLASS).of(MainFrameBridge.class);
        handle.onReady(MainFrameBridge::showSuppliersTable);
    }

    void setSelectionListener(Consumer<Long> listener) {
        this.selectionListener = listener;
    }

    void setEditRequestListener(Consumer<Long> listener) {
        this.editRequestListener = listener;
    }

    void refresh() {
        handle.onReady(MainFrameBridge::refreshSuppliers);
    }

    void setFilter(String filter) {
        handle.onReady(b -> b.setSupplierFilter(filter == null ? "" : filter));
    }

    @Override
    protected void onAttach(AttachEvent e) {
        super.onAttach(e);
        callbackRegistration = handle.registerCallback(this);
    }

    @VaadinCallback(observerFor = SupplierSelectionListener.class)
    public void onSelectionChanged(SupplierSelectedEvent event) {
        selectionListener.accept(event.id);
    }

    /** A double-click in the Swing table asks the React side for the edit form. */
    @VaadinCallback(observerFor = SupplierEditRequestListener.class)
    public void onEditRequested(SupplierEditRequestEvent event) {
        editRequestListener.accept(event.id);
    }

    @Override
    protected void onDetach(DetachEvent e) {
        if (callbackRegistration != null) {
            callbackRegistration.remove();
            callbackRegistration = null;
        }
        super.onDetach(e);
    }
}
