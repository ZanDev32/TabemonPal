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
        setMessage("Nutrition analysis is not available because no OpenAI API key is configured.\n\n" +
                  "To enable this feature:\n" +
                  "1. Get an API key from OpenAI\n" +
                  "2. Save it in ~/.tabemonpal/Database/.SECRET_KEY.xml\n\n" +
                  "Your post will be created with default nutrition values.");
    }
    
    /**
     * Sets a predefined warning message for network/connectivity issues
     */
    public void setNetworkWarning() {
        setMessage("Nutrition analysis failed due to network connectivity issues.\n\n" +
                  "Please check your internet connection and try again.\n\n" +
                  "Your post will be created with default nutrition values.");
    }
    
    /**
     * Sets a predefined warning message for analysis failure after retries
     */
    public void setAnalysisFailedWarning() {
        setMessage("Nutrition analysis failed after multiple attempts.\n\n" +
                  "This could be due to:\n" +
                  "• Temporary server issues\n" +
                  "• Network connectivity problems\n" +
                  "• API service unavailability\n\n" +
                  "Your post will be created with default nutrition values.");
    }
    
    // ======= SUCCESS MESSAGES =======
    
    /**
     * Sets a success message for post creation
     */
    public void setPostCreatedSuccess() {
        setMessage("Post created successfully!\n\n" +
                  "Your recipe has been saved and is now available in the community feed.");
    }
    
    /**
     * Sets a success message for post update
     */
    public void setPostUpdatedSuccess() {
        setMessage("Post updated successfully!\n\n" +
                  "Your changes have been saved and are now visible to the community.");
    }
    
    /**
     * Sets a success message for post deletion
     */
    public void setPostDeletedSuccess() {
        setMessage("Post deleted successfully!\n\n" +
                  "The post has been removed from the community feed.");
    }
    
    /**
     * Sets a success message for account update
     */
    public void setAccountUpdatedSuccess() {
        setMessage("Account updated successfully!\n\n" +
                  "Your profile changes have been saved.");
    }
    
    /**
     * Sets a success message for account deletion
     */
    public void setAccountDeletedSuccess() {
        setMessage("Account deleted successfully!\n\n" +
                  "Your account and all associated data have been removed.");
    }
    
    /**
     * Sets a success message for nutrition analysis completion
     */
    public void setNutritionAnalysisSuccess() {
        setMessage("Nutrition analysis completed successfully!\n\n" +
                  "AI-powered nutrition facts have been added to your post.");
    }
    
    // ======= FAILURE MESSAGES =======
    
    /**
     * Sets a failure message for post creation
     */
    public void setPostCreationFailed() {
        setMessage("Failed to create post!\n\n" +
                  "There was an error saving your recipe. Please check your input and try again.");
    }
    
    /**
     * Sets a failure message for post update
     */
    public void setPostUpdateFailed() {
        setMessage("Failed to update post!\n\n" +
                  "There was an error saving your changes. Please try again.");
    }
    
    /**
     * Sets a failure message for post deletion
     */
    public void setPostDeletionFailed() {
        setMessage("Failed to delete post!\n\n" +
                  "There was an error removing the post. Please try again.");
    }
    
    /**
     * Sets a failure message for account update
     */
    public void setAccountUpdateFailed() {
        setMessage("Failed to update account!\n\n" +
                  "There was an error saving your profile changes. Please try again.");
    }
    
    /**
     * Sets a failure message for account deletion
     */
    public void setAccountDeletionFailed() {
        setMessage("Failed to delete account!\n\n" +
                  "There was an error removing your account. Please try again or contact support.");
    }
}
