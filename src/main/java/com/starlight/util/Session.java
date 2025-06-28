package com.starlight.util;

import com.starlight.models.User;

/**
 * Simple holder for the currently logged in user.
 */
public class Session {
    private static User currentUser;

    /**
     * Returns the currently logged in user or {@code null} if none is set.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Updates the currently logged in user reference.
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}

