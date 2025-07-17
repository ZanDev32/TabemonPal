package com.starlight.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for verifying the application's ability to access and use
 * the file system for storing user data and application files.
 */
public class FileSystemAccessTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_FILENAME = "test_image.png";
    
    @BeforeEach
    void setUp() {
        // Initialize the app data directory before each test
        FileSystemManager.initializeAppDataDirectory();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up test files after each test
        try {
            Path testUserDir = Paths.get(FileSystemManager.getUserDataDirectory(), TEST_USERNAME);
            if (Files.exists(testUserDir)) {
                Files.walk(testUserDir)
                    .sorted((a, b) -> -a.compareTo(b)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    });
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testAppDataDirectoryIsAccessible() {
        // Test that the app data directory is accessible and writable
        assertTrue(FileSystemManager.isAppDataDirectoryAccessible(), 
            "App data directory should be accessible and writable");
    }

    @Test
    void testDirectoryCreation() {
        // Test that required directories are created
        assertTrue(Files.exists(Paths.get(FileSystemManager.getAppDataDirectory())), 
            "Main app data directory should exist");
        assertTrue(Files.exists(Paths.get(FileSystemManager.getUserDataDirectory())), 
            "User data directory should exist");
        assertTrue(Files.exists(Paths.get(FileSystemManager.getDatabaseDirectory())), 
            "Database directory should exist");
    }

    @Test
    void testUserImageDirectoryCreation() {
        // Test creating a user-specific directory
        String userDir = FileSystemManager.createUserImageDirectory(TEST_USERNAME);
        
        assertNotNull(userDir, "User directory creation should return a path");
        assertTrue(Files.exists(Paths.get(userDir)), 
            "User directory should exist after creation");
        assertEquals(Paths.get(FileSystemManager.getUserDataDirectory(), TEST_USERNAME).toString(), userDir,
            "User directory should be in the correct location");
    }

    @Test
    void testFileCopyToUserDirectory() throws Exception {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".png");
        Files.write(tempFile, "test image data".getBytes());
        
        try {
            // Test copying file to user directory
            String copiedFilePath = FileSystemManager.copyFileToUserDirectory(
                tempFile.toFile(), TEST_USERNAME, TEST_FILENAME);
            
            assertNotNull(copiedFilePath, "File copy should return a path");
            assertTrue(Files.exists(Paths.get(copiedFilePath)), 
                "Copied file should exist");
            
            // Verify file contents
            byte[] originalContent = Files.readAllBytes(tempFile);
            byte[] copiedContent = Files.readAllBytes(Paths.get(copiedFilePath));
            assertArrayEquals(originalContent, copiedContent, 
                "Copied file should have same content as original");
            
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testFileCopyWithUniqueFilename() throws Exception {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".jpg");
        Files.write(tempFile, "test image data".getBytes());
        
        try {
            // Test copying file with unique filename
            String copiedFilePath1 = FileSystemManager.copyFileToUserDirectoryWithUniqueFilename(
                tempFile.toFile(), TEST_USERNAME);
            String copiedFilePath2 = FileSystemManager.copyFileToUserDirectoryWithUniqueFilename(
                tempFile.toFile(), TEST_USERNAME);
            
            assertNotNull(copiedFilePath1, "First file copy should return a path");
            assertNotNull(copiedFilePath2, "Second file copy should return a path");
            assertNotEquals(copiedFilePath1, copiedFilePath2, 
                "Two copies should have different filenames");
            
            assertTrue(Files.exists(Paths.get(copiedFilePath1)), 
                "First copied file should exist");
            assertTrue(Files.exists(Paths.get(copiedFilePath2)), 
                "Second copied file should exist");
            
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testCrossplatformPath() {
        // Test that paths work correctly on different platforms
        String appDataDir = FileSystemManager.getAppDataDirectory();
        String userDataDir = FileSystemManager.getUserDataDirectory();
        String databaseDir = FileSystemManager.getDatabaseDirectory();
        
        // Verify paths contain the correct separators for the current platform
        assertTrue(appDataDir.contains(File.separator), 
            "App data directory should use correct file separator");
        assertTrue(userDataDir.startsWith(appDataDir), 
            "User data directory should be under app data directory");
        assertTrue(databaseDir.startsWith(appDataDir), 
            "Database directory should be under app data directory");
    }

    @Test
    void testDirectoryPermissions() throws Exception {
        // Test that directories have correct permissions
        Path appDir = Paths.get(FileSystemManager.getAppDataDirectory());
        Path userDir = Paths.get(FileSystemManager.getUserDataDirectory());
        Path dbDir = Paths.get(FileSystemManager.getDatabaseDirectory());
        
        assertTrue(Files.isWritable(appDir), "App directory should be writable");
        assertTrue(Files.isWritable(userDir), "User data directory should be writable");
        assertTrue(Files.isWritable(dbDir), "Database directory should be writable");
        
        assertTrue(Files.isReadable(appDir), "App directory should be readable");
        assertTrue(Files.isReadable(userDir), "User data directory should be readable");
        assertTrue(Files.isReadable(dbDir), "Database directory should be readable");
    }

    @Test
    void testFileSystemAccessFromJar() {
        // Test that the file system access works the same way when running from JAR
        // This test verifies that we're not using any development-only file access methods
        
        // The app data directory should be in user home, not in project directory
        String appDataDir = FileSystemManager.getAppDataDirectory();
        String userHome = System.getProperty("user.home");
        
        assertTrue(appDataDir.startsWith(userHome), 
            "App data directory should be in user home directory");
        assertFalse(appDataDir.contains("src"), 
            "App data directory should not be in project source directory");
        assertFalse(appDataDir.contains("target"), 
            "App data directory should not be in project target directory");
    }
}
