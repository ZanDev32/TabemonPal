package com.starlight.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.starlight.util.FileSystemManager;
import java.nio.file.Path;

/**
 * Test class for ConsultController avatar path resolution.
 */
public class ConsultControllerTest {
    
    @Test
    public void testAvatarPathResolution() {
        // Test that the avatar paths can be resolved by FileSystemManager
        String botAvatarPath = "src/main/resources/com/starlight/images/dummy/profilewoman.jpg";
        String userAvatarPath = "src/main/resources/com/starlight/images/dummy/profilewoman.jpg";
        
        // Test bot avatar path resolution
        Path resolvedBotPath = FileSystemManager.resolveImagePath(botAvatarPath);
        assertNotNull(resolvedBotPath, "Bot avatar path should be resolvable");
        
        // Test user avatar path resolution
        Path resolvedUserPath = FileSystemManager.resolveImagePath(userAvatarPath);
        assertNotNull(resolvedUserPath, "User avatar path should be resolvable");
        
        // Test fallback path resolution
        String fallbackPath = "src/main/resources/com/starlight/images/missing.png";
        Path resolvedFallback = FileSystemManager.resolveImagePath(fallbackPath);
        assertNotNull(resolvedFallback, "Fallback image path should be resolvable");
    }
    
    @Test
    public void testResourcePathFormat() {
        // Test that our path format works with the FileSystemManager
        String resourcePath = "src/main/resources/com/starlight/images/dummy/profilewoman.jpg";
        
        // Should not be null and should not generate warnings
        Path resolved = FileSystemManager.resolveImagePath(resourcePath);
        assertNotNull(resolved, "Resource path should be resolvable");
        
        // Check that it's either a valid file path or a resource marker
        assertTrue(resolved.toString().contains("profilewoman.jpg") || 
                  resolved.toString().startsWith("resource:"), 
                  "Resolved path should contain the image name or be a resource marker");
    }
}
