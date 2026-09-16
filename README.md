# WarehousePro — Vaadin Swing Bridge Demo

A four-stage migration demo showing how a legacy Java Swing desktop application
(**WarehousePro 3.5**) can be modernised to run in the browser using the
[Vaadin Swing Bridge](https://vaadin.com/swing-bridge), without rewriting the
existing UI code.

This branch takes the shell and the views to **React/Hilla**: the application
shell, the login page, the modern dashboard and the Suppliers view are React,
while the bridged Swing panels keep running inside them.

---

## Projects

| Module | Description |
|---|---|
| `swing-app` | The original Swing desktop application |
| `vaadin-app` | The Vaadin Spring Boot host application (Flow + Hilla/React) |

---

## Running the apps

### Swing desktop app (standalone)

```bash
./start-swing.sh
```

Builds the fat JAR and launches the desktop app directly.
Login: `admin / admin` or `manager / manager`.

### Vaadin app (all three stages)

```bash
./start-vaadin.sh
```

Builds the Swing JAR, copies it to `vaadin-app/applibs/`, then starts the
Spring Boot server. Open `http://localhost:8888` in a browser.
Login: `admin / admin`, `manager / manager`, or `viewer / viewer`.

---

## Stage 1 — Swing Bridge wrap (`/`)

The root route serves the **entire Swing application** running inside the
browser via the Swing Bridge — zero UI changes to the Swing code.

The bridge launches the Swing `MainFrame` as a headless server-side process and
streams its pixels to the browser. Users interact with the full Swing UI as if
it were a native desktop app.

It renders full-bleed and is not behind the React login — the Swing application
brings its own sign-in screen. `views/@layout.tsx` deliberately steps aside for
this one route.

**What to point out:** the complete Swing application running in a browser tab,
with no code changes on the Swing side.

---

## Stage 2 — React shell (`/dashboard`, `/products`, `/orders`, `/stock`)

The application shell is React, while the per-view Swing panels are still
embedded via the bridge.

- **Login page** — `src/main/frontend/auth/LoginPage.tsx`, authenticating through
  `AuthService` against the existing Hibernate `User` entity.
- **App shell** — `src/main/frontend/views/@layout.tsx`, a React `AppLayout` with
  `SideNav` and a user/logout menu. `routes.tsx` registers `Flow` as the fallback
  for every branch, which is what lets the Java views render inside it.
- **Per-view bridge frames** — `DashboardView`, `ProductsView`, `OrdersView` and
  `StockView` are still Flow views, each embedding its Swing panel through a
  bridge class (`DashboardBridge`, `ProductsBridge`, …) in `swing-app/src/…/bridge/`.

**What to point out:** the chrome is React; the content area still shows Swing.

---

## Stage 2b — A native React view (`/dashboard-modern`)

`views/dashboard-modern.tsx` rebuilds the dashboard in React — KPI tiles, Vaadin
Charts and a Grid — reading the same data through `DashboardService`, a
`@BrowserCallable` service that wraps the legacy Swing services.

---

## Stage 3 — Interoperability (`/suppliers`)

The Suppliers view demonstrates **bidirectional communication** between a React
toolbar and a Swing table, using the official Swing Bridge interop API — not a
hand-rolled event bus.

### Module layout

```
swing-app/
└── com/example/warehouse/bridge/
    ├── SuppliersBridge.java              # the Swing suppliers table
    ├── SupplierSelectedEvent.java        # fired on row selection (id=null → deselect)
    └── SupplierEditRequestEvent.java     # fired on double-click in the Swing table

vaadin-app/
├── com/example/suppliers/
│   ├── SupplierBridgePanel.java          # the SwingBridge, plus the @VaadinCallback handlers
│   ├── SupplierBridgeExporter.java       # exports it as <supplier-bridge>
│   ├── SupplierEndpoint.java             # @BrowserCallable CRUD
│   └── SupplierRecord.java
└── src/main/frontend/views/suppliers.tsx # React toolbar, dialogs and CRUD
```

### How it works

`SwingBridge` extends `Div` — it is a server-side Flow component with no custom
element, so React cannot render one directly. `SupplierBridgeExporter` exports it
as a web component, and the React view places it with
`reactElement('supplier-bridge')`. Everything around it is ordinary React.

Two channels, and they are not interchangeable:

| Concern | Path |
|---|---|
| Supplier CRUD + CSV | React → `SupplierEndpoint` over Hilla, typed client, no Flow involved |
| Bridge commands | React → exported properties. `filter` drives the Swing filter; `refreshToken` is a counter React increments to request a reload |
| Selection, edit requests | Swing → `@VaadinCallback` → `webComponent.fireEvent(...)` → DOM events in React |

So a save is two steps: write through the endpoint, then bump `refreshToken` so
the Swing table reloads.

**What to point out:** the React toolbar and the Swing table react to each other
in real time — row selection enables/disables buttons, a double-click in the
Swing table opens the React dialog, edits refresh the Swing table, and the search
field filters it live.

---

## Tech stack

| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F |
| Charts | JFreeChart 1.5 (Swing) / Vaadin Charts (web) |
| ORM | Hibernate 6.4 / Jakarta Persistence |
| Database | H2 embedded (file-based) |
| Vaadin | Vaadin 25.2, Aura theme |
| React views | Hilla (`hilla-spring-boot-starter`), file-based routing |
| Swing Bridge | 1.3.0 |
| Spring Boot | 4.1 |
| Java | swing-app 17, vaadin-app 21 |
| Build | Maven 3 |
