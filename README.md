# WarehousePro — Vaadin Swing Bridge Demo

A staged migration demo showing how a legacy Java Swing desktop application
(**WarehousePro 3.5**) can be modernised to run in the browser using the
[Vaadin Swing Bridge](https://vaadin.com/swing-bridge), without rewriting the
existing UI code — and how individual screens can then be rebuilt natively in
Vaadin once they live in the browser.

---

## Projects

| Module | Description |
|---|---|
| `swing-app` | The original Swing desktop application, plus a small `bridge/` package with the bridge entry point and the interop listener/event types |
| `vaadin-app` | The Vaadin 25 / Spring Boot host application that embeds the Swing app via the bridge and adds native Vaadin views |

---

## Running the apps

### Swing desktop app (standalone)

```bash
./start-swing.sh
```

Builds the fat JAR and launches the desktop app directly.
Login: `admin / admin`, `manager / manager`, or `viewer / viewer`.

### Vaadin app — development

```bash
./start-vaadin.sh
```

Builds the Swing JAR, copies it to `vaadin-app/applibs/`, then starts the
Spring Boot server with `spring-boot:run`. Open `http://localhost:8888`.

### Vaadin app — production

```bash
./start-vaadin-prod.sh
```

Builds an optimised production jar and starts it with `java -jar` and the
Swing Bridge JVM flags.

The Vaadin app needs **JDK 21**.

To reset data: delete `warehousedb.mv.db` in the relevant app directory and restart.

---

## Stage 1 — Swing Bridge wrap (`/`)

The root route serves the **entire Swing application** running inside the
browser via the Swing Bridge — zero UI changes to the Swing code.
`WarehouseView` is just `new SwingBridge("com.example.warehouse.Main")`: the
bridge runs the unchanged app (its own login frame, menu bar, tabs) server-side
in an isolated classloader per session and streams it to the browser.

**What to point out:** the complete Swing application running in a browser tab,
with no code changes on the Swing side.

---

## Stage 2 — Vaadin foundation (`/dashboard`, `/products`, `/orders`, `/stock`)

The application shell has been replaced with native Vaadin components while the
screen content is still the original Swing panels.

What is already Vaadin-native:

- **Login screen** (`LoginView`) — Vaadin form backed by the existing Hibernate
  `User` entity and `AppSession`.
- **App shell** (`MainLayout`) — Vaadin `AppLayout` with `SideNav`, `DrawerToggle`,
  and a user avatar/logout menu. Navigation is handled by the Vaadin router.

Every view embeds the same bridged entry point, `bridge/BridgeMain`, which opens
`MainFrame` in *bridge mode*: undecorated, with no menu or tabs, the five panels
in a `CardLayout`. The view then tells the frame which panel to show through the
interop API, e.g.
`SwingBridge.interop(MAIN_CLASS).of(MainFrameBridge.class).onReady(MainFrameBridge::showDashboard)`.

**What to point out:** the browser chrome is now fully Vaadin; only the content
area still shows Swing panels.

---

## Stage 3 — Interoperability (`/suppliers`)

The Suppliers view demonstrates **two-way communication** between a native
Vaadin toolbar and the Swing suppliers table, using the Swing Bridge interop
API (`@ExposedMethod` / `@VaadinCallback`).

### How it works

**Vaadin → Swing (method calls)**

Methods on `MainFrame` annotated with `@ExposedMethod` are scanned at build time
by the `swing-bridge-codegen-maven-plugin`, which generates a typed
`MainFrameBridge` interface (in `target/generated-sources/swing-bridge`, not
checked in). `SuppliersView` calls them through the handle:

| Vaadin action | Swing method called |
|---|---|
| View opens | `showSuppliersTable()` (suppliers card, Swing toolbar hidden) |
| Refresh button | `refreshSuppliers()` |
| Search field | `setSupplierFilter(String)` |
| After save/delete | `refreshSuppliers()` |

The **Export CSV** button is handled entirely on the Vaadin side: `SuppliersView`
reads the data via `SupplierService`, builds the CSV in memory, and serves it
as a browser download with a `DownloadHandler`. No Swing involved.

**Swing → Vaadin (callbacks)**

`SuppliersView` handlers annotated with
`@VaadinCallback(observerFor = …Listener.class)` are registered with the Swing
frame (in `onAttach`, removed in `onDetach`). When the user interacts with the
Swing table, the bridge delivers the events across the classloader boundary:

| User action | Event | Vaadin reaction |
|---|---|---|
| Row selected | `SupplierSelectedEvent` (with data) | Enable Edit + Delete buttons, store selection |
| Row deselected | `SupplierSelectedEvent` (id = null) | Disable Edit + Delete buttons |
| Double-click row | `SupplierEditRequestEvent` | Open the Edit form pre-filled |

**What to point out:** the Vaadin toolbar and the Swing table react to each
other in real time — row selection enables/disables buttons, edits made in the
Vaadin dialog refresh the Swing table, and the search field filters the Swing
table live.

---

## Stage 4 — Native Vaadin rebuild (`/dashboard-modern`)

`DashboardModernView` is the same dashboard rebuilt as a **fully native Vaadin
screen** — no bridge. It uses the Vaadin `Dashboard` component, Vaadin Charts,
and a `Grid` for low-stock items, reading through `DashboardService`, which
wraps the legacy Swing app's services. Because it hits the same H2 data as the
Swing dashboard, the numbers match.

**What to point out:** once a screen lives in the browser, you can replace it
incrementally with native Vaadin against the same data and services.

---

## Tech stack

| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F |
| Charts | JFreeChart 1.5 (Swing) / Vaadin Charts (Vaadin) |
| ORM | Hibernate 6.4 / Jakarta Persistence |
| Database | H2 embedded (file-based) |
| Vaadin | Vaadin 25.3, Aura theme |
| Swing Bridge | 1.3.0 |
| Spring Boot | 4.1 |
| Java | swing-app 17, vaadin-app 21 |
| Build | Maven 3 |
