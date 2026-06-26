package com.example.warehouse.startup;

import com.example.warehouse.config.PersistenceUtil;
import com.example.warehouse.dao.UserDAO;
import com.example.warehouse.model.User;
import com.example.warehouse.seed.DataSeeder;
import com.example.warehouse.ui.util.Status;
import com.example.warehouse.AppSession;
import org.openide.LifecycleManager;
import org.openide.modules.OnStart;

import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Startup sequence for the WarehousePro RCP app, mirroring what {@code Main.main()}
 * did in the Swing edition: initialise the embedded H2 database, seed demo data,
 * then present the login screen <em>before</em> the main window appears.
 *
 * <p>{@code @OnStart} runnables run during startup and are guaranteed to finish
 * <strong>before</strong> the main window is shown, so showing the modal login
 * dialog here (via {@code invokeAndWait}) gates the whole application — exactly
 * like the Swing {@code LoginFrame} did. If authentication is cancelled or fails,
 * the platform shuts down before any window is displayed.</p>
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

        boolean authenticated = showLogin();
        if (!authenticated) {
            LifecycleManager.getDefault().exit();
            return;
        }
        Status.message("Signed in as " + AppSession.getCurrentUser().getFullName()
                + "  [" + AppSession.getCurrentUser().getRole() + "]");
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

    private boolean showLogin() {
        final boolean[] ok = {false};
        try {
            SwingUtilities.invokeAndWait(() -> ok[0] = LoginDialog.authenticate());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Login dialog failed", e);
        }
        return ok[0];
    }
}
