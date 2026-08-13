# WarehousePro Demo — Context for Claude

## What this project is
A Swing → Vaadin migration demo showing enterprise Java developers how a legacy
Swing desktop app can be modernised to run in the browser using the Vaadin Swing
Bridge. The demo has three stages: full bridge wrap → Vaadin shell + bridge views
→ full Vaadin interoperability (Suppliers view).

## Project structure
```
warehouse-demo/
├── .gitignore
├── README.md                  ← developer-facing project overview
├── CLAUDE.md                  ← you are here
├── SPEC.md                    ← full feature spec for the Swing app
├── pom.xml                    ← thin aggregator
├── start-swing.sh             ← builds + runs the Swing desktop app
├── start-vaadin.sh            ← builds Swing jar, copies it, starts Vaadin app (dev)
├── start-vaadin-prod.sh       ← production build + `java -jar` with the bridge JVM flags
├── swing-app/                 ← the Swing application (WarehousePro 3.5)
│   ├── pom.xml                ← Java 17, Maven, Hibernate 6.4, H2, JFreeChart 1.5
│   └── src/main/java/com/example/warehouse/
│       ├── Main.java          ← entry point: Nimbus L&F, inits DB, seeds data, opens LoginFrame
│       ├── AppSession.java    ← static current-user holder
│       ├── config/PersistenceUtil.java    ← Hibernate EntityManagerFactory (H2 embedded)
│       ├── model/             ← JPA entities: User, Category, Supplier, Product, PurchaseOrder, PurchaseOrderItem, StockMovement
│       ├── dao/               ← plain Hibernate DAOs (no Spring): one per entity
│       ├── service/           ← ProductService, SupplierService, PurchaseOrderService
│       ├── seed/DataSeeder.java  ← seeds 3 users, 5 categories, suppliers, products, orders, movements
│       ├── bridge/            ← bridge entry points + interop event types
│       │   ├── BridgeMain.java
│       │   ├── BridgeUtil.java
│       │   ├── DashboardBridge.java
│       │   ├── ProductsBridge.java
│       │   ├── OrdersBridge.java
│       │   ├── StockBridge.java
│       │   ├── SuppliersBridge.java
│       │   ├── SupplierSelectedEvent.java / SupplierSelectionListener.java
│       │   └── SupplierEditRequestEvent.java / SupplierEditRequestListener.java
│       └── ui/
│           ├── LoginFrame.java
│           ├── MainFrame.java           ← JTabbedPane (5 tabs), menu bar, status bar, logout;
│           │                              carries the @ExposedMethod interop surface
│           ├── util/UIConstants.java    ← shared colors, fonts, border factories; styleTable() sets SINGLE_SELECTION
│           ├── util/StatusBar.java
│           ├── util/TableExporter.java  ← Swing-side CSV export (JFileChooser)
│           ├── dashboard/DashboardPanel.java
│           ├── products/ProductsPanel.java, ProductDialog.java
│           ├── suppliers/SuppliersPanel.java, SupplierDialog.java
│           ├── orders/PurchaseOrdersPanel.java, PurchaseOrderDialog.java
│           └── stock/StockMovementsPanel.java
└── vaadin-app/                ← Vaadin 25.2 Spring Boot host (Aura theme)
    ├── pom.xml                ← Java 21, Spring Boot 4.1, Vaadin 25.2, Aura theme
    ├── applibs/               ← warehouse-swing-app-3.5.0.jar (copied by the start scripts)
    └── src/main/java/com/example/
        ├── Application.java          ← @SpringBootApplication, @Push, Aura stylesheet
        ├── SwingAppInitializer.java  ← @PostConstruct: PersistenceUtil.init() + DataSeeder.seedIfEmpty()
        ├── dashboard/                ← DashboardService + KpiSummary, CategoryStock, LowStockItem, OrderStatusCount
        ├── swingbridge/ui/WarehouseView.java
        └── views/
            ├── LoginView.java        ← Vaadin login form, authenticates via Hibernate User entity
            ├── MainLayout.java       ← AppLayout + SideNav (Barlow font, filled variant, Aura theme)
            ├── DashboardView.java    ← embeds DashboardBridge
            ├── DashboardModernView.java ← native Vaadin dashboard (KPI tiles + charts)
            ├── ProductsView.java     ← embeds ProductsBridge
            ├── SuppliersView.java    ← Vaadin toolbar + bridge table (full interop demo)
            ├── OrdersView.java       ← embeds OrdersBridge
            └── StockView.java        ← embeds StockBridge
```

