package com.example.suppliers;

import com.example.warehouse.model.Supplier;
import com.example.warehouse.service.SupplierService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Supplier CRUD for the React view, wrapping the legacy Swing-side
 * {@link SupplierService}.
 *
 * <p>The React toolbar and dialogs call these methods directly through the
 * generated TypeScript client. Refreshing the Swing table afterwards is *not*
 * done here — that goes through the bridge, from {@code SuppliersView}, because
 * only the Flow side holds the interop handle. So a save is two steps from the
 * browser's point of view: write through this endpoint, then ask the bridge to
 * reload. Keeping them separate is deliberate; folding the bridge call in here
 * would couple the data layer to whichever UI happens to be showing.
 */
@Service
@BrowserCallable
@AnonymousAllowed
public class SupplierEndpoint {

    private final SupplierService service = new SupplierService();

    public @NonNull List<@NonNull SupplierRecord> findAll() {
        return service.findAll().stream().map(SupplierRecord::of).toList();
    }

    public @Nullable SupplierRecord findById(long id) {
        Supplier supplier = service.findById(id);
        return supplier == null ? null : SupplierRecord.of(supplier);
    }

    /** Inserts when {@code supplier.id()} is null, updates otherwise. */
    public void save(@NonNull SupplierRecord supplier) {
        if (supplier.id() == null) {
            Supplier created = new Supplier();
            supplier.applyTo(created);
            service.save(created);
            return;
        }
        Supplier existing = service.findById(supplier.id());
        if (existing == null) {
            throw new IllegalArgumentException("No supplier with id " + supplier.id());
        }
        supplier.applyTo(existing);
        service.update(existing);
    }

    /**
     * @return an error message when the supplier could not be deleted (it is
     *         still referenced by products or orders), or {@code null} on
     *         success. Returning the message beats throwing: Hilla reports a
     *         thrown exception to the client as a generic error with no detail.
     */
    public @Nullable String delete(long id) {
        try {
            service.delete(id);
            return null;
        } catch (Exception e) {
            return "Cannot delete: supplier may be referenced by products or orders.";
        }
    }

    /**
     * The CSV as text. The Flow view streamed this through a {@code
     * DownloadHandler} behind an {@code Anchor}; React builds a Blob from this
     * string and clicks an object URL instead.
     */
    public @NonNull String exportCsv() {
        return new String(service.exportToCsv(), StandardCharsets.UTF_8);
    }
}
