package com.example.warehouse.seed;

import com.example.warehouse.dao.*;
import com.example.warehouse.model.*;
import com.example.warehouse.model.PurchaseOrder.OrderStatus;
import com.example.warehouse.model.StockMovement.MovementType;
import com.example.warehouse.model.User.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DataSeeder {

    public static void seedIfEmpty() {
        UserDAO userDAO = new UserDAO();
        if (userDAO.count() > 0) return;

        seedUsers(userDAO);
        List<Category> categories = seedCategories();
        List<Supplier> suppliers = seedSuppliers();
        List<Product> products = seedProducts(categories, suppliers);
        List<PurchaseOrder> orders = seedPurchaseOrders(suppliers, products);
        seedStockMovements(products);
    }

    private static void seedUsers(UserDAO userDAO) {
        userDAO.save(new User("admin", "admin", "System Administrator", UserRole.ADMIN));
        userDAO.save(new User("manager", "manager", "Warehouse Manager", UserRole.MANAGER));
        userDAO.save(new User("viewer", "viewer", "Stock Viewer", UserRole.VIEWER));
    }

    private static List<Category> seedCategories() {
        CategoryDAO dao = new CategoryDAO();
        Category[] cats = {
            new Category("Electronics", "Barcode scanners, terminals, printers, networking"),
            new Category("Power Tools", "Cordless and electric power tools"),
            new Category("Hand Tools", "Manual tools and measuring instruments"),
            new Category("Safety Equipment", "PPE and workplace safety gear"),
            new Category("Office Supplies", "Stationery, printing, and consumables")
        };
        for (Category c : cats) dao.save(c);
        return dao.findAll();
    }

    private static List<Supplier> seedSuppliers() {
        SupplierDAO dao = new SupplierDAO();
        dao.save(new Supplier("TechSource Inc.", "Mark Henderson", "mark@techsource.com", "+1-800-555-0101", "1200 Silicon Ave, San Jose, CA 95101"));
        dao.save(new Supplier("Industrial Pro Supply", "Linda Kowalski", "linda@industrialpro.com", "+1-800-555-0202", "450 Commerce Dr, Detroit, MI 48201"));
        dao.save(new Supplier("SafeGuard Products", "David Osei", "david@safeguardpro.com", "+1-800-555-0303", "78 Safety Blvd, Houston, TX 77001"));
        dao.save(new Supplier("Office Depot Business", "Sarah Chen", "sarah.chen@officedepot.biz", "+1-800-555-0404", "900 Business Park, Chicago, IL 60601"));
        dao.save(new Supplier("Global Parts Co.", "James Wilton", "jwilton@globalparts.com", "+1-800-555-0505", "300 Harbor Rd, Seattle, WA 98101"));
        dao.save(new Supplier("FastTrack Logistics", "Maria Santos", "m.santos@fasttrack.com", "+1-800-555-0606", "55 Freight Ave, Memphis, TN 38101"));
        return dao.findAll();
    }

    private static List<Product> seedProducts(List<Category> categories, List<Supplier> suppliers) {
        ProductDAO dao = new ProductDAO();

        Category electronics = cat(categories, "Electronics");
        Category powerTools = cat(categories, "Power Tools");
        Category handTools = cat(categories, "Hand Tools");
        Category safety = cat(categories, "Safety Equipment");
        Category office = cat(categories, "Office Supplies");

        Supplier techSource = sup(suppliers, "TechSource Inc.");
        Supplier industrialPro = sup(suppliers, "Industrial Pro Supply");
        Supplier safeGuard = sup(suppliers, "SafeGuard Products");
        Supplier officeDepot = sup(suppliers, "Office Depot Business");
        Supplier globalParts = sup(suppliers, "Global Parts Co.");

        // Electronics (6 products — 2 below reorder)
        dao.save(product("EL-001", "Barcode Scanner (USB)", electronics, techSource, "Handheld 1D/2D USB barcode scanner", "49.95", 24, 10, "pcs"));
        dao.save(product("EL-002", "Label Printer — Thermal", electronics, techSource, "4-inch direct thermal label printer", "189.00", 12, 5, "pcs"));
        dao.save(product("EL-003", "Handheld Terminal RF", electronics, techSource, "Wireless RF inventory terminal, 802.11b/g", "349.00", 7, 5, "pcs"));
        dao.save(product("EL-004", "RFID Reader — Industrial", electronics, techSource, "UHF RFID fixed reader, IP67 rated", "520.00", 2, 5, "pcs"));   // LOW
        dao.save(product("EL-005", "Network Switch 24-Port", electronics, techSource, "Managed 24-port Gigabit switch", "285.00", 6, 4, "pcs"));
        dao.save(product("EL-006", "UPS Battery Unit 1500VA", electronics, globalParts, "Online double-conversion UPS", "265.00", 1, 3, "pcs"));        // LOW

        // Power Tools (6 products — 1 below reorder)
        dao.save(product("PT-001", "Cordless Drill 18V", powerTools, industrialPro, "18V Li-ion drill/driver with 2 batteries", "119.00", 2, 8, "pcs")); // LOW
        dao.save(product("PT-002", "Impact Driver 18V", powerTools, industrialPro, "18V brushless impact driver", "99.00", 15, 6, "pcs"));
        dao.save(product("PT-003", "Circular Saw 7.25\"", powerTools, industrialPro, "15A corded circular saw", "79.00", 9, 5, "pcs"));
        dao.save(product("PT-004", "Angle Grinder 4.5\"", powerTools, industrialPro, "11A 4.5-inch angle grinder", "65.00", 11, 5, "pcs"));
        dao.save(product("PT-005", "Heat Gun Variable Temp", powerTools, industrialPro, "1500W dual-temperature heat gun", "44.00", 8, 4, "pcs"));
        dao.save(product("PT-006", "Jigsaw Orbital Action", powerTools, industrialPro, "6A jigsaw with orbital action", "88.00", 7, 4, "pcs"));

        // Hand Tools (6 products)
        dao.save(product("HT-001", "Torque Wrench Set 3/8\"", handTools, globalParts, "3/8\" drive torque wrench 10–80 Nm", "55.00", 14, 6, "pcs"));
        dao.save(product("HT-002", "Hex Key Set Metric", handTools, globalParts, "9-piece metric hex key set", "18.50", 32, 10, "sets"));
        dao.save(product("HT-003", "Digital Caliper 150mm", handTools, globalParts, "IP54 digital vernier caliper", "28.00", 19, 8, "pcs"));
        dao.save(product("HT-004", "Spirit Level Set", handTools, industrialPro, "3-piece aluminum spirit level set", "34.00", 11, 5, "sets"));
        dao.save(product("HT-005", "Tape Measure 8m", handTools, industrialPro, "8m/26ft magnetic hook tape", "14.00", 40, 15, "pcs"));
        dao.save(product("HT-006", "Utility Knife Heavy Duty", handTools, globalParts, "Heavy-duty retractable utility knife", "9.50", 55, 20, "pcs"));

        // Safety Equipment (6 products — 2 below reorder)
        dao.save(product("SE-001", "Hard Hat Class E", safety, safeGuard, "Vented hard hat, ANSI Class E", "16.50", 45, 20, "pcs"));
        dao.save(product("SE-002", "High-Vis Safety Vest", safety, safeGuard, "Class 2 hi-vis surveyor vest, ANSI", "12.00", 8, 20, "pcs"));            // LOW
        dao.save(product("SE-003", "Safety Glasses Clear", safety, safeGuard, "Anti-fog polycarbonate safety glasses", "5.50", 80, 30, "pcs"));
        dao.save(product("SE-004", "Steel-Toe Boot Size 10", safety, safeGuard, "6\" waterproof composite toe work boot", "89.00", 3, 10, "pcs"));       // LOW
        dao.save(product("SE-005", "Leather Work Gloves L", safety, safeGuard, "Premium leather palm work gloves", "11.00", 60, 25, "pairs"));
        dao.save(product("SE-006", "Ear Protection 28dB NRR", safety, safeGuard, "Foam ear plugs, NRR 28, box of 200", "24.00", 22, 10, "boxes"));

        // Office Supplies (6 products — 1 below reorder)
        dao.save(product("OF-001", "Copy Paper A4 80gsm", office, officeDepot, "A4 80gsm copy paper, 500 sheets/ream", "6.50", 120, 50, "reams"));
        dao.save(product("OF-002", "Ink Cartridge — Black", office, officeDepot, "Standard yield black ink cartridge", "22.00", 5, 15, "pcs"));           // LOW
        dao.save(product("OF-003", "Ink Cartridge — Color", office, officeDepot, "Standard yield color ink cartridge", "28.50", 18, 10, "pcs"));
        dao.save(product("OF-004", "Binder Clip Set Large", office, officeDepot, "Large binder clips, box of 12", "3.20", 75, 20, "boxes"));
        dao.save(product("OF-005", "Permanent Markers Black", office, officeDepot, "Fine tip permanent markers, box of 12", "8.80", 38, 10, "boxes"));
        dao.save(product("OF-006", "Sticky Notes 76x76mm", office, officeDepot, "Self-stick notes, 4 pads of 100", "4.50", 95, 30, "packs"));

        return dao.findAll();
    }

    private static List<PurchaseOrder> seedPurchaseOrders(List<Supplier> suppliers, List<Product> products) {
        PurchaseOrderDAO dao = new PurchaseOrderDAO();

        // Delivered orders
        dao.save(order(sup(suppliers, "TechSource Inc."), LocalDate.now().minusDays(45), LocalDate.now().minusDays(30), OrderStatus.DELIVERED, "Regular quarterly restock",
            products, "EL-001", 20, new BigDecimal("46.00"), "EL-002", 5, new BigDecimal("179.00")));

        dao.save(order(sup(suppliers, "Industrial Pro Supply"), LocalDate.now().minusDays(38), LocalDate.now().minusDays(22), OrderStatus.DELIVERED, "Power tools restocking",
            products, "PT-001", 10, new BigDecimal("109.00"), "PT-002", 10, new BigDecimal("89.00")));

        dao.save(order(sup(suppliers, "SafeGuard Products"), LocalDate.now().minusDays(30), LocalDate.now().minusDays(18), OrderStatus.DELIVERED, "Safety gear top-up",
            products, "SE-001", 30, new BigDecimal("14.50"), "SE-003", 100, new BigDecimal("4.80")));

        dao.save(order(sup(suppliers, "Office Depot Business"), LocalDate.now().minusDays(20), LocalDate.now().minusDays(10), OrderStatus.DELIVERED, "Monthly office supplies",
            products, "OF-001", 200, new BigDecimal("6.20"), "OF-005", 20, new BigDecimal("8.00")));

        // Ordered
        dao.save(order(sup(suppliers, "TechSource Inc."), LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), OrderStatus.ORDERED, "Urgent: RFID and UPS restock",
            products, "EL-004", 10, new BigDecimal("495.00"), "EL-006", 5, new BigDecimal("249.00")));

        dao.save(order(sup(suppliers, "Industrial Pro Supply"), LocalDate.now().minusDays(7), LocalDate.now().plusDays(8), OrderStatus.ORDERED, "Cordless drill restock",
            products, "PT-001", 15, new BigDecimal("105.00")));

        // Pending
        dao.save(order(sup(suppliers, "SafeGuard Products"), LocalDate.now().minusDays(3), LocalDate.now().plusDays(14), OrderStatus.PENDING, "Emergency PPE order — low stock alert",
            products, "SE-002", 50, new BigDecimal("11.00"), "SE-004", 20, new BigDecimal("82.00")));

        dao.save(order(sup(suppliers, "Office Depot Business"), LocalDate.now().minusDays(2), LocalDate.now().plusDays(7), OrderStatus.PENDING, "Printer consumables",
            products, "OF-002", 30, new BigDecimal("20.50"), "OF-003", 20, new BigDecimal("26.00")));

        dao.save(order(sup(suppliers, "Global Parts Co."), LocalDate.now().minusDays(1), LocalDate.now().plusDays(10), OrderStatus.PENDING, "Hand tools replenishment",
            products, "HT-001", 10, new BigDecimal("50.00"), "HT-002", 15, new BigDecimal("16.50")));

        // Cancelled
        dao.save(order(sup(suppliers, "FastTrack Logistics"), LocalDate.now().minusDays(15), LocalDate.now().minusDays(5), OrderStatus.CANCELLED, "Duplicate order — cancelled",
            products, "EL-005", 5, new BigDecimal("270.00")));

        return dao.findAll();
    }

    private static void seedStockMovements(List<Product> products) {
        StockMovementDAO dao = new StockMovementDAO();

        // Generate realistic movement history
        String[][] movements = {
            {"EL-001", "IN", "20", "Delivery PO-001"},
            {"EL-002", "IN", "5", "Delivery PO-001"},
            {"PT-001", "IN", "10", "Delivery PO-002"},
            {"PT-002", "IN", "10", "Delivery PO-002"},
            {"SE-001", "IN", "30", "Delivery PO-003"},
            {"SE-003", "IN", "100", "Delivery PO-003"},
            {"OF-001", "IN", "200", "Delivery PO-004"},
            {"EL-001", "OUT", "8", "Issued to warehouse floor"},
            {"EL-003", "OUT", "3", "Issued to dispatch team"},
            {"PT-001", "OUT", "13", "Issued to maintenance"},
            {"PT-003", "OUT", "2", "Issued to dock"},
            {"SE-002", "OUT", "22", "Issued to floor staff"},
            {"SE-004", "OUT", "7", "Issued to loading bay"},
            {"OF-001", "OUT", "80", "Office replenishment"},
            {"OF-002", "OUT", "10", "Printer maintenance"},
            {"HT-005", "OUT", "10", "Issued to floor staff"},
            {"EL-006", "OUT", "2", "Replaced failed units"},
            {"SE-001", "OUT", "5", "Issued — new hires"},
            {"PT-002", "OUT", "5", "Issued to maintenance crew"},
            {"SE-003", "OUT", "20", "Issued — monthly safety kit"},
            {"EL-004", "ADJUSTMENT", "2", "Inventory count correction"},
            {"HT-002", "IN", "15", "Emergency purchase"},
            {"OF-003", "OUT", "5", "Color printing project"},
            {"PT-004", "OUT", "3", "Issued to cutting station"},
            {"HT-006", "OUT", "5", "Issued to receiving team"},
        };

        int daysBack = 90;
        for (int i = 0; i < movements.length; i++) {
            String[] m = movements[i];
            Product prod = prod(products, m[0]);
            if (prod == null) continue;

            MovementType type = MovementType.valueOf(m[1]);
            int qty = Integer.parseInt(m[2]);

            StockMovement movement = new StockMovement(prod, type, qty, m[3], "admin");
            movement.setMovementDate(LocalDateTime.now().minusDays(daysBack - i * 3L));
            dao.save(movement);
        }
    }

    // --- helpers ---

    private static Product product(String sku, String name, Category cat, Supplier sup,
            String desc, String price, int qty, int reorder, String unit) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setCategory(cat);
        p.setSupplier(sup);
        p.setDescription(desc);
        p.setUnitPrice(new BigDecimal(price));
        p.setQuantity(qty);
        p.setReorderLevel(reorder);
        p.setUnit(unit);
        return p;
    }

    private static PurchaseOrder order(Supplier supplier, LocalDate orderDate, LocalDate expected,
            OrderStatus status, String notes, List<Product> products, Object... itemArgs) {
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setOrderDate(orderDate);
        order.setExpectedDate(expected);
        order.setStatus(status);
        order.setNotes(notes);

        for (int i = 0; i < itemArgs.length; i += 3) {
            String sku = (String) itemArgs[i];
            int qty = (Integer) itemArgs[i + 1];
            BigDecimal price = (BigDecimal) itemArgs[i + 2];
            Product p = prod(products, sku);
            if (p != null) {
                order.addItem(new PurchaseOrderItem(p, qty, price));
            }
        }
        return order;
    }

    private static Category cat(List<Category> list, String name) {
        return list.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    private static Supplier sup(List<Supplier> list, String name) {
        return list.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }

    private static Product prod(List<Product> list, String sku) {
        return list.stream().filter(p -> p.getSku().equals(sku)).findFirst().orElse(null);
    }
}
