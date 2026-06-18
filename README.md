# WarehousePro — Vaadin Swing Bridge Demo

A staged migration demo showing how a legacy Java Swing desktop application
(**WarehousePro 3.5**) can be modernised to run in the browser using the
[Vaadin Swing Bridge](https://vaadin.com/swing-bridge), without rewriting the
existing UI code — and how individual screens can then be rebuilt natively in
Vaadin once they live in the browser.

---

## Modules

| Module | Description |
|---|---|
| `swing-app` | The original Swing desktop application (WarehousePro 3.5). Also contains the bridge entry points and the interop event/listener types. |
| `vaadin-app` | The Vaadin 25 / Spring Boot host application that embeds the Swing app and adds native Vaadin views. |

The root `pom.xml` is a Maven aggregator over the two modules. There is **no
separate `shared-api` module** — the interop contract (event POJOs and listener
interfaces) lives in `swing-app/.../bridge/`, and the Vaadin host sees those
types because the built Swing JAR is on its classpath (`vaadin-app/applibs/`).

---

## Running the apps

### Swing desktop app (standalone)

```bash
./start-swing.sh
```

Builds the fat JAR and launches the desktop app directly.
Login: `admin / admin` or `manager / manager`.

### Vaadin app (all stages)

```bash
./start-vaadin.sh
```

Builds the Swing JAR, copies it to `vaadin-app/applibs/`, then starts the
Spring Boot server. Open `http://localhost:8080` in a browser.

On startup `SwingAppInitializer` initialises the embedded H2 database and seeds
demo data. To reset, delete `warehousedb.mv.db` and restart.

---

## Stage 1 — Full Swing app in the browser (`/`)

The root route (`WarehouseView`) wraps the **entire, unchanged Swing
application** (`com.example.warehouse.Main`) inside a single `SwingBridge`
component. The bridge runs the Swing UI server-side and streams it to the
browser; the user interacts with the complete desktop app in a browser tab.

**What to point out:** the full Swing application running in the browser with
zero changes to the Swing code.

---

## Stage 2 — Vaadin shell + bridged Swing tabs (`/dashboard`, `/products`, `/orders`, `/stock`)

The desktop window chrome has been replaced with a native Vaadin shell, while
each tab's content is still the original Swing panel, embedded via the bridge.

What is already Vaadin-native:

- **Login** (`LoginView`) — Vaadin form backed by the existing Hibernate `User`
  entity and `AppSession`.
- **App shell** (`MainLayout`) — Vaadin `AppLayout` with `SideNav`,
  `DrawerToggle`, and a user/logout menu. Navigation is handled by the Vaadin
  router.
- **Per-view bridge embedding** — `DashboardView`, `ProductsView`,
  `OrdersView`, and `StockView` each create a `SwingBridge` for the shared Swing
  `MainFrame` (`com.example.warehouse.bridge.BridgeMain`) and then call the
  matching `showXxx()` method to display the right tab.

**What to point out:** the browser chrome is fully Vaadin; only the content area
is still Swing — and switching Vaadin views drives the embedded Swing app.

---

## Stage 3 — Two-way interop (`/suppliers`)

The Suppliers view demonstrates **bidirectional, type-safe communication**
between a Vaadin toolbar and the embedded Swing table, using the Vaadin Swing
Bridge interop API.

### Vaadin → Swing (typed method calls)

Swing methods that should be callable from the browser are annotated with
`@ExposedMethod` on `MainFrame`. At build time the Swing Bridge annotation
processor generates a typed interface, `MainFrameBridge` (into
`target/generated-sources/swing-bridge/`), with members such as:

```java
void showSuppliersTable();
void refreshSuppliers();
void setSupplierFilter(String filter);
void setSupplierSelectionListener(SupplierSelectionListener l);
void setSupplierEditRequestListener(SupplierEditRequestListener l);
```

The Vaadin view obtains a handle and calls those methods type-safely — no
reflection, no stringly-typed method names:

```java
BridgeHandle<MainFrameBridge> handle =
        SwingBridge.interop(MAIN_CLASS).of(MainFrameBridge.class);

handle.onReady(MainFrameBridge::showSuppliersTable);              // show the tab
refreshBtn.addClickListener(e -> handle.onReady(MainFrameBridge::refreshSuppliers));
searchField.addValueChangeListener(e -> handle.onReady(b -> b.setSupplierFilter(e.getValue())));
```

| Vaadin action | Swing method called |
|---|---|
| (on ready) | `showSuppliersTable()` |
| Refresh button | `refreshSuppliers()` |
| Search field | `setSupplierFilter(String)` |
| After save/delete | `refreshSuppliers()` |

### Swing → Vaadin (events)

The Swing side defines plain listener interfaces and event objects in
`swing-app/.../bridge/`:

```
SupplierSelectedEvent / SupplierSelectionListener     # row selected (id = null → deselected)
SupplierEditRequestEvent / SupplierEditRequestListener # row double-clicked
```

`MainFrame` exposes setters for these listeners (also part of the generated
bridge). The Vaadin view registers itself with `handle.registerCallback(this)`
and annotates its handler methods with `@VaadinCallback`, naming the Swing
listener they implement:

```java
@VaadinCallback(observerFor = SupplierSelectionListener.class)
public void onSelectionChanged(SupplierSelectedEvent event) {
    selectedId = event.id;
    editBtn.setEnabled(selectedId != null);
    deleteBtn.setEnabled(selectedId != null);
}

@VaadinCallback(observerFor = SupplierEditRequestListener.class)
public void onEditRequested(SupplierEditRequestEvent event) {
    openForm(event.id);
}
```

| User action (in Swing table) | Vaadin reaction |
|---|---|
| Row selected | Enable Edit + Delete, store selection |
| Row deselected (`id == null`) | Disable Edit + Delete |
| Double-click row | Open the Vaadin Edit form, pre-filled |

### CSV export — entirely Vaadin

The **Export CSV** button never touches Swing: `SuppliersView` calls
`SupplierService.exportToCsv()` and serves the bytes as a browser download via
`DownloadHandler` + `Anchor`.

**What to point out:** the Vaadin toolbar and Swing table react to each other in
real time — selection toggles buttons, Vaadin-side edits refresh the Swing
table, and the search field filters it live — all through generated, type-safe
bridge calls.

---

## Stage 4 — Native Vaadin rebuild (`/dashboard-modern`)

`DashboardModernView` ("Dashboard · Modern" in the side nav) is the same
dashboard data rebuilt as a **fully native Vaadin screen** — no bridge at all.
It uses the Vaadin `Dashboard` component (responsive widget grid), Vaadin
Charts, and a `Grid` for low-stock items.

It reads through `com.example.dashboard.DashboardService` (a Spring `@Service`)
which wraps the legacy `ProductService` / `PurchaseOrderService` and exposes
clean typed records (`KpiSummary`, `CategoryStock`, `LowStockItem`,
`OrderStatusCount`) instead of Hibernate entities or `Object[]` rows. Because it
hits the same H2 data as the Swing dashboard, the numbers match exactly.

**What to point out:** once a screen lives in the browser, you can replace it
incrementally with native Vaadin against the same data and services — the
endpoint of the migration.

---

## Tech stack

| Layer | Technology |
|---|---|
| Swing UI | Java Swing, Nimbus L&F |
| Swing charts | JFreeChart 1.5 |
| Vaadin | Vaadin 25.1, Aura theme |
| Vaadin charts | Vaadin Charts + Vaadin `Dashboard` (modern view) |
| Interop | Vaadin Swing Bridge (`com.vaadin.swingbridge`) |
| Web host | Spring Boot 4 |
| ORM | Hibernate 6.4 / Jakarta Persistence |
| Database | H2 embedded (file-based) |
| Java | 17 |
| Build | Maven 3 |
