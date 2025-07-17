package com.starlight.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Manages the application's file system operations including directory creation,
 * file copying, and data migration. Provides cross-platform support for storing
 * user data and application files in the user's home directory.
 */
public class FileSystemManager {

    /** Application data directory path */
    private static final String APP_DATA_DIR = System.getProperty("user.home") + File.separator + ".tabemonpal";
    
    /** User data directory for storing user images */
    private static final String USER_DATA_DIR = APP_DATA_DIR + File.separator + "UserData";
    
    /** Database directory for storing XML files */
    private static final String DATABASE_DIR = APP_DATA_DIR + File.separator + "Database";

    /**
     * Initializes the application data directory structure.
     * Creates .tabemonpal directory with UserData and Database subdirectories.
     * This method is cross-platform compatible and works when running as JAR.
     */
    public static void initializeAppDataDirectory() {
        try {
            // Create main app directory
            Path appDir = Paths.get(APP_DATA_DIR);
            Files.createDirectories(appDir);
            
            // Create UserData directory for user images
            Path userDataDir = Paths.get(USER_DATA_DIR);
            Files.createDirectories(userDataDir);
            
            // Create Database directory for XML files
            Path databaseDir = Paths.get(DATABASE_DIR);
            Files.createDirectories(databaseDir);
            
            System.out.println("App data directory initialized at: " + APP_DATA_DIR);
            
        } catch (IOException e) {
            System.err.println("Failed to initialize app data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the path to the application data directory.
     * @return absolute path to .tabemonpal directory
     */
    public static String getAppDataDirectory() {
        return APP_DATA_DIR;
    }

    /**
     * Gets the path to the user data directory for storing user images.
     * @return absolute path to UserData directory
     */
    public static String getUserDataDirectory() {
        return USER_DATA_DIR;
    }

    /**
     * Gets the path to the database directory for storing XML files.
     * @return absolute path to Database directory
     */
    public static String getDatabaseDirectory() {
        return DATABASE_DIR;
    }

    /**
     * Creates a user-specific directory for storing their images.
     * @param username the username to create directory for
     * @return the created user directory path
     */
    public static String createUserImageDirectory(String username) {
        try {
            Path userDir = Paths.get(USER_DATA_DIR, username);
            Files.createDirectories(userDir);
            return userDir.toString();
        } catch (IOException e) {
            System.err.println("Failed to create user directory for " + username + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Copies a file from source to the user's image directory and returns the new path.
     * @param sourceFile the source file to copy
     * @param username the username whose directory to copy to
     * @param newFileName optional new filename, or null to keep original
     * @return the path to the copied file in the user directory
     */
    public static String copyFileToUserDirectory(File sourceFile, String username, String newFileName) {
        try {
            // Ensure user directory exists
            String userDirPath = createUserImageDirectory(username);
            if (userDirPath == null) {
                return null;
            }
            
            // Determine target filename
            String targetFileName = newFileName != null ? newFileName : sourceFile.getName();
            Path targetPath = Paths.get(userDirPath, targetFileName);
            
            // Copy file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File copied to: " + targetPath.toString());
            return targetPath.toString();
            
        } catch (IOException e) {
            System.err.println("Failed to copy file to user directory: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Copies a file from source to the user's image directory with a unique filename.
     * @param sourceFile the source file to copy
     * @param username the username whose directory to copy to
     * @return the path to the copied file in the user directory
     */
    public static String copyFileToUserDirectoryWithUniqueFilename(File sourceFile, String username) {
        try {
            // Get file extension
            String originalName = sourceFile.getName();
            String extension = "";
            int lastDot = originalName.lastIndexOf('.');
            if (lastDot > 0) {
                extension = originalName.substring(lastDot);
            }
            
            // Generate unique filename using timestamp and a counter to ensure uniqueness
            String userDirPath = createUserImageDirectory(username);
            if (userDirPath == null) {
                return null;
            }
            
            String uniqueFileName;
            Path targetPath;
            int counter = 0;
            
            do {
                String timestamp = String.valueOf(System.currentTimeMillis());
                uniqueFileName = timestamp + (counter > 0 ? "_" + counter : "") + extension;
                targetPath = Paths.get(userDirPath, uniqueFileName);
                counter++;
            } while (Files.exists(targetPath));
            
            return copyFileToUserDirectory(sourceFile, username, uniqueFileName);
            
        } catch (Exception e) {
            System.err.println("Failed to copy file with unique filename: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if the application data directory is accessible and writable.
     * @return true if the directory is accessible and writable
     */
    public static boolean isAppDataDirectoryAccessible() {
        try {
            Path appDir = Paths.get(APP_DATA_DIR);
            
            // Check if directory exists and is writable
            if (!Files.exists(appDir)) {
                return false;
            }
            
            if (!Files.isWritable(appDir)) {
                return false;
            }
            
            // Try creating a test file
            Path testFile = appDir.resolve("test_access.tmp");
            Files.write(testFile, "test".getBytes());
            Files.deleteIfExists(testFile);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("App data directory is not accessible: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resolves an image path to an absolute path, handling various path formats
     * @param imagePath The image path from XML (could be relative, absolute, or resource path)
     * @return Resolved absolute path or null if not found
     */
    public static Path resolveImagePath(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }
        
        // Handle paths starting with "src/main/resources/" - these are project resource paths
        if (imagePath.startsWith("src/main/resources/")) {
            Path projectResourcesPath = Paths.get(imagePath);
            if (Files.exists(projectResourcesPath)) {
                return projectResourcesPath;
            }
            
            // Try to convert to classpath resource path and resolve
            String resourcePath = imagePath.substring("src/main/resources".length());
            URL resource = FileSystemManager.class.getResource(resourcePath);
            if (resource != null) {
                try {
                    return Paths.get(resource.toURI());
                } catch (Exception e) {
                    // Resource exists but cannot convert to path (might be in JAR)
                    // Return a special marker indicating it's a valid resource
                    return Paths.get("resource:" + resourcePath);
                }
            }
            
            System.out.println("Warning: Could not resolve image path: " + imagePath);
            return null;
        }
        
        // Handle absolute paths (including user directory paths)
        Path absolutePath = Paths.get(imagePath);
        if (absolutePath.isAbsolute()) {
            if (Files.exists(absolutePath)) {
                return absolutePath;
            } else {
                System.out.println("Warning: Could not resolve absolute path: " + imagePath);
                return null;
            }
        }
        
        // Handle resource paths (paths starting with /)
        if (imagePath.startsWith("/")) {
            // Try to resolve as resource from classpath
            URL resource = FileSystemManager.class.getResource(imagePath);
            if (resource != null) {
                try {
                    return Paths.get(resource.toURI());
                } catch (Exception e) {
                    // Resource exists but cannot convert to path (might be in JAR)
                    // Return a special marker indicating it's a valid resource
                    return Paths.get("resource:" + imagePath);
                }
            }
            
            // If not found as resource, try relative to project src/main/resources
            String resourcePath = imagePath.substring(1);
            Path projectResourcesPath = Paths.get("src/main/resources").resolve(resourcePath);
            if (Files.exists(projectResourcesPath)) {
                return projectResourcesPath;
            }
            
            System.out.println("Warning: Could not resolve resource path: " + imagePath);
            return null;
        }
        
        // Handle relative paths - try different base directories
        
        // Try relative to user data directory
        Path userDataPath = Paths.get(getUserDataDirectory()).resolve(imagePath);
        if (Files.exists(userDataPath)) {
            return userDataPath;
        }
        
        // Try relative to user images directory (UserData subdirectory)
        Path userImagesPath = Paths.get(getUserDataDirectory()).resolve(imagePath);
        if (Files.exists(userImagesPath)) {
            return userImagesPath;
        }
        
        // Try relative to project resources
        Path projectResourcesPath = Paths.get("src/main/resources").resolve(imagePath);
        if (Files.exists(projectResourcesPath)) {
            return projectResourcesPath;
        }
        
        System.out.println("Warning: Could not resolve image path: " + imagePath);
        return null;
    }

    /**
     * Gets a fallback image path for when the original image cannot be found.
     * @param imageType type of image (e.g., "profile", "post", "default")
     * @return path to a fallback image, or null if no fallback available
     */
    public static String getFallbackImagePath(String imageType) {
        String[] fallbackPaths = {
            "src/main/resources/com/starlight/images/dummy/profileman.jpg",
            "src/main/resources/com/starlight/images/dummy/2.png",
            "src/main/resources/com/starlight/images/dummy/recent_1.png",
            "src/main/resources/com/starlight/images/dummy/image_2.jpg"
        };
        
        for (String fallbackPath : fallbackPaths) {
            Path resolvedPath = resolveImagePath(fallbackPath);
            if (resolvedPath != null) {
                return resolvedPath.toString();
            }
        }
        
        return null;
    }

    /**
     * Resolves an image path with automatic fallback to default images.
     * @param imagePath the original image path
     * @param imageType the type of image for fallback selection
     * @return a valid image path or fallback path
     */
    public static String resolveImagePathWithFallback(String imagePath, String imageType) {
        Path resolvedPath = resolveImagePath(imagePath);
        if (resolvedPath != null) {
            return resolvedPath.toString();
        }
        
        String fallbackPath = getFallbackImagePath(imageType);
        if (fallbackPath != null) {
            System.out.println("Using fallback image for: " + imagePath);
            return fallbackPath;
        }
        
        System.err.println("No image found for path: " + imagePath);
        return null;
    }
}
