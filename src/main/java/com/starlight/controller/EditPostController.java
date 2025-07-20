package com.starlight.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

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
import javafx.stage.Modality;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;


/**
 * Controller responsible for the "edit post" dialog.
 */
public class EditPostController implements Initializable {
    private static final Logger logger = Logger.getLogger(EditPostController.class.getName());

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
            logger.log(Level.SEVERE, "Failed to copy image to user directory: " + e.getMessage(), e);
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
                logger.warning("No post to edit");
                return;
            }

            String postTitle = title.getText();
            String postDescription = description.getText();
            String postIngredients = ingredients.getText();
            String postDirections = directions.getText();

            if (postTitle.isEmpty() || postDescription.isEmpty() || postIngredients.isEmpty() || postDirections.isEmpty()) {
                logger.warning("Please complete all fields.");
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
            try {
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
                logger.info("Post updated successfully");

                // Close the dialog first
                Stage stage = (Stage) submit.getScene().getWindow();
                stage.close();
                
                // Show success message
                showResultDialog("post_updated_success");
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to update post: " + e.getMessage(), e);
                
                // Show failure message
                showResultDialog("post_update_failed");
                
                // Still close the dialog
                Stage stage = (Stage) submit.getScene().getWindow();
                stage.close();
            }
        });

        // Cancel button logic
        cancel.setOnAction(event -> {
            // Close the dialog without saving
            Stage stage = (Stage) cancel.getScene().getWindow();
            stage.close();
        });
    }
    
    /**
     * Shows a popup dialog with success or failure message
     */
    private void showResultDialog(String resultType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/popupDialog.fxml"));
            Parent root = loader.load();
            
            PopupDialogController controller = loader.getController();
            
            // Set appropriate message based on result type
            switch (resultType) {
                case "post_updated_success":
                    controller.setPostUpdatedSuccess();
                    break;
                case "post_update_failed":
                    controller.setPostUpdateFailed();
                    break;
                default:
                    controller.setMessage("Operation completed.");
                    break;
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resultType.contains("success") ? "Success" : "Error");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            
            // Center the dialog
            dialogStage.centerOnScreen();
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show result dialog: " + e.getMessage(), e);
        }
    }
}