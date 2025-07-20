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
import java.util.logging.Logger;

public class RecipeItemController {
    private static final Logger logger = Logger.getLogger(RecipeItemController.class.getName());

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
    private MainController mainController; // Reference to main controller for navigation

    @FXML
    private void initialize() {
        // Connect edit and delete buttons
        editPost.setOnAction(event -> handleEditPost());
        deletePost.setOnAction(event -> handleDeletePost());
        
        // Click handling will be set up when main controller is set
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
     * Sets the main controller reference for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        // Now that we have the main controller, set up click handling
        setupRecipeClickHandling();    
    }
    
    /**
     * Handles the edit post button click
     */
    private void handleEditPost() {
        if (currentPost == null) {
            logger.warning("No post data available for editing");
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
            logger.warning("Failed to load edit post dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the delete post button click
     */
    private void handleDeletePost() {
        if (currentPost == null) {
            logger.warning("No post data available for deletion");
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
            logger.warning("Failed to load delete dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up click handling for the recipe item (excluding buttons)
     */
    private void setupRecipeClickHandling() {
        // Get the root container (could be GridPane or other layout)
        javafx.scene.Node rootContainer = null;
        if (image != null && image.getParent() != null) {
            rootContainer = image.getParent();
        }
        
        // Make the root container clickable (but not the buttons)
        if (rootContainer != null) {
            rootContainer.setOnMouseClicked(event -> {
                // Check if click target is a button or interactive element
                javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
                
                // Prevent clicks on edit/delete buttons from triggering navigation
                if (isClickableButton(target)) {
                    return;
                }
                
                handleRecipeClick();
            });
            
            // Set cursor to indicate clickable area
            rootContainer.setStyle("-fx-cursor: hand;");
        }
        
        // Also make the image and title explicitly clickable
        if (image != null) {
            image.setOnMouseClicked(event -> handleRecipeClick());
            image.setStyle("-fx-cursor: hand;");
        }
        
        if (title != null) {
            title.setOnMouseClicked(event -> handleRecipeClick());
            title.setStyle("-fx-cursor: hand;");
        }
        
        if (rating != null) {
            rating.setOnMouseClicked(event -> handleRecipeClick());
            rating.setStyle("-fx-cursor: hand;");
        }
    }
    
    /**
     * Checks if the clicked element is a button or interactive element
     */
    private boolean isClickableButton(javafx.scene.Node target) {
        // Check if the target itself or any of its parents is a button
        javafx.scene.Node current = target;
        while (current != null) {
            if (current == editPost || current == deletePost) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
    
    /**
     * Handles clicks on the recipe content to navigate to full post view
     */
    private void handleRecipeClick() {
        if (currentPost != null && mainController != null) {
            mainController.setCurrentPost(currentPost);
            mainController.loadPage("Post");
        } else {
            if (currentPost == null) {
                logger.warning("Cannot navigate: currentPost is null");
            }
            if (mainController == null) {
                logger.warning("Cannot navigate: mainController is null");
            }
        }
    }
}
