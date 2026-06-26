package com.example.views;

import com.example.dashboard.CategoryStock;
import com.example.dashboard.DashboardService;
import com.example.dashboard.KpiSummary;
import com.example.dashboard.LowStockItem;
import com.example.dashboard.OrderStatusCount;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Credits;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.LayoutDirection;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A native-Vaadin reimagining of the dashboard. The Swing dashboard is still
 * available under "Dashboard"; this view ("Dashboard · Modern") shows what the
 * same data looks like rebuilt with the Vaadin {@link Dashboard} component plus
 * Vaadin Charts. The Dashboard lays its widgets out on a responsive column grid
 * that reflows automatically — four KPI tiles across on a wide screen, down to a
 * single column on a phone — without any custom media queries.
 */
@Route(value = "dashboard-modern", layout = MainLayout.class)
@PageTitle("Dashboard · Modern")
public class DashboardModernView extends Div {

    private static final SolidColor NAVY  = new SolidColor("#1b3a5c");
    private static final SolidColor BLUE  = new SolidColor("#3d7ab5");
    private static final SolidColor GREEN = new SolidColor("#2e9e5b");
    private static final SolidColor AMBER = new SolidColor("#d08a1e");
    private static final SolidColor RED   = new SolidColor("#a31515");

    private final DashboardService dashboardService;

    public DashboardModernView(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        addClassName("modern-dashboard");
        build();
    }

    private void build() {
        removeAll();
        add(buildHeader(), buildDashboard());
    }

    // ----------------------------------------------------------------- header

    private Component buildHeader() {
        H1 title = new H1("Operations Overview");
        title.addClassName("md-title");

        Span subtitle = new Span("Live inventory snapshot · rebuilt natively with Vaadin");
        subtitle.addClassName("md-subtitle");

        Div titleGroup = new Div(title, subtitle);
        titleGroup.addClassName("md-title-group");

        Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> {
            build();
            Notification.show("Dashboard refreshed", 2000, Notification.Position.BOTTOM_START);
        });
        refresh.addThemeVariants(ButtonVariant.TERTIARY);
        refresh.addClassName("md-refresh");

