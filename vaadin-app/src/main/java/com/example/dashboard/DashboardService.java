package com.example.dashboard;

import com.example.warehouse.model.Product;
import com.example.warehouse.service.ProductService;
import com.example.warehouse.service.PurchaseOrderService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read model for the modern dashboard. This is the "modern" boundary the new
 * dashboard talks to: it wraps the legacy Swing-app services
 * ({@link ProductService}, {@link PurchaseOrderService}) and exposes typed
 * records instead of leaking Hibernate entities or raw {@code Object[]} rows
 * into the Vaadin view. Both the Swing and Vaadin dashboards read the same H2
 * data, so the numbers stay identical — but the Vaadin side sees a clean,
 * testable API.
 *
 * <p>{@code @BrowserCallable} additionally publishes every public method here to
 * the React dashboard: Hilla generates a typed TypeScript client from this class
 * at build time, so {@code dashboard-modern.tsx} calls {@code kpiSummary()} and
 * friends directly, with the records above generated as TS interfaces. The same
 * Java read model therefore serves the browser without a hand-written REST
 * controller in between.
 *
 * <p>{@code @AnonymousAllowed} is required because browser-callable services
 * reject every call by default. This app authenticates through the Swing-side
 * {@code User} entity and a Vaadin session attribute rather than Spring
 * Security, so there are no roles for Hilla to check; view-level protection
 * stays in the React shell, {@code src/main/frontend/views/@layout.tsx}.
 */
@Service
@BrowserCallable
@AnonymousAllowed
public class DashboardService {

    private final ProductService productService;
    private final PurchaseOrderService orderService;

    /** Spring entry point — the Swing services are plain objects, not beans. */
    public DashboardService() {
        this(new ProductService(), new PurchaseOrderService());
    }

    /** Visible for testing: inject doubles for the legacy services. */
    DashboardService(ProductService productService, PurchaseOrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    public @NonNull KpiSummary kpiSummary() {
        return new KpiSummary(
                productService.count(),
                productService.totalInventoryValue(),
                orderService.countPending(),
                productService.countLowStock());
    }

    public @NonNull List<@NonNull CategoryStock> stockByCategory() {
        return productService.findStockByCategory().stream()
                .map(row -> new CategoryStock(
                        String.valueOf(row[0]),
                        ((Number) row[1]).intValue()))
                .toList();
    }

    public @NonNull List<@NonNull OrderStatusCount> ordersByStatus() {
        return orderService.countByAllStatuses().stream()
                .map(row -> new OrderStatusCount(
                        String.valueOf(row[0]),
                        ((Number) row[1]).intValue()))
                .toList();
    }

    public @NonNull List<@NonNull LowStockItem> lowStockItems() {
        return productService.findLowStock().stream()
                .map(DashboardService::toLowStockItem)
                .toList();
    }

    private static LowStockItem toLowStockItem(Product p) {
        int shortage = p.getReorderLevel() - p.getQuantity();
        return new LowStockItem(
                p.getSku(),
                p.getName(),
                p.getCategory().getName(),
                p.getSupplier() != null ? p.getSupplier().getName() : "—",
                p.getQuantity(),
                p.getReorderLevel(),
                shortage,
                p.getQuantity() == 0);
    }
}
