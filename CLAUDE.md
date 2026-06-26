# WarehousePro Demo — Context for Claude

## What this project is
A Swing → Vaadin migration demo showing enterprise Java developers how a legacy
Swing desktop app can be modernised to run in the browser using the Vaadin Swing
Bridge. The demo has four stages:
1. **Full bridge wrap** — the entire Swing app served at `/`.
2. **Vaadin shell + bridged tabs** — native Vaadin shell, each tab still a Swing
   panel embedded via the bridge (`/dashboard`, `/products`, `/orders`, `/stock`).
3. **Two-way interop** — Vaadin toolbar ↔ Swing table on `/suppliers`.
4. **Native Vaadin rebuild** — the dashboard re-implemented in pure Vaadin on
   `/dashboard-modern`.

A third app, `netbeans-app`, is a parallel "before" state: the **same**
WarehousePro application rebuilt on the **NetBeans Platform (RCP)** — screens are
`TopComponent`s in the platform window system, login runs from `@OnStart`, and the
domain layer is reused verbatim. See `netbeans-app/README.md`.

A fourth module, `vaadin-app-nb`, is the NetBeans-RCP counterpart of `vaadin-app`:
a Vaadin/Spring Boot host that embeds the `netbeans-app` distribution via
`com.vaadin.swingbridge.NetBeansRcpBridge` (SwingBridge **1.2.0**) — full shell at
`/`, one bridged `TopComponent` per route under a Vaadin shell, and a native
`/dashboard-modern`. The embedded RCP runs with `-Dwarehouse.bridge=true` so its
`@OnStart` skips the desktop login (the Vaadin `LoginView` authenticates instead).
Requires **JDK 21**. See `vaadin-app-nb/README.md`.

## Project structure
```
warehouse-demo/
├── .gitignore
├── README.md                  ← developer-facing project overview
├── CLAUDE.md                  ← you are here
├── SPEC.md                    ← full feature spec for the Swing app ("before" state)
├── pom.xml                    ← Maven aggregator over swing-app + vaadin-app
├── start-swing.sh             ← builds + runs the Swing desktop app
├── start-vaadin.sh            ← builds Swing jar, copies it, starts Vaadin app
├── swing-app/                 ← the Swing application (WarehousePro 3.5)
│   ├── pom.xml                ← Java 17, Maven, Hibernate 6.4, H2, JFreeChart 1.5
│   └── src/main/java/com/example/warehouse/
│       ├── Main.java          ← entry point: Nimbus L&F, inits DB, seeds data, opens LoginFrame
│       ├── AppSession.java    ← static current-user holder
│       ├── config/PersistenceUtil.java    ← Hibernate EntityManagerFactory (H2 embedded)
│       ├── model/             ← JPA entities: User, Category, Supplier, Product, PurchaseOrder, PurchaseOrderItem, StockMovement
│       ├── dao/               ← plain Hibernate DAOs (no Spring): one per entity
│       ├── service/           ← ProductService, SupplierService, PurchaseOrderService
│       ├── seed/DataSeeder.java  ← seeds 3 users, 5 categories, 6 suppliers, 30 products (6 low-stock), 10 orders, 25 movements
│       ├── bridge/            ← bridge entry points + interop contract types
│       │   ├── BridgeMain.java       ← launches the full MainFrame (the main class ALL bridge views use)
│       │   ├── BridgeUtil.java       ← shared init (DB + L&F) for bridge entry points
│       │   ├── DashboardBridge.java  ← standalone per-tab launchers (alt entry points; views use BridgeMain)
│       │   ├── ProductsBridge.java
│       │   ├── OrdersBridge.java
│       │   ├── StockBridge.java
│       │   ├── SuppliersBridge.java  ← standalone SuppliersPanel launcher
│       │   ├── SupplierSelectedEvent.java / SupplierSelectionListener.java   ← row selection (id=null → deselect)
│       │   └── SupplierEditRequestEvent.java / SupplierEditRequestListener.java  ← row double-click
│       └── ui/
│           ├── LoginFrame.java
│           ├── MainFrame.java           ← JTabbedPane (5 tabs); @ExposedMethod showXxx()/refresh/filter for the bridge
│           ├── util/UIConstants.java    ← shared colors, fonts, border factories; styleTable() sets SINGLE_SELECTION
│           ├── util/StatusBar.java
│           ├── util/TableExporter.java
│           ├── dashboard/DashboardPanel.java
│           ├── products/ProductsPanel.java
│           ├── products/ProductDialog.java
│           ├── suppliers/SuppliersPanel.java
│           ├── suppliers/SupplierDialog.java
│           ├── orders/PurchaseOrdersPanel.java
│           ├── orders/PurchaseOrderDialog.java  ← reentrancy guard on updateTotal()
│           └── stock/StockMovementsPanel.java
└── vaadin-app/                ← Vaadin 25.1 Spring Boot host (Aura theme)
    ├── pom.xml                ← Spring Boot 4, Vaadin 25.1, Aura theme, Vaadin Swing Bridge
    ├── applibs/               ← warehouse-swing-app-3.5.0.jar (copied by start-vaadin.sh)
    └── src/main/java/com/example/
        ├── Application.java            ← Spring Boot main
        ├── SwingAppInitializer.java    ← @PostConstruct: inits H2 + seeds demo data
        ├── dashboard/                  ← native read model for the modern dashboard
        │   ├── DashboardService.java   ← @Service wrapping ProductService/PurchaseOrderService
        │   └── KpiSummary / CategoryStock / LowStockItem / OrderStatusCount  ← typed records
        ├── swingbridge/ui/WarehouseView.java  ← Stage 1: wraps com.example.warehouse.Main at "/"
        └── views/
            ├── LoginView.java       ← Vaadin login form, authenticates via Hibernate User entity
            ├── MainLayout.java      ← AppLayout + SideNav (Barlow font, filled variant, Aura theme)
            ├── DashboardView.java   ← embeds MainFrame via SwingBridge, calls showDashboard()
            ├── ProductsView.java    ← embeds MainFrame, calls showProducts()
            ├── OrdersView.java      ← embeds MainFrame, calls showOrders()
            ├── StockView.java       ← embeds MainFrame, calls showStock()
            ├── SuppliersView.java   ← Vaadin toolbar + two-way interop (Stage 3)
            └── DashboardModernView.java ← native Vaadin dashboard, no bridge (Stage 4)
```

