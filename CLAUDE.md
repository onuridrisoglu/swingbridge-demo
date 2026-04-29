# WarehousePro Demo — Context for Claude

## What this project is
A Swing → Vaadin migration demo showing enterprise Java developers how a legacy
Swing desktop app can be modernised to run in the browser using the Vaadin Swing
Bridge. The demo has three stages: full bridge wrap → Vaadin shell + bridge tabs
→ full Vaadin interoperability (Suppliers view).

## Project structure
```
new-demo/
├── .gitignore
├── README.md                  ← developer-facing project overview
├── CLAUDE.md                  ← you are here
├── SPEC.md                    ← full feature spec for the Swing app
├── MIGRATION_GUIDE.md         ← Swing → Vaadin code mapping, screen by screen
├── DEMO_SCRIPT.md             ← presenter talking points for the live demo
├── start-swing.sh             ← builds + runs the Swing desktop app
├── start-vaadin.sh            ← builds Swing jar, copies it, starts Vaadin app
├── shared-api/                ← event types + publisher interface (used by both apps)
│   └── src/main/java/com/example/warehouse/shared/
│       ├── SupplierSelectionEvent.java   (id=null signals deselection)
│       ├── SupplierEditRequestEvent.java
│       └── SwingEventPublisher.java
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
│       ├── bridge/            ← bridge entry points for each Vaadin view
│       │   ├── BridgeUtil.java
│       │   ├── SuppliersTableFrame.java  ← table-only frame; toolbar lives on Vaadin side
│       │   ├── DashboardBridge.java
│       │   ├── ProductsBridge.java
│       │   ├── OrdersBridge.java
│       │   └── StockBridge.java
│       └── ui/
│           ├── LoginFrame.java
│           ├── MainFrame.java           ← JTabbedPane (5 tabs), menu bar, status bar, logout
│           ├── util/UIConstants.java    ← shared colors, fonts, border factories; styleTable() sets SINGLE_SELECTION
│           ├── util/StatusBar.java
│           ├── dashboard/DashboardPanel.java
│           ├── products/ProductsPanel.java
│           ├── products/ProductDialog.java
│           ├── suppliers/SuppliersPanel.java
│           ├── suppliers/SupplierDialog.java
│           ├── orders/PurchaseOrdersPanel.java
│           ├── orders/PurchaseOrderDialog.java  ← reentrancy guard on updateTotal()
│           └── stock/StockMovementsPanel.java
└── vaadin-app/                ← Vaadin 25.1 Spring Boot host (Aura theme)
    ├── pom.xml                ← Spring Boot 4, Vaadin 25.1, Aura theme
    ├── applibs/               ← warehouse-swing-app-3.5.0.jar (copied by start-vaadin.sh)
    └── src/main/java/com/example/
        ├── swingbridge/SwingEventBridge.java
        └── views/
            ├── LoginView.java       ← Vaadin login form, authenticates via Hibernate User entity
            ├── MainLayout.java      ← AppLayout + SideNav (Barlow font, filled variant, Aura theme)
            ├── DashboardView.java   ← embeds DashboardBridge
            ├── ProductsView.java    ← embeds ProductsBridge
            ├── SuppliersView.java   ← Vaadin toolbar + SuppliersTableFrame (interop demo)
            ├── OrdersView.java      ← embeds OrdersBridge
            └── StockView.java       ← embeds StockBridge
```

## How to run

### Swing desktop app
```bash
./start-swing.sh
```

### Vaadin app (all three demo stages)
```bash
./start-vaadin.sh
```
Builds the Swing JAR, copies it to `vaadin-app/applibs/`, then starts Spring Boot.
Open `http://localhost:8080`.

Login: `admin / admin` or `manager / manager`

To reset data: delete `warehousedb.mv.db` in the relevant app directory and restart.

## Tech stack
| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F (enterprise blue overrides) |
| Charts | JFreeChart 1.5.4 |
| ORM | Hibernate 6.4 / jakarta.persistence |
| Database | H2 embedded (file-based, no setup needed) |
| Vaadin | 25.1, Aura theme, Spring Boot 4 |
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

## Notable implementation details
- **Single selection**: all JTables use `ListSelectionModel.SINGLE_SELECTION` (set in `UIConstants.styleTable()`), avoiding multi-select confusion in the bridge.
- **PurchaseOrderDialog**: `updateTotal()` has a reentrancy guard (`updatingTotal` flag) to prevent infinite recursion when `setValueAt()` fires the table model listener.
- **Deselection events**: `SuppliersTableFrame` publishes `SupplierSelectionEvent(null,…)` on row deselection; `SuppliersView` checks `event.id == null` to disable Edit/Delete.
- **CSV export**: handled entirely on the Vaadin side (`StreamResource` + `Anchor`); the Swing table is not involved.
- **CSS**: uses Aura theme properties (`--aura-*`, `--vaadin-*`). `--lumo-*` variables are not used except where Aura inherits them implicitly.
