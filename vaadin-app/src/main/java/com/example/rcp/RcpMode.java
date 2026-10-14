package com.example.rcp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.swingbridge.NetBeansRcpBridge;
import org.springframework.beans.factory.annotation.Value;

import java.util.Locale;

/**
 * Which {@link NetBeansRcpBridge} embedding mode this host runs in, set by
 * {@code warehouse.rcp.mode} ({@code full} or {@code views}).
 *
 * <p>The full shell and single-{@code TopComponent} bridges must not be mixed in one
 * Vaadin session: single-TopComponent mode hides the platform main window the full
 * shell needs. All routes stay registered: in {@code views} mode {@code /} forwards to
 * the dashboard, in {@code full} mode the TopComponent views render a placeholder, so
 * a session only ever boots one kind.</p>
 *
 * <p>The mode also sets {@code warehouse.bridge}, read by the RCP's {@code @OnStart}:
 * in {@code views} mode the Vaadin {@code LoginView} authenticates and the RCP skips
 * its desktop login; in {@code full} mode the unchanged RCP shows its own login.</p>
 */
@org.springframework.stereotype.Component
public class RcpMode {

    public enum Mode { FULL, VIEWS }

    private final Mode mode;

    public RcpMode(@Value("${warehouse.rcp.mode:full}") String mode) {
        this.mode = Mode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        System.setProperty("warehouse.bridge", String.valueOf(this.mode == Mode.VIEWS));
    }

    public Mode mode() {
        return mode;
    }

    /** Route to land on after login. */
    public String homeRoute() {
        return mode == Mode.FULL ? "" : "dashboard";
    }

    /** The entire RCP shell. Only used in {@code full} mode ({@code /} forwards otherwise). */
    public Component fullShell() {
        if (mode != Mode.FULL) {
            throw new IllegalStateException("Full RCP shell requested in " + mode + " mode");
        }
        return sized(new NetBeansRcpBridge());
    }

    /** A single embedded TopComponent, or a placeholder when running in {@code full} mode. */
    public Component topComponent(String preferredId) {
        if (mode != Mode.VIEWS) {
            return placeholder("Embedded TopComponents are disabled",
                    "This host runs in full mode, where the whole NetBeans shell is served at /.",
                    "views");
        }
        return sized(new NetBeansRcpBridge().embedTopComponent(preferredId));
    }

    private static NetBeansRcpBridge sized(NetBeansRcpBridge rcp) {
        rcp.setSizeFull();
        return rcp;
    }

    private static Component placeholder(String title, String reason, String otherMode) {
        Div box = new Div(
                new H2(title),
                new Paragraph(reason),
                new Paragraph("Restart with warehouse.rcp.mode=" + otherMode
                        + " (e.g. ./start-vaadin.sh " + otherMode + ") to enable it."));
        box.addClassName("rcp-placeholder");
        return box;
    }
}
