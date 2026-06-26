package com.example.warehouse.actions;

import com.example.warehouse.AppSession;
import com.example.warehouse.startup.LoginDialog;
import com.example.warehouse.ui.util.Status;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * File ▸ Logout — clears the session and re-presents the login dialog, mirroring
 * the Swing {@code MainFrame}'s Logout menu item / title-bar button. Cancelling
 * the re-login shuts the application down (there is no signed-in user).
 */
@ActionID(category = "File", id = "com.example.warehouse.actions.LogoutAction")
@ActionRegistration(displayName = "Logout")
@ActionReference(path = "Menu/File", position = 0, separatorAfter = 50)
public final class LogoutAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        AppSession.logout();
        boolean reauthenticated = LoginDialog.authenticate();
        if (!reauthenticated) {
            LifecycleManager.getDefault().exit();
            return;
        }
        Status.message("Signed in as " + AppSession.getCurrentUser().getFullName()
                + "  [" + AppSession.getCurrentUser().getRole() + "]");
    }
}
