package com.example.auth;

import com.example.warehouse.AppSession;
import com.example.warehouse.dao.UserDAO;
import com.example.warehouse.model.User;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedHttpSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The single place this application decides who is logged in.
 *
 * <p>It exists because the Flow and React halves of the app cannot see the same
 * session object. Flow code runs inside a Vaadin request and reaches
 * {@link VaadinSession}; a {@code @BrowserCallable} method is invoked by Hilla's
 * own controller, where {@code VaadinSession.getCurrent()} is normally null. The
 * common ground is the servlet {@link HttpSession} that the Vaadin session is
 * itself stored in, so that is where the current user is kept — see
 * {@link #httpSession(boolean)}, which resolves it from whichever of the two
 * request types is actually in flight.
 *
 * <p>The user is therefore addressed the same way from both sides: Flow views
 * inject this bean, React views call the generated TypeScript client. Neither
 * touches the session key directly.
 *
 * <p>{@code @AnonymousAllowed} is required — a browser-callable service rejects
 * every call otherwise, and an anonymous visitor must be able to reach
 * {@link #login} and {@link #currentUser} to find out that they are anonymous.
 */
@Service
@BrowserCallable
@AnonymousAllowed
public class AuthService {

    /**
     * Session key for the authenticated {@link User}. Namespaced so it cannot
     * collide with anything Vaadin stores in the same session.
     */
    private static final String USER_KEY = "com.example.auth.user";

    private final UserDAO userDAO = new UserDAO();

    /**
     * Verifies credentials and, on success, establishes the session.
     *
     * @return the logged-in user, or {@code null} when the credentials are wrong
     */
    public @Nullable CurrentUser login(@NonNull String username, @NonNull String password) {
        User user = userDAO.authenticate(username, password);
        if (user == null) {
            return null;
        }
        HttpSession session = httpSession(true);
        if (session != null) {
            session.setAttribute(USER_KEY, user);
        }
        // The Swing side reads its current user from this static holder — the
        // bridge panels (DashboardBridge, ProductsBridge, …) depend on it.
        AppSession.login(user);
        return CurrentUser.of(user);
    }

    /** @return the logged-in user, or {@code null} when nobody is signed in. */
    public @Nullable CurrentUser currentUser() {
        User user = sessionUser();
        return user == null ? null : CurrentUser.of(user);
    }

    /** Clears the session. Navigating away afterwards is the caller's job. */
    public void logout() {
        HttpSession session = httpSession(false);
        if (session != null) {
            session.removeAttribute(USER_KEY);
        }
        AppSession.logout();
    }

    /**
     * Must stay private: every public method of a {@code @BrowserCallable} class
     * becomes a callable endpoint, and the {@link User} entity carries a
     * password field. {@link CurrentUser} is what leaves the server.
     */
    private @Nullable User sessionUser() {
        HttpSession session = httpSession(false);
        return session == null ? null : (User) session.getAttribute(USER_KEY);
    }

    /**
     * Resolves the servlet session from either kind of request.
     *
     * <p>In a Vaadin request the session is reachable through the current
     * {@link VaadinSession}; in a Hilla endpoint call it comes from Spring's
     * request context. Whichever path is taken, it is the same underlying
     * {@code HttpSession}, because the Vaadin session is stored as one of its
     * attributes.
     *
     * @param create whether to start a session if none exists yet
     */
    private static @Nullable HttpSession httpSession(boolean create) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null
                && vaadinSession.getSession() instanceof WrappedHttpSession wrapped) {
            return wrapped.getHttpSession();
        }
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getSession(create);
        }
        return null;
    }
}
