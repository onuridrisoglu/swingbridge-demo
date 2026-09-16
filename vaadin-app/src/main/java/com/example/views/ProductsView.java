package com.example.views;

import com.example.warehouse.ui.MainFrameBridge;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.SwingBridge;

@Route("products")
public class ProductsView extends Div {

    private static final String MAIN_CLASS = "com.example.warehouse.bridge.BridgeMain";

    public ProductsView() {
        setSizeFull();
        SwingBridge bridge = new SwingBridge(MAIN_CLASS);
        add(bridge);

        SwingBridge.interop(MAIN_CLASS)
                   .of(MainFrameBridge.class)
                   .onReady(MainFrameBridge::showProducts);
    }
}
