package com.example.warehouse.bridge;

@FunctionalInterface
public interface SupplierSelectionListener {
    void onSelectionChanged(SupplierSelectedEvent event);
}
