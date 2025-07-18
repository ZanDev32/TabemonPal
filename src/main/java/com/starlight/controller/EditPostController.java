package com.starlight.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

import com.starlight.models.Post;
import com.starlight.repository.PostDataRepository;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;


/**
 * Controller responsible for the "edit post" dialog.
 */
public class EditPostController implements Initializable {

    @FXML
    private MFXTextField title;

    @FXML
    private MFXButton imagepicker;

    @FXML
    private Label pickerstatus;

    @FXML
    private MFXTextField description;

    @FXML
    private TextArea ingredients;

    @FXML
    private TextArea directions;

    @FXML
    private MFXButton submit;

    @FXML
    private MFXButton cancel;

    private File selectedImage;
    private Post currentPost;
    private boolean success;

    private final PostDataRepository repository = new PostDataRepository();

    /**
     * Sets the post to edit and populates the form fields
     */
    public void setPost(Post post) {
        this.currentPost = post;
        populateFields();
    }

    /**
     * Populates the form fields with the current post data
     */
    private void populateFields() {
        if (currentPost == null) return;

        title.setText(currentPost.title != null ? currentPost.title : "");
        description.setText(currentPost.description != null ? currentPost.description : "");
        ingredients.setText(currentPost.ingredients != null ? currentPost.ingredients : "");
        directions.setText(currentPost.directions != null ? currentPost.directions : "");
        
        // Set image status
        if (currentPost.image != null && !currentPost.image.isEmpty()) {
            pickerstatus.setText("Current: " + new File(currentPost.image).getName());
        } else {
            pickerstatus.setText("No image selected");
        }
    }

    /**
     * Copies the selected image to the user's data directory.
     */
    private String copyImageToUserDir(File image) {
        try {
            String username = Session.getCurrentUser() != null ? Session.getCurrentUser().username : "unknown";
            return com.starlight.util.FileSystemManager.copyFileToUserDirectoryWithUniqueFilename(image, username);
        } catch (Exception e) {
            System.err.println("Failed to copy image to user directory: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Indicates whether the user successfully updated the post.
     */
    public boolean isSuccess() {
        return success;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pickerstatus.setText("No image selected");

        // Image picker logic - reuse from CreatePostController
        imagepicker.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose an image");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImage = file;
                pickerstatus.setText("Selected: " + file.getName());
            } else {
                // Reset to current image status if no new image selected
                if (currentPost != null && currentPost.image != null && !currentPost.image.isEmpty()) {
                    pickerstatus.setText("Current: " + new File(currentPost.image).getName());
                } else {
                    pickerstatus.setText("No image selected");
                }
            }
        });

        // Submit button logic
        submit.setOnAction(event -> {
            if (currentPost == null) {
                System.err.println("No post to edit");
                return;
            }

            String postTitle = title.getText();
            String postDescription = description.getText();
            String postIngredients = ingredients.getText();
            String postDirections = directions.getText();

            if (postTitle.isEmpty() || postDescription.isEmpty() || postIngredients.isEmpty() || postDirections.isEmpty()) {
                System.err.println("Please complete all fields.");
                return;
            }

            // Update the post object
            currentPost.title = postTitle;
            currentPost.description = postDescription;
            currentPost.ingredients = postIngredients;
            currentPost.directions = postDirections;

            // Handle image update if new image was selected
            if (selectedImage != null) {
                try {
                    String storedPath = copyImageToUserDir(selectedImage);
                    currentPost.image = storedPath != null ? storedPath : selectedImage.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                    currentPost.image = selectedImage.getAbsolutePath();
                }
            }

            // Save the updated post
            List<Post> posts = repository.loadPosts();
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                if (post.uuid != null && post.uuid.equals(currentPost.uuid)) {
                    posts.set(i, currentPost);
                    break;
                }
            }
            repository.savePosts(posts);

            success = true;

            // Close the dialog
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.close();
        });

        // Cancel button logic
        cancel.setOnAction(event -> {
            // Close the dialog without saving
            Stage stage = (Stage) cancel.getScene().getWindow();
            stage.close();
        });
    }
}