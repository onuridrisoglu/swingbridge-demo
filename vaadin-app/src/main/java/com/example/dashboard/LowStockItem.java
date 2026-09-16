package com.example.dashboard;

import org.jspecify.annotations.NonNull;

/** A product that has fallen to or below its reorder level. */
public record LowStockItem(
        @NonNull String sku,
        @NonNull String name,
        @NonNull String category,
        @NonNull String supplier,
        int onHand,
        int reorderLevel,
        int shortage,
        boolean outOfStock) {
}
