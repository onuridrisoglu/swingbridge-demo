package com.example.warehouse.ui.orders;

import com.example.warehouse.model.PurchaseOrder;
import com.example.warehouse.model.PurchaseOrderItem;
import com.example.warehouse.service.PurchaseOrderService;
import com.example.warehouse.ui.util.Status;
import com.example.warehouse.ui.util.TableExporter;
import com.example.warehouse.ui.util.UIConstants;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Purchase Orders screen — split pane with the order list above and the selected
 * order's line items below, status colour-coding and CSV export. Ported from the
 * Swing {@code PurchaseOrdersPanel}.
 */
@TopComponent.Description(
        preferredID = "OrdersTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = true, position = 400)
@ActionID(category = "Window", id = "com.example.warehouse.ui.orders.OrdersTopComponent")
@ActionReference(path = "Menu/Window", position = 3322)
@TopComponent.OpenActionRegistration(displayName = "Orders", preferredID = "OrdersTopComponent")
public final class OrdersTopComponent extends TopComponent {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PurchaseOrderService service = new PurchaseOrderService();

    private JTable orderTable;
    private DefaultTableModel orderTableModel;
    private JTable itemTable;
    private DefaultTableModel itemTableModel;

    private List<PurchaseOrder> orders = new ArrayList<>();

    private JButton editBtn;
    private JButton deleteBtn;

    public OrdersTopComponent() {
        setName("Orders");
        setToolTipText("Purchase orders and line items");

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildOrderTable(), buildItemPanel());
        split.setDividerLocation(280);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.TOOLBAR_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);

        JButton addBtn = UIConstants.toolbarButton("+ New Order");
        editBtn = UIConstants.toolbarButton("Edit / View");
        deleteBtn = UIConstants.toolbarButton("Delete");
        JButton refreshBtn = UIConstants.toolbarButton("↺ Refresh");

        addBtn.setBackground(new Color(230, 245, 230));
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> refresh());

        left.add(addBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(editBtn);
        left.add(deleteBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(refreshBtn);

        JButton exportBtn = UIConstants.toolbarButton("⬇ Export CSV");
        exportBtn.addActionListener(e -> TableExporter.exportToCsv(orderTable, "orders.csv", this));
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(exportBtn);

        bar.add(left, BorderLayout.WEST);
        return bar;
    }

    private JScrollPane buildOrderTable() {
        String[] cols = {"Order #", "Supplier", "Order Date", "Expected", "Status", "Items", "Total Amount"};
        orderTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        orderTable = new JTable(orderTableModel);
        UIConstants.styleTable(orderTable);
        orderTable.setDefaultRenderer(Object.class, new OrderStatusRenderer());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(orderTableModel);
        orderTable.setRowSorter(sorter);

        orderTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        orderTable.getColumnModel().getColumn(6).setPreferredWidth(110);

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = orderTable.getSelectedRowCount() > 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
            showOrderItems();
        });

        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });

        JPopupMenu popup = new JPopupMenu();
        JMenuItem viewItem = new JMenuItem("View / Edit Order");
        JMenuItem delItem = new JMenuItem("Delete Order");
        viewItem.addActionListener(e -> openEditDialog());
        delItem.addActionListener(e -> deleteSelected());
        popup.add(viewItem);
        popup.add(delItem);
        orderTable.setComponentPopupMenu(popup);

        JScrollPane sp = new JScrollPane(orderTable);
        sp.setBorder(UIConstants.sectionBorder("Purchase Orders"));
        return sp;
    }

    private JPanel buildItemPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(UIConstants.sectionBorder("Order Line Items"));
        panel.setBackground(Color.WHITE);

        String[] cols = {"Product SKU", "Product Name", "Quantity", "Unit Price", "Line Total"};
        itemTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        itemTable = new JTable(itemTableModel);
        UIConstants.styleTable(itemTable);
        itemTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.ROW_ALT);
                }
                if (col >= 2) {
                    setHorizontalAlignment(CENTER);
                } else {
                    setHorizontalAlignment(LEFT);
                }
                return c;
            }
        });

        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        return panel;
    }

    private void showOrderItems() {
        itemTableModel.setRowCount(0);
        PurchaseOrder order = getSelectedOrder();
        if (order == null) {
            return;
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        for (PurchaseOrderItem item : order.getItems()) {
            itemTableModel.addRow(new Object[]{
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                currency.format(item.getUnitPrice()),
                currency.format(item.getLineTotal())
            });
        }
    }

    public void refresh() {
        orders = service.findAll();
        orderTableModel.setRowCount(0);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        for (PurchaseOrder o : orders) {
            orderTableModel.addRow(new Object[]{
                String.format("PO-%04d", o.getId()),
                o.getSupplier().getName(),
                o.getOrderDate().format(DATE_FMT),
                o.getExpectedDate() != null ? o.getExpectedDate().format(DATE_FMT) : "—",
                o.getStatus().toString(),
                o.getItems().size(),
                currency.format(o.getTotalAmount())
            });
        }
        itemTableModel.setRowCount(0);
        Status.rowCount(orders.size(), "purchase order");
    }

    private PurchaseOrder getSelectedOrder() {
        int viewRow = orderTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = orderTable.convertRowIndexToModel(viewRow);
        return orders.get(modelRow);
    }

    private Frame mainFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void openAddDialog() {
        PurchaseOrderDialog dlg = new PurchaseOrderDialog(mainFrame(), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
        }
    }

    private void openEditDialog() {
        PurchaseOrder o = getSelectedOrder();
        if (o == null) {
            return;
        }
        PurchaseOrderDialog dlg = new PurchaseOrderDialog(mainFrame(), o);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refresh();
        }
    }

    private void deleteSelected() {
        PurchaseOrder o = getSelectedOrder();
        if (o == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete purchase order PO-" + String.format("%04d", o.getId()) + "?\n\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(o.getId());
            refresh();
            Status.message("Purchase order deleted.");
        }
    }

    private class OrderStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel) {
                int modelRow = t.convertRowIndexToModel(row);
                if (modelRow < orders.size()) {
                    PurchaseOrder.OrderStatus status = orders.get(modelRow).getStatus();
                    switch (status) {
                        case CANCELLED -> c.setBackground(new Color(250, 235, 235));
                        case DELIVERED -> c.setBackground(new Color(235, 250, 235));
                        case ORDERED -> c.setBackground(new Color(235, 242, 252));
                        default -> c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.ROW_ALT);
                    }
                }
                if (col == 4) {
                    setFont(UIConstants.FONT_BOLD);
                    String status = v != null ? v.toString() : "";
                    switch (status) {
                        case "Cancelled" -> setForeground(UIConstants.DANGER);
                        case "Delivered" -> setForeground(UIConstants.SUCCESS);
                        case "Ordered" -> setForeground(UIConstants.ACCENT);
                        default -> setForeground(UIConstants.WARNING);
                    }
                } else {
                    setFont(UIConstants.FONT_REGULAR);
                    setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }
}
