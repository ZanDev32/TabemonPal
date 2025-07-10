package com.starlight.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.starlight.models.Post;
import com.starlight.models.PostDataRepository;
import com.starlight.models.User;
import com.starlight.models.UserDataRepository;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
     * This method does not scale up images that are smaller than the frame.
     */
    private void scaleToFit(ImageView imageView, double frameWidth, double frameHeight, double arcRadius) {
        if (imageView.getImage() == null) return;
        Image image = imageView.getImage();
        
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        // Determine the scale factor to fit the image within the frame
        double widthScale = frameWidth / imageWidth;
        double heightScale = frameHeight / imageHeight;
        double scale = Math.min(widthScale, heightScale);

        // If image is smaller than frame, don't scale up
        if (scale > 1.0) {
            scale = 1.0;
        }

        double newWidth = imageWidth * scale;
        double newHeight = imageHeight * scale;

        imageView.setFitWidth(newWidth);
        imageView.setFitHeight(newHeight);
        
        Rectangle clip = new Rectangle(newWidth, newHeight);
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
                c.uploadtime.setText(time);
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
