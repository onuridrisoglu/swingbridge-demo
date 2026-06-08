package com.example.warehouse.ui;

import com.example.warehouse.AppSession;
import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.ui.dashboard.DashboardPanel;
import com.example.warehouse.ui.orders.PurchaseOrdersPanel;
import com.example.warehouse.ui.products.ProductsPanel;
import com.example.warehouse.ui.stock.StockMovementsPanel;
import com.example.warehouse.ui.suppliers.SuppliersPanel;
import com.example.warehouse.ui.util.StatusBar;
import com.example.warehouse.ui.util.UIConstants;
import com.example.warehouse.bridge.SupplierEditRequestListener;
import com.example.warehouse.bridge.SupplierSelectedEvent;
import com.example.warehouse.bridge.SupplierSelectionListener;
import com.vaadin.swingbridge.interop.ExposedMethod;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private StatusBar statusBar;
    private JTabbedPane tabbedPane;
    private CardLayout cardLayout;
    private JPanel cards;
    private SuppliersPanel suppliersPanel;

    public MainFrame(GraphicsConfiguration gc) {
        this(gc, false);
    }

    public MainFrame(GraphicsConfiguration gc, boolean bridgeMode) {
        super(gc);
        setTitle("WarehousePro™ 3.5  —  Inventory & Warehouse Management");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);

        if (bridgeMode) {
            statusBar = new StatusBar("");
            setUndecorated(true);

            cardLayout = new CardLayout();
            cards = new JPanel(cardLayout);
            suppliersPanel = new SuppliersPanel(statusBar);
            cards.add(new DashboardPanel(statusBar),      "dashboard");
            cards.add(new ProductsPanel(statusBar),       "products");
            cards.add(suppliersPanel,                     "suppliers");
            cards.add(new PurchaseOrdersPanel(statusBar), "orders");
            cards.add(new StockMovementsPanel(statusBar), "stock");

            JPanel root = new JPanel(new BorderLayout());
            root.add(cards, BorderLayout.CENTER);
            setContentPane(root);
        } else {
            statusBar = new StatusBar(AppSession.getCurrentUser().getFullName());
            setMinimumSize(new Dimension(900, 600));
            Rectangle screen = gc.getBounds();
            setLocation(screen.x + (screen.width - 1280) / 2, screen.y + (screen.height - 800) / 2);

            setJMenuBar(buildMenuBar());

            JPanel root = new JPanel(new BorderLayout());
            root.add(buildTitleBar(), BorderLayout.NORTH);
            tabbedPane = buildTabbedPane();
            root.add(tabbedPane,  BorderLayout.CENTER);
            root.add(statusBar,   BorderLayout.SOUTH);
            setContentPane(root);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    confirmExit();
                }
            });
        }

        setVisible(true);
    }

    @ExposedMethod
    public void showDashboard() {
        if (cardLayout != null) cardLayout.show(cards, "dashboard");
        else tabbedPane.setSelectedIndex(0);

    }

    @ExposedMethod
    public void showProducts() {
        if (cardLayout != null) cardLayout.show(cards, "products");
        else tabbedPane.setSelectedIndex(1);
    }

    @ExposedMethod
    public void showSuppliers() {
        if (cardLayout != null) cardLayout.show(cards, "suppliers");
        else tabbedPane.setSelectedIndex(2);
    }

    @ExposedMethod
    public void showOrders() {
        if (cardLayout != null) cardLayout.show(cards, "orders");
        else tabbedPane.setSelectedIndex(3);
    }

    @ExposedMethod
    public void showStock() {
        if (cardLayout != null) cardLayout.show(cards, "stock");
        else tabbedPane.setSelectedIndex(4);
    }

    @ExposedMethod
    public void showSuppliersTable() {
        if (suppliersPanel != null) suppliersPanel.setToolbarVisible(false);
        if (cardLayout != null) cardLayout.show(cards, "suppliers");
    }

    @ExposedMethod
    public void setSupplierSelectionListener(SupplierSelectionListener listener) {
        if (suppliersPanel != null) suppliersPanel.setSelectionListener(listener);
    }

    @ExposedMethod
    public void setSupplierEditRequestListener(SupplierEditRequestListener listener) {
        if (suppliersPanel != null) suppliersPanel.setEditRequestListener(listener);
    }

    @ExposedMethod
    public void refreshSuppliers() {
        if (suppliersPanel != null) suppliersPanel.refresh();
    }

    @ExposedMethod
    public void setSupplierFilter(String filter) {
        if (suppliersPanel != null) suppliersPanel.filterTable(filter);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.PRIMARY_DARK, getWidth(), 0, UIConstants.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bar.setPreferredSize(new Dimension(0, 42));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JLabel title = new JLabel("WarehousePro™ 3.5");
        title.setFont(new Font("Tahoma", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JLabel userInfo = new JLabel(AppSession.getCurrentUser().getFullName() +
                "  [" + AppSession.getCurrentUser().getRole() + "]");
        userInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
        userInfo.setForeground(new Color(190, 215, 240));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(100, 130, 170));
        logoutBtn.setOpaque(true);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setMargin(new Insets(2, 8, 2, 8));
        logoutBtn.addActionListener(e -> doLogout());

        right.add(userInfo);
        right.add(logoutBtn);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UIConstants.FONT_BOLD);
        tabs.setBackground(UIConstants.TOOLBAR_BG);

        tabs.addTab("  Dashboard  ",  new DashboardPanel(statusBar));
        tabs.addTab("  Products  ",   new ProductsPanel(statusBar));
        tabs.addTab("  Suppliers  ",  new SuppliersPanel(statusBar));
        tabs.addTab("  Orders  ",     new PurchaseOrdersPanel(statusBar));
        tabs.addTab("  Stock Log  ",  new StockMovementsPanel(statusBar));

        return tabs;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 244, 250));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(UIConstants.FONT_REGULAR);
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(UIConstants.FONT_REGULAR);
        logoutItem.addActionListener(e -> doLogout());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(UIConstants.FONT_REGULAR);
        exitItem.addActionListener(e -> confirmExit());
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(UIConstants.FONT_REGULAR);
        String[] tabs = {"Dashboard", "Products", "Suppliers", "Orders", "Stock Log"};
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            JMenuItem item = new JMenuItem(tabs[i]);
            item.setFont(UIConstants.FONT_REGULAR);
            item.addActionListener(e -> tabbedPane.setSelectedIndex(idx));
            viewMenu.add(item);
        }

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(UIConstants.FONT_REGULAR);
        JMenuItem aboutItem = new JMenuItem("About WarehousePro");
        aboutItem.setFont(UIConstants.FONT_REGULAR);
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "WarehousePro™ 3.5\nInventory & Warehouse Management System\n\n" +
                "© 2009 Acme Logistics Software Inc.\nAll rights reserved.\n\n" +
                "Build 3.5.0 (2009-11-04)",
                "About WarehousePro", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void doLogout() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        AppSession.logout();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame(gc));
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit WarehousePro?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            PersistenceUtil.shutdown();
            System.exit(0);
        }
    }
}
