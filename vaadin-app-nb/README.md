# vaadin-app-nb — Vaadin host that bridges the **NetBeans RCP** app

This is the NetBeans-RCP counterpart of [`vaadin-app`](../vaadin-app). Instead of
bridging the plain-Swing WarehousePro, it embeds the **NetBeans Platform (RCP)**
edition ([`netbeans-app`](../netbeans-app)) in the browser using the Vaadin Swing
Bridge's `NetBeansRcpBridge` (SwingBridge **1.2.0**).

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

### Login

The browser-side **Vaadin `LoginView`** authenticates (against the shared H2 `User`
table). The embedded RCP is launched with `-Dwarehouse.bridge=true`, which makes its
`@OnStart` skip the desktop login dialog and auto-sign-in (see
`netbeans-app/.../startup/DatabaseStartup`). Host and RCP share the same H2 file
(`jdbc:h2:./warehousedb;AUTO_SERVER=TRUE`).

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
../start-netbeans-vaadin.sh
```

It builds `netbeans-app`, copies the RCP **distribution** into
`vaadin-app-nb/applibs/warehousepro/` and the **domain jar** into
`vaadin-app-nb/lib/`, then starts the host. Open `http://localhost:8888`.
Login: `admin / admin` or `manager / manager`.

> The first session has a ~5–8 s cold-start while the platform initialises;
> later sessions reuse the warmed platform.

`applibs/` and `lib/` are build-copied (git-ignored). This module is **not** in the
root Maven aggregator because it needs that copied distribution present first — run
the script (or build `netbeans-app` and copy) before building it standalone.
