# WarehousePro 3.5 — NetBeans Platform (RCP) edition

This is the **NetBeans Rich Client Platform** edition of WarehousePro: the exact
same warehouse-management application described in [`../SPEC.md`](../SPEC.md) — same
domain model, same embedded H2 + Hibernate backend, same five screens and the same
behaviours — but built on the **NetBeans Platform** instead of being a hand-rolled
`JFrame`/`JTabbedPane` Swing app.

It sits alongside the `swing-app` (plain Swing) and `vaadin-app` (browser) editions
as another realistic "before" rich client for migration demos.

## How it maps to the NetBeans Platform

| WarehousePro concept (Swing) | NetBeans Platform realisation |
|---|---|
| `MainFrame` + `JTabbedPane` (5 tabs) | The platform **window system**; each screen is a `TopComponent` opened in the **`editor`** mode (the tabbed document area) |
| `DashboardPanel`, `ProductsPanel`, … | `DashboardTopComponent`, `ProductsTopComponent`, `SuppliersTopComponent`, `OrdersTopComponent`, `StockTopComponent` |
| `LoginFrame` shown before `MainFrame` | `LoginDialog` shown from an `@OnStart` runnable, which blocks the startup sequence **before** the main window appears |
| `Main.main()` DB init + seed | `@OnStart` `DatabaseStartup` (TCCL pinned to the module loader so Hibernate boots under the NetBeans classloader) |
| Bottom `StatusBar` (message / user / clock) | The platform **status line** — `Status` pushes to `StatusDisplayer`; `UserStatusLineElement` (a `StatusLineElementProvider`) shows the user + clock |
| `File ▸ Logout` / title-bar Logout | `LogoutAction` (`@ActionRegistration` in `Menu/File`) |
| `File ▸ Exit`, `Help ▸ About` | Provided by the platform out of the box |
| Window title / product version | Platform **branding** (`src/main/nbm-branding`, applied via `--branding warehousepro`) |
| `JTable` + `TableRowSorter` + `RowFilter`, `JDialog` forms, `JSplitPane`, JFreeChart, custom `TableCellRenderer`s | Unchanged — these are plain Swing and are reused verbatim inside the `TopComponent`s and dialogs |

The domain layer (`model`, `dao`, `service`, `config`, `seed`, `AppSession`) and the
Swing helpers (`UIConstants`, `TableExporter`) are reused **verbatim** from the Swing
edition.

## Project layout (Maven, `nbm-maven-plugin`)

```
netbeans-app/
├── pom.xml            ← parent: pins NetBeans RELEASE250 + plugin versions, JDK 17
├── warehouse/         ← the application module (packaging: nbm)
│   ├── pom.xml        ← depends on org.netbeans.api:* + bundles Hibernate/H2/JFreeChart
│   └── src/main/
│       ├── java/com/example/warehouse/…   ← domain layer + TopComponents + dialogs + startup
│       ├── resources/META-INF/persistence.xml
│       └── nbm-branding/                   ← window-title / product-version branding
└── application/       ← the assembly (packaging: nbm-application)
    ├── pom.xml        ← platform cluster + the warehouse module → a runnable, branded app
    └── src/main/etc/warehousepro.conf      ← launcher conf (JDK 17+ --add-opens/--add-exports)
```

The non-NetBeans dependencies (Hibernate, H2, JFreeChart, SLF4J) are packaged into
the module's own classloader as class-path extensions, so JPA runs correctly under
the NetBeans module class-loading scheme.

## Build & run

From the repo root:

```bash
./start-netbeans.sh
```

Or directly:

```bash
cd netbeans-app
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean install
cd application
mvn nbm:run-platform
```

Login: `admin / admin` or `manager / manager`.

A throwaway H2 database file `warehousedb.mv.db` is created in the working directory
on first run; delete it to reset demo data.

## Notes

- **JDK 17** is used to build and launch. The custom `warehousepro.conf` supplies the
  `--add-opens` / `--add-exports` flags a Swing-based platform needs on modern JDKs.
- The platform cluster (`org.netbeans.cluster:platform:RELEASE250`) downloads from
  Maven Central on first build (cached thereafter).
- This edition intentionally omits the Vaadin **SwingBridge** `@ExposedMethod`
  plumbing that the Swing edition carries — that is migration-demo infrastructure,
  not part of the WarehousePro application spec. Bridging the NetBeans RCP app would
  be a natural follow-up.
```
