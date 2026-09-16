# WarehousePro Demo — Context for Claude

## What this project is
A Swing → Vaadin migration demo showing enterprise Java developers how a legacy
Swing desktop app can be modernised to run in the browser using the Vaadin Swing
Bridge. The demo has four stages: full bridge wrap → Vaadin shell + bridge views
→ full Vaadin interoperability (Suppliers view) → React/Hilla shell and React
views driving the bridge (this branch, `swingbridge-hilla`).

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
    ├── src/main/frontend/
    │   ├── routes.tsx            ← custom router: file routes + /login outside the shell + Flow fallback
    │   ├── auth/
    │   │   ├── session.ts        ← shared auth signal over AuthService
    │   │   ├── LoginPage.tsx     ← React sign-in page (replaces LoginView.java)
    │   │   └── graphics.tsx      ← brand mark + login backdrop SVGs, as JSX
    │   └── views/
    │       ├── @layout.tsx       ← React AppLayout + SideNav shell (replaces MainLayout.java)
    │       ├── dashboard-modern.tsx  ← React dashboard (KPI tiles + charts) via Hilla
    │       └── suppliers.tsx     ← React view embedding the Swing table as <supplier-bridge>
    └── src/main/java/com/example/
        ├── Application.java          ← @SpringBootApplication, @Push, Aura stylesheet
        ├── SwingAppInitializer.java  ← @PostConstruct: PersistenceUtil.init() + DataSeeder.seedIfEmpty()
        ├── auth/                     ← AuthService (@BrowserCallable) + CurrentUser
        ├── suppliers/                ← SupplierEndpoint (@BrowserCallable) + SupplierRecord,
        │                                 SupplierBridgePanel + SupplierBridgeExporter (the Flow island)
        ├── dashboard/                ← DashboardService (@BrowserCallable) + KpiSummary, CategoryStock, LowStockItem, OrderStatusCount
        ├── swingbridge/ui/WarehouseView.java
        └── views/                    ← Flow views, no layout: the React shell wraps them
            ├── DashboardView.java    ← embeds DashboardBridge
            ├── ProductsView.java     ← embeds ProductsBridge
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
Sign-in goes through `AuthService`, not Spring Security.

To reset data: delete `warehousedb.mv.db` in the relevant app directory and restart.

## Tech stack
| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F (enterprise blue overrides) |
| Charts | JFreeChart 1.5.4 (Swing) / Vaadin Charts (Vaadin) |
| ORM | Hibernate 6.4 / jakarta.persistence |
| Database | H2 embedded (file-based, no setup needed) |
| Vaadin | 25.2, Aura theme, Spring Boot 4.1 |
| React views | Hilla (`hilla-spring-boot-starter`), file-based routing under `src/main/frontend/views` |
| App shell | React (`views/@layout.tsx`) — hosts both React and Flow views |
| Flow in React | `WebComponentExporter` + `reactElement` (Suppliers view) |
| Swing Bridge | 1.3.0 |
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

## Interop (the Suppliers demo)
A React view driving a Swing table. It uses the official Swing Bridge interop
API, not a hand-rolled event bus.

| Direction | Mechanism |
|---|---|
| Vaadin → Swing | `@ExposedMethod` on `MainFrame` methods; called via a generated bridge interface |
| Swing → Vaadin | `@VaadinCallback(observerFor = …Listener.class)` on `SupplierBridgePanel` handlers |

- The `swing-bridge-codegen-maven-plugin` scans the Swing jar at build time and
  generates `MainFrameBridge` into `target/generated-sources/swing-bridge`
  (added to the compile path by `build-helper-maven-plugin`). It is generated,
  not checked in.
- Vaadin obtains the handle with
  `SwingBridge.interop(MAIN_CLASS).of(MainFrameBridge.class)` and calls e.g.
  `handle.onReady(MainFrameBridge::showSuppliersTable)`.
- Callbacks are registered in `onAttach` and removed in `onDetach` so listeners
  don't leak across navigation.

### The Swing table as an island in a React view
`views/suppliers.tsx` is an ordinary file-router React view: toolbar, dialogs and
CRUD are plain React, and supplier data comes from `SupplierEndpoint` over Hilla.
The only Flow on the screen is `<supplier-bridge>` — `SupplierBridgePanel`
exported by `SupplierBridgeExporter` and placed with `reactElement`.

That is the whole reason for the split: `SwingBridge extends
com.vaadin.flow.component.html.Div`, a server-side Flow component with no custom
element, so React cannot render one. Exporting it as a web component is the only
way to put it inside a React view.

**Two channels, not interchangeable:**

| Concern | Path |
|---|---|
| Supplier CRUD + CSV | React → `SupplierEndpoint` (`@BrowserCallable`), typed client, no Flow involved |
| Bridge commands | React → exported properties. `filter` maps onto the Swing filter; `refreshToken` is a command dressed as state — properties only notify on *change*, so React increments a counter to request a reload |
| Selection, edit requests | Swing → `@VaadinCallback` → `webComponent.fireEvent(...)` → bubbling DOM events caught on a wrapper element in React |

