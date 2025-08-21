package com.starlight.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User} data model.
 */
class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
    assertNull(user.getUsername());
    assertNull(user.getEmail());
    assertNull(user.getFullname());
    assertNull(user.getPassword());
    assertNull(user.getBirthDay());
    }

    @Test
    void testSetAndGetUsername() {
        String testUsername = "testuser123";
    user.setUsername(testUsername);
    assertEquals(testUsername, user.getUsername());
    }

    @Test
    void testSetAndGetEmail() {
        String testEmail = "test@example.com";
    user.setEmail(testEmail);
    assertEquals(testEmail, user.getEmail());
    }

    @Test
    void testSetAndGetFullname() {
        String testFullname = "John Doe";
    user.setFullname(testFullname);
    assertEquals(testFullname, user.getFullname());
    }

    @Test
    void testSetAndGetPassword() {
        String testPassword = "securePassword123";
    user.setPassword(testPassword);
    assertEquals(testPassword, user.getPassword());
    }

    @Test
    void testSetAndGetBirthDay() {
        String testBirthDay = "1990-05-15";
    user.setBirthDay(testBirthDay);
    assertEquals(testBirthDay, user.getBirthDay());
    }

    @Test
    void testUserWithAllFieldsSet() {
    user.setUsername("johndoe");
    user.setEmail("john.doe@example.com");
    user.setFullname("John Doe");
    user.setPassword("myPassword123");
    user.setBirthDay("1985-12-25");

    assertEquals("johndoe", user.getUsername());
    assertEquals("john.doe@example.com", user.getEmail());
    assertEquals("John Doe", user.getFullname());
    assertEquals("myPassword123", user.getPassword());
    assertEquals("1985-12-25", user.getBirthDay());
    }

    @Test
    void testUserFieldsCanBeNull() {
        // Ensure that setting fields to null works
    user.setUsername("test");
    user.setEmail("test@example.com");
    user.setFullname("Test User");
    user.setPassword("password");
    user.setBirthDay("1990-01-01");

    user.setUsername(null);
    user.setEmail(null);
    user.setFullname(null);
    user.setPassword(null);
    user.setBirthDay(null);

    assertNull(user.getUsername());
    assertNull(user.getEmail());
    assertNull(user.getFullname());
    assertNull(user.getPassword());
    assertNull(user.getBirthDay());
    }

    @Test
    void testUserFieldsCanBeEmpty() {
    user.setUsername("");
    user.setEmail("");
    user.setFullname("");
    user.setPassword("");
    user.setBirthDay("");

    assertEquals("", user.getUsername());
    assertEquals("", user.getEmail());
    assertEquals("", user.getFullname());
    assertEquals("", user.getPassword());
    assertEquals("", user.getBirthDay());
    }

    @Test
    void testUserWithSpecialCharacters() {
    user.setUsername("user_with-special.chars");
    user.setEmail("test+tag@example-domain.co.uk");
    user.setFullname("José María O'Connor");
    user.setPassword("P@ssw0rd!#$");
    user.setBirthDay("2000-02-29"); // Leap year

    assertEquals("user_with-special.chars", user.getUsername());
    assertEquals("test+tag@example-domain.co.uk", user.getEmail());
    assertEquals("José María O'Connor", user.getFullname());
    assertEquals("P@ssw0rd!#$", user.getPassword());
    assertEquals("2000-02-29", user.getBirthDay());
    }
}
