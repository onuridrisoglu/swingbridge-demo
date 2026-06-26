package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.NetBeansRcpBridge;

/** Stage 2 — the Suppliers {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "suppliers", layout = MainLayout.class)
public class SuppliersView extends Div {

    public SuppliersView() {
        setSizeFull();
        NetBeansRcpBridge rcp = new NetBeansRcpBridge().embedTopComponent("SuppliersTopComponent");
        rcp.setSizeFull();
        add(rcp);
    }
}
