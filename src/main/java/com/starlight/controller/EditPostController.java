package com.starlight.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.starlight.model.Post;
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

    // Reused status message when no image is chosen
    private static final String NO_IMAGE_SELECTED = "No image selected";
    private static final String EXCEPTION_DETAILS = "Exception details";

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
            pickerstatus.setText(NO_IMAGE_SELECTED);
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
            logger.log(Level.SEVERE, "Failed to copy image to user directory: {0}", new Object[]{e.getMessage()});
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
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
        pickerstatus.setText(NO_IMAGE_SELECTED);
        setupImagePicker();
        setupSubmitHandler();
        setupCancelHandler();
    }

    /** Sets up the image picker button logic. */
    private void setupImagePicker() {
        imagepicker.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose an image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImage = file;
                pickerstatus.setText("Selected: " + file.getName());
            } else {
                restoreOrShowNoImage();
            }
        });
    }

    /** If current post has an image keep showing it, else show no image message. */
    private void restoreOrShowNoImage() {
        if (currentPost != null && currentPost.image != null && !currentPost.image.isEmpty()) {
            pickerstatus.setText("Current: " + new File(currentPost.image).getName());
        } else {
            pickerstatus.setText(NO_IMAGE_SELECTED);
        }
    }

    /** Sets up the submit button logic extracting complexity into helpers. */
    private void setupSubmitHandler() {
        submit.setOnAction(event -> {
            if (!validateCurrentPost()) return;
            if (!validateFormInputs()) return;
            applyFormToPost();
            handleImageUpdate();
            persistPostAndShowResult();
        });
    }

    private boolean validateCurrentPost() {
        if (currentPost == null) {
            logger.warning("No post to edit");
            return false;
        }
        return true;
    }

    private boolean validateFormInputs() {
        if (isEmpty(title) || isEmpty(description) || isEmpty(ingredients) || isEmpty(directions)) {
            logger.warning("Please complete all fields.");
            return false;
        }
        return true;
    }

    private boolean isEmpty(MFXTextField field) { return field.getText() == null || field.getText().isEmpty(); }
    private boolean isEmpty(TextArea area) { return area.getText() == null || area.getText().isEmpty(); }

    private void applyFormToPost() {
        currentPost.title = title.getText();
        currentPost.description = description.getText();
        currentPost.ingredients = ingredients.getText();
        currentPost.directions = directions.getText();
    }

    private void handleImageUpdate() {
        if (selectedImage == null) return;
        try {
            String storedPath = copyImageToUserDir(selectedImage);
            currentPost.image = storedPath != null ? storedPath : selectedImage.getAbsolutePath();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Image copy failed, using original path: {0}", new Object[]{e.getMessage()});
            currentPost.image = selectedImage.getAbsolutePath();
        }
    }

    private void persistPostAndShowResult() {
        try {
            List<Post> posts = repository.loadPosts();
            replacePostInList(posts, currentPost);
            repository.savePosts(posts);
            success = true;
            logger.info("Post updated successfully");
            closeDialog();
            showResultDialog("post_updated_success");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update post: {0}", new Object[]{e.getMessage()});
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
            showResultDialog("post_update_failed");
            closeDialog();
        }
    }

    private void replacePostInList(List<Post> posts, Post updated) {
        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            if (p.uuid != null && p.uuid.equals(updated.uuid)) {
                posts.set(i, updated);
                return;
            }
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) submit.getScene().getWindow();
        stage.close();
    }

    /** Sets up cancel button to simply close the dialog. */
    private void setupCancelHandler() {
        cancel.setOnAction(event -> closeDialog());
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
            logger.log(Level.SEVERE, "Failed to show result dialog: {0}", new Object[]{e.getMessage()});
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
        }
    }
}