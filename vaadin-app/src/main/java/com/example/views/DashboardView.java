package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends Div {

    public DashboardView() {
        setSizeFull();
        SwingBridge bridge = new SwingBridge("com.example.warehouse.bridge.DashboardBridge");
        bridge.setSizeFull();
        add(bridge);
    }
}
