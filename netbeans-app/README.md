# WarehousePro 3.5 — NetBeans Platform (RCP) edition

This is the **NetBeans Rich Client Platform** edition of WarehousePro: the exact
same warehouse-management application described in [`../SPEC.md`](../SPEC.md) — same
domain model, same embedded H2 + Hibernate backend, same five screens and the same
behaviours — but built on the **NetBeans Platform** instead of being a hand-rolled
`JFrame`/`JTabbedPane` Swing app.

It is the "before" rich client of this branch's migration demo; [`../vaadin-app`](../vaadin-app)
embeds it in the browser with the Vaadin Swing Bridge's `NetBeansRcpBridge`.

## How it maps to the NetBeans Platform

| WarehousePro concept (Swing) | NetBeans Platform realisation |
|---|---|
| `MainFrame` + `JTabbedPane` (5 tabs) | The platform **window system**; each screen is a `TopComponent` opened in the **`editor`** mode (the tabbed document area) |
| `DashboardPanel`, `ProductsPanel`, … | `DashboardTopComponent`, `ProductsTopComponent`, `SuppliersTopComponent`, `OrdersTopComponent`, `StockTopComponent` |
| `LoginFrame` shown before `MainFrame` | `LoginDialog`, owned by the main window, shown via `WindowManager.invokeWhenUIReady` once the (still empty) main window is up; the five `TopComponent`s (`openAtStartup = false`) are opened only after a successful login. Not shown straight from `@OnStart`: an owner-less dialog before the main window blocks the platform from opening it, so the SwingBridge never streams anything |
| `Main.main()` DB init + seed | `@OnStart` `DatabaseStartup` (TCCL pinned to the module loader so Hibernate boots under the NetBeans classloader) |
| Bottom `StatusBar` (message / user / clock) | The platform **status line** — `Status` pushes to `StatusDisplayer`; `UserStatusLineElement` (a `StatusLineElementProvider`) shows the user + clock |
| `File ▸ Logout` / title-bar Logout | `LogoutAction` (`@ActionRegistration` in `Menu/File`) |
| `File ▸ Exit`, `Help ▸ About` | Provided by the platform out of the box |
| Window title / product version | Platform **branding** (`src/main/nbm-branding`, applied via `--branding warehousepro`) |
| `JTable` + `TableRowSorter` + `RowFilter`, `JDialog` forms, `JSplitPane`, JFreeChart, custom `TableCellRenderer`s | Unchanged — these are plain Swing and are reused verbatim inside the `TopComponent`s and dialogs |

The domain layer (`model`, `dao`, `service`, `config`, `seed`, `AppSession`) and the
Swing helpers (`UIConstants`, `TableExporter`) are reused **verbatim** from the Swing
edition (on the `main` branch).

## Project layout (Maven, `nbm-maven-plugin`)

```
netbeans-app/
├── pom.xml            ← parent: pins NetBeans RELEASE250 + plugin versions, compiles with --release 17
├── warehouse/         ← the application module (packaging: nbm)
│   ├── pom.xml        ← depends on org.netbeans.api:* + bundles Hibernate/H2/JFreeChart
│   └── src/main/
│       ├── java/com/example/warehouse/…   ← domain layer + TopComponents + dialogs + startup
│       ├── resources/META-INF/persistence.xml
│       └── nbm-branding/                   ← window-title / product-version branding
└── application/       ← the assembly (packaging: nbm-application)
    ├── pom.xml        ← platform cluster + the warehouse module → a runnable, branded app
    └── src/main/etc/warehousepro.conf      ← launcher conf (--add-opens/--add-exports, security.manager=allow)
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
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean install
cd application
mvn nbm:run-platform
```

Login: `admin / admin` or `manager / manager`.

A throwaway H2 database file `warehousedb.mv.db` is created in the working directory
on first run; delete it to reset demo data.

## Notes

- **JDK 21** is used to build and launch (the same JDK SwingBridge needs). The custom
  `warehousepro.conf` supplies the `--add-opens` / `--add-exports` flags a Swing-based
  platform needs on modern JDKs, plus `-Djava.security.manager=allow`: the platform
  installs a Security Manager at startup, which JDK 21 refuses without it and JDK 24+
  no longer supports at all.
- The platform cluster (`org.netbeans.cluster:platform:RELEASE250`) downloads from
  Maven Central on first build (cached thereafter).
- The module carries no SwingBridge code; the only bridge hook is `-Dwarehouse.bridge=true`,
  which makes `DatabaseStartup` skip the desktop login (set by `vaadin-app` in views mode).
- Besides the NBM, `warehouse/` attaches a plain **`domain` classifier jar** (`AppSession`,
  `config`, `model`, `dao`, `service`, `seed`, `persistence.xml`) without the
  `OpenIDE-Module` manifest, for `vaadin-app`'s own login and native dashboard. The module
  jar itself must not go on the host classpath: the embedded platform would load it as a
  duplicate module and reject the real one.
```
