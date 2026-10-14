package com.example.views;

import com.example.rcp.RcpMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/** Stage 2 — the Orders {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "orders", layout = MainLayout.class)
public class OrdersView extends Div {

    public OrdersView(RcpMode rcpMode) {
        setSizeFull();
        add(rcpMode.topComponent("OrdersTopComponent"));
    }
}
