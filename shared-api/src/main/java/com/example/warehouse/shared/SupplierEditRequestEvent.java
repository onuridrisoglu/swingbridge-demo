package com.example.warehouse.shared;

/**
 * Published by the Swing suppliers table when the user double-clicks a row,
 * signalling that the Vaadin edit dialog should open immediately.
 *
 * Follows the same String-only field convention as {@link SupplierSelectionEvent}
 * so it crosses the SwingBridge classloader boundary automatically.
 */
public class SupplierEditRequestEvent {

    public final String id;
    public final String name;
    public final String contactName;
    public final String email;
    public final String phone;
    public final String address;

    public SupplierEditRequestEvent(String id, String name, String contactName,
                                    String email, String phone, String address) {
        this.id          = id;
        this.name        = name;
        this.contactName = contactName;
        this.email       = email;
        this.phone       = phone;
        this.address     = address;
    }
}
