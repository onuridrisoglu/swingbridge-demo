package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.NetBeansRcpBridge;

/** Stage 2 — the Orders {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "orders", layout = MainLayout.class)
public class OrdersView extends Div {

    public OrdersView() {
        setSizeFull();
        NetBeansRcpBridge rcp = new NetBeansRcpBridge().embedTopComponent("OrdersTopComponent");
        rcp.setSizeFull();
        add(rcp);
    }
}
