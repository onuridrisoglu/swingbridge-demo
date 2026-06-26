package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.NetBeansRcpBridge;

/** Stage 2 — the Stock Log {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "stock", layout = MainLayout.class)
public class StockView extends Div {

    public StockView() {
        setSizeFull();
        NetBeansRcpBridge rcp = new NetBeansRcpBridge().embedTopComponent("StockTopComponent");
        rcp.setSizeFull();
        add(rcp);
    }
}
