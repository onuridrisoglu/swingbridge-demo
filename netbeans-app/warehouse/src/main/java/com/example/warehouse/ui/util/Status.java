package com.example.warehouse.ui.util;

import org.openide.awt.StatusDisplayer;

/**
 * Thin facade over the NetBeans status line ({@link StatusDisplayer}).
 *
 * <p>In the Swing edition each panel pushed text into a {@code StatusBar}
 * instance threaded through its constructor. NetBeans {@code TopComponent}s are
 * created by the window system with a no-arg constructor, so instead they report
 * progress through the platform-wide status line, exactly like a native NetBeans
 * application would.</p>
 */
public final class Status {

    private Status() {
    }

    public static void message(String message) {
        StatusDisplayer.getDefault().setStatusText(message);
    }

    public static void rowCount(int count, String entity) {
        message(count + " " + entity + " record" + (count != 1 ? "s" : "") + " loaded");
    }
}
