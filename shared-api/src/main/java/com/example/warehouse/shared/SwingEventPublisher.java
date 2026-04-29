package com.example.warehouse.shared;

/**
 * Callback registered by the Vaadin side on a Swing frame so the frame can
 * publish typed events without knowing anything about Vaadin.
 *
 * The parameter type is {@link Object} (not generic) so the method signature
 * contains only bootstrap-classloader types. This lets SwingEventBridge create
 * a {@link java.lang.reflect.Proxy} of this interface in the Swing classloader
 * and pass it across the SwingBridge classloader boundary without a
 * {@link ClassCastException}.
 */
@FunctionalInterface
public interface SwingEventPublisher {
    void publish(Object event);
}
