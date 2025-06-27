package com.starlight.util;

import com.starlight.models.User;

/**
 * Simple holder for the currently logged in user.
 */
public class Session {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}