So a save is two steps: write through the endpoint, then bump `refreshToken` so
the Swing table reloads. Folding the bridge call into the endpoint would couple
the data layer to whichever UI happens to be showing.

### Things that cost time here, in case they come up again
- **`@Push` must be on the `WebComponentExporter`.** The exported component runs
  in its own `UI`, which the `@Push` on `Application` does not cover. Without it
  the Swing app boots server-side but its frames never reach the browser and the
  bridge sits on its loading spinner forever — with no error anywhere. The
  documented "only one `@Push`" limitation only bites with several exporters.
- **The embedded UI does not start a second Swing app.** `SwingBridge.interop`
  resolves the session's existing instance; verified by there being a single
  `AWT-EventQueue` thread.
- **`ConfirmDialog` creates its buttons as light-DOM children.** Pass React
  children and React reconciles them away, leaving a message and no buttons. Use
  the `message` property.
- **A form seeded with `useState(props.x)` keeps stale values** when the parent
  swaps the record without unmounting — Edit then Add showed the previous
  supplier under an "Add" title. Key the component so it remounts.

## The React shell (Hilla)
The application shell is React. `views/@layout.tsx` provides the AppLayout,
drawer and SideNav; the Flow views — each embedding a Swing bridge — render
inside its `<Outlet/>`. That is the stage-four payoff: a React frontend hosting
a Swing desktop application, with a Vaadin toolbar driving the Swing table on
the Suppliers view.

| Concern | How it works |
|---|---|
| Java → TypeScript | `@BrowserCallable` services (`DashboardService`, `AuthService`); Hilla generates `Frontend/generated/endpoints.ts` and a TS interface per record at build time |
| Flow views inside React | `routes.tsx` calls `withFallback(Flow)`, which adds the `Flow` component to every branch — so `/products` and friends resolve to Java views rendered inside the React layout |
| Routing | File-based under `views/`. `routes.tsx` is a customised copy of the generated one; it **must** keep `withFileRoutes`, or the file router's output is silently ignored |
| Login outside the shell | `LoginPage.tsx` lives in `auth/`, not `views/`, and is mounted by `routes.tsx` as a sibling route — anything under `views/` would be wrapped in the layout |
| `/` outside the shell | Stage one (`WarehouseView` — the whole Swing app, its own login screen included) must be full-bleed and unguarded. It cannot be declared as a sibling in `routes.tsx`: the file router owns path `""` and `withFallback` adds an index route inside that branch, which always shadows a sibling `/`. `@layout.tsx` checks `FULL_BLEED_PATHS` and returns a bare `<Outlet/>` instead |
| Access control | `@AnonymousAllowed` on both services (browser-callable services reject every call otherwise). The session check is in `@layout.tsx`; there is no `.protect()`, which would hook Hilla's Spring Security integration this app doesn't use |
| Nullability | JSpecify `@NonNull` on records' reference-typed fields, so the generated TS isn't all-optional |
| Styling | Unchanged — `styles.css` is global, so the React DOM reuses the same `.login-*`, `.nav-*`, `.drawer-*` and `.md-*` rules |

### Session sharing between Flow and Hilla
`AuthService` is the only place the current user is resolved, because **Flow and
Hilla cannot see the same session object**. Flow code runs in a Vaadin request
and reaches `VaadinSession`; a `@BrowserCallable` method is invoked by Hilla's
controller, where `VaadinSession.getCurrent()` is null. The shared ground is the
servlet `HttpSession` the Vaadin session is itself stored in, so `AuthService`
resolves it from whichever request is in flight — `VaadinSession.getSession()`
or Spring's `RequestContextHolder`.

**Every public method of a `@BrowserCallable` class becomes an endpoint.** A
helper returning the Hibernate `User` entity was briefly public here, and Hilla
duly generated and published a type carrying the password field. Keep anything
returning an entity private; DTOs like `CurrentUser` are what leave the server.

In Vaadin 25 Hilla is opt-in: `hilla-spring-boot-starter` must be declared, as
`vaadin-spring-boot-starter` no longer brings it.

## Notable implementation details
- **Single selection**: all JTables use `ListSelectionModel.SINGLE_SELECTION` (set in `UIConstants.styleTable()`), avoiding multi-select confusion in the bridge.
- **PurchaseOrderDialog**: `updateTotal()` has a reentrancy guard (`updatingTotal` flag) to prevent infinite recursion when `setValueAt()` fires the table model listener.
- **Deselection**: `SupplierSelectedEvent.id` is `null` on row deselection; it
  reaches React as an event with `detail.id === null`, which disables Edit/Delete.
- **CSV export exists on both sides**: `TableExporter` (Swing, `JFileChooser`) is
  used by `ProductsPanel` and `SuppliersPanel`; the React Suppliers view
  additionally offers a browser download — `SupplierEndpoint.exportCsv()` returns
  the text and the view saves it via a Blob URL.
- **CSS**: uses Aura theme properties (`--aura-*`, `--vaadin-*`). `--lumo-*` variables are not used except where Aura inherits them implicitly.