## How to run

### Swing desktop app
```bash
./start-swing.sh
```

### Vaadin app — development
```bash
./start-vaadin.sh
```
Builds the Swing JAR, copies it to `vaadin-app/applibs/`, then starts Spring Boot
via `spring-boot:run`. The bridge JVM flags come from the pom's `<jvmArguments>`.

### Vaadin app — production
```bash
./start-vaadin-prod.sh
```
Runs `mvn clean package` and starts the resulting jar with `java -jar`.

Open `http://localhost:8888` (see `server.port` in `application.properties`).

Login: `admin / admin`, `manager / manager`, or `viewer / viewer`.

To reset data: delete `warehousedb.mv.db` in the relevant app directory and restart.

## Tech stack
| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F (enterprise blue overrides) |
| Charts | JFreeChart 1.5.4 (Swing) / Vaadin Charts (Vaadin) |
| ORM | Hibernate 6.4 / jakarta.persistence |
| Database | H2 embedded (file-based, no setup needed) |
| Vaadin | 25.2, Aura theme, Spring Boot 4.1 |
| Swing Bridge | 1.2.2 |
| Build | Maven 3.x |
| Java | swing-app 17, vaadin-app 21 |

Version properties live in `vaadin-app/pom.xml` (`vaadin.version`,
`swing-bridge.version`) and `swing-app/pom.xml` (`swing-bridge.version`) —
**keep the `swing-bridge.version` values in the two poms in sync.**

## Domain model
- **Product**: sku, name, category, supplier, unitPrice, quantity, reorderLevel, unit
- **Category**: 5 seeded (Electronics, Power Tools, Hand Tools, Safety Equipment, Office Supplies)
- **Supplier**: seeded (TechSource Inc., Industrial Pro Supply, SafeGuard Products, Office Depot Business, Global Parts Co., FastTrack Logistics)
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

## Interop (the SuppliersView demo)
This is the stage-three payoff: a native Vaadin toolbar driving a Swing table.
It uses the official Swing Bridge interop API, not a hand-rolled event bus.

| Direction | Mechanism |
|---|---|
| Vaadin → Swing | `@ExposedMethod` on `MainFrame` methods; called via a generated bridge interface |
| Swing → Vaadin | `@VaadinCallback(observerFor = …Listener.class)` on `SuppliersView` handlers |

- The `swing-bridge-codegen-maven-plugin` scans the Swing jar at build time and
  generates `MainFrameBridge` into `target/generated-sources/swing-bridge`
  (added to the compile path by `build-helper-maven-plugin`). It is generated,
  not checked in.
- Vaadin obtains the handle with
  `SwingBridge.interop(MAIN_CLASS).of(MainFrameBridge.class)` and calls e.g.
  `handle.onReady(MainFrameBridge::showSuppliersTable)`.
- Callbacks are registered in `onAttach` and removed in `onDetach` so listeners
  don't leak across navigation.

## Notable implementation details
- **Single selection**: all JTables use `ListSelectionModel.SINGLE_SELECTION` (set in `UIConstants.styleTable()`), avoiding multi-select confusion in the bridge.
- **PurchaseOrderDialog**: `updateTotal()` has a reentrancy guard (`updatingTotal` flag) to prevent infinite recursion when `setValueAt()` fires the table model listener.
- **Deselection**: `SupplierSelectedEvent.id` is `null` on row deselection;
  `SuppliersView.onSelectionChanged` uses that to disable Edit/Delete.
- **CSV export exists on both sides**: `TableExporter` (Swing, `JFileChooser`) is
  used by `ProductsPanel` and `SuppliersPanel`; `SuppliersView` additionally
  offers a Vaadin-side download via `StreamResource` + `Anchor`.
- **CSS**: uses Aura theme properties (`--aura-*`, `--vaadin-*`). `--lumo-*` variables are not used except where Aura inherits them implicitly.
