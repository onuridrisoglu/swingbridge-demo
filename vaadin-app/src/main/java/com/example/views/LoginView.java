package com.example.views;

import com.example.rcp.RcpMode;
import com.example.warehouse.AppSession;
import com.example.warehouse.dao.UserDAO;
import com.example.warehouse.model.User;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("login")
public class LoginView extends Div {

    private final RcpMode rcpMode;

    public LoginView(RcpMode rcpMode) {
        this.rcpMode = rcpMode;
        addClassName("login-view");
        // Inline styles guarantee dark background wins over Lumo defaults
        getStyle()
            .set("width", "100%")
            .set("min-height", "100vh")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("position", "relative")
            .set("overflow", "hidden")
            .set("background",
                "radial-gradient(ellipse at 20% 10%, rgba(61,122,181,0.18) 0%, transparent 55%)," +
                "radial-gradient(ellipse at 85% 85%, rgba(13,27,42,0.9) 0%, transparent 50%)," +
                "linear-gradient(158deg, #0d1b2a 0%, #112236 45%, #1b3a5c 100%)");

        add(buildBgDeco());
        add(buildCard());
    }

    // -----------------------------------------------------------------------
    // Background decorative SVG layer
    // -----------------------------------------------------------------------

    private Div buildBgDeco() {
        Div deco = new Div();
        deco.addClassName("login-bg-deco");

        // Right-side shelving rack silhouette
        Div rackRight = new Div();
        rackRight.addClassName("bg-rack-right");
        rackRight.getElement().setProperty("innerHTML", """
            <svg viewBox="0 0 200 900" fill="none" xmlns="http://www.w3.org/2000/svg">
              <!-- Vertical uprights -->
              <rect x="8"  y="0"   width="12" height="900" fill="#7eb8e8"/>
              <rect x="180" y="0"  width="12" height="900" fill="#7eb8e8"/>
              <!-- Horizontal shelves -->
              <rect x="8"  y="60"  width="184" height="10" fill="#7eb8e8"/>
              <rect x="8"  y="200" width="184" height="10" fill="#7eb8e8"/>
              <rect x="8"  y="340" width="184" height="10" fill="#7eb8e8"/>
              <rect x="8"  y="480" width="184" height="10" fill="#7eb8e8"/>
              <rect x="8"  y="620" width="184" height="10" fill="#7eb8e8"/>
              <rect x="8"  y="760" width="184" height="10" fill="#7eb8e8"/>
              <!-- Diagonal braces -->
              <line x1="20" y1="70"  x2="180" y2="200" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="180" y1="70" x2="20"  y2="200" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="20" y1="210" x2="180" y2="340" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="180" y1="210" x2="20" y2="340" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="20" y1="350" x2="180" y2="480" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="180" y1="350" x2="20" y2="480" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="20" y1="490" x2="180" y2="620" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="180" y1="490" x2="20" y2="620" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="20" y1="630" x2="180" y2="760" stroke="#7eb8e8" stroke-width="4"/>
              <line x1="180" y1="630" x2="20" y2="760" stroke="#7eb8e8" stroke-width="4"/>
              <!-- Boxes on shelves -->
              <rect x="20" y="100" width="48" height="90"  rx="3" fill="#7eb8e8"/>
              <rect x="76" y="115" width="52" height="75"  rx="3" fill="#7eb8e8"/>
              <rect x="136" y="90" width="44" height="100" rx="3" fill="#7eb8e8"/>
              <rect x="20" y="240" width="40" height="88"  rx="3" fill="#7eb8e8"/>
              <rect x="68" y="255" width="60" height="73"  rx="3" fill="#7eb8e8"/>
              <rect x="140" y="230" width="40" height="98" rx="3" fill="#7eb8e8"/>
              <rect x="20" y="380" width="56" height="84"  rx="3" fill="#7eb8e8"/>
              <rect x="84" y="395" width="44" height="69"  rx="3" fill="#7eb8e8"/>
              <rect x="136" y="370" width="44" height="98" rx="3" fill="#7eb8e8"/>
            </svg>
            """);

        // Left-side shorter rack
        Div rackLeft = new Div();
        rackLeft.addClassName("bg-rack-left");
        rackLeft.getElement().setProperty("innerHTML", """
            <svg viewBox="0 0 180 600" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="8"  y="0"   width="10" height="600" fill="#7eb8e8"/>
              <rect x="162" y="0"  width="10" height="600" fill="#7eb8e8"/>
              <rect x="8"  y="80"  width="164" height="8"  fill="#7eb8e8"/>
              <rect x="8"  y="220" width="164" height="8"  fill="#7eb8e8"/>
              <rect x="8"  y="360" width="164" height="8"  fill="#7eb8e8"/>
              <rect x="8"  y="500" width="164" height="8"  fill="#7eb8e8"/>
              <line x1="18" y1="88" x2="162" y2="220" stroke="#7eb8e8" stroke-width="3"/>
              <line x1="162" y1="88" x2="18" y2="220" stroke="#7eb8e8" stroke-width="3"/>
              <rect x="18" y="100" width="42" height="110" rx="3" fill="#7eb8e8"/>
              <rect x="68" y="120" width="46" height="90"  rx="3" fill="#7eb8e8"/>
              <rect x="122" y="90" width="38" height="120" rx="3" fill="#7eb8e8"/>
              <rect x="18" y="240" width="50" height="108" rx="3" fill="#7eb8e8"/>
              <rect x="76" y="255" width="40" height="93"  rx="3" fill="#7eb8e8"/>
              <rect x="124" y="235" width="36" height="113" rx="3" fill="#7eb8e8"/>
            </svg>
            """);

        // Top-left stacked boxes
        Div boxesTop = new Div();
        boxesTop.addClassName("bg-boxes-top");
        boxesTop.getElement().setProperty("innerHTML", """
            <svg width="180" height="160" viewBox="0 0 180 160" fill="none" xmlns="http://www.w3.org/2000/svg">
              <!-- Stack of crates, isometric style -->
              <rect x="0"  y="60"  width="55" height="45" rx="3" fill="#7eb8e8"/>
              <rect x="62" y="60"  width="55" height="45" rx="3" fill="#7eb8e8"/>
              <rect x="124" y="60" width="55" height="45" rx="3" fill="#7eb8e8"/>
              <rect x="0"  y="110" width="55" height="45" rx="3" fill="#7eb8e8"/>
              <rect x="62" y="110" width="55" height="45" rx="3" fill="#7eb8e8"/>
              <rect x="31" y="10"  width="55" height="45" rx="3" fill="#7eb8e8"/>
              <rect x="93" y="10"  width="55" height="45" rx="3" fill="#7eb8e8"/>
              <!-- Straps on crates -->
              <line x1="27"  y1="60" x2="27"  y2="105" stroke="#0d1b2a" stroke-width="2" opacity="0.3"/>
              <line x1="89"  y1="60" x2="89"  y2="105" stroke="#0d1b2a" stroke-width="2" opacity="0.3"/>
              <line x1="151" y1="60" x2="151" y2="105" stroke="#0d1b2a" stroke-width="2" opacity="0.3"/>
            </svg>
            """);

        deco.add(rackRight, rackLeft, boxesTop);
        return deco;
    }

