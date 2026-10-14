package com.example.warehouse.ui.util;

import com.example.warehouse.AppSession;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Right-aligned status-line element showing the signed-in user and a live clock —
 * the NetBeans equivalent of the Swing {@code StatusBar}'s right panel.
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 100)
public final class UserStatusLineElement implements StatusLineElementProvider {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    private final JLabel userLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();

    public UserStatusLineElement() {
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        userLabel.setFont(UIConstants.FONT_REGULAR);
        timeLabel.setFont(UIConstants.FONT_REGULAR);

        panel.add(userLabel);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(timeLabel);

        refresh();
        Timer clock = new Timer(30_000, e -> refresh());
        clock.start();
    }

    /** Updates the user label right away (e.g. after sign-in) instead of on the next clock tick. */
    public static void refreshAll() {
        for (StatusLineElementProvider p : Lookup.getDefault().lookupAll(StatusLineElementProvider.class)) {
            if (p instanceof UserStatusLineElement element) {
                element.refresh();
            }
        }
    }

    private void refresh() {
        userLabel.setText("User: " + AppSession.getUsername());
        timeLabel.setText(LocalDateTime.now().format(FMT));
    }

    @Override
    public Component getStatusLineElement() {
        return panel;
    }
}
