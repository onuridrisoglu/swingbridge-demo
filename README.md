# WarehousePro — Vaadin Swing Bridge Demo (NetBeans Platform edition)

A staged migration demo showing how a legacy **NetBeans Platform (RCP)** desktop
application (**WarehousePro 3.5**) can be modernised to run in the browser using the
[Vaadin Swing Bridge](https://vaadin.com/swing-bridge)'s `NetBeansRcpBridge`, without
changing the RCP source — and how individual screens can then be rebuilt natively in
Vaadin once they live in the browser.

> The plain-Swing edition of this demo (with two-way interop) lives on the `main` branch.

---

## Modules

| Module | Description |
|---|---|
| `netbeans-app` | The WarehousePro app on the **NetBeans Platform**: screens are `TopComponent`s in the platform window system, login runs from `@OnStart`. Also attaches a plain `domain` jar (entities, DAOs, services) for the Vaadin host. See [`netbeans-app/README.md`](netbeans-app/README.md). |
| `vaadin-app` | The Vaadin 25 / Spring Boot host that embeds the RCP distribution via `NetBeansRcpBridge` (SwingBridge 1.3.0) and adds native Vaadin views. See [`vaadin-app/README.md`](vaadin-app/README.md). |

The root `pom.xml` aggregates both (`netbeans-app` first, since `vaadin-app` depends
on its `domain` jar).

---

## Running the apps

Both need **JDK 21**: the NetBeans Platform installs a Security Manager, which JDK 21
only allows with `-Djava.security.manager=allow` and JDK 24+ no longer supports. The
scripts pick a JDK 21 via `/usr/libexec/java_home` on macOS.

### NetBeans Platform desktop app

```bash
./start-netbeans.sh
```

Builds the NetBeans Platform application and launches it (the first build downloads
the platform cluster). Login: `admin / admin` or `manager / manager`.

### Vaadin app (NetBeans RCP in the browser)

```bash
./start-vaadin.sh          # mode from application.properties (full by default)
./start-vaadin.sh views    # override: views mode
```

Builds `netbeans-app`, copies its distribution into `vaadin-app/applibs/`, and starts
the Spring Boot server. Open `http://localhost:8888`.

The full shell and per-`TopComponent` bridges must not be mixed in one Vaadin session,
so the host runs in one of two modes (`warehouse.rcp.mode` in
`vaadin-app/src/main/resources/application.properties`):

| Mode | Stage shown | Login |
|---|---|---|
| `full` | Stage 1 — the unchanged RCP shell at `/` | The RCP's own login dialog |
| `views` | Stage 2 — Vaadin shell, one `TopComponent` per route | Vaadin `LoginView` |

In `views` mode `/` forwards to `/dashboard`; in `full` mode the TopComponent routes show
a placeholder. Stage 4
(`/dashboard-modern`) works in both.

To reset data: delete `warehousedb.mv.db` in the relevant app directory and restart.

---

## Stage 1 — Full RCP app in the browser (`/`, `full` mode)

`WarehouseRcpView` mounts `new NetBeansRcpBridge()`: the **entire, unchanged NetBeans
Platform application** — menu bar, toolbar, docked windows, its own login dialog — runs
server-side and streams to the browser.

**What to point out:** a NetBeans Platform application running in a browser tab, with
no bridge code in the RCP. (One startup tweak was needed: the login dialog is shown once
the main window is up rather than before it, because the bridge only starts streaming
when the main window appears.)

---

## Stage 2 — Vaadin shell + bridged TopComponents (`/dashboard`, `/products`, `/suppliers`, `/orders`, `/stock`, `views` mode)

The platform chrome is replaced with a native Vaadin shell, while each route's content
is still an original RCP `TopComponent`, embedded with
`new NetBeansRcpBridge().embedTopComponent("…TopComponent")`.

What is already Vaadin-native:

- **Login** (`LoginView`) — Vaadin form backed by the existing Hibernate `User` entity
  and `AppSession`. The embedded RCP runs with `-Dwarehouse.bridge=true`, so its
  `@OnStart` skips the desktop login.
- **App shell** (`MainLayout`) — Vaadin `AppLayout` with `SideNav`, `DrawerToggle`, and
  a user/logout menu.

All routes share one embedded platform per session: the first view boots it, the others
reuse it.

**What to point out:** the browser chrome is fully Vaadin; only the content area is
still RCP.

Two-way interop (Stage 3 on `main`) is omitted — the RCP bridge has no
`@ExposedMethod` equivalent.

---

## Stage 4 — Native Vaadin rebuild (`/dashboard-modern`)

`DashboardModernView` ("Dashboard · Modern" in the side nav) is the same dashboard data
rebuilt as a **fully native Vaadin screen** — no bridge at all. It uses the Vaadin
`Dashboard` component, Vaadin Charts, and a `Grid` for low-stock items.

It reads through `com.example.dashboard.DashboardService` (a Spring `@Service`), which
wraps the legacy `ProductService` / `PurchaseOrderService` from the RCP module's
`domain` jar and exposes typed records (`KpiSummary`, `CategoryStock`, `LowStockItem`,
`OrderStatusCount`). Because it hits the same H2 data as the RCP dashboard, the numbers
match exactly.

**What to point out:** once a screen lives in the browser, you can replace it
incrementally with native Vaadin against the same data and services.

---

## Tech stack

| Layer | Technology |
|---|---|
| Desktop UI | NetBeans Platform 25 (RCP), FlatLaf |
| Desktop charts | JFreeChart 1.5 |
| Vaadin | Vaadin 25.3, Aura theme |
| Vaadin charts | Vaadin Charts + Vaadin `Dashboard` (modern view) |
| Bridge | Vaadin Swing Bridge 1.3.0 (`NetBeansRcpBridge`) |
| Web host | Spring Boot 4.1 |
| ORM | Hibernate 6.4 / Jakarta Persistence |
| Database | H2 embedded (file-based) |
| Java | 21 |
| Build | Maven 3 |
