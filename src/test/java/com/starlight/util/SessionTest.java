package com.starlight.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.model.User;

/**
 * Unit tests for {@link Session} utility class.
 */
class SessionTest {
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear session before each test
        Session.setCurrentUser(null);
        
        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullname("Test User");
        testUser.setPassword("password123");
        testUser.setBirthDay("1990-01-01");
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
        assertEquals("testuser", currentUser.getUsername());
        assertEquals("test@example.com", currentUser.getEmail());
        assertEquals("Test User", currentUser.getFullname());
        assertEquals("password123", currentUser.getPassword());
        assertEquals("1990-01-01", currentUser.getBirthDay());
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
        assertEquals("testuser", Session.getCurrentUser().getUsername());
        
        // Create a different user
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setFullname("New User");
        newUser.setPassword("newpassword");
        newUser.setBirthDay("1985-05-15");
        
        // Update current user
        Session.setCurrentUser(newUser);
        User currentUser = Session.getCurrentUser();
        
        assertNotNull(currentUser);
        assertEquals(newUser, currentUser);
        assertEquals("newuser", currentUser.getUsername());
        assertEquals("new@example.com", currentUser.getEmail());
        assertEquals("New User", currentUser.getFullname());
        assertEquals("newpassword", currentUser.getPassword());
        assertEquals("1985-05-15", currentUser.getBirthDay());
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
        sessionUser.setFullname("Modified Name");
        
        // The change should be reflected when getting the user again
        assertEquals("Modified Name", Session.getCurrentUser().getFullname());
    }

    @Test
    void testSetCurrentUserWithPartiallyPopulatedUser() {
        User partialUser = new User();
        partialUser.setUsername("partial");
        // Leave other fields null
        
        Session.setCurrentUser(partialUser);
        User currentUser = Session.getCurrentUser();
        
        assertNotNull(currentUser);
        assertEquals("partial", currentUser.getUsername());
        assertNull(currentUser.getEmail());
        assertNull(currentUser.getFullname());
        assertNull(currentUser.getPassword());
        assertNull(currentUser.getBirthDay());
    }

    @Test
    void testSetCurrentUserWithEmptyFieldsUser() {
        User emptyUser = new User();
        emptyUser.setUsername("");
        emptyUser.setEmail("");
        emptyUser.setFullname("");
        emptyUser.setPassword("");
        emptyUser.setBirthDay("");
        
        Session.setCurrentUser(emptyUser);
        User currentUser = Session.getCurrentUser();
        
        assertNotNull(currentUser);
    assertEquals("", currentUser.getUsername());
    assertEquals("", currentUser.getEmail());
    assertEquals("", currentUser.getFullname());
    assertEquals("", currentUser.getPassword());
    assertEquals("", currentUser.getBirthDay());
    }
}
