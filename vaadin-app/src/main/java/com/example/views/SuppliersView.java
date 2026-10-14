package com.example.views;

import com.example.rcp.RcpMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/** Stage 2 — the Suppliers {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "suppliers", layout = MainLayout.class)
public class SuppliersView extends Div {

    public SuppliersView(RcpMode rcpMode) {
        setSizeFull();
        add(rcpMode.topComponent("SuppliersTopComponent"));
    }
}