        Div header = new Div(titleGroup, refresh);
        header.addClassName("md-header");
        return header;
    }

    // -------------------------------------------------------------- dashboard

    private Component buildDashboard() {
        Dashboard dashboard = new Dashboard();
        dashboard.addClassName("md-dashboard");
        dashboard.setMinimumColumnWidth("230px");
        dashboard.setMaximumColumnCount(4);

        KpiSummary kpi = dashboardService.kpiSummary();
        List<CategoryStock> byCategory = dashboardService.stockByCategory();

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        currency.setMaximumFractionDigits(0);

        dashboard.add(
                kpiWidget("primary", VaadinIcon.PACKAGE, "Total Products",
                        String.valueOf(kpi.totalProducts()),
                        "across " + byCategory.size() + " categories"),
                kpiWidget("green", VaadinIcon.DOLLAR, "Inventory Value",
                        currency.format(kpi.inventoryValue()), "total stock valuation"),
                kpiWidget("amber", VaadinIcon.CART, "Pending Orders",
                        String.valueOf(kpi.pendingOrders()),
                        kpi.pendingOrders() > 0 ? "awaiting processing" : "all caught up"),
                kpiWidget("red", VaadinIcon.WARNING, "Low Stock Alerts",
                        String.valueOf(kpi.lowStockCount()),
                        kpi.lowStockCount() > 0 ? "below reorder level" : "stock levels healthy")
        );

        DashboardWidget categoryChart = chartWidget("Stock Quantity by Category",
                buildCategoryChart(byCategory));
        categoryChart.setColspan(2);
        dashboard.add(categoryChart);

        DashboardWidget statusChart = chartWidget("Purchase Orders by Status",
                buildOrderStatusChart());
        statusChart.setColspan(2);
        dashboard.add(statusChart);

        DashboardWidget alerts = buildAlertsWidget();
        alerts.setColspan(4);
        dashboard.add(alerts);

        return dashboard;
    }

    // -------------------------------------------------------------- KPI tiles

    private DashboardWidget kpiWidget(String accent, VaadinIcon icon, String label,
                                      String value, String sub) {
        Span valueSpan = new Span(value);
        valueSpan.addClassName("md-kpi-value");

        Span subSpan = new Span(sub);
        subSpan.addClassName("md-kpi-sub");

        Div content = new Div(valueSpan, subSpan);
        content.addClassNames("md-kpi", "md-kpi--" + accent);

        Icon glyph = icon.create();
        glyph.addClassName("md-kpi-glyph");
        Div iconBox = new Div(glyph);
        iconBox.addClassNames("md-kpi-icon", "md-kpi-icon--" + accent);

        DashboardWidget widget = new DashboardWidget(label);
        widget.addClassName("md-kpi-widget");
        widget.setHeaderContent(iconBox);
        widget.setContent(content);
        return widget;
    }

    // ----------------------------------------------------------------- charts

    private DashboardWidget chartWidget(String title, Chart chart) {
        DashboardWidget widget = new DashboardWidget(title);
        widget.addClassName("md-chart-widget");
        widget.setContent(chart);
        return widget;
    }

    private Chart buildCategoryChart(List<CategoryStock> data) {
        Chart chart = new Chart(ChartType.COLUMN);
        chart.addClassName("md-chart");
        chart.setHeight("300px");
        chart.setWidthFull();

        Configuration conf = chart.getConfiguration();
        conf.setTitle("");

        String[] categories = data.stream().map(CategoryStock::category).toArray(String[]::new);
        Number[] values = data.stream().map(CategoryStock::units).toArray(Number[]::new);

        XAxis x = new XAxis();
        x.setCategories(categories);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Units in stock");
        y.setMin(0);
        conf.addyAxis(y);

        ListSeries series = new ListSeries("Units in stock", values);
        PlotOptionsColumn options = new PlotOptionsColumn();
        options.setColor(BLUE);
        options.setBorderRadius(4);
        series.setPlotOptions(options);
        conf.addSeries(series);

        conf.getLegend().setEnabled(false);
        conf.setTooltip(new Tooltip());
        conf.setCredits(disabledCredits());
        return chart;
    }

    private Chart buildOrderStatusChart() {
        Chart chart = new Chart(ChartType.PIE);
        chart.addClassName("md-chart");
        chart.setHeight("300px");
        chart.setWidthFull();

        Configuration conf = chart.getConfiguration();
        conf.setTitle("");

        Map<String, SolidColor> palette = new LinkedHashMap<>();
        palette.put("Delivered", GREEN);
        palette.put("Ordered", BLUE);
        palette.put("Pending", AMBER);
        palette.put("Cancelled", RED);

        DataSeries series = new DataSeries();
        for (OrderStatusCount row : dashboardService.ordersByStatus()) {
            DataSeriesItem item = new DataSeriesItem(row.status(), row.count());
            item.setColor(palette.getOrDefault(row.status(), NAVY));
            series.add(item);
        }

        PlotOptionsPie options = new PlotOptionsPie();
        options.setInnerSize("62%");
        options.setBorderWidth(2);
        DataLabels labels = new DataLabels(true);
        labels.setFormat("{point.name}: {point.y}");
        options.setDataLabels(labels);
        series.setPlotOptions(options);
        conf.addSeries(series);

        Legend legend = conf.getLegend();
        legend.setEnabled(true);
        legend.setLayout(LayoutDirection.HORIZONTAL);

        conf.setTooltip(new Tooltip());
        conf.setCredits(disabledCredits());
        return chart;
    }

    private static Credits disabledCredits() {
        Credits credits = new Credits();
        credits.setEnabled(false);
        return credits;
    }

    // ----------------------------------------------------------- alerts table

    private DashboardWidget buildAlertsWidget() {
        List<LowStockItem> alerts = dashboardService.lowStockItems();

        Grid<LowStockItem> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassName("md-alert-grid");
        grid.setAllRowsVisible(true);
        grid.setItems(alerts);

        grid.addColumn(LowStockItem::sku).setHeader("SKU").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(LowStockItem::name).setHeader("Product").setFlexGrow(1);
        grid.addColumn(LowStockItem::category).setHeader("Category").setAutoWidth(true);
        grid.addColumn(LowStockItem::supplier).setHeader("Supplier").setAutoWidth(true);
        grid.addColumn(LowStockItem::onHand).setHeader("On Hand").setAutoWidth(true)
            .setFlexGrow(0).setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(LowStockItem::reorderLevel).setHeader("Reorder").setAutoWidth(true)
            .setFlexGrow(0).setTextAlign(ColumnTextAlign.CENTER);
        grid.addComponentColumn(this::statusBadge).setHeader("Status").setAutoWidth(true).setFlexGrow(0);

        Span meta = new Span(alerts.size() + " item" + (alerts.size() == 1 ? "" : "s") + " need attention");
        meta.addClassName("md-panel-meta");

        DashboardWidget widget = new DashboardWidget("Low Stock Alerts");
        widget.addClassName("md-alert-widget");
        widget.setHeaderContent(meta);
        widget.setContent(grid);
        return widget;
    }

    private Component statusBadge(LowStockItem item) {
        Span badge = new Span(item.outOfStock() ? "OUT OF STOCK" : "LOW · short " + item.shortage());
        badge.addClassNames("md-badge", item.outOfStock() ? "md-badge--critical" : "md-badge--low");
        return badge;
    }
}
