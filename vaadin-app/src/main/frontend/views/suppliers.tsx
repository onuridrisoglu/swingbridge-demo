import { useCallback, useEffect, useRef, useState } from 'react';
import { Button } from '@vaadin/react-components/Button.js';
import { ConfirmDialog } from '@vaadin/react-components/ConfirmDialog.js';
import { Dialog } from '@vaadin/react-components/Dialog.js';
import { FormLayout } from '@vaadin/react-components/FormLayout.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { TextArea } from '@vaadin/react-components/TextArea.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import { reactElement } from 'Frontend/generated/flow/Flow';
import { SupplierEndpoint } from 'Frontend/generated/endpoints.js';
import type SupplierRecord from 'Frontend/generated/com/example/suppliers/SupplierRecord.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';

/**
 * The interop demo: a React view driving a Swing table.
 *
 * <p>This is an ordinary file-router React view — toolbar, dialogs and CRUD are
 * plain React, and supplier data comes from `SupplierEndpoint` over Hilla like
 * any other view. The only Flow on the screen is `<supplier-bridge>`, an island
 * roughly the size of the table, created by `reactElement` from a
 * `WebComponentExporter` on the Java side.
 *
 * <p>The bridge cannot be called directly from here — the interop handle is
 * server-side — so that channel is deliberately narrow: two properties out
 * (`filter`, `refreshToken`) and two DOM events in (`supplier-selected`,
 * `supplier-edit-requested`). Data goes over Hilla; only bridge commands go over
 * the element. A save therefore does both: write through the endpoint, then bump
 * `refreshToken` so the Swing table reloads.
 */
export const config: ViewConfig = { title: 'Suppliers' };

const EMPTY: SupplierRecord = {
  id: undefined,
  name: '',
  contactName: '',
  email: '',
  phone: '',
  address: '',
};

export default function SuppliersView() {
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [editing, setEditing] = useState<SupplierRecord | null>(null);
  const [confirming, setConfirming] = useState<SupplierRecord | null>(null);
  const [search, setSearch] = useState('');
  const [refreshToken, setRefreshToken] = useState(0);

  const refreshBridge = useCallback(() => setRefreshToken((t) => t + 1), []);

  const openForm = useCallback(async (id: number | null) => {
    setEditing(id === null ? { ...EMPTY } : ((await SupplierEndpoint.findById(id)) ?? { ...EMPTY }));
  }, []);

  const save = async (supplier: SupplierRecord) => {
    if (!supplier.name.trim()) {
      notify('Supplier name is required.', true);
      return;
    }
    await SupplierEndpoint.save(supplier);
    setEditing(null);
    notify(supplier.id === undefined ? 'Supplier added.' : 'Supplier updated.', false);
    refreshBridge();
  };

  const startDelete = async () => {
    if (selectedId === null) {
      return;
    }
    const supplier = await SupplierEndpoint.findById(selectedId);
    if (supplier) {
      setConfirming(supplier);
    }
  };

  const confirmDelete = async () => {
    const target = confirming;
    setConfirming(null);
    if (!target?.id) {
      return;
    }
    const error = await SupplierEndpoint.delete(target.id);
    if (error) {
      notify(error, true);
      return;
    }
    notify('Supplier deleted.', false);
    refreshBridge();
  };

  const exportCsv = async () => {
    downloadCsv(await SupplierEndpoint.exportCsv(), 'suppliers.csv');
  };

  return (
    <div className="suppliers-view">
      <div className="suppliers-toolbar">
        <div className="suppliers-toolbar-actions">
          <Button theme="primary small" className="toolbar-btn-dark" onClick={() => openForm(null)}>
            + Add Supplier
          </Button>
          <Button
            theme="primary small"
            className="toolbar-btn-dark"
            disabled={selectedId === null}
            onClick={() => openForm(selectedId)}
          >
            Edit
          </Button>
          <Button
            theme="error primary small"
            className="toolbar-btn-delete"
            disabled={selectedId === null}
            onClick={startDelete}
          >
            Delete
          </Button>
          <Button theme="tertiary small" onClick={refreshBridge}>
            ↺ Refresh
          </Button>
          <Button theme="tertiary small" onClick={exportCsv}>
            ⬇ Export CSV
          </Button>
        </div>

        <TextField
          theme="small"
          placeholder="Search..."
          clearButtonVisible
          value={search}
          onValueChanged={(e) => setSearch(e.detail.value)}
        />
      </div>

      <SupplierBridge
        filter={search}
        refreshToken={refreshToken}
        onSelect={setSelectedId}
        onEditRequest={openForm}
      />

      {editing && (
        // Keyed so switching straight from Edit to Add remounts the form.
        // Without it React reuses the instance and useState keeps the previous
        // supplier's values while the title updates — stale fields, fresh title.
        <SupplierDialog
          key={editing.id ?? 'new'}
          supplier={editing}
          onSave={save}
          onCancel={() => setEditing(null)}
        />
      )}

      {/* The message goes in the `message` property, not as children: the web
          component creates its own Cancel/Confirm buttons as light-DOM children,
          and giving React children to reconcile makes it remove them — leaving a
          dialog with text and no buttons. */}
      <ConfirmDialog
        opened={confirming !== null}
        header="Delete Supplier"
        message={confirming ? `Delete '${confirming.name}'? This cannot be undone.` : ''}
        cancelButtonVisible
        confirmText="Delete"
        confirmTheme="error primary"
        onConfirm={confirmDelete}
        onCancel={() => setConfirming(null)}
      />
    </div>
  );
}

