package com.starlight.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import com.starlight.models.Post;
import com.starlight.models.PostDataRepository;
import com.starlight.models.User;
import com.starlight.models.UserDataRepository;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Controller backing the community page. It displays recent posts and allows
 * the user to create new posts.
 */
public class CommunityController implements Initializable {
    @FXML
    private MFXButton toProfile;

    @FXML
    private VBox dailypost1;

    @FXML
    private VBox dailypost2;

    @FXML
    private VBox dailypost3;

    @FXML
    private VBox dailypost4;

    @FXML
    private GridPane manu;

    @FXML
    private ImageView dailyphoto1;

    @FXML
    private Label dailytitle1;

    @FXML
    private Label starrating;

    @FXML
    private Label dailylikecounter;

    @FXML
    private ImageView dailyphoto2;

    @FXML
    private Label dailytitle2;

    @FXML
    private Label starrating2;

    @FXML
    private Label dailylikecounter2;

    @FXML
    private ImageView dailyphoto3;

    @FXML
    private Label dailytitle3;

    @FXML
    private Label starrating3;

    @FXML
    private Label dailylikecounter3;

    @FXML
    private ImageView dailyphoto4;

    @FXML
    private Label dailytitle4;

    @FXML
    private Label starrating4;

    @FXML
    private Label dailylikecounter4;

    @FXML
    private VBox postlist;

    private MainController main;

    private final PostDataRepository repository = new PostDataRepository();
    private final UserDataRepository userRepository = new UserDataRepository();

    public void setMainController(MainController main) {
        this.main = main;
    }

    @FXML
    void toProfile(MouseEvent event) {
        main.loadPage("profile");
    }
    
    /**
     * Gets the profile picture path for a given username from UserData.xml
     */
    public String getProfilePictureForUser(String username) {
        if (username == null) return null;
        
        List<User> users = userRepository.loadUsers();
        for (User user : users) {
            if (username.equals(user.username)) {
                return user.profilepicture;
            }
        }
        return null;
    }

    /**
     * Gets the display username from UserData.xml for a given username
     */
    public String getDisplayUsernameForUser(String username) {
        if (username == null) return null;
        
        List<User> users = userRepository.loadUsers();
        for (User user : users) {
            if (username.equals(user.username)) {
                // Return fullname if available, otherwise return username
                return user.fullname != null && !user.fullname.trim().isEmpty() 
                    ? user.fullname : user.username;
            }
        }
        return username; // fallback to original username if not found
    }

    /**
     * Resizes and centers the given {@link ImageView} to fit inside the specified frame size
     * while preserving aspect ratio and applying rounded corners.
     * This method scales the image to fill the frame, cropping it if necessary.
     */
    public void scaleToFit(ImageView imageView, double frameWidth, double frameHeight, double arcRadius) {
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
        Rectangle clip = new Rectangle(frameWidth, frameHeight);
        clip.setArcWidth(arcRadius);
        clip.setArcHeight(arcRadius);
        imageView.setClip(clip);
    }

