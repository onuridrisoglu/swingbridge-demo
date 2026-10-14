package com.example.views;

import com.example.rcp.RcpMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/** Stage 2 — the Products {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "products", layout = MainLayout.class)
public class ProductsView extends Div {

    public ProductsView(RcpMode rcpMode) {
        setSizeFull();
        add(rcpMode.topComponent("ProductsTopComponent"));
    }
}
