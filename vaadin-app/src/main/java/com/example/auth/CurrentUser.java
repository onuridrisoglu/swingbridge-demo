package com.example.auth;

import com.example.warehouse.model.User;
import org.jspecify.annotations.NonNull;

/**
 * The logged-in user as the browser sees them. Deliberately not the Hibernate
 * {@link User} entity: that carries a password field and a persistence-context
 * lifecycle, neither of which belongs in a JSON payload.
 */
public record CurrentUser(
        @NonNull String username,
        @NonNull String fullName,
        User.@NonNull UserRole role) {

    static CurrentUser of(User user) {
        return new CurrentUser(user.getUsername(), user.getFullName(), user.getRole());
    }
}
