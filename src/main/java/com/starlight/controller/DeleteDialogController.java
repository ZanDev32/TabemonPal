package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import com.starlight.model.Post;
import com.starlight.repository.PostDataRepository;

import java.util.List;
import java.util.logging.Level;

public class DeleteDialogController {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DeleteDialogController.class.getName());

    @FXML
    private MFXButton delete;

    @FXML
    private MFXButton cancel;

    private Post postToDelete;
    private boolean confirmed = false;
    private final PostDataRepository repository = new PostDataRepository();

    /**
     * Sets the post to be deleted
     */
    public void setPost(Post post) {
        this.postToDelete = post;
    }

    /**
     * Returns whether the user confirmed the deletion
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void initialize() {
        // Delete button logic
        delete.setOnAction(event -> {
            if (postToDelete == null) {
                logger.warning("No post to delete");
                return;
            }

            // Remove the post from the repository
            try {
                List<Post> posts = repository.loadPosts();
                posts.removeIf(post -> post.uuid != null && post.uuid.equals(postToDelete.uuid));
                repository.savePosts(posts);

                confirmed = true;
                logger.info("Post deleted successfully");

                // Close the dialog first
                Stage stage = (Stage) delete.getScene().getWindow();
                stage.close();
                
                // Show success message
                showResultDialog("post_deleted_success");
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to delete post: " + e.getMessage(), e);
                
                // Show failure message
                showResultDialog("post_deletion_failed");
                
                // Still close the dialog
                Stage stage = (Stage) delete.getScene().getWindow();
                stage.close();
            }
        });

        // Cancel button logic
        cancel.setOnAction(event -> {
            confirmed = false;
            // Close the dialog without deleting
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
                case "post_deleted_success":
                    controller.setPostDeletedSuccess();
                    break;
                case "post_deletion_failed":
                    controller.setPostDeletionFailed();
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
