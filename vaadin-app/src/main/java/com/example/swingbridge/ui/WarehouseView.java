package com.example.swingbridge.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@Route(value = "")
public class WarehouseView extends VerticalLayout {
    private static final String MAIN_CLASS = "com.example.warehouse.Main";

    public WarehouseView() {
        add(new SwingBridge(MAIN_CLASS));
    }

}
