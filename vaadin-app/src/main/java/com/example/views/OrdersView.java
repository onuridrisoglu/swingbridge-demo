package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@Route(value = "orders", layout = MainLayout.class)
public class OrdersView extends Div {

    public OrdersView() {
        setSizeFull();
        SwingBridge bridge = new SwingBridge("com.example.warehouse.bridge.OrdersBridge");
        bridge.setSizeFull();
        add(bridge);
    }
}
