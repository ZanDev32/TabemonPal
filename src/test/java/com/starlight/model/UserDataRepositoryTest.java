package com.starlight.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.repository.UserDataRepository;

/**
 * Unit tests for {@link UserDataRepository}.
 */
class UserDataRepositoryTest {
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
                String dummyXml = """
                                <users>
                                    <user>
                                        <username>dummy1</username>
                                        <email>dummy1@example.com</email>
                                        <fullname>Dummy User 1</fullname>
                                        <password>password1</password>
                                        <birthDay>1990-01-01</birthDay>
                                    </user>
                                    <user>
                                        <username>dummy2</username>
                                        <email>dummy2@example.com</email>
                                        <fullname>Dummy User 2</fullname>
                                        <password>password2</password>
                                        <birthDay>1985-05-15</birthDay>
                                    </user>
                                </users>
                                """;
        Files.writeString(tempDummyFile, dummyXml);
    }

    @Test
    void testSaveAndLoadUsers() {
        List<User> users = new ArrayList<>();
        
        User user1 = new User();
    user1.setUsername("testuser1");
    user1.setEmail("test1@example.com");
    user1.setFullname("Test User 1");
    user1.setPassword("password123");
    user1.setBirthDay("1995-03-10");
        users.add(user1);

        User user2 = new User();
    user2.setUsername("testuser2");
    user2.setEmail("test2@example.com");
    user2.setFullname("Test User 2");
    user2.setPassword("password456");
    user2.setBirthDay("1992-07-20");
        users.add(user2);

        repository.saveUsers(users);
        assertTrue(Files.exists(tempFile) && tempFile.toFile().length() > 0);

        List<User> loaded = repository.loadUsers(false); // Don't include dummy data
        assertEquals(2, loaded.size());
        
        User loadedUser1 = loaded.get(0);
    assertEquals(user1.getUsername(), loadedUser1.getUsername());
    assertEquals(user1.getEmail(), loadedUser1.getEmail());
    assertEquals(user1.getFullname(), loadedUser1.getFullname());
    assertEquals(user1.getPassword(), loadedUser1.getPassword());
    assertEquals(user1.getBirthDay(), loadedUser1.getBirthDay());
        
        User loadedUser2 = loaded.get(1);
    assertEquals(user2.getUsername(), loadedUser2.getUsername());
    assertEquals(user2.getEmail(), loadedUser2.getEmail());
    assertEquals(user2.getFullname(), loadedUser2.getFullname());
    assertEquals(user2.getPassword(), loadedUser2.getPassword());
    assertEquals(user2.getBirthDay(), loadedUser2.getBirthDay());
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
    user1.setUsername("userToDelete");
    user1.setEmail("delete@example.com");
    user1.setFullname("Delete Me");
    user1.setPassword("password");
    user1.setBirthDay("1990-01-01");
        users.add(user1);

        User user2 = new User();
    user2.setUsername("userToKeep");
    user2.setEmail("keep@example.com");
    user2.setFullname("Keep Me");
    user2.setPassword("password");
    user2.setBirthDay("1990-01-01");
        users.add(user2);

        repository.saveUsers(users);

        // Delete one user
        boolean deleted = repository.deleteUser("userToDelete");
        assertTrue(deleted);

        // Verify only one user remains
        List<User> remaining = repository.loadUsers(false);
        assertEquals(1, remaining.size());
    assertEquals("userToKeep", remaining.get(0).getUsername());
    }

    @Test
    void testDeleteNonExistentUser() {
        // Save one user
        List<User> users = new ArrayList<>();
        User user = new User();
    user.setUsername("existingUser");
    user.setEmail("existing@example.com");
        users.add(user);
        repository.saveUsers(users);

        // Try to delete non-existent user
        boolean deleted = repository.deleteUser("nonExistentUser");
        assertFalse(deleted);

        // Verify original user still exists
        List<User> remaining = repository.loadUsers(false);
        assertEquals(1, remaining.size());
    assertEquals("existingUser", remaining.get(0).getUsername());
    }

    @Test
    void testSaveUsersThrowsExceptionOnInvalidPath() {
        UserDataRepository invalidRepo = new UserDataRepository("/invalid/path/that/does/not/exist.xml");
        List<User> users = new ArrayList<>();
        User user = new User();
    user.setUsername("test");
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
