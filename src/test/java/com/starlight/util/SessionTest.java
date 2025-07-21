package com.starlight.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.model.User;

/**
 * Unit tests for {@link Session} utility class.
 */
public class SessionTest {
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear session before each test
        Session.setCurrentUser(null);
        
        // Create a test user
        testUser = new User();
        testUser.username = "testuser";
        testUser.email = "test@example.com";
        testUser.fullname = "Test User";
        testUser.password = "password123";
        testUser.birthDay = "1990-01-01";
    }

    @AfterEach
    void tearDown() {
        // Clean up session after each test
        Session.setCurrentUser(null);
    }

    @Test
    void testGetCurrentUserInitiallyNull() {
        User currentUser = Session.getCurrentUser();
        assertNull(currentUser);
    }

    @Test
    void testSetAndGetCurrentUser() {
        Session.setCurrentUser(testUser);
        User currentUser = Session.getCurrentUser();
        
        assertNotNull(currentUser);
        assertEquals(testUser, currentUser);
        assertEquals("testuser", currentUser.username);
        assertEquals("test@example.com", currentUser.email);
        assertEquals("Test User", currentUser.fullname);
        assertEquals("password123", currentUser.password);
        assertEquals("1990-01-01", currentUser.birthDay);
    }

    @Test
    void testSetCurrentUserToNull() {
        // First set a user
        Session.setCurrentUser(testUser);
        assertNotNull(Session.getCurrentUser());
        
        // Then set to null
        Session.setCurrentUser(null);
        assertNull(Session.getCurrentUser());
    }

    @Test
    void testUpdateCurrentUser() {
        // Set initial user
        Session.setCurrentUser(testUser);
        assertEquals("testuser", Session.getCurrentUser().username);
        
        // Create a different user
        User newUser = new User();
        newUser.username = "newuser";
        newUser.email = "new@example.com";
        newUser.fullname = "New User";
        newUser.password = "newpassword";
        newUser.birthDay = "1985-05-15";
        
        // Update current user
        Session.setCurrentUser(newUser);
        User currentUser = Session.getCurrentUser();
        
        assertNotNull(currentUser);
        assertEquals(newUser, currentUser);
        assertEquals("newuser", currentUser.username);
        assertEquals("new@example.com", currentUser.email);
        assertEquals("New User", currentUser.fullname);
        assertEquals("newpassword", currentUser.password);
        assertEquals("1985-05-15", currentUser.birthDay);
    }

    @Test
    void testSessionPersistsAcrossMultipleCalls() {
        Session.setCurrentUser(testUser);
        
        // Multiple calls should return the same user
        User user1 = Session.getCurrentUser();
        User user2 = Session.getCurrentUser();
        User user3 = Session.getCurrentUser();
        
        assertEquals(user1, user2);
        assertEquals(user2, user3);
        assertEquals(testUser, user1);
        assertEquals(testUser, user2);
        assertEquals(testUser, user3);
    }

    @Test
    void testSessionIsStaticSingleton() {
        // Test that Session behaves as a singleton across different instances
        Session.setCurrentUser(testUser);
        
        // Even though we're calling static methods, the state should be consistent
        assertEquals(testUser, Session.getCurrentUser());
        
        // Modify user through session reference
        User sessionUser = Session.getCurrentUser();
        sessionUser.fullname = "Modified Name";
        
        // The change should be reflected when getting the user again
        assertEquals("Modified Name", Session.getCurrentUser().fullname);
    }

    @Test
    void testSetCurrentUserWithPartiallyPopulatedUser() {
        User partialUser = new User();
        partialUser.username = "partial";
        // Leave other fields null
        
        Session.setCurrentUser(partialUser);
        User currentUser = Session.getCurrentUser();
        
        assertNotNull(currentUser);
        assertEquals("partial", currentUser.username);
        assertNull(currentUser.email);
        assertNull(currentUser.fullname);
        assertNull(currentUser.password);
        assertNull(currentUser.birthDay);
    }

    @Test
    void testSetCurrentUserWithEmptyFieldsUser() {
        User emptyUser = new User();
        emptyUser.username = "";
        emptyUser.email = "";
        emptyUser.fullname = "";
        emptyUser.password = "";
        emptyUser.birthDay = "";
        
        Session.setCurrentUser(emptyUser);
        User currentUser = Session.getCurrentUser();
        
        assertNotNull(currentUser);
        assertEquals("", currentUser.username);
        assertEquals("", currentUser.email);
        assertEquals("", currentUser.fullname);
        assertEquals("", currentUser.password);
        assertEquals("", currentUser.birthDay);
    }
}
