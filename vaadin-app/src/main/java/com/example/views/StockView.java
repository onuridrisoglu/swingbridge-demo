package com.example.views;

import com.example.warehouse.ui.MainFrameBridge;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.SwingBridge;

@Route(value = "stock", layout = MainLayout.class)
public class StockView extends Div {

    private static final String MAIN_CLASS = "com.example.warehouse.bridge.BridgeMain";

    public StockView() {
        setSizeFull();
        SwingBridge bridge = new SwingBridge(MAIN_CLASS);
        add(bridge);

        SwingBridge.interop(MAIN_CLASS)
                   .of(MainFrameBridge.class)
                   .onReady(MainFrameBridge::showStock);
    }
}
