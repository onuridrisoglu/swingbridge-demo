package com.example.warehouse;

import com.example.warehouse.model.User;

public class AppSession {

    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    public static String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "unknown";
    }
}
