package com.example.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.NetBeansRcpBridge;

/**
 * Stage 2 — Vaadin shell (AppLayout + SideNav) hosting a single bridged NetBeans
 * {@code TopComponent}. Only the Dashboard window's content is rendered, without the
 * platform's own menu/toolbar, so it sits inside the native Vaadin chrome.
 */
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends Div {

    public DashboardView() {
        setSizeFull();
        NetBeansRcpBridge rcp = new NetBeansRcpBridge().embedTopComponent("DashboardTopComponent");
        rcp.setSizeFull();
        add(rcp);
    }
}
