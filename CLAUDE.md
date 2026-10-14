# WarehousePro Demo (NetBeans Platform edition) — Context for Claude

## What this project is
A NetBeans Platform (RCP) → Vaadin migration demo showing enterprise Java developers
how a legacy NetBeans RCP desktop app can be modernised to run in the browser using
the Vaadin Swing Bridge's `NetBeansRcpBridge`. This branch is the RCP counterpart of
the plain-Swing demo on `main`. Stages:
1. **Full bridge wrap** — the entire, unchanged RCP shell served at `/` (`full` mode).
2. **Vaadin shell + bridged TopComponents** — native Vaadin shell, each route one RCP
   `TopComponent` embedded via the bridge (`/dashboard`, `/products`, `/suppliers`,
   `/orders`, `/stock`; `views` mode).
3. *(Two-way interop is omitted — the RCP bridge has no `@ExposedMethod` equivalent.)*
4. **Native Vaadin rebuild** — the dashboard re-implemented in pure Vaadin on
   `/dashboard-modern` (works in both modes).

## Project structure
```
warehouse-demo/
├── .gitignore
├── README.md                  ← developer-facing project overview
├── CLAUDE.md                  ← you are here
├── SPEC.md                    ← full feature spec of the WarehousePro app ("before" state)
├── pom.xml                    ← Maven aggregator: netbeans-app, then vaadin-app
├── start-netbeans.sh / .bat   ← builds + runs the NetBeans desktop app
├── start-vaadin.sh / .bat     ← builds netbeans-app, copies its distribution, starts vaadin-app [full|views]
├── netbeans-app/              ← WarehousePro on the NetBeans Platform 25 (see netbeans-app/README.md)
│   ├── pom.xml                ← parent: NetBeans RELEASE250 + plugin versions
│   ├── application/           ← branded platform application (distribution in target/warehousepro)
│   │   └── src/main/etc/warehousepro.conf  ← launcher conf: --add-opens/exports, -Djava.security.manager=allow
│   └── warehouse/             ← the module (code-name base com.example.warehouse)
│       ├── pom.xml            ← nbm packaging; also attaches a plain "domain" classifier jar
│       └── src/main/java/com/example/warehouse/
│           ├── AppSession, config/, model/, dao/, service/, seed/  ← domain layer (→ domain jar)
│           ├── startup/DatabaseStartup.java  ← @OnStart: init H2 + seed; once the main window is up,
│           │                                   login dialog, then opens the 5 TopComponents
│           │                                   (login + opening skipped when -Dwarehouse.bridge=true)
│           ├── startup/LoginDialog.java
│           ├── actions/LogoutAction.java
│           └── ui/{dashboard,products,suppliers,orders,stock}/  ← TopComponents + dialogs; ui/util
└── vaadin-app/                ← Vaadin 25.3 Spring Boot host (Aura theme), port 8888
    ├── pom.xml                ← Spring Boot 4.1, Vaadin 25.3, SwingBridge 1.3.0; depends on
    │                            com.example:warehouse:3.5.0:domain (all transitive deps excluded)
    ├── applibs/warehousepro/  ← RCP distribution (copied by start-vaadin.sh, git-ignored)
    └── src/main/java/com/example/
        ├── Application.java          ← @SpringBootApplication, @Push, Aura stylesheet
        ├── SwingAppInitializer.java  ← @PostConstruct: inits H2 + seeds demo data
        ├── dashboard/                ← DashboardService + KpiSummary, CategoryStock, LowStockItem, OrderStatusCount
        ├── rcp/RcpMode.java          ← warehouse.rcp.mode (full|views); builds the bridge or a placeholder;
        │                               sets warehouse.bridge=true only in views mode
        ├── rcp/WarehouseRcpView.java ← Stage 1: full RCP shell at "/" (no Vaadin login); forwards to
        │                               /dashboard in views mode
        └── views/
            ├── LoginView.java        ← Vaadin login (views mode), navigates to RcpMode.homeRoute()
            ├── MainLayout.java       ← AppLayout + SideNav (Barlow font, filled variant)
            ├── DashboardView, ProductsView, SuppliersView, OrdersView, StockView
            │                         ← Stage 2: rcpMode.topComponent("XxxTopComponent")
            └── DashboardModernView.java ← Stage 4: native Vaadin dashboard, no bridge
```

## How to run

### NetBeans desktop app
```bash
./start-netbeans.sh
```
Builds the NetBeans Platform app (first build downloads the platform cluster) and
launches it via `nbm:run-platform`. Login: `admin / admin` or `manager / manager`.

