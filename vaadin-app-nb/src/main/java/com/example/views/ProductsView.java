package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.NetBeansRcpBridge;

/** Stage 2 — the Products {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "products", layout = MainLayout.class)
public class ProductsView extends Div {

    public ProductsView() {
        setSizeFull();
        NetBeansRcpBridge rcp = new NetBeansRcpBridge().embedTopComponent("ProductsTopComponent");
        rcp.setSizeFull();
        add(rcp);
    }
}
