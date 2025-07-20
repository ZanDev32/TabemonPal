package com.starlight.models;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.model.User;
import com.starlight.repository.UserDataRepository;

/**
 * Unit tests for {@link UserDataRepository}.
 */
public class UserDataRepositoryTest {
    private Path tempFile;
    private Path tempDummyFile;
    private UserDataRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("users", ".xml");
        tempDummyFile = Files.createTempFile("usersDummy", ".xml");
        repository = new UserDataRepository(tempFile.toString());
        
        // Create some dummy data for testing
        createDummyData();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDummyFile);
    }

    private void createDummyData() throws IOException {
        String dummyXml = "<users>\n" +
                "  <user>\n" +
                "    <username>dummy1</username>\n" +
                "    <email>dummy1@example.com</email>\n" +
                "    <fullname>Dummy User 1</fullname>\n" +
                "    <password>password1</password>\n" +
                "    <birthDay>1990-01-01</birthDay>\n" +
                "  </user>\n" +
                "  <user>\n" +
                "    <username>dummy2</username>\n" +
                "    <email>dummy2@example.com</email>\n" +
                "    <fullname>Dummy User 2</fullname>\n" +
                "    <password>password2</password>\n" +
                "    <birthDay>1985-05-15</birthDay>\n" +
                "  </user>\n" +
                "</users>";
        Files.writeString(tempDummyFile, dummyXml);
    }

    @Test
    void testSaveAndLoadUsers() {
        List<User> users = new ArrayList<>();
        
        User user1 = new User();
        user1.username = "testuser1";
        user1.email = "test1@example.com";
        user1.fullname = "Test User 1";
        user1.password = "password123";
        user1.birthDay = "1995-03-10";
        users.add(user1);

        User user2 = new User();
        user2.username = "testuser2";
        user2.email = "test2@example.com";
        user2.fullname = "Test User 2";
        user2.password = "password456";
        user2.birthDay = "1992-07-20";
        users.add(user2);

        repository.saveUsers(users);
        assertTrue(Files.exists(tempFile) && tempFile.toFile().length() > 0);

        List<User> loaded = repository.loadUsers(false); // Don't include dummy data
        assertEquals(2, loaded.size());
        
        User loadedUser1 = loaded.get(0);
        assertEquals(user1.username, loadedUser1.username);
        assertEquals(user1.email, loadedUser1.email);
        assertEquals(user1.fullname, loadedUser1.fullname);
        assertEquals(user1.password, loadedUser1.password);
        assertEquals(user1.birthDay, loadedUser1.birthDay);
        
        User loadedUser2 = loaded.get(1);
        assertEquals(user2.username, loadedUser2.username);
        assertEquals(user2.email, loadedUser2.email);
        assertEquals(user2.fullname, loadedUser2.fullname);
        assertEquals(user2.password, loadedUser2.password);
        assertEquals(user2.birthDay, loadedUser2.birthDay);
    }

    @Test
    void testLoadUsersWithEmptyFile() {
        // Ensure the file is empty
        try {
            Files.writeString(tempFile, "");
        } catch (IOException e) {
            fail("Failed to create empty file: " + e.getMessage());
        }
        
        List<User> users = repository.loadUsers(false);
        assertNotNull(users);
        // When the main file is empty, the repository falls back to dummy data
        // even when includeDummy is false (this is the intended behavior)
        assertEquals(4, users.size()); // Should load dummy users as fallback
    }

    @Test
    void testLoadUsersIncludingDummy() {
        // Since we're using a custom path, this won't actually load dummy data
        // but we can test the method call
        List<User> users = repository.loadUsers(true);
        assertNotNull(users);
    }

    @Test
    void testDeleteUser() {
        // First, save some users
        List<User> users = new ArrayList<>();
        
        User user1 = new User();
        user1.username = "userToDelete";
        user1.email = "delete@example.com";
        user1.fullname = "Delete Me";
        user1.password = "password";
        user1.birthDay = "1990-01-01";
        users.add(user1);

        User user2 = new User();
        user2.username = "userToKeep";
        user2.email = "keep@example.com";
        user2.fullname = "Keep Me";
        user2.password = "password";
        user2.birthDay = "1990-01-01";
        users.add(user2);

        repository.saveUsers(users);

        // Delete one user
        boolean deleted = repository.deleteUser("userToDelete");
        assertTrue(deleted);

        // Verify only one user remains
        List<User> remaining = repository.loadUsers(false);
        assertEquals(1, remaining.size());
        assertEquals("userToKeep", remaining.get(0).username);
    }

    @Test
    void testDeleteNonExistentUser() {
        // Save one user
        List<User> users = new ArrayList<>();
        User user = new User();
        user.username = "existingUser";
        user.email = "existing@example.com";
        users.add(user);
        repository.saveUsers(users);

        // Try to delete non-existent user
        boolean deleted = repository.deleteUser("nonExistentUser");
        assertFalse(deleted);

        // Verify original user still exists
        List<User> remaining = repository.loadUsers(false);
        assertEquals(1, remaining.size());
        assertEquals("existingUser", remaining.get(0).username);
    }

    @Test
    void testSaveUsersThrowsExceptionOnInvalidPath() {
        UserDataRepository invalidRepo = new UserDataRepository("/invalid/path/that/does/not/exist.xml");
        List<User> users = new ArrayList<>();
        User user = new User();
        user.username = "test";
        users.add(user);

        assertThrows(RuntimeException.class, () -> invalidRepo.saveUsers(users));
    }

    @Test
    void testDefaultConstructor() {
        // Test that we can create a repository with default constructor
        UserDataRepository defaultRepo = new UserDataRepository();
        assertNotNull(defaultRepo);
        
        // Should be able to call methods without throwing exceptions
        List<User> users = defaultRepo.loadUsers(false);
        assertNotNull(users);
    }
}
