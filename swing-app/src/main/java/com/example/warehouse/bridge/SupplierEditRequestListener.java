package com.example.warehouse.bridge;

@FunctionalInterface
public interface SupplierEditRequestListener {
    void onEditRequested(SupplierEditRequestEvent event);
}
