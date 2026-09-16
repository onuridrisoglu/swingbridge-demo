package com.example.dashboard;

import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

/**
 * Top-line metrics for the dashboard's KPI tiles. A pure data carrier with no
 * dependency on the Swing domain model — the {@link DashboardService} maps the
 * legacy entities into this record.
 */
public record KpiSummary(
        long totalProducts,
        @NonNull BigDecimal inventoryValue,
        long pendingOrders,
        long lowStockCount) {
}
