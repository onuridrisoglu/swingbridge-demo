import { useCallback, useEffect, useState } from 'react';
import { Button } from '@vaadin/react-components/Button.js';
import { Grid } from '@vaadin/react-components/Grid.js';
import { GridColumn } from '@vaadin/react-components/GridColumn.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { Chart } from '@vaadin/react-components-pro/Chart.js';
import { ChartSeries } from '@vaadin/react-components-pro/ChartSeries.js';
import { DashboardLayout } from '@vaadin/react-components-pro/DashboardLayout.js';
import { DashboardWidget } from '@vaadin/react-components-pro/DashboardWidget.js';
import { DashboardService } from 'Frontend/generated/endpoints.js';
import type CategoryStock from 'Frontend/generated/com/example/dashboard/CategoryStock.js';
import type KpiSummary from 'Frontend/generated/com/example/dashboard/KpiSummary.js';
import type LowStockItem from 'Frontend/generated/com/example/dashboard/LowStockItem.js';
import type OrderStatusCount from 'Frontend/generated/com/example/dashboard/OrderStatusCount.js';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import '@vaadin/icons';

/**
 * The modern dashboard, rebuilt as a React view. Stage three of the demo showed
 * a Vaadin toolbar driving a Swing table; this is the other half of the same
 * story — the same Java read model (`DashboardService`), reached from React
 * instead of from Flow.
 *
 * Nothing here talks to the Swing app directly. `DashboardService` is annotated
 * `@BrowserCallable`, so Hilla generates the typed client imported above, and
 * the records it returns arrive as TypeScript interfaces. The view renders
 * inside the Flow `MainLayout` because that class carries `@Layout`, so the
 * drawer, navbar and Aura theming are shared with the bridge views.
 */

/** Replaces the @PageTitle the Flow version carried. */
export const config: ViewConfig = { title: 'Dashboard · Modern' };

const PALETTE = {
  navy: '#1b3a5c',
  blue: '#3d7ab5',
  green: '#2e9e5b',
  amber: '#d08a1e',
  red: '#a31515',
} as const;

const STATUS_COLORS: Record<string, string> = {
  Delivered: PALETTE.green,
  Ordered: PALETTE.blue,
  Pending: PALETTE.amber,
  Cancelled: PALETTE.red,
};

const currency = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 0,
});

/** Everything the view renders, fetched in one round trip. */
type DashboardData = {
  kpi: KpiSummary;
  byCategory: CategoryStock[];
  byStatus: OrderStatusCount[];
  alerts: LowStockItem[];
};

