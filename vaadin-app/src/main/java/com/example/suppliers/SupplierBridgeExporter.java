package com.example.suppliers;

import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.webcomponent.EventOptions;
import com.vaadin.flow.component.webcomponent.WebComponent;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Exports {@link SupplierBridgePanel} as {@code <supplier-bridge>} so the React
 * view can embed it with {@code reactElement('supplier-bridge')}.
 *
 * <p>This is where the interop surface is re-expressed in web-component terms,
 * because React cannot hold a Flow reference or the bridge handle:
 *
 * <ul>
 *   <li><b>Swing → React</b> — selection and edit requests are fired as bubbling,
 *   composed DOM events, so React can listen on a wrapping element instead of
 *   needing a ref to the custom element.</li>
 *   <li><b>React → Swing</b> — properties. {@code filter} maps onto the Swing
 *   filter directly. {@code refreshToken} is a command dressed as state:
 *   properties only notify on <em>change</em>, so React increments a counter to
 *   ask for a reload.</li>
 * </ul>
 *
 * <p>{@code @Push} is required here and is easy to miss: the embedded component
 * runs in its own {@code UI}, which the {@code @Push} on {@code Application}
 * does not cover. Without it the Swing app boots server-side but its frames
 * never reach the browser, leaving the bridge on its loading spinner forever.
 * The documented limitation — one {@code @Push} per application — only bites
 * with several exporters; this application has one.
 */
@Push
public class SupplierBridgeExporter extends WebComponentExporter<SupplierBridgePanel> {

    public static final String TAG = "supplier-bridge";

    /** Required: the exporter is instantiated reflectively. */
    public SupplierBridgeExporter() {
        super(TAG);
        addProperty("filter", "").onChange(SupplierBridgePanel::setFilter);
        addProperty("refreshToken", 0).onChange((panel, token) -> {
            // Ignore the initial 0 so attaching doesn't trigger a reload.
            if (token != null && token > 0) {
                panel.refresh();
            }
        });
    }

    @Override
    protected void configureInstance(WebComponent<SupplierBridgePanel> webComponent,
                                     SupplierBridgePanel panel) {
        panel.setSelectionListener(id ->
                webComponent.fireEvent("supplier-selected", idDetail(id), bubbling()));
        panel.setEditRequestListener(id ->
                webComponent.fireEvent("supplier-edit-requested", idDetail(id), bubbling()));
    }

    private static ObjectNode idDetail(Long id) {
        ObjectNode detail = JsonNodeFactory.instance.objectNode();
        if (id == null) {
            detail.putNull("id");
        } else {
            detail.put("id", id);
        }
        return detail;
    }

    /** Bubbling and composed, so a listener on an ancestor element sees it. */
    private static EventOptions bubbling() {
        return new EventOptions(true, true, false);
    }
}
