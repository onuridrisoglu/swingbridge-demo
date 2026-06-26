package com.example.rcp;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.NetBeansRcpBridge;

/**
 * Stage 1 — the entire, unchanged WarehousePro <strong>NetBeans RCP</strong>
 * application rendered in the browser.
 *
 * <p>{@code NetBeansRcpBridge} boots the pre-built NetBeans Platform distribution
 * (discovered via {@code -Dswingbridge.rcp.cluster.dir}) server-side, in an isolated
 * classloader/thread-group per Vaadin session, and streams its full shell — menu
 * bar, toolbar and all five docked {@code TopComponent}s — to the browser. No RCP
 * source changes are required.</p>
 */
@Route(value = "")
public class WarehouseRcpView extends VerticalLayout {

    public WarehouseRcpView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        NetBeansRcpBridge rcp = new NetBeansRcpBridge();
        rcp.setSizeFull();
        add(rcp);
        expand(rcp);
    }
}
