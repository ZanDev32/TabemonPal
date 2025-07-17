package com.starlight.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.starlight.models.Post;
import com.starlight.models.PostDataRepository;
import com.starlight.models.User;
import com.starlight.models.UserDataRepository;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private MFXButton createpost;

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

    private final PostDataRepository repository = new PostDataRepository();
    private final UserDataRepository userRepository = new UserDataRepository();

    /**
     * Gets the profile picture path for a given username from UserData.xml
     */
    private String getProfilePictureForUser(String username) {
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
    private String getDisplayUsernameForUser(String username) {
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
    private void scaleToFit(ImageView imageView, double frameWidth, double frameHeight, double arcRadius) {
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
     * Overloaded method that resizes and centers the given {@link ImageView} to fit inside the specified frame size
     * while preserving aspect ratio and applying rounded corners with individual corner control.
     * This method scales the image to fill the frame, cropping it if necessary.
     * 
     * @param imageView the ImageView to resize and crop
     * @param frameWidth the target width of the frame
     * @param frameHeight the target height of the frame
     * @param topLeftRadius the arc radius for the top-left corner
     * @param topRightRadius the arc radius for the top-right corner
     * @param bottomLeftRadius the arc radius for the bottom-left corner
     * @param bottomRightRadius the arc radius for the bottom-right corner
     */
    private void scaleToFit(ImageView imageView, double frameWidth, double frameHeight, 
                           double topLeftRadius, double topRightRadius, 
                           double bottomLeftRadius, double bottomRightRadius) {
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
        
        // Create a custom clip with individual corner radii using a rounded rectangle approach
        javafx.scene.shape.Path clipPath = new javafx.scene.shape.Path();
        
        // Move to starting point (top-left, accounting for radius)
        clipPath.getElements().add(new javafx.scene.shape.MoveTo(topLeftRadius, 0));
        
        // Top edge to top-right corner
        clipPath.getElements().add(new javafx.scene.shape.LineTo(frameWidth - topRightRadius, 0));
        
        // Top-right corner arc
        if (topRightRadius > 0) {
            clipPath.getElements().add(new javafx.scene.shape.QuadCurveTo(
                frameWidth, 0, frameWidth, topRightRadius));
        } else {
            clipPath.getElements().add(new javafx.scene.shape.LineTo(frameWidth, 0));
        }
        
        // Right edge to bottom-right corner
        clipPath.getElements().add(new javafx.scene.shape.LineTo(frameWidth, frameHeight - bottomRightRadius));
        
        // Bottom-right corner arc
        if (bottomRightRadius > 0) {
            clipPath.getElements().add(new javafx.scene.shape.QuadCurveTo(
                frameWidth, frameHeight, frameWidth - bottomRightRadius, frameHeight));
        } else {
            clipPath.getElements().add(new javafx.scene.shape.LineTo(frameWidth, frameHeight));
        }
        
        // Bottom edge to bottom-left corner
        clipPath.getElements().add(new javafx.scene.shape.LineTo(bottomLeftRadius, frameHeight));
        
        // Bottom-left corner arc
        if (bottomLeftRadius > 0) {
            clipPath.getElements().add(new javafx.scene.shape.QuadCurveTo(
                0, frameHeight, 0, frameHeight - bottomLeftRadius));
        } else {
            clipPath.getElements().add(new javafx.scene.shape.LineTo(0, frameHeight));
        }
        
        // Left edge to top-left corner
        clipPath.getElements().add(new javafx.scene.shape.LineTo(0, topLeftRadius));
        
        // Top-left corner arc
        if (topLeftRadius > 0) {
            clipPath.getElements().add(new javafx.scene.shape.QuadCurveTo(
                0, 0, topLeftRadius, 0));
        } else {
            clipPath.getElements().add(new javafx.scene.shape.LineTo(0, 0));
        }
        
        // Close the path
        clipPath.getElements().add(new javafx.scene.shape.ClosePath());
        
        imageView.setClip(clipPath);
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
     * Loads the top posts into the daily post slots with relative time formatting
     */
    private void loadDailyPosts() {
        List<Post> posts = repository.loadPosts();
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // Take up to 4 posts for daily posts
        for (int i = 0; i < Math.min(4, posts.size()); i++) {
            Post post = posts.get(i);
            switch (i) {
                case 0:
                    dailytitle1.setText(post.title);
                    starrating.setText(post.rating);
                    dailylikecounter.setText(post.likecount);
                    loadImage(dailyphoto1, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto1, 280, 200, 30);
                    break;
                case 1:
                    dailytitle2.setText(post.title);
                    starrating2.setText(post.rating);
                    dailylikecounter2.setText(post.likecount);
                    loadImage(dailyphoto2, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto2, 280, 200, 30);
                    break;
                case 2:
                    dailytitle3.setText(post.title);
                    starrating3.setText(post.rating);
                    dailylikecounter3.setText(post.likecount);
                    loadImage(dailyphoto3, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto3, 280, 200, 30, 30, 0, 0);
                    break;
                case 3:
                    dailytitle4.setText(post.title);
                    starrating4.setText(post.rating);
                    dailylikecounter4.setText(post.likecount);
                    loadImage(dailyphoto4, post.image, "/com/starlight/images/missing.png");
                    scaleToFit(dailyphoto4, 280, 200, 30, 30, 0, 0);
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
    private void loadImage(ImageView imageView, String path, String fallbackPath) {
        Image imageToLoad = null;
        if (path != null && !path.trim().isEmpty()) {
            // Check if it's a classpath resource
            if (path.startsWith("/")) {
                URL resource = getClass().getResource(path);
                if (resource != null) {
                    imageToLoad = new Image(resource.toExternalForm());
                }
            } else {
                // Treat as a file path
                File file = new File(path);
                if (file.exists()) {
                    imageToLoad = new Image(file.toURI().toString());
                }
            }
        }

        // If the image failed to load, use the fallback
        if (imageToLoad == null) {
            URL fallbackResource = getClass().getResource(fallbackPath);
            if (fallbackResource != null) {
                imageToLoad = new Image(fallbackResource.toExternalForm());
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

    @FXML
    private void handleCreatePost() {
        showCreatePostPopup();
    }

    /**
     * Displays a modal dialog that allows the user to create a new post.
     */
    private void showCreatePostPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/createPost.fxml"));
            Parent popupRoot = loader.load();
            CreatePostController controller = loader.getController();

            popupRoot.setScaleX(0.7);
            popupRoot.setScaleY(0.7);

            Scene popupScene = new Scene(popupRoot);
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(popupScene);
            popupStage.setTitle("Create Post");
            popupStage.setResizable(false);

            popupStage.setOnShown(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(220), popupRoot);
                st.setFromX(0.7);
                st.setFromY(0.7);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            popupStage.showAndWait();
            if (controller.isSuccess()) {
                loadPosts();
                loadDailyPosts();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Formats a timestamp string into a relative time format like "2h ago", "1d ago"
     */
    private String formatRelativeTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return "unknown";
        }
        
        try {
            // Handle the format used in XML: "2025-07-11 17:12:55"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime postTime = LocalDateTime.parse(timeString.trim(), formatter);
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
