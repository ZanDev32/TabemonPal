package com.starlight.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the application's file system operations including directory creation,
 * file copying, and data migration. Provides cross-platform support for storing
 * user data and application files in the user's home directory.
 */
public class FileSystemManager {

    private FileSystemManager() {}

    private static final Logger LOGGER = Logger.getLogger(FileSystemManager.class.getName());

    /** Application data directory path */
    private static final String APP_DATA_DIR = System.getProperty("user.home") + File.separator + ".tabemonpal";
    
    /** User data directory for storing user images */
    private static final String USER_DATA_DIR = APP_DATA_DIR + File.separator + "UserData";
    
    /** Database directory for storing XML files */
    private static final String DATABASE_DIR = APP_DATA_DIR + File.separator + "Database";

    /**
     * Initializes the application data directory structure.
     * Creates .tabemonpal directory with UserData and Database subdirectories.
     * Also creates .SECRET_KEY.xml file with default configuration.
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
            
            // Create .SECRET_KEY.xml file if it doesn't exist
            Path secretKeyPath = databaseDir.resolve(".SECRET_KEY.xml");
            if (!Files.exists(secretKeyPath)) {
                String secretKeyContent = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <config>
                            <openai-key>PASTE-YOUR-SECRET-KEY-HERE</openai-key>
                        </config>
                        """.stripIndent();
                Files.write(secretKeyPath, secretKeyContent.getBytes(StandardCharsets.UTF_8));
                LOGGER.log(Level.INFO, "Paste your secret key at: {0}", secretKeyPath);
            }

            LOGGER.log(Level.INFO, () -> "App data directory initialized at: " + APP_DATA_DIR);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize app data directory: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "Initialization stacktrace", e);
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
            LOGGER.log(Level.SEVERE, "Failed to create user directory for {0}: {1}", new Object[]{username, e.getMessage()});
            LOGGER.log(Level.SEVERE, "Create user directory stacktrace", e);
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
            
            LOGGER.log(Level.INFO, () -> "File copied to: " + targetPath);
            return targetPath.toString();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to copy file to user directory: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "Copy stacktrace", e);
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
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Failed to copy file with unique filename: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "Unique filename copy stacktrace", e);
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
            Files.write(testFile, "test".getBytes(StandardCharsets.UTF_8));
            Files.deleteIfExists(testFile);
            
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "App data directory is not accessible: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "Accessibility stacktrace", e);
            return false;
        }
    }
    // -------- Image path resolution (refactored to reduce complexity) --------
    private static final String PROJECT_RESOURCES = "src/main/resources";
    private static final String PROJECT_RESOURCES_PREFIX = PROJECT_RESOURCES + "/";
    private static final String RESOURCE_MARKER = "resource:";

    /**
     * Resolves an image path to an absolute path, handling various path formats.
     */
    public static Path resolveImagePath(String imagePath) {
        if (isNullOrBlank(imagePath)) return null;
        if (isProjectResourcePath(imagePath)) return resolveProjectResourcePath(imagePath);
        if (isClasspathResourcePath(imagePath)) return resolveClasspathResourcePath(imagePath);
        Path absolute = Paths.get(imagePath);
        if (absolute.isAbsolute()) return validateAbsolute(absolute, imagePath);
        return resolveRelativeImagePath(imagePath);
    }

    private static boolean isNullOrBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static boolean isProjectResourcePath(String p) { return p.startsWith(PROJECT_RESOURCES_PREFIX); }
    private static boolean isClasspathResourcePath(String p) { return p.startsWith("/") && p.contains("com/starlight"); }

    private static Path resolveProjectResourcePath(String imagePath) {
        Path projectResourcesPath = Paths.get(imagePath);
        if (Files.exists(projectResourcesPath)) return projectResourcesPath;
        String resourcePath = imagePath.substring(PROJECT_RESOURCES.length());
        URL resource = FileSystemManager.class.getResource(resourcePath);
        if (resource != null) {
            try { return Paths.get(resource.toURI()); } catch (Exception e) { return Paths.get(RESOURCE_MARKER + resourcePath); }
        }
        LOGGER.log(Level.WARNING, () -> "Could not resolve project resource image path: " + imagePath);
        return null;
    }

    private static Path resolveClasspathResourcePath(String imagePath) {
        URL resource = FileSystemManager.class.getResource(imagePath);
        if (resource != null) {
            try { return Paths.get(resource.toURI()); } catch (Exception e) { return Paths.get(RESOURCE_MARKER + imagePath); }
        }
        String resourcePath = imagePath.substring(1);
        Path projectResourcesPath = Paths.get(PROJECT_RESOURCES).resolve(resourcePath);
        if (Files.exists(projectResourcesPath)) return projectResourcesPath;
        LOGGER.log(Level.WARNING, () -> "Could not resolve classpath resource path: " + imagePath);
        return null;
    }

    private static Path validateAbsolute(Path absolute, String original) {
        if (Files.exists(absolute)) return absolute;
        LOGGER.log(Level.WARNING, () -> "Could not resolve absolute path: " + original);
        return null;
    }

    private static Path resolveRelativeImagePath(String imagePath) {
        Path userDataPath = Paths.get(getUserDataDirectory()).resolve(imagePath);
        if (Files.exists(userDataPath)) return userDataPath;
        Path projectResourcesPath = Paths.get(PROJECT_RESOURCES).resolve(imagePath);
        if (Files.exists(projectResourcesPath)) return projectResourcesPath;
        LOGGER.log(Level.WARNING, () -> "Could not resolve relative image path: " + imagePath);
        return null;
    }

    /**
     * Gets a fallback image path for when the original image cannot be found.
     * @param imageType type of image (e.g., "profile", "post", "default")
     * @return path to a fallback image, or null if no fallback available
     */
    private static final String DEFAULT_MISSING_IMAGE = PROJECT_RESOURCES + "/com/starlight/images/missing.png";

    public static String getFallbackImagePath(String imageType) {
        // Potential hook for different fallback images per type in future.
        String fallbackPath = DEFAULT_MISSING_IMAGE;
        Path resolvedPath = resolveImagePath(fallbackPath);
        if (resolvedPath != null) return resolvedPath.toString();
        LOGGER.log(Level.WARNING, () -> "No fallback image available for type: " + imageType);
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
            LOGGER.log(Level.INFO, () -> "Using fallback image for: " + imagePath);
            return fallbackPath;
        }
        LOGGER.log(Level.SEVERE, () -> "No image found for path: " + imagePath);
        return null;
    }
}