## How to run

### Swing desktop app
```bash
./start-swing.sh
```

### Vaadin app (all demo stages)
```bash
./start-vaadin.sh
```
Builds the Swing JAR, copies it to `vaadin-app/applibs/`, then starts Spring Boot.
Open `http://localhost:8080`.

Login: `admin / admin` or `manager / manager`

### NetBeans Platform (RCP) app
```bash
./start-netbeans.sh
```
Builds the NetBeans Platform app (first build downloads the platform cluster) and
launches it via `nbm:run-platform`. Uses JDK 17. Login: `admin / admin` or
`manager / manager`.

To reset data: delete `warehousedb.mv.db` in the relevant app directory and restart.

## Tech stack
| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F (enterprise blue overrides) |
| Swing charts | JFreeChart 1.5.4 |
| Interop | Vaadin Swing Bridge (`com.vaadin.swingbridge`) |
| ORM | Hibernate 6.4 / jakarta.persistence |
| Database | H2 embedded (file-based, no setup needed) |
| Vaadin | 25.1, Aura theme, Spring Boot 4 |
| Vaadin charts | Vaadin Charts + Vaadin `Dashboard` (modern view) |
| Build | Maven 3.x |
| Java | 17 |

## Domain model
- **Product**: sku, name, category, supplier, unitPrice, quantity, reorderLevel, unit
- **Category**: 5 seeded (Electronics, Power Tools, Hand Tools, Safety Equipment, Office Supplies)
- **Supplier**: 6 seeded (TechSource Inc., Industrial Pro Supply, SafeGuard Products, Office Depot Business, Global Parts Co., FastTrack Logistics)
- **PurchaseOrder**: supplier, orderDate, expectedDate, status (PENDING/ORDERED/DELIVERED/CANCELLED), items
- **StockMovement**: product, movementType (IN/OUT/ADJUSTMENT), quantity, reason, date

## Key Swing patterns (each maps to a Vaadin equivalent)
| Swing | Vaadin |
|---|---|
| `JTabbedPane` (MainFrame) | `AppLayout` + `SideNav` |
| `JTable` + `TableRowSorter` | `Grid<T>` |
| `RowFilter` live search | Grid data provider filter |
| `JDialog` modal form | `Dialog` + `FormLayout` |
| `JComboBox` | `ComboBox<T>` |
| `JSpinner` | `NumberField` |
| JFreeChart bar/pie | Vaadin Charts |
| `JSplitPane` | `SplitLayout` |
| Custom `TableCellRenderer` | Grid column renderer |

## How the Swing Bridge interop works (Stage 3)
- **Exposing Swing methods**: methods on `MainFrame` are annotated `@ExposedMethod`
  (`com.vaadin.swingbridge.interop.ExposedMethod`). At build time the bridge
  annotation processor generates a typed `MainFrameBridge` interface (into
  `vaadin-app/target/generated-sources/swing-bridge/`) plus a proxy + metadata.
- **Vaadin → Swing (typed calls)**: the view gets a handle via
  `SwingBridge.interop(MAIN_CLASS).of(MainFrameBridge.class)` and invokes typed
  methods, e.g. `handle.onReady(MainFrameBridge::refreshSuppliers)` or
  `handle.onReady(b -> b.setSupplierFilter(text))`. No reflection / no string method names.
- **Swing → Vaadin (events)**: the Swing side defines listener interfaces +
  event POJOs in `swing-app/.../bridge/`. The Vaadin view registers with
  `handle.registerCallback(this)` and annotates handlers with
  `@VaadinCallback(observerFor = SupplierSelectionListener.class)` etc.

## Notable implementation details
- **Single selection**: all JTables use `ListSelectionModel.SINGLE_SELECTION` (set in `UIConstants.styleTable()`), avoiding multi-select confusion in the bridge.
- **PurchaseOrderDialog**: `updateTotal()` has a reentrancy guard (`updatingTotal` flag) to prevent infinite recursion when `setValueAt()` fires the table model listener.
- **Deselection events**: `SuppliersPanel` fires `SupplierSelectedEvent(null)` on row deselection; `SuppliersView` checks `event.id == null` to disable Edit/Delete.
- **Shared Swing process**: all four bridged views point at `BridgeMain` (the full `MainFrame`) and switch the visible tab via the generated `showXxx()` methods.
- **CSV export**: handled entirely on the Vaadin side — `SuppliersView` calls `SupplierService.exportToCsv()` and serves it via `DownloadHandler` + `Anchor`; the Swing table is not involved.
- **Native dashboard (Stage 4)**: `DashboardModernView` uses no bridge — it reads `DashboardService` (typed records over the same H2 data) and renders with the Vaadin `Dashboard` component + Vaadin Charts, so its numbers match the Swing dashboard exactly.
- **CSS**: uses Aura theme properties (`--aura-*`, `--vaadin-*`). `--lumo-*` variables are not used except where Aura inherits them implicitly.
