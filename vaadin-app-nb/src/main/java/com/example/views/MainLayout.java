package com.example.views;

import com.example.warehouse.AppSession;
import com.example.warehouse.model.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.sidenav.SideNavVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (VaadinSession.getCurrent().getAttribute("user") == null) {
            event.rerouteTo(LoginView.class);
        }
    }

    public MainLayout() {
        addClassName("main-layout");
        setPrimarySection(Section.NAVBAR);
        addToDrawer(buildDrawerHeader(), buildNav());
        addToNavbar(buildNavbar());
    }

    private Div buildDrawerHeader() {
        Div iconBox = new Div();
        iconBox.addClassName("drawer-icon-box");
        iconBox.getElement().setProperty("innerHTML", """
            <svg width="22" height="22" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="2" y="12" width="24" height="14" rx="1" stroke="#1b3a5c" stroke-width="1.8" fill="none"/>
              <polyline points="1,13 14,4 27,13" stroke="#1b3a5c" stroke-width="1.8" stroke-linejoin="round" fill="none"/>
              <rect x="10" y="18" width="8" height="8" rx="1" stroke="#1b3a5c" stroke-width="1.5" fill="none"/>
              <rect x="4"  y="16" width="4" height="3" rx="0.5" fill="#1b3a5c" opacity="0.5"/>
              <rect x="20" y="16" width="4" height="3" rx="0.5" fill="#1b3a5c" opacity="0.5"/>
            </svg>
            """);

        Div titleGroup = new Div();
        titleGroup.addClassName("drawer-brand-text");

        Span title = new Span("WarehousePro");
        title.addClassName("drawer-brand-title");

        Span sub = new Span("v3.5 · Enterprise");
        sub.addClassName("drawer-brand-sub");

        titleGroup.add(title, sub);

        Div header = new Div(iconBox, titleGroup);
        header.addClassName("drawer-header");
        return header;
    }

    private SideNav buildNav() {
        SideNav nav = new SideNav();
        nav.addClassName("main-nav");
        nav.addThemeVariants(SideNavVariant.AURA_FILLED);
        nav.addItem(new SideNavItem("Dashboard", DashboardView.class,  VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Dashboard · Modern", DashboardModernView.class, VaadinIcon.CHART_GRID.create()));
        nav.addItem(new SideNavItem("Products",  ProductsView.class,   VaadinIcon.PACKAGE.create()));
        nav.addItem(new SideNavItem("Suppliers", SuppliersView.class,  VaadinIcon.FACTORY.create()));
        nav.addItem(new SideNavItem("Orders",    OrdersView.class,     VaadinIcon.CART.create()));
        nav.addItem(new SideNavItem("Stock",     StockView.class,      VaadinIcon.RECORDS.create()));
        return nav;
    }

    private HorizontalLayout buildNavbar() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("nav-toggle");

        H1 title = new H1("WarehousePro");
        title.addClassName("nav-title");

        User user = (User) VaadinSession.getCurrent().getAttribute("user");
        String username = user != null ? user.getUsername() : "";
        String role     = user != null ? user.getRole().name()     : "";

        Avatar avatar = new Avatar(username);
        avatar.addClassName("nav-avatar");

        Span nameSpan = new Span(username);
        nameSpan.addClassName("nav-username");

        Span roleSpan = new Span(role);
        roleSpan.addClassName("nav-role");

        Div userInfo = new Div(nameSpan, roleSpan);
        userInfo.addClassName("nav-user-info");

        Div userButton = new Div(avatar, userInfo);
        userButton.addClassName("nav-user-btn");
        userButton.getElement().setAttribute("tabindex", "0");

        ContextMenu contextMenu = new ContextMenu(userButton);
        contextMenu.setOpenOnClick(true);
        contextMenu.addItem("Logout", e -> {
            AppSession.logout();
            VaadinSession.getCurrent().close();
            UI.getCurrent().navigate("login");
        });

        HorizontalLayout navbar = new HorizontalLayout(toggle, title, userButton);
        navbar.addClassName("main-navbar");
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.expand(title);
        navbar.setWidthFull();

        return navbar;
    }
}
