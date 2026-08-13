# WarehousePro — Vaadin Swing Bridge Demo

A three-stage migration demo showing how a legacy Java Swing desktop application
(**WarehousePro 3.5**) can be modernised to run in the browser using the
[Vaadin Swing Bridge](https://vaadin.com/swing-bridge), without rewriting the
existing UI code.

---

## Projects

| Module | Description |
|---|---|
| `swing-app` | The original Swing desktop application |
| `vaadin-app` | The Vaadin Spring Boot host application |
| `shared-api` | Event and publisher interfaces shared across both runtimes |

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
Spring Boot server. Open `http://localhost:8080` in a browser.

---

## Stage 1 — Swing Bridge wrap (`/`)

The root route serves the **entire Swing application** running inside the
browser via the Swing Bridge — zero UI changes to the Swing code.

The bridge launches the Swing `MainFrame` as a headless server-side process and
streams its pixels to the browser. Users interact with the full Swing UI as if
it were a native desktop app.

**What to point out:** the complete Swing application running in a browser tab,
with no code changes on the Swing side.

---

## Stage 2 — Vaadin foundation (`/dashboard`, `/products`, `/orders`, `/stock`)

The application shell has been replaced with native Vaadin components while the
per-tab Swing panels are still embedded via the bridge.

What is already Vaadin-native:

- **Login screen** (`LoginView`) — Vaadin form backed by the existing Hibernate
  `User` entity and `AppSession`.
- **App shell** (`MainLayout`) — Vaadin `AppLayout` with `SideNav`, `DrawerToggle`,
  and a user avatar/logout menu. Navigation is handled by the Vaadin router.
- **Per-view bridge frames** — each view (`DashboardView`, `ProductsView`,
  `OrdersView`, `StockView`) embeds its corresponding Swing panel through a
  dedicated bridge class (`DashboardBridge`, `ProductsBridge`, …) that lives in
  `swing-app/src/…/bridge/`.

**What to point out:** the browser chrome is now fully Vaadin; only the content
area still shows Swing panels.

---

## Stage 3 — Interoperability (`/suppliers`)

The Suppliers view demonstrates **bidirectional communication** between the
Vaadin toolbar and a Swing table, using the `shared-api` module as the
contract layer.

### Module layout

```
shared-api/
└── com/example/warehouse/shared/
    ├── SupplierSelectionEvent.java   # fired when a row is selected (id=null → deselect)
    ├── SupplierEditRequestEvent.java # fired on double-click in the Swing table
    └── SwingEventPublisher.java      # functional interface: publish(Object event)

swing-app/
└── com/example/warehouse/bridge/
    └── SuppliersTableFrame.java      # table-only JFrame; no toolbar

vaadin-app/
└── com/example/views/
    └── SuppliersView.java            # Vaadin toolbar + SwingEventBridge
```

### How it works

**Vaadin → Swing (method calls)**

`SwingEventBridge.callSwingMethod(name, args…)` invokes a public method on the
Swing frame from the Vaadin UI thread. Used for:

| Vaadin action | Swing method called |
|---|---|
| Refresh button | `SuppliersTableFrame.refreshTable()` |
| Search field | `SuppliersTableFrame.setFilter(String)` |
| After save/delete | `SuppliersTableFrame.refreshTable()` |

The **Export CSV** button is handled entirely on the Vaadin side: `SuppliersView`
reads the data via `SupplierService`, builds the CSV in-memory, and delivers it
as a browser file download using a `StreamResource` + `Anchor`. No Swing
involved.

**Swing → Vaadin (events)**

The Swing frame holds `SwingEventPublisher` references injected by the bridge
at startup via setter methods. When the user interacts with the Swing table,
events are published and the bridge converts them across the classloader
boundary into the Vaadin-side equivalents:

| User action | Event published | Vaadin reaction |
|---|---|---|
| Row selected | `SupplierSelectionEvent` (with data) | Enable Edit + Delete buttons, store selection |
| Row deselected | `SupplierSelectionEvent` (id = null) | Disable Edit + Delete buttons |
| Double-click row | `SupplierEditRequestEvent` | Open Edit form pre-filled |

**What to point out:** the Vaadin toolbar and the Swing table react to each
other in real time — row selection enables/disables buttons, edits made in the
Vaadin dialog refresh the Swing table, and the search field filters the Swing
table live.

---

## Tech stack

| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F |
| Charts | JFreeChart 1.5 |
| ORM | Hibernate 6.4 / Jakarta Persistence |
| Database | H2 embedded (file-based) |
| Vaadin | Vaadin 25.1, Aura theme |
| Spring Boot | 4.0 |
| Java | 17 |
| Build | Maven 3 |
