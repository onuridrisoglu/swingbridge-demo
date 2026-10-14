package com.example.rcp;

import com.example.views.DashboardView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Stage 1 — the entire, unchanged WarehousePro <strong>NetBeans RCP</strong>
 * application rendered in the browser.
 *
 * <p>{@code NetBeansRcpBridge} boots the pre-built NetBeans Platform distribution
 * (discovered via {@code -Dswingbridge.rcp.cluster.dir}) server-side, in an isolated
 * classloader/thread-group per Vaadin session, and streams its full shell — menu
 * bar, toolbar and all five docked {@code TopComponent}s — to the browser. No RCP
 * source changes are required.</p>
 *
 * <p>Only active when {@code warehouse.rcp.mode=full} (see {@link RcpMode}). There is
 * no Vaadin login in front of it: the RCP shows its own desktop login dialog. In
 * {@code views} mode this route forwards to the Vaadin shell's dashboard.</p>
 */
@Route(value = "")
public class WarehouseRcpView extends VerticalLayout implements BeforeEnterObserver {

    private final RcpMode rcpMode;

    public WarehouseRcpView(RcpMode rcpMode) {
        this.rcpMode = rcpMode;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        if (rcpMode.mode() == RcpMode.Mode.FULL) {
            Component rcp = rcpMode.fullShell();
            add(rcp);
            expand(rcp);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (rcpMode.mode() == RcpMode.Mode.VIEWS) {
            event.forwardTo(DashboardView.class);
        }
    }
}
