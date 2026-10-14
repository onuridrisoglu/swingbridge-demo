package com.example.warehouse.startup;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.dao.UserDAO;
import com.example.warehouse.model.User;
import com.example.warehouse.seed.DataSeeder;
import com.example.warehouse.ui.util.Status;
import com.example.warehouse.ui.util.UserStatusLineElement;
import com.example.warehouse.AppSession;
import org.openide.LifecycleManager;
import org.openide.modules.OnStart;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Startup sequence for the WarehousePro RCP app, mirroring what {@code Main.main()}
 * did in the Swing edition: initialise the embedded H2 database, seed demo data,
 * then present the login screen.
 *
 * <p>The modal login dialog is shown once the main window is up
 * ({@link WindowManager#invokeWhenUIReady}), owned by it, rather than from the
 * {@code @OnStart} runnable itself: an owner-less dialog shown before the main window
 * exists blocks the platform from ever opening that window, which the Vaadin Swing
 * Bridge waits for before it streams anything — the browser would hang on a dialog it
 * cannot see. If authentication is cancelled or fails, the platform shuts down.</p>
 *
 * <p>The five screens are registered with {@code openAtStartup = false} and opened
 * here only after a successful login, so nothing but the empty main window is
 * visible behind the login dialog. In bridge mode they stay closed: the Vaadin host
 * pulls each one into its own route via {@code embedTopComponent}, which looks them
 * up by ID whether or not they are open.</p>
 *
 * <p>The thread context class loader is pinned to this module's loader while
 * Hibernate boots so the JPA provider, {@code persistence.xml} and entity classes
 * (all bundled in this NBM) resolve under the NetBeans module class-loading scheme.</p>
 */
@OnStart
public final class DatabaseStartup implements Runnable {

    private static final Logger LOG = Logger.getLogger(DatabaseStartup.class.getName());

    /** Set by the Vaadin SwingBridge host (-Dwarehouse.bridge=true) when this RCP is
     *  embedded in the browser. In that mode the desktop login dialog is skipped. */
    private static final boolean BRIDGE_MODE = Boolean.getBoolean("warehouse.bridge");

    /** Screens opened after login, in tab order; the first one ends up active. */
    private static final String[] SCREENS = {
            "DashboardTopComponent", "ProductsTopComponent", "SuppliersTopComponent",
            "OrdersTopComponent", "StockTopComponent"};

    @Override
    public void run() {
        initDatabase();

        if (BRIDGE_MODE) {
            // Embedded under the Vaadin Swing Bridge: there is no desktop login dialog;
            // the browser-side Vaadin LoginView authenticates. Auto sign-in a default
            // user so the session (e.g. stock-movement "created by") is populated.
            autoLoginForBridge();
            return;
        }

        WindowManager.getDefault().invokeWhenUIReady(DatabaseStartup::login);
    }

    /** Runs on the EDT once the main window is showing. */
    private static void login() {
        boolean authenticated = LoginDialog.authenticate(WindowManager.getDefault().getMainWindow());
        if (!authenticated) {
            LifecycleManager.getDefault().exit();
            return;
        }
        UserStatusLineElement.refreshAll();
        openScreens();
        Status.message("Signed in as " + AppSession.getCurrentUser().getFullName()
                + "  [" + AppSession.getCurrentUser().getRole() + "]");
    }

    private static void openScreens() {
        TopComponent first = null;
        for (String id : SCREENS) {
            TopComponent tc = WindowManager.getDefault().findTopComponent(id);
            if (tc == null) {
                LOG.warning("TopComponent not found: " + id);
                continue;
            }
            tc.open();
            if (first == null) {
                first = tc;
            }
        }
        if (first != null) {
            first.requestActive();
        }
    }

    private void autoLoginForBridge() {
        try {
            User admin = new UserDAO().findByUsername("admin");
            if (admin != null) {
                AppSession.login(admin);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Bridge auto-login failed", e);
        }
    }

    private void initDatabase() {
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(DatabaseStartup.class.getClassLoader());
            PersistenceUtil.init();
            DataSeeder.seedIfEmpty();
        } finally {
            thread.setContextClassLoader(previous);
        }
    }
}
