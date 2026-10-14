package com.example.views;

import com.example.rcp.RcpMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Stage 2 — Vaadin shell (AppLayout + SideNav) hosting a single bridged NetBeans
 * {@code TopComponent}. Only the Dashboard window's content is rendered, without the
 * platform's own menu/toolbar, so it sits inside the native Vaadin chrome.
 */
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends Div {

    public DashboardView(RcpMode rcpMode) {
        setSizeFull();
        add(rcpMode.topComponent("DashboardTopComponent"));
    }
}