    /**
     * Loads posts from the repository and populates the UI. All posts are
     * added to the postlist container.
     */
    private void loadPosts() {
        postlist.getChildren().clear();

        List<Post> posts = repository.loadPosts();
        if (posts == null || posts.isEmpty()) {
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            String tits = p.title;
            String usr = p.username;
            String desc = p.description;
            String image = p.image;
            String time = p.uploadtime;
            String likes = p.likecount;
            
            // Get profile picture and display username from UserData.xml instead of Post data
            String pp = getProfilePictureForUser(usr);
            String displayUsername = getDisplayUsernameForUser(usr);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/postItem.fxml"));
                VBox node = loader.load();
                PostItemController c = loader.getController();

                c.username.setText(displayUsername != null ? displayUsername : usr);
                c.title.setText(tits);
                c.uploadtime.setText(formatRelativeTime(time));
                c.description.setText(desc);
                c.likecounter.setText(likes);
                
                // Set comment count (default to 0 if not set)
                String commentCount = p.commentcount != null ? p.commentcount : "0";
                c.commentcounter.setText(commentCount);
                
                // Set the post data for the controller
                c.setPost(p);
                
                // Initialize isLiked field if not set
                if (p.isLiked == null) {
                    p.isLiked = "false";
                }

                // Load profile picture
                loadImage(c.profile1, pp, "/com/starlight/images/missing.png");
                scaleToFit(c.profile1, 40, 40, 40);

                // Load post image
                loadImage(c.recentphoto1, image, "/com/starlight/images/missing.png");
                scaleToFit(c.recentphoto1, 674, 485, 20);

                postlist.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Public method to refresh the posts and daily posts - used when posts are updated externally
     */
    public void refreshPosts() {
        if (postlist != null) {
            loadPosts();
        }
        loadDailyPosts();
    }

    /**
     * Loads the top posts into the daily post slots with random selection
     */
    private void loadDailyPosts() {
        List<Post> posts = repository.loadPosts();
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // Create a copy of the posts list and shuffle it randomly
        List<Post> shuffledPosts = new java.util.ArrayList<>(posts);
        Collections.shuffle(shuffledPosts);

        // Take up to 4 posts for daily posts from the shuffled list
        for (int i = 0; i < Math.min(4, shuffledPosts.size()); i++) {
            Post post = shuffledPosts.get(i);
            switch (i) {
                case 0:
                    dailytitle1.setText(post.title);
                    starrating.setText(post.rating);
                    dailylikecounter.setText(post.likecount);
                    loadImage(dailyphoto1, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto1, 280, 174, 30);
                    break;
                case 1:
                    dailytitle2.setText(post.title);
                    starrating2.setText(post.rating);
                    dailylikecounter2.setText(post.likecount);
                    loadImage(dailyphoto2, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto2, 280, 174, 30);
                    break;
                case 2:
                    dailytitle3.setText(post.title);
                    starrating3.setText(post.rating);
                    dailylikecounter3.setText(post.likecount);
                    loadImage(dailyphoto3, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto3, 280, 174, 30);
                    break;
                case 3:
                    dailytitle4.setText(post.title);
                    starrating4.setText(post.rating);
                    dailylikecounter4.setText(post.likecount);
                    loadImage(dailyphoto4, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto4, 280, 174, 30);
                    break;
            }
        }
    }

    /**
     * Helper method to load an image from a given path into an ImageView.
     * It handles both classpath-relative and absolute file paths.
     *
     * @param imageView the ImageView to load the image into
     * @param path the path to the image (can be classpath-relative or absolute)
     * @param fallbackPath the classpath-relative path to a fallback image
     */
    public void loadImage(ImageView imageView, String path, String fallbackPath) {
        // Use FileSystemManager to resolve the image path
        Path resolvedPath = com.starlight.util.FileSystemManager.resolveImagePath(path);
        
        Image imageToLoad = null;
        if (resolvedPath != null) {
            // Check if it's a special resource marker
            if (resolvedPath.toString().startsWith("resource:")) {
                String resourcePath = resolvedPath.toString().substring("resource:".length());
                URL resource = getClass().getResource(resourcePath);
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
            Path resolvedFallbackPath = com.starlight.util.FileSystemManager.resolveImagePath(fallbackPath);
            if (resolvedFallbackPath != null) {
                if (resolvedFallbackPath.toString().startsWith("resource:")) {
                    String resourcePath = resolvedFallbackPath.toString().substring("resource:".length());
                    URL fallbackResource = getClass().getResource(resourcePath);
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
            String defaultFallback = com.starlight.util.FileSystemManager.getFallbackImagePath("default");
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
     * Initializes the controller by loading dummy data and wiring up actions.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        repository.ensureDummyData();
        loadPosts();
        loadDailyPosts();
    }

    /**
     * Formats a timestamp string into a relative time format like "2h ago", "1d ago"
     */
    private String formatRelativeTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return "unknown";
        }
        
        try {
            LocalDateTime postTime;
            String trimmedTime = timeString.trim();
            
            // Try different formats to handle various timestamp formats
            try {
                // First try format with seconds: "2025-07-11 17:12:55"
                DateTimeFormatter formatterWithSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                postTime = LocalDateTime.parse(trimmedTime, formatterWithSeconds);
            } catch (Exception e1) {
                try {
                    // Then try format without seconds: "2025-07-11 17:12"
                    DateTimeFormatter formatterWithoutSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    postTime = LocalDateTime.parse(trimmedTime, formatterWithoutSeconds);
                } catch (Exception e2) {
                    // If both fail, return original string
                    return timeString;
                }
            }
            
            long hours = ChronoUnit.HOURS.between(postTime, LocalDateTime.now());
            
            if (hours < 1) return "just now";
            if (hours < 24) return hours + "h ago";
            
            long days = hours / 24;
            if (days < 30) return days + "d ago";
            
            long months = days / 30;
            if (months < 12) return months + "mo ago";
            
            return (months / 12) + "y ago";
        } catch (Exception e) {
            return timeString;
        }
    }
}
