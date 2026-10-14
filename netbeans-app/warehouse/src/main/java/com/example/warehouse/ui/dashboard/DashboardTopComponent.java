package com.example.warehouse.ui.dashboard;

import com.example.warehouse.model.Product;
import com.example.warehouse.service.ProductService;
import com.example.warehouse.service.PurchaseOrderService;
import com.example.warehouse.ui.util.Status;
import com.example.warehouse.ui.util.UIConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard screen — KPI cards, JFreeChart bar + pie charts and a low-stock alert
 * table. Ported from the Swing {@code DashboardPanel} into a NetBeans
 * {@code TopComponent} that opens in the document area at startup.
 */
@TopComponent.Description(
        preferredID = "DashboardTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 100)
@ActionID(category = "Window", id = "com.example.warehouse.ui.dashboard.DashboardTopComponent")
@ActionReference(path = "Menu/Window", position = 3301)
@TopComponent.OpenActionRegistration(displayName = "Dashboard", preferredID = "DashboardTopComponent")
public final class DashboardTopComponent extends TopComponent {

    private final ProductService productService = new ProductService();
    private final PurchaseOrderService orderService = new PurchaseOrderService();

    private JLabel totalProductsValue;
    private JLabel stockValueLabel;
    private JLabel pendingOrdersValue;
    private JLabel lowStockValue;
    private JTable alertTable;
    private DefaultTableModel alertTableModel;

    public DashboardTopComponent() {
        setName("Dashboard");
        setToolTipText("Inventory KPIs, charts and low-stock alerts");

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 247, 250));

        add(buildKpiPanel(), BorderLayout.NORTH);
        add(buildChartsPanel(), BorderLayout.CENTER);
        add(buildAlertPanel(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildKpiPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        totalProductsValue = new JLabel("—");
        stockValueLabel = new JLabel("—");
        pendingOrdersValue = new JLabel("—");
        lowStockValue = new JLabel("—");

        panel.add(kpiCard("Total Products", totalProductsValue, UIConstants.PRIMARY));
        panel.add(kpiCard("Inventory Value", stockValueLabel, new Color(0, 100, 60)));
        panel.add(kpiCard("Pending Orders", pendingOrdersValue, new Color(140, 80, 0)));
        panel.add(kpiCard("Low Stock Alerts", lowStockValue, UIConstants.DANGER));

        return panel;
    }

    private JPanel kpiCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accentColor);
                g.fillRect(0, 0, 5, getHeight());
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 230)),
                BorderFactory.createEmptyBorder(12, 16, 12, 12)));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(UIConstants.FONT_KPI_LBL);
        lbl.setForeground(new Color(100, 110, 130));

        valueLabel.setFont(UIConstants.FONT_KPI_VAL);
        valueLabel.setForeground(UIConstants.PRIMARY_DARK);

        JPanel inner = new JPanel(new BorderLayout(0, 4));
        inner.setOpaque(false);
        inner.add(lbl, BorderLayout.NORTH);
        inner.add(valueLabel, BorderLayout.CENTER);

        card.add(inner, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(0, 90));
        return card;
    }

    private JPanel buildChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setOpaque(false);
        panel.add(buildStockByCategoryChart());
        panel.add(buildOrderStatusChart());
        return panel;
    }

    private ChartPanel buildStockByCategoryChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> data = productService.findStockByCategory();
        for (Object[] row : data) {
            dataset.addValue(((Number) row[1]).doubleValue(), "Qty", (String) row[0]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Stock Quantity by Category", "Category", "Units in Stock", dataset);
        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 253));
        plot.setRangeGridlinePaint(new Color(200, 210, 225));
        plot.setOutlinePaint(null);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UIConstants.ACCENT);
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 230)));
        cp.setPreferredSize(new Dimension(0, 220));
        return cp;
    }

    private ChartPanel buildOrderStatusChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        List<Object[]> data = orderService.countByAllStatuses();
        for (Object[] row : data) {
            dataset.setValue(row[0].toString(), ((Number) row[1]).doubleValue());
        }

        JFreeChart chart = ChartFactory.createPieChart("Purchase Orders by Status", dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setSectionPaint("Delivered", new Color(50, 160, 80));
        plot.setSectionPaint("Ordered", new Color(70, 130, 180));
        plot.setSectionPaint("Pending", new Color(200, 140, 30));
        plot.setSectionPaint("Cancelled", new Color(180, 60, 60));
        plot.setShadowPaint(null);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 230)));
        cp.setPreferredSize(new Dimension(0, 220));
        return cp;
    }

    private JPanel buildAlertPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(UIConstants.sectionBorder(" ⚠ Low Stock Alerts"));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(0, 180));

        String[] cols = {"SKU", "Product Name", "Category", "Supplier", "On Hand", "Reorder Level", "Shortage"};
        alertTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        alertTable = new JTable(alertTableModel);
        UIConstants.styleTable(alertTable);
        alertTable.setDefaultRenderer(Object.class, new LowStockRenderer());

        panel.add(new JScrollPane(alertTable), BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        long total = productService.count();
        long lowStock = productService.countLowStock();
        long pendingOrders = orderService.countPending();
        BigDecimal value = productService.totalInventoryValue();

        totalProductsValue.setText(String.valueOf(total));
        lowStockValue.setText(String.valueOf(lowStock));
        lowStockValue.setForeground(lowStock > 0 ? UIConstants.DANGER : UIConstants.SUCCESS);
        pendingOrdersValue.setText(String.valueOf(pendingOrders));
        pendingOrdersValue.setForeground(pendingOrders > 0 ? UIConstants.WARNING : UIConstants.SUCCESS);

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        stockValueLabel.setText(currency.format(value));

        alertTableModel.setRowCount(0);
        List<Product> alerts = productService.findLowStock();
        for (Product p : alerts) {
            int shortage = p.getReorderLevel() - p.getQuantity();
            alertTableModel.addRow(new Object[]{
                p.getSku(), p.getName(),
                p.getCategory().getName(),
                p.getSupplier() != null ? p.getSupplier().getName() : "—",
                p.getQuantity(), p.getReorderLevel(), shortage
            });
        }

        Status.message("Dashboard refreshed — " + alerts.size() + " low-stock alert(s)");
    }

    private static class LowStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel) {
                int onHand = (int) t.getValueAt(row, 4);
                c.setBackground(onHand == 0 ? UIConstants.CRITICAL_BG : UIConstants.LOW_STOCK_BG);
            }
            if (v instanceof Number) {
                setHorizontalAlignment(CENTER);
            } else {
                setHorizontalAlignment(LEFT);
            }
            return c;
        }
    }
}
