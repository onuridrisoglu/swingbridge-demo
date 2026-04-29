package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@Route(value = "stock", layout = MainLayout.class)
public class StockView extends Div {

    public StockView() {
        setSizeFull();
        SwingBridge bridge = new SwingBridge("com.example.warehouse.bridge.StockBridge");
        bridge.setSizeFull();
        add(bridge);
    }
}
