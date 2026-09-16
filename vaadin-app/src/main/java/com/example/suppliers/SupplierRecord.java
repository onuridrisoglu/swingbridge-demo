package com.example.suppliers;

import com.example.warehouse.model.Supplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A supplier as the React view sees it. The Hibernate {@link Supplier} entity is
 * deliberately not exposed: {@code @BrowserCallable} publishes every public
 * method, and an entity on that boundary drags its persistence lifecycle — and
 * whatever fields it happens to gain later — into the browser.
 *
 * @param id {@code null} for a supplier that has not been saved yet
 */
public record SupplierRecord(
        @Nullable Long id,
        @NonNull String name,
        @NonNull String contactName,
        @NonNull String email,
        @NonNull String phone,
        @NonNull String address) {

    static SupplierRecord of(Supplier s) {
        return new SupplierRecord(
                s.getId(),
                orEmpty(s.getName()),
                orEmpty(s.getContactName()),
                orEmpty(s.getEmail()),
                orEmpty(s.getPhone()),
                orEmpty(s.getAddress()));
    }

    /** Copies this record's editable fields onto an entity. */
    void applyTo(Supplier target) {
        target.setName(name);
        target.setContactName(contactName);
        target.setEmail(email);
        target.setPhone(phone);
        target.setAddress(address);
    }

    private static String orEmpty(String value) {
        return value != null ? value : "";
    }
}
