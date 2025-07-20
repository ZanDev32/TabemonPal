package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.io.IOException;
import com.starlight.models.Post;

public class RecipeItemController {
    @FXML
    private ImageView image;

    @FXML
    private Label title;

    @FXML
    private Label rating;

    @FXML
    private Label likecount;

    @FXML
    private MFXButton editPost;

    @FXML
    private MFXButton deletePost;

    private Post currentPost;
    private Runnable onPostUpdated; // Callback to refresh the parent view

    @FXML
    private void initialize() {
        // Connect edit and delete buttons
        editPost.setOnAction(event -> handleEditPost());
        deletePost.setOnAction(event -> handleDeletePost());
    }
    
    /**
     * Sets the recipe data for this recipe item
     */
    public void setRecipeData(String title, String rating, String likecount) {
        if (this.title != null) {
            this.title.setText(title);
        }
        if (this.rating != null) {
            this.rating.setText(rating);
        }
        if (this.likecount != null) {
            this.likecount.setText(likecount);
        }
    }

    /**
     * Sets the post data for this recipe item
     */
    public void setPost(Post post) {
        this.currentPost = post;
    }

    /**
     * Sets the callback to refresh the parent view after updates
     */
    public void setOnPostUpdated(Runnable callback) {
        this.onPostUpdated = callback;
    }
    
    /**
     * Gets the image view for external image loading
     */
    public ImageView getImageView() {
        return image;
    }

    /**
     * Handles the edit post button click
     */
    private void handleEditPost() {
        if (currentPost == null) {
            System.err.println("No post data available for editing");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/editPost.fxml"));
            Parent editRoot = loader.load();
            EditPostController editController = loader.getController();
            
            // Set the post data in the edit controller
            editController.setPost(currentPost);

            // Create and configure the modal dialog
            editRoot.setScaleX(0.7);
            editRoot.setScaleY(0.7);

            Scene editScene = new Scene(editRoot);
            Stage editStage = new Stage();
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.setScene(editScene);
            editStage.setTitle("Edit Recipe");
            editStage.setResizable(false);

            // Add scale animation
            editStage.setOnShown(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(220), editRoot);
                st.setFromX(0.7);
                st.setFromY(0.7);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            editStage.showAndWait();

            // Refresh the parent view if the edit was successful
            if (editController.isSuccess() && onPostUpdated != null) {
                onPostUpdated.run();
            }

        } catch (IOException e) {
            System.err.println("Failed to load edit post dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the delete post button click
     */
    private void handleDeletePost() {
        if (currentPost == null) {
            System.err.println("No post data available for deletion");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/deleteDialog.fxml"));
            Parent deleteRoot = loader.load();
            DeleteDialogController deleteController = loader.getController();
            
            // Set the post data in the delete controller
            deleteController.setPost(currentPost);

            // Create and configure the modal dialog
            Scene deleteScene = new Scene(deleteRoot);
            Stage deleteStage = new Stage();
            deleteStage.initModality(Modality.APPLICATION_MODAL);
            deleteStage.setScene(deleteScene);
            deleteStage.setTitle("Delete Recipe");
            deleteStage.setResizable(false);

            deleteStage.showAndWait();

            // Refresh the parent view if the deletion was confirmed
            if (deleteController.isConfirmed() && onPostUpdated != null) {
                onPostUpdated.run();
            }

        } catch (IOException e) {
            System.err.println("Failed to load delete dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