// ----------------------------------------------------------------- the island

/**
 * Wraps the exported Flow web component. The events it fires are bubbling and
 * composed, so they are caught on this wrapper rather than by holding a ref to
 * the custom element — `reactElement` builds the element itself and gives no
 * handle to it.
 */
function SupplierBridge({
  filter,
  refreshToken,
  onSelect,
  onEditRequest,
}: {
  readonly filter: string;
  readonly refreshToken: number;
  readonly onSelect: (id: number | null) => void;
  readonly onEditRequest: (id: number) => void;
}) {
  // Kept in refs so the listeners can be attached once and still see fresh
  // callbacks; re-attaching on every render would drop events mid-flight.
  const onSelectRef = useRef(onSelect);
  const onEditRequestRef = useRef(onEditRequest);
  onSelectRef.current = onSelect;
  onEditRequestRef.current = onEditRequest;

  const [host, setHost] = useState<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!host) {
      return;
    }
    const idOf = (e: Event) => (e as CustomEvent<{ id: number | null }>).detail?.id ?? null;
    const selected = (e: Event) => onSelectRef.current(idOf(e));
    const editRequested = (e: Event) => {
      const id = idOf(e);
      if (id !== null) {
        onEditRequestRef.current(id);
      }
    };
    host.addEventListener('supplier-selected', selected);
    host.addEventListener('supplier-edit-requested', editRequested);
    return () => {
      host.removeEventListener('supplier-selected', selected);
      host.removeEventListener('supplier-edit-requested', editRequested);
    };
  }, [host]);

  return (
    <div className="suppliers-bridge" ref={setHost}>
      {reactElement('supplier-bridge', { filter, refreshToken: String(refreshToken) })}
    </div>
  );
}

// ------------------------------------------------------------------ edit form

function SupplierDialog({
  supplier,
  onSave,
  onCancel,
}: {
  readonly supplier: SupplierRecord;
  readonly onSave: (supplier: SupplierRecord) => void;
  readonly onCancel: () => void;
}) {
  const [draft, setDraft] = useState(supplier);
  const field = (key: keyof SupplierRecord) => (e: { detail: { value: string } }) =>
    setDraft((d) => ({ ...d, [key]: e.detail.value }));

  return (
    <Dialog
      opened
      headerTitle={supplier.id === undefined ? 'Add Supplier' : 'Edit Supplier'}
      width="420px"
      onOpenedChanged={(e) => !e.detail.value && onCancel()}
      footerRenderer={() => (
        <>
          <Button onClick={onCancel}>Cancel</Button>
          <Button theme="primary" onClick={() => onSave(draft)}>
            Save
          </Button>
        </>
      )}
    >
      <FormLayout responsiveSteps={[{ minWidth: '0', columns: 1 }]}>
        <TextField label="Supplier Name" required value={draft.name} onValueChanged={field('name')} />
        <TextField label="Contact Person" value={draft.contactName} onValueChanged={field('contactName')} />
        <TextField label="Email" value={draft.email} onValueChanged={field('email')} />
        <TextField label="Phone" value={draft.phone} onValueChanged={field('phone')} />
        <TextArea label="Address" style={{ maxHeight: '80px' }} value={draft.address} onValueChanged={field('address')} />
      </FormLayout>
    </Dialog>
  );
}

// ---------------------------------------------------------------------- utils

function notify(message: string, error: boolean) {
  Notification.show(message, {
    duration: 3000,
    position: 'bottom-start',
    theme: error ? 'error' : 'success',
  });
}

/** The Flow view streamed this through an Anchor; in React it's a Blob URL. */
function downloadCsv(csv: string, filename: string) {
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}
