# vaadin-app — Vaadin host that bridges the **NetBeans RCP** app

Embeds the **NetBeans Platform (RCP)** edition of WarehousePro
([`netbeans-app`](../netbeans-app)) in the browser using the Vaadin Swing Bridge's
`NetBeansRcpBridge` (SwingBridge **1.3.0**). The plain-Swing counterpart lives on the
`main` branch.

Docs: <https://vaadin.com/docs/latest/tools/modernization-toolkit/swing-bridge/netbeans-rcp>

## How it works

`com.vaadin.swingbridge.NetBeansRcpBridge` boots a **pre-built NetBeans Platform
distribution** server-side, in an isolated classloader / thread group / AWT
`AppContext` per Vaadin session, and streams it to the browser — without changing
the RCP source. It offers two modes, both used here:

- `new NetBeansRcpBridge()` — the **full platform shell** (menu, toolbar, all docked windows).
- `new NetBeansRcpBridge().embedTopComponent("ProductsTopComponent")` — a **single TopComponent**, no shell, so it drops into a Vaadin route.

The distribution is discovered via `-Dswingbridge.rcp.cluster.dir=<abs path>`
(pointing at `applibs/warehousepro`, which must contain `etc/<branding>.clusters`
and `platform/lib/boot.jar`).

## Stages

| Route | What | Bridge |
|---|---|---|
| `/` | **Stage 1** — entire unchanged NetBeans RCP shell in the browser | `new NetBeansRcpBridge()` |
| `/dashboard`, `/products`, `/suppliers`, `/orders`, `/stock` | **Stage 2** — Vaadin shell (AppLayout + SideNav) hosting one bridged `TopComponent` each | `.embedTopComponent("…TopComponent")` |
| `/dashboard-modern` | **Stage 4** — native Vaadin dashboard, no bridge, over the same H2 data | — (reads `DashboardService`) |

Two-way interop (Stage 3) is omitted — the RCP bridge has no `@ExposedMethod`
equivalent.

### Mode

The full shell and per-`TopComponent` bridges must not be mixed in one Vaadin
session (single-TopComponent mode hides the platform main window the shell needs).
`warehouse.rcp.mode` in `application.properties` picks one:

| Mode | Active routes | Login |
|---|---|---|
| `full` (default) | `/` — the unchanged RCP shell | The RCP's own desktop login dialog |
| `views` | `/dashboard`, `/products`, … in the Vaadin shell | Vaadin `LoginView`; the RCP skips its login |

All routes stay registered: in `views` mode `/` forwards to `/dashboard`; in `full`
mode the TopComponent routes show a placeholder.
`/dashboard-modern` works in both. `com.example.rcp.RcpMode` holds the mode and sets
`-Dwarehouse.bridge` for the RCP's `@OnStart` (`netbeans-app/.../startup/DatabaseStartup`),
which skips the desktop login and auto-signs-in when it's `true`. Host and RCP share
the same H2 file (`jdbc:h2:./warehousedb;AUTO_SERVER=TRUE`).

## Requirements

- **JDK 21** — NetBeans Platform 25 + `-Djava.security.manager=allow` (JDK 24+ would
  need an RCP variant without `TopSecurityManager`).
- A valid SwingBridge license (same as `vaadin-app`).
- The base SwingBridge runtime flags (`--patch-module`, `-Xbootclasspath/a`, the
  `--add-exports`/`--add-opens` set) **plus** the RCP extras
  (`--add-opens=java.base/java.lang|java.net|java.security`,
  `--add-opens=java.desktop/javax.swing[.text]`, `-Djava.security.manager=allow`) —
  all configured in `spring-boot-maven-plugin`'s `<jvmArguments>`.

## Build & run

```bash
../start-vaadin.sh          # mode from application.properties (full by default)
../start-vaadin.sh views    # override: views mode
```

It builds `netbeans-app` with `mvn install` (which also installs the module's plain
**`domain` jar** this host depends on — see below), copies the RCP **distribution**
into `vaadin-app/applibs/warehousepro/` (git-ignored), then starts the host. Open
`http://localhost:8888`. Login: `admin / admin` or `manager / manager` (in the RCP
dialog in `full` mode, the Vaadin login in `views` mode).

> The first session has a ~5–8 s cold-start while the platform initialises;
> later sessions reuse the warmed platform.

### Domain jar

The host's own login and native dashboard use the RCP's domain layer through
`com.example:warehouse:3.5.0:domain`, with **all transitive dependencies excluded**
(Hibernate/H2 are declared directly). Nothing carrying an `OpenIDE-Module` manifest —
the module jar or the `org-openide-*` API jars — may be on the host classpath: the
embedded platform picks those up as duplicate modules (`DuplicateException`) and
rejects the real ones, so `@OnStart` never runs and the shell stays empty.
