# WarehousePro 3.5 — Swing Application Spec

## Overview
A realistic enterprise Java Swing desktop application for inventory and warehouse management.
Designed as the **"before"** state in a Swing → Vaadin migration demo.

## Tech Stack
| Component | Technology |
|---|---|
| Language | Java 17 |
| Build | Maven 3.x |
| UI | Java Swing (Nimbus L&F, enterprise blue theme) |
| ORM | Hibernate 6.4 / JPA |
| Database | H2 embedded (file: `./warehousedb`) |
| Charts | JFreeChart 1.5.4 |
| Packaging | Fat JAR via Maven Shade Plugin |

## Running the App
```bash
./start-swing.sh
```
Login: `admin / admin` or `manager / manager`

---

## Domain Model

### Entities

**User** — `app_user` table
- username (PK), password, fullName, role (ADMIN / MANAGER / VIEWER)

**Category** — `category` table
- id, name, description
- Seed data: Electronics, Power Tools, Hand Tools, Safety Equipment, Office Supplies

**Supplier** — `supplier` table
- id, name, contactName, email, phone, address
- 6 seeded suppliers (TechSource Inc., Industrial Pro Supply, SafeGuard Products, etc.)

**Product** — `product` table
- id, sku (unique), name, category (FK), supplier (FK, nullable)
- unitPrice, quantity, reorderLevel, unit, description
- 30 seeded products — 6 intentionally below reorder level

**PurchaseOrder** — `purchase_order` table
- id, supplier (FK), orderDate, expectedDate, status, notes
- Status: PENDING / ORDERED / DELIVERED / CANCELLED
- 10 seeded orders (mix of all statuses)

**PurchaseOrderItem** — `purchase_order_item` table
- id, order (FK), product (FK), quantity, unitPrice

**StockMovement** — `stock_movement` table
- id, product (FK), movementType (IN / OUT / ADJUSTMENT), quantity, reason, movementDate, createdBy
- 25 seeded movements over 90-day history

---

## Screens

### 1. Login Screen (`LoginFrame`)
- Gradient banner with app name and version
- Username / password form
- "Sign In" button
- Error label for bad credentials
- Footer hint with demo credentials

### 2. Main Frame (`MainFrame`)
- Title bar with gradient, user info, Logout button
- Menu bar: File | View | Help
- **JTabbedPane** with 5 tabs (primary Swing pattern)
- Status bar at bottom (message, user, clock)

### 3. Dashboard Tab (`DashboardPanel`)
- **4 KPI cards**: Total Products, Inventory Value, Pending Orders, Low Stock Alerts
- **Bar chart**: Stock quantity by category (JFreeChart)
- **Pie chart**: Purchase orders by status (JFreeChart)
- **Low-stock alert table**: Products at or below reorder level (highlighted rows)

### 4. Products Tab (`ProductsPanel`)
- **JTable** with columns: SKU, Name, Category, Supplier, On Hand, Reorder, Unit Price, Status
- `TableRowSorter` — click column headers to sort
- Live search/filter via `JTextField` + `RowFilter`
- Row color coding: red = out of stock, yellow = low stock, alternating = normal
- Toolbar: Add Product, Edit, Delete, **± Stock Adjust**, Refresh
- Double-click row → opens edit dialog
- Right-click context menu

### 5. Product Dialog (`ProductDialog`) — `JDialog`
- Modal form: SKU, Name, Category (JComboBox), Supplier (JComboBox), Unit Price, Quantity (JSpinner), Reorder Level, Unit, Description (JTextArea)
- Validates required fields before save

### Stock Adjustment Dialog — inline `JDialog`
- Opened from Products toolbar
- Fields: current qty display, movement type (IN/OUT/ADJUSTMENT), quantity (JSpinner), reason
- Creates a StockMovement record and updates product quantity atomically

### 6. Suppliers Tab (`SuppliersPanel`)
- JTable: ID, Name, Contact, Email, Phone, Address — **single-row selection only**
- Toolbar: Add, Edit, Delete, Refresh, Export CSV
- Live search filter
- Double-click / right-click to edit

### 7. Supplier Dialog (`SupplierDialog`) — `JDialog`
- Modal form: Company Name, Contact Person, Email, Phone, Address (JTextArea)

### 8. Purchase Orders Tab (`PurchaseOrdersPanel`)
- **Split pane** (vertical): order list above, line items below
- Order table: PO#, Supplier, Order Date, Expected, Status, Items, Total
- Status column color-coded (green=delivered, blue=ordered, orange=pending, red=cancelled)
- Line items auto-populate when order is selected
- Toolbar: New Order, Edit/View, Delete, Refresh

### 9. Purchase Order Dialog (`PurchaseOrderDialog`) — `JDialog`
- **Split pane**: order header above, line items table below
- Header fields: Supplier (JComboBox), Order Date, Expected Date, Status, Notes
- Items table: editable Quantity and Unit Price columns, auto-calculates Line Total and Order Total
- Add/Remove item buttons with product picker
- `updateTotal()` uses a reentrancy guard to prevent infinite recursion when writing calculated values back to the table model

### 10. Stock Movements Tab (`StockMovementsPanel`)
- Read-only audit log
- Columns: Date/Time, SKU, Product, Category, Type, Quantity, Reason, Created By
- Row color: green = IN, red/orange = OUT, neutral = ADJUSTMENT
- Live search filter
- "Type" column colored and bold

---

## Swing Patterns Summary
| Pattern | Location | Vaadin Equivalent |
|---|---|---|
| `JTabbedPane` | `MainFrame` | `Tabs` / `SideNav` in `AppLayout` |
| `JTable` + `TableRowSorter` | All data panels | `Grid<T>` |
| `RowFilter` (live search) | Products, Suppliers, Orders, Stock | `Grid` column filters |
| `JDialog` (modal form) | Product, Supplier, Order dialogs | `Dialog` + `FormLayout` |
| `JComboBox` | Category, Supplier selectors | `ComboBox<T>` / `Select<T>` |
| `JSpinner` | Quantity / reorder fields | `NumberField` |
| `JTextArea` | Description, Notes, Address | `TextArea` |
| JFreeChart bar chart | Dashboard | `Vaadin Charts` — ColumnSeries |
| JFreeChart pie chart | Dashboard | `Vaadin Charts` — PieSeries |
| `JSplitPane` | Orders panel | `SplitLayout` |
| Custom `TableCellRenderer` | Row color coding | `Grid` column renderers |
| `JMenuBar` | Main frame | `MenuBar` / AppLayout header |
| `JPopupMenu` | Tables right-click | `GridContextMenu` |
| Gradient `paintComponent` | Banner, title bar | CSS linear-gradient |

## Notes
- All JTables use `ListSelectionModel.SINGLE_SELECTION` (enforced in `UIConstants.styleTable()`).