    // -----------------------------------------------------------------------
    // Login card
    // -----------------------------------------------------------------------

    private Div buildCard() {
        Div card = new Div();
        card.addClassName("login-card");

        card.add(buildHeader());
        card.add(buildStatus());

        Div divider = new Div();
        divider.addClassName("login-divider");
        card.add(divider);

        card.add(buildForm());

        Paragraph footer = new Paragraph("© 2025 WarehousePro. All rights reserved.");
        footer.addClassName("login-footer");
        card.add(footer);

        return card;
    }

    private Div buildHeader() {
        Div iconBox = new Div();
        iconBox.addClassName("login-icon-box");
        iconBox.getElement().setProperty("innerHTML", """
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <!-- Warehouse building -->
              <rect x="2" y="12" width="24" height="14" rx="1" stroke="#1b3a5c" stroke-width="1.8" fill="none"/>
              <!-- Roof peak -->
              <polyline points="1,13 14,4 27,13" stroke="#1b3a5c" stroke-width="1.8" stroke-linejoin="round" fill="none"/>
              <!-- Door -->
              <rect x="10" y="18" width="8" height="8" rx="1" stroke="#1b3a5c" stroke-width="1.5" fill="none"/>
              <!-- Windows -->
              <rect x="4"  y="16" width="4" height="3" rx="0.5" fill="#1b3a5c" opacity="0.5"/>
              <rect x="20" y="16" width="4" height="3" rx="0.5" fill="#1b3a5c" opacity="0.5"/>
              <!-- Door handle -->
              <circle cx="17" cy="22.5" r="0.8" fill="#1b3a5c"/>
            </svg>
            """);

        Div titleGroup = new Div();
        titleGroup.addClassName("login-title-group");

        H1 title = new H1("WarehousePro");
        title.addClassName("login-title");

        Span version = new Span("v3.5 · Enterprise Edition");
        version.addClassName("login-version");

        titleGroup.add(title, version);

        Div header = new Div(iconBox, titleGroup);
        header.addClassName("login-header");
        return header;
    }

    private Div buildStatus() {
        Div dot = new Div();
        dot.addClassName("status-dot");

        Span text = new Span("SYSTEM ONLINE");
        text.addClassName("status-text");

        Div status = new Div(dot, text);
        status.addClassName("login-status");
        return status;
    }

    private VerticalLayout buildForm() {
        TextField username = new TextField("Username");
        username.setWidthFull();

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();

        Button signIn = new Button("Sign In", e -> authenticate(username.getValue(), password.getValue()));
        signIn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signIn.setWidthFull();
        signIn.addClassName("login-btn");

        password.addKeyPressListener(Key.ENTER, e ->
                authenticate(username.getValue(), password.getValue()));

        VerticalLayout form = new VerticalLayout(username, password, signIn);
        form.setPadding(false);
        form.setSpacing(true);
        return form;
    }

    // -----------------------------------------------------------------------
    // Auth
    // -----------------------------------------------------------------------

    private void authenticate(String username, String password) {
        User user = new UserDAO().authenticate(username, password);
        if (user == null) {
            Notification n = Notification.show("Invalid username or password");
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            VaadinSession.getCurrent().setAttribute("user", user);
            AppSession.login(user);
            UI.getCurrent().navigate(rcpMode.homeRoute());
        }
    }
}
