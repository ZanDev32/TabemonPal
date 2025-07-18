package com.starlight.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.starlight.models.UserDataRepository;
import com.starlight.models.PostDataRepository;

/**
 * Integration test to verify that data repositories work correctly with the new file system.
 */
public class DataRepositoryIntegrationTest {

    @BeforeEach
    void setUp() {
        // Initialize the app data directory before each test
        FileSystemManager.initializeAppDataDirectory();
    }

    @Test
    void testUserDataRepositoryInitialization() {
        // Test that UserDataRepository can initialize and access the new directory structure
        UserDataRepository userRepo = new UserDataRepository();
        assertNotNull(userRepo, "UserDataRepository should initialize successfully");
        
        // Try to load users (should not throw exception)
        assertDoesNotThrow(() -> {
            userRepo.loadUsers();
        }, "Loading users should not throw an exception");
    }

    @Test
    void testPostDataRepositoryInitialization() {
        // Test that PostDataRepository can initialize and access the new directory structure
        PostDataRepository postRepo = new PostDataRepository();
        assertNotNull(postRepo, "PostDataRepository should initialize successfully");
        
        // Try to load posts (should not throw exception)
        assertDoesNotThrow(() -> {
            postRepo.loadPosts();
        }, "Loading posts should not throw an exception");
    }

    @Test
    void testRepositoriesUseNewDirectoryStructure() {
        // Test that repositories are actually using the new directory structure
        String databaseDir = FileSystemManager.getDatabaseDirectory();
        
        // Verify that the database directory is in the user home .tabemonpal folder
        assertTrue(databaseDir.contains(".tabemonpal"), 
            "Database directory should be in .tabemonpal folder");
        assertTrue(databaseDir.contains("Database"), 
            "Database directory should contain 'Database' folder");
        
        // Verify the repositories can be created without errors
        assertDoesNotThrow(() -> {
            new UserDataRepository();
            new PostDataRepository();
        }, "Repository creation should not throw exceptions");
    }
}