export default function DashboardModernView() {
  const [data, setData] = useState<DashboardData | undefined>();

  const load = useCallback(async () => {
    const [kpi, byCategory, byStatus, alerts] = await Promise.all([
      DashboardService.kpiSummary(),
      DashboardService.stockByCategory(),
      DashboardService.ordersByStatus(),
      DashboardService.lowStockItems(),
    ]);
    setData({ kpi, byCategory, byStatus, alerts });
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const refresh = async () => {
    await load();
    Notification.show('Dashboard refreshed', { duration: 2000, position: 'bottom-start' });
  };

  return (
    <div className="modern-dashboard">
      <div className="md-header">
        <div className="md-title-group">
          <h1 className="md-title">Operations Overview</h1>
          <span className="md-subtitle">Live inventory snapshot · rebuilt in React with Hilla</span>
        </div>
        <Button className="md-refresh" theme="tertiary" onClick={refresh}>
          <Icon icon="vaadin:refresh" slot="prefix" />
          Refresh
        </Button>
      </div>

      {data && <DashboardGrid data={data} />}
    </div>
  );
}

// -------------------------------------------------------------------- widgets

function DashboardGrid({ data }: { readonly data: DashboardData }) {
  const { kpi, byCategory, byStatus, alerts } = data;

  return (
    <DashboardLayout
      className="md-dashboard"
      style={{
        '--vaadin-dashboard-col-min-width': '230px',
        '--vaadin-dashboard-col-max-count': 4,
      } as React.CSSProperties}
    >
      <KpiWidget
        accent="primary"
        icon="vaadin:package"
        label="Total Products"
        value={String(kpi.totalProducts)}
        sub={`across ${byCategory.length} categories`}
      />
      <KpiWidget
        accent="green"
        icon="vaadin:dollar"
        label="Inventory Value"
        value={currency.format(Number(kpi.inventoryValue))}
        sub="total stock valuation"
      />
      <KpiWidget
        accent="amber"
        icon="vaadin:cart"
        label="Pending Orders"
        value={String(kpi.pendingOrders)}
        sub={kpi.pendingOrders > 0 ? 'awaiting processing' : 'all caught up'}
      />
      <KpiWidget
        accent="red"
        icon="vaadin:warning"
        label="Low Stock Alerts"
        value={String(kpi.lowStockCount)}
        sub={kpi.lowStockCount > 0 ? 'below reorder level' : 'stock levels healthy'}
      />

      <DashboardWidget className="md-chart-widget" widgetTitle="Stock Quantity by Category" style={colspan(2)}>
        <CategoryChart data={byCategory} />
      </DashboardWidget>

      <DashboardWidget className="md-chart-widget" widgetTitle="Purchase Orders by Status" style={colspan(2)}>
        <OrderStatusChart data={byStatus} />
      </DashboardWidget>

      <DashboardWidget className="md-alert-widget" widgetTitle="Low Stock Alerts" style={colspan(4)}>
        <span slot="header-content" className="md-panel-meta">
          {alerts.length} item{alerts.length === 1 ? '' : 's'} need attention
        </span>
        <AlertsGrid items={alerts} />
      </DashboardWidget>
    </DashboardLayout>
  );
}

/** The widget colspan is a CSS custom property rather than a component prop. */
function colspan(columns: number): React.CSSProperties {
  return { '--vaadin-dashboard-widget-colspan': columns } as React.CSSProperties;
}

function KpiWidget({
  accent,
  icon,
  label,
  value,
  sub,
}: {
  readonly accent: string;
  readonly icon: string;
  readonly label: string;
  readonly value: string;
  readonly sub: string;
}) {
  return (
    <DashboardWidget className="md-kpi-widget" widgetTitle={label}>
      <div slot="header-content" className={`md-kpi-icon md-kpi-icon--${accent}`}>
        <Icon className="md-kpi-glyph" icon={icon} />
      </div>
      <div className={`md-kpi md-kpi--${accent}`}>
        <span className="md-kpi-value">{value}</span>
        <span className="md-kpi-sub">{sub}</span>
      </div>
    </DashboardWidget>
  );
}

// --------------------------------------------------------------------- charts

function CategoryChart({ data }: { readonly data: readonly CategoryStock[] }) {
  return (
    <Chart
      className="md-chart"
      type="column"
      categories={data.map((row) => row.category)}
      style={{ height: '300px', width: '100%' }}
      additionalOptions={{
        yAxis: { title: { text: 'Units in stock' }, min: 0 },
        legend: { enabled: false },
        credits: { enabled: false },
      }}
    >
      <ChartSeries
        title="Units in stock"
        values={data.map((row) => row.units)}
        additionalOptions={{ type: 'column', color: PALETTE.blue, borderRadius: 4 }}
      />
    </Chart>
  );
}

function OrderStatusChart({ data }: { readonly data: readonly OrderStatusCount[] }) {
  return (
    <Chart
      className="md-chart"
      type="pie"
      tooltip
      style={{ height: '300px', width: '100%' }}
      additionalOptions={{
        credits: { enabled: false },
        legend: { enabled: true, layout: 'horizontal' },
        plotOptions: {
          pie: {
            innerSize: '62%',
            borderWidth: 2,
            dataLabels: { enabled: true, format: '{point.name}: {point.y}' },
          },
        },
      }}
    >
      <ChartSeries
        title="Orders"
        values={data.map((row) => ({
          name: row.status,
          y: row.count,
          color: STATUS_COLORS[row.status] ?? PALETTE.navy,
        }))}
      />
    </Chart>
  );
}

// ---------------------------------------------------------------- alerts grid

function AlertsGrid({ items }: { readonly items: readonly LowStockItem[] }) {
  return (
    <Grid className="md-alert-grid" items={items as LowStockItem[]} theme="row-stripes no-border" allRowsVisible>
      <GridColumn path="sku" header="SKU" autoWidth flexGrow={0} />
      <GridColumn path="name" header="Product" flexGrow={1} />
      <GridColumn path="category" header="Category" autoWidth />
      <GridColumn path="supplier" header="Supplier" autoWidth />
      <GridColumn path="onHand" header="On Hand" autoWidth flexGrow={0} textAlign="center" />
      <GridColumn path="reorderLevel" header="Reorder" autoWidth flexGrow={0} textAlign="center" />
      <GridColumn header="Status" autoWidth flexGrow={0}>
        {({ item }) => <StatusBadge item={item} />}
      </GridColumn>
    </Grid>
  );
}

function StatusBadge({ item }: { readonly item: LowStockItem }) {
  return (
    <span className={`md-badge ${item.outOfStock ? 'md-badge--critical' : 'md-badge--low'}`}>
      {item.outOfStock ? 'OUT OF STOCK' : `LOW · short ${item.shortage}`}
    </span>
  );
}
