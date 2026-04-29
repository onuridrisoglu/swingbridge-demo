package com.example.warehouse.shared;

/**
 * Published by the Swing suppliers table when the user selects a row.
 *
 * All fields are {@link String} so the event can cross the SwingBridge
 * classloader boundary automatically (Strings are bootstrap-CL, identical on
 * both sides). A single all-args constructor in field-declaration order is
 * required by {@code SwingEventBridge.convertEvent()}.
 */
public class SupplierSelectionEvent {

    public final String id;
    public final String name;
    public final String contactName;
    public final String email;
    public final String phone;
    public final String address;

    public SupplierSelectionEvent(String id, String name, String contactName,
                                  String email, String phone, String address) {
        this.id          = id;
        this.name        = name;
        this.contactName = contactName;
        this.email       = email;
        this.phone       = phone;
        this.address     = address;
    }
}
