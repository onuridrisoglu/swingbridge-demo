package com.example.swingbridge;

import com.vaadin.modernization.swing.bridge.component.SwingBridge;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A {@link SwingBridge} subclass that provides a typed, classloader-transparent
 * event listener API for Swing→Vaadin communication, and a convenience method
 * for Vaadin→Swing method calls.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * SwingEventBridge bridge =
 *     new SwingEventBridge("com.example.warehouse.bridge.SuppliersTableFrame")
 *         .registerPublisher(SwingEventPublisher.class, "setEventPublisher")
 *         .addEventListener(SupplierSelectionEvent.class, e -> { ... });
 * add(bridge);
 *
 * // Invoke a no-arg method on the Swing frame from the Vaadin thread:
 * bridge.callSwingMethod("refreshTable");
 * }</pre>
 *
 * <h2>Classloader bridging</h2>
 * SwingBridge loads the Swing JAR in a dedicated {@link java.net.URLClassLoader}.
 * Event objects from Swing are instances of Swing-CL classes and cannot be cast
 * directly on the Vaadin side.  This class creates a {@link Proxy} of the
 * publisher interface in the Swing CL, reads all declared {@link String} fields
 * from the Swing-CL event via reflection, and reconstructs an equivalent
 * instance in the application CL before dispatching to listeners.
 *
 * <h2>Event class convention</h2>
 * Event classes must declare only {@link String} instance fields and provide a
 * single all-args constructor whose parameter order matches field declaration order.
 */
public class SwingEventBridge extends SwingBridge {

    private record PublisherRegistration(
            String interfaceName,
            String frameSetter,
            String samMethodName) {}

    private final List<PublisherRegistration> publishers = new ArrayList<>();

    private final ConcurrentHashMap<String, List<Consumer<Object>>> listeners =
            new ConcurrentHashMap<>();

    public SwingEventBridge(String mainClass) {
        super(mainClass);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public SwingEventBridge registerPublisher(Class<?> publisherInterface, String frameSetter) {
        String samName = findSamMethodName(publisherInterface);
        publishers.add(new PublisherRegistration(
                publisherInterface.getName(), frameSetter, samName));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <E> SwingEventBridge addEventListener(Class<E> eventType, Consumer<E> listener) {
        listeners.computeIfAbsent(eventType.getName(), k -> new ArrayList<>())
                 .add(evt -> listener.accept((E) evt));
        return this;
    }

    public void callSwingMethod(String methodName) {
        Component frame = getComponent();
        if (frame == null) return;
        SwingUtilities.invokeLater(() -> {
            try {
                frame.getClass().getMethod(methodName).invoke(frame);
            } catch (Exception e) {
                throw new RuntimeException(
                        "SwingEventBridge.callSwingMethod(\"" + methodName + "\") failed", e);
            }
        });
    }

    public void callSwingMethod(String methodName, String arg) {
        Component frame = getComponent();
        if (frame == null) return;
        SwingUtilities.invokeLater(() -> {
            try {
                frame.getClass().getMethod(methodName, String.class).invoke(frame, arg);
            } catch (Exception e) {
                throw new RuntimeException(
                        "SwingEventBridge.callSwingMethod(\"" + methodName + "\", ...) failed", e);
            }
        });
    }

    // -----------------------------------------------------------------------
    // SwingBridge hook
    // -----------------------------------------------------------------------

    @Override
    protected void afterInit(Component component) {
        super.afterInit(component);
        if (publishers.isEmpty() || listeners.isEmpty()) return;

        var ui = getComponentUI();
        ClassLoader swingCL = component.getClass().getClassLoader();

        for (var reg : publishers) {
            try {
                Class<?> publisherIfc = swingCL.loadClass(reg.interfaceName());
                String   samName      = reg.samMethodName();

                Object proxy = Proxy.newProxyInstance(
                        swingCL,
                        new Class<?>[]{publisherIfc},
                        (p, method, args) -> {
                            if (method.getName().equals(samName)) {
                                Object swingEvent = args[0];
                                var eventListeners = listeners.get(
                                        swingEvent.getClass().getName());
                                if (eventListeners != null) {
                                    Object vaadinEvent = convertEvent(swingEvent);
                                    ui.access(() -> eventListeners.forEach(
                                            l -> l.accept(vaadinEvent)));
                                }
                                return null;
                            }
                            return switch (method.getName()) {
                                case "equals"   -> p == args[0];
                                case "hashCode" -> System.identityHashCode(p);
                                case "toString" -> p.getClass().getName() + "@"
                                        + Integer.toHexString(System.identityHashCode(p));
                                default -> null;
                            };
                        });

                component.getClass()
                         .getMethod(reg.frameSetter(), publisherIfc)
                         .invoke(component, proxy);

            } catch (Exception e) {
                throw new RuntimeException(
                        "SwingEventBridge: failed to wire publisher '"
                                + reg.interfaceName() + "'", e);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String findSamMethodName(Class<?> iface) {
        for (var method : iface.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                return method.getName();
            }
        }
        throw new IllegalArgumentException(
                iface.getName() + " has no abstract method — is it a @FunctionalInterface?");
    }

    private static Object convertEvent(Object swingEvent) throws Exception {
        Class<?> appClass = Class.forName(swingEvent.getClass().getName());

        List<String>   values = new ArrayList<>();
        List<Class<?>> types  = new ArrayList<>();

        for (var field : swingEvent.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
            field.setAccessible(true);
            values.add((String) field.get(swingEvent));
            types.add(String.class);
        }

        return appClass
                .getConstructor(types.toArray(Class<?>[]::new))
                .newInstance(values.toArray());
    }
}
