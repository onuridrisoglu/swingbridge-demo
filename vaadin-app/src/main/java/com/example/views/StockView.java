package com.example.views;

import com.example.rcp.RcpMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/** Stage 2 — the Stock Log {@code TopComponent} embedded in the Vaadin shell. */
@Route(value = "stock", layout = MainLayout.class)
public class StockView extends Div {

    public StockView(RcpMode rcpMode) {
        setSizeFull();
        add(rcpMode.topComponent("StockTopComponent"));
    }
}
