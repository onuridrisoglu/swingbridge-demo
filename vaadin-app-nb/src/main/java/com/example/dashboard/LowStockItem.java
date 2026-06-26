package com.example.dashboard;

/** A product that has fallen to or below its reorder level. */
public record LowStockItem(
        String sku,
        String name,
        String category,
        String supplier,
        int onHand,
        int reorderLevel,
        int shortage,
        boolean outOfStock) {
}
