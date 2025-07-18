package com.starlight.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * Utility class for common image operations in the application.
 * Contains methods for loading and scaling images with proper fallback handling.
 */
public class ImageUtils {
    
    /**
     * Resizes and centers the given {@link ImageView} to fit inside the specified frame size
     * while preserving aspect ratio and applying rounded corners.
     * This method scales the image to fill the frame, cropping it if necessary.
     *
     * @param imageView the ImageView to scale
     * @param frameWidth the target width of the frame
     * @param frameHeight the target height of the frame
     * @param arcRadius the radius for rounded corners (0 for no rounding)
     */
    public static void scaleToFit(ImageView imageView, double frameWidth, double frameHeight, double arcRadius) {
        if (imageView.getImage() == null) return;
        Image image = imageView.getImage();
        
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        // Set the ImageView size to match the frame
        imageView.setFitWidth(frameWidth);
        imageView.setFitHeight(frameHeight);

        // Calculate aspect ratios
        double imageAspect = imageWidth / imageHeight;
        double frameAspect = frameWidth / frameHeight;

        double newWidth, newHeight;
        double xOffset = 0, yOffset = 0;

        // Determine the cropping area
        if (imageAspect > frameAspect) {
            // Image is wider than the frame, so we crop the sides
            newHeight = imageHeight;
            newWidth = newHeight * frameAspect;
            xOffset = (imageWidth - newWidth) / 2;
        } else {
            // Image is taller than the frame, so we crop the top and bottom
            newWidth = imageWidth;
            newHeight = newWidth / frameAspect;
            yOffset = (imageHeight - newHeight) / 2;
        }

        // Set the viewport to the centered, cropped portion of the image
        imageView.setViewport(new javafx.geometry.Rectangle2D(xOffset, yOffset, newWidth, newHeight));
        
        // Apply a clip to round the corners of the ImageView
        if (arcRadius > 0) {
            Rectangle clip = new Rectangle(frameWidth, frameHeight);
            clip.setArcWidth(arcRadius);
            clip.setArcHeight(arcRadius);
            imageView.setClip(clip);
        }
    }

    /**
     * Loads an image from a given path into an ImageView with proper fallback handling.
     * It handles both classpath-relative and absolute file paths.
     *
     * @param imageView the ImageView to load the image into
     * @param path the path to the image (can be classpath-relative or absolute)
     * @param fallbackPath the classpath-relative path to a fallback image
     */
    public static void loadImage(ImageView imageView, String path, String fallbackPath) {
        // Use FileSystemManager to resolve the image path
        Path resolvedPath = FileSystemManager.resolveImagePath(path);
        
        Image imageToLoad = null;
        if (resolvedPath != null) {
            // Check if it's a special resource marker
            if (resolvedPath.toString().startsWith("resource:")) {
                String resourcePath = resolvedPath.toString().substring("resource:".length());
                URL resource = ImageUtils.class.getResource(resourcePath);
                if (resource != null) {
                    imageToLoad = new Image(resource.toExternalForm());
                }
            } else {
                // Treat as a file path
                File file = resolvedPath.toFile();
                if (file.exists()) {
                    imageToLoad = new Image(file.toURI().toString());
                }
            }
        }

        // If the image failed to load, try the fallback
        if (imageToLoad == null && fallbackPath != null) {
            Path resolvedFallbackPath = FileSystemManager.resolveImagePath(fallbackPath);
            if (resolvedFallbackPath != null) {
                if (resolvedFallbackPath.toString().startsWith("resource:")) {
                    String resourcePath = resolvedFallbackPath.toString().substring("resource:".length());
                    URL fallbackResource = ImageUtils.class.getResource(resourcePath);
                    if (fallbackResource != null) {
                        imageToLoad = new Image(fallbackResource.toExternalForm());
                    }
                } else {
                    File fallbackFile = resolvedFallbackPath.toFile();
                    if (fallbackFile.exists()) {
                        imageToLoad = new Image(fallbackFile.toURI().toString());
                    }
                }
            }
        }
        
        // Final fallback - try to use FileSystemManager's fallback system
        if (imageToLoad == null) {
            String defaultFallback = FileSystemManager.getFallbackImagePath("default");
            if (defaultFallback != null) {
                File defaultFile = new File(defaultFallback);
                if (defaultFile.exists()) {
                    imageToLoad = new Image(defaultFile.toURI().toString());
                }
            }
        }
        
        imageView.setImage(imageToLoad);
    }

    /**
     * Loads an image from a given path into an ImageView with a default fallback.
     * Convenience method that uses "/com/starlight/images/missing.png" as the fallback.
     *
     * @param imageView the ImageView to load the image into
     * @param path the path to the image (can be classpath-relative or absolute)
     */
    public static void loadImage(ImageView imageView, String path) {
        loadImage(imageView, path, "/com/starlight/images/missing.png");
    }
}
