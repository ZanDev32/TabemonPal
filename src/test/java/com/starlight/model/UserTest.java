package com.starlight.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User} data model.
 */
public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertNull(user.username);
        assertNull(user.email);
        assertNull(user.fullname);
        assertNull(user.password);
        assertNull(user.birthDay);
    }

    @Test
    void testSetAndGetUsername() {
        String testUsername = "testuser123";
        user.username = testUsername;
        assertEquals(testUsername, user.username);
    }

    @Test
    void testSetAndGetEmail() {
        String testEmail = "test@example.com";
        user.email = testEmail;
        assertEquals(testEmail, user.email);
    }

    @Test
    void testSetAndGetFullname() {
        String testFullname = "John Doe";
        user.fullname = testFullname;
        assertEquals(testFullname, user.fullname);
    }

    @Test
    void testSetAndGetPassword() {
        String testPassword = "securePassword123";
        user.password = testPassword;
        assertEquals(testPassword, user.password);
    }

    @Test
    void testSetAndGetBirthDay() {
        String testBirthDay = "1990-05-15";
        user.birthDay = testBirthDay;
        assertEquals(testBirthDay, user.birthDay);
    }

    @Test
    void testUserWithAllFieldsSet() {
        user.username = "johndoe";
        user.email = "john.doe@example.com";
        user.fullname = "John Doe";
        user.password = "myPassword123";
        user.birthDay = "1985-12-25";

        assertEquals("johndoe", user.username);
        assertEquals("john.doe@example.com", user.email);
        assertEquals("John Doe", user.fullname);
        assertEquals("myPassword123", user.password);
        assertEquals("1985-12-25", user.birthDay);
    }

    @Test
    void testUserFieldsCanBeNull() {
        // Ensure that setting fields to null works
        user.username = "test";
        user.email = "test@example.com";
        user.fullname = "Test User";
        user.password = "password";
        user.birthDay = "1990-01-01";

        user.username = null;
        user.email = null;
        user.fullname = null;
        user.password = null;
        user.birthDay = null;

        assertNull(user.username);
        assertNull(user.email);
        assertNull(user.fullname);
        assertNull(user.password);
        assertNull(user.birthDay);
    }

    @Test
    void testUserFieldsCanBeEmpty() {
        user.username = "";
        user.email = "";
        user.fullname = "";
        user.password = "";
        user.birthDay = "";

        assertEquals("", user.username);
        assertEquals("", user.email);
        assertEquals("", user.fullname);
        assertEquals("", user.password);
        assertEquals("", user.birthDay);
    }

    @Test
    void testUserWithSpecialCharacters() {
        user.username = "user_with-special.chars";
        user.email = "test+tag@example-domain.co.uk";
        user.fullname = "José María O'Connor";
        user.password = "P@ssw0rd!#$";
        user.birthDay = "2000-02-29"; // Leap year

        assertEquals("user_with-special.chars", user.username);
        assertEquals("test+tag@example-domain.co.uk", user.email);
        assertEquals("José María O'Connor", user.fullname);
        assertEquals("P@ssw0rd!#$", user.password);
        assertEquals("2000-02-29", user.birthDay);
    }
}