### Vaadin app
```bash
./start-vaadin.sh          # mode from application.properties (full by default)
./start-vaadin.sh views    # override: views mode
```
Builds `netbeans-app` (`mvn install`, which also installs the domain jar), copies the
distribution to `vaadin-app/applibs/warehousepro/`, then runs `spring-boot:run` with
`--warehouse.rcp.mode=<mode>`. Open `http://localhost:8888`.

To reset data: delete `warehousedb.mv.db` in the relevant app directory and restart.

## Tech stack
| Layer | Technology |
|---|---|
| Desktop UI | NetBeans Platform 25 (RCP), FlatLaf |
| Desktop charts | JFreeChart 1.5.4 |
| Bridge | Vaadin Swing Bridge 1.3.0 (`com.vaadin.swingbridge.NetBeansRcpBridge`) |
| ORM | Hibernate 6.4 / jakarta.persistence |
| Database | H2 embedded (file-based, `AUTO_SERVER=TRUE`, shared by host and RCP) |
| Vaadin | 25.3, Aura theme, Spring Boot 4.1 |
| Vaadin charts | Vaadin Charts + Vaadin `Dashboard` (modern view) |
| Build | Maven 3.x |
| Java | 21 |

## Domain model
- **Product**: sku, name, category, supplier, unitPrice, quantity, reorderLevel, unit
- **Category**: 5 seeded (Electronics, Power Tools, Hand Tools, Safety Equipment, Office Supplies)
- **Supplier**: 6 seeded (TechSource Inc., Industrial Pro Supply, SafeGuard Products, Office Depot Business, Global Parts Co., FastTrack Logistics)
- **PurchaseOrder**: supplier, orderDate, expectedDate, status (PENDING/ORDERED/DELIVERED/CANCELLED), items
- **StockMovement**: product, movementType (IN/OUT/ADJUSTMENT), quantity, reason, date

## Key RCP/Swing patterns (each maps to a Vaadin equivalent)
| NetBeans RCP / Swing | Vaadin |
|---|---|
| Platform window system + `TopComponent`s | `AppLayout` + `SideNav` + routes |
| `JTable` + `TableRowSorter` | `Grid<T>` |
| `RowFilter` live search | Grid data provider filter |
| `JDialog` modal form | `Dialog` + `FormLayout` |
| `JComboBox` | `ComboBox<T>` |
| `JSpinner` | `NumberField` |
| JFreeChart bar/pie | Vaadin Charts |
| `JSplitPane` | `SplitLayout` |
| Custom `TableCellRenderer` | Grid column renderer |

## Gotchas (learned the hard way)
- **One bridge mode per session**: a full-shell bridge and `embedTopComponent` bridges
  must not be mixed in one Vaadin session (single-TopComponent mode hides the platform
  main window the shell needs). Hence `warehouse.rcp.mode`: all routes stay registered,
  in views mode `/` forwards to `/dashboard`, in full mode the TopComponent routes render
  a placeholder.
- **No NetBeans module jars on the host classpath**: anything with an `OpenIDE-Module`
  manifest (the module jar itself, or the `org-openide-*` API jars it depends on) is
  picked up by the embedded platform as a duplicate module (`DuplicateException`), and
  the real one is rejected — no `@OnStart`, empty shell. That's why the host uses the
  `domain` classifier jar with `<exclusions>*:*</exclusions>`.
- **JDK 21 only, and not JetBrains Runtime**: the platform needs the Security Manager
  (`-Djava.security.manager=allow` on 21; gone on 24+). JBR's patched `java.desktop`
  calls `PlatformGraphicsInfo.getSplashScreenLib()`, which the SwingBridge
  `java.desktop` patch lacks, so the JVM exits when the platform boots. The start
  scripts prefer a non-JBR JDK 21.
- **No dialogs before the main window**: the NetBeans bridge streams nothing until the
  platform main window (`NbMainWindow`) appears. A modal, owner-less dialog shown from
  `@OnStart` (the original login) blocks the platform from ever opening that window, so
  the browser loads forever. The login is therefore shown via
  `WindowManager.invokeWhenUIReady`, owned by the main window, and the TopComponents
  (`openAtStartup = false`) are opened after login. In views mode they stay closed —
  `embedTopComponent` finds them by ID anyway.
- **One NetBeans instance per HTTP session**: the bridge keys instances by HTTP session
  id + main class, and `NetBeansRcpBridge` always uses `NetBeansLauncher`, so every
  bridge in a session shares one platform. A custom class loader
  (`createClassLoaderSupplier()`) doesn't change that; separate instances need separate
  HTTP sessions.
- **`warehouse.bridge`** is set at runtime by `RcpMode` (not in the pom), before the
  platform boots: `true` in views mode (Vaadin login, RCP auto-signs-in as admin),
  `false` in full mode (RCP shows its own login).
- **CSS**: uses Aura theme properties (`--aura-*`, `--vaadin-*`).
