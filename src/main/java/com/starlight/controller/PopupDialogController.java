package com.starlight.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class PopupDialogController implements Initializable {
    @FXML
    private Label message;
    
    @FXML
    private MFXButton okButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up OK button to close the dialog
        if (okButton != null) {
            okButton.setOnAction(event -> {
                Stage stage = (Stage) okButton.getScene().getWindow();
                stage.close();
            });
        }
    }
    
    /**
     * Sets the warning message to display
     */
    public void setMessage(String warningMessage) {
        if (message != null) {
            message.setText(warningMessage);
        }
    }
    
    /**
     * Sets a predefined warning message for API key issues
     */
    public void setApiKeyWarning() {
        setMessage("""
            Nutrition analysis is not available because no OpenAI API key is configured.

            To enable this feature:
            1. Get an API key from OpenAI
            2. Save it in ~/.tabemonpal/Database/.SECRET_KEY.xml

            Your post will be created with default nutrition values.
            """);
    }
    
    /**
     * Sets a predefined warning message for network/connectivity issues
     */
    public void setNetworkWarning() {
        setMessage("""
            Nutrition analysis failed due to network connectivity issues.

            Please check your internet connection and try again.

            Your post will be created with default nutrition values.
            """);
    }
    
    /**
     * Sets a predefined warning message for analysis failure after retries
     */
    public void setAnalysisFailedWarning() {
        setMessage("""
            Nutrition analysis failed after multiple attempts.

            This could be due to:
            • Temporary server issues
            • Network connectivity problems
            • API service unavailability

            Your post will be created with default nutrition values.
            """);
    }
    
    // ======= SUCCESS MESSAGES =======
    
    /**
     * Sets a success message for post creation
     */
    public void setPostCreatedSuccess() {
        setMessage("""
            Post created successfully!

            Your recipe has been saved and is now available in the community feed.
            """);
    }
    
    /**
     * Sets a success message for post update
     */
    public void setPostUpdatedSuccess() {
        setMessage("""
            Post updated successfully!

            Your changes have been saved and are now visible to the community.
            """);
    }
    
    /**
     * Sets a success message for post deletion
     */
    public void setPostDeletedSuccess() {
        setMessage("""
            Post deleted successfully!

            The post has been removed from the community feed.
            """);
    }
    
    /**
     * Sets a success message for account update
     */
    public void setAccountUpdatedSuccess() {
        setMessage("""
            Account updated successfully!

            Your profile changes have been saved.
            """);
    }
    
    /**
     * Sets a success message for account deletion
     */
    public void setAccountDeletedSuccess() {
        setMessage("""
            Account deleted successfully!

            Your account and all associated data have been removed.
            """);
    }
    
    /**
     * Sets a success message for nutrition analysis completion
     */
    public void setNutritionAnalysisSuccess() {
        setMessage("""
            Nutrition analysis completed successfully!

            AI-powered nutrition facts have been added to your post.
            """);
    }
    
    // ======= FAILURE MESSAGES =======
    
    /**
     * Sets a failure message for post creation
     */
    public void setPostCreationFailed() {
        setMessage("""
            Failed to create post!

            There was an error saving your recipe. Please check your input and try again.
            """);
    }
    
    /**
     * Sets a failure message for post update
     */
    public void setPostUpdateFailed() {
        setMessage("""
            Failed to update post!

            There was an error saving your changes. Please try again.
            """);
    }
    
    /**
     * Sets a failure message for post deletion
     */
    public void setPostDeletionFailed() {
        setMessage("""
            Failed to delete post!

            There was an error removing the post. Please try again.
            """);
    }
    
    /**
     * Sets a failure message for account update
     */
    public void setAccountUpdateFailed() {
        setMessage("""
            Failed to update account!

            There was an error saving your profile changes. Please try again.
            """);
    }
    
    /**
     * Sets a failure message for account deletion
     */
    public void setAccountDeletionFailed() {
        setMessage("""
            Failed to delete account!

            There was an error removing your account. Please try again or contact support.
            """);
    }
}
