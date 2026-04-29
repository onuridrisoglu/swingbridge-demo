package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@Route(value = "products", layout = MainLayout.class)
public class ProductsView extends Div {

    public ProductsView() {
        setSizeFull();
        SwingBridge bridge = new SwingBridge("com.example.warehouse.bridge.ProductsBridge");
        bridge.setSizeFull();
        add(bridge);
    }
}
