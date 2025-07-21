package com.starlight.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.util.logging.Level;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.util.Session;
import com.starlight.model.User;
import com.starlight.util.ImageUtils;

/**
 * Controller for the user settings dialog where profile information can be
 * edited.
 * 
 * Architecture: Uses CommunityController's public utility methods for image loading
 * and scaling to maintain consistency and reduce code redundancy across the application.
 */
public class EditProfileController implements Initializable {
    private static final Logger logger = Logger.getLogger(EditProfileController.class.getName());
    @FXML
    private ImageView Image;

    @FXML
    private MFXButton imagepicker;

    @FXML
    private Label welcomeLabel;

    @FXML
    private MFXTextField emailField;
    
    @FXML 
    private MFXTextField passwordField;
    
    @FXML 
    private MFXDatePicker birthDayPicker;

    @FXML
    private MFXButton savebutton;

    @FXML
    private MFXButton deleteaccbutton;

    private User currentUser;
    private Runnable onLogout;
    private Stage previousStage;

    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) welcomeLabel.setText("Hello, " + user.username);
        if (emailField != null) emailField.setText(user.email);
        if (passwordField != null) passwordField.setText(user.password);
        if (birthDayPicker != null && user.birthDay != null && !user.birthDay.isEmpty()) {
            birthDayPicker.setValue(java.time.LocalDate.parse(user.birthDay));
        }
        
        // Load and scale profile image using ImageUtils
        if (Image != null) {
            ImageUtils.loadImage(Image, user.profilepicture, "/com/starlight/images/missing.png");
            ImageUtils.scaleToFit(Image, 170, 170, 200);
        }
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }
    
    /**
     * Sets the stage to return to when the "Back to previous menu" button is clicked.
     *
     * @param stage the stage to return to
     */
    public void setPreviousStage(Stage stage) {
        this.previousStage = stage;
    }
    
    /**
     * Loads the current user's profile image using ImageUtils
     */
    private void loadCurrentUserProfileImage() {
        User currentSessionUser = Session.getCurrentUser();
        if (currentSessionUser != null && Image != null) {
            // Load profile image using ImageUtils
            ImageUtils.loadImage(Image, currentSessionUser.profilepicture, "/com/starlight/images/missing.png");
            ImageUtils.scaleToFit(Image, 170, 170, 85); // Circular profile image with rounded corners
        }
    }
    
    /**
     * Handles the image picker button click to allow user to select a new profile image
     */
    @FXML
    public void onImagePickerClick(javafx.event.ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        
        // Set extension filters for image files
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "Image Files", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);
        
        // Show the file chooser dialog
        Stage stage = (Stage) imagepicker.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            updateProfileImage(selectedFile);
        }
    }
    
    /**
     * Updates the user's profile image in UserData XML and refreshes the display.
     * Copies the selected file to the user's directory and updates the XML with the new path.
     */
    private void updateProfileImage(File selectedFile) {
        try {
            User currentSessionUser = Session.getCurrentUser();
            if (currentSessionUser == null) {
                logger.warning("No current session user found");
                return;
            }
            
            // Copy the selected file to the user's directory
            String copiedFilePath = com.starlight.util.FileSystemManager.copyFileToUserDirectoryWithUniqueFilename(
                selectedFile, currentSessionUser.username);
            
            if (copiedFilePath == null) {
                logger.warning("Failed to copy image file to user directory");
                return;
            }
            
            // Update the current user's profile picture path
            currentSessionUser.profilepicture = copiedFilePath;
            
            // Also update currentUser if it's set
            if (currentUser != null) {
                currentUser.profilepicture = copiedFilePath;
            }
            
            // Update the UserData XML file
            com.starlight.repository.UserDataRepository repo = new com.starlight.repository.UserDataRepository();
            java.util.List<com.starlight.model.User> users = repo.loadUsers();
            for (com.starlight.model.User u : users) {
                if (u.username.equals(currentSessionUser.username)) {
                    u.profilepicture = copiedFilePath;
                    break;
                }
            }
            repo.saveUsers(users);
            
            // Refresh the profile image display
            loadCurrentUserProfileImage();
            
            logger.info("Profile image updated successfully: " + copiedFilePath);
        } catch (Exception e) {
            logger.warning("Failed to update profile image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wires up the save and cancel buttons for the dialog.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        savebutton.setOnAction(event -> {
            String newEmail = emailField.getText();
            String newPass = passwordField.getText();
            String birth = birthDayPicker.getValue() != null ? birthDayPicker.getValue().toString() : null;
            try {
                URL endpoint = new URL("http://localhost:8000/users/" + currentUser.username);
                HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/xml");
                conn.setDoOutput(true);
                User u = new User();
                u.email = newEmail;
                u.password = newPass;
                u.birthDay = birth;
                XStream xs = new XStream(new DomDriver());
                xs.allowTypesByWildcard(new String[]{"com.starlight.model.*"});
                xs.alias("user", User.class);
                String xml = xs.toXML(u);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(xml.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) {
                    logger.info("User updated successfully");
                    showResultDialog("account_updated_success");
                } else {
                    logger.info("Update failed: " + conn.getResponseCode());
                    showResultDialog("account_update_failed");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to update user: " + e.getMessage(), e);
                showResultDialog("account_update_failed");
            }
        });

        // Add delete account button handler
        deleteaccbutton.setOnAction(event -> onDelete(event));

        User currentSessionUser = Session.getCurrentUser();
        if (currentSessionUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Hello, " + currentSessionUser.username);
        }
        
        // Load the current user's profile image
        loadCurrentUserProfileImage();
    }

    @FXML
    private void onUpdate(javafx.event.ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        java.time.LocalDate birthDay = birthDayPicker.getValue();
        if (email.isEmpty() || password.isEmpty()) {
            logger.warning("All fields are required.");
            return;
        }
        com.starlight.repository.UserDataRepository repo = new com.starlight.repository.UserDataRepository();
        java.util.List<com.starlight.model.User> users = repo.loadUsers();
        for (com.starlight.model.User u : users) {
            if (u.username.equals(currentUser.username)) {
                u.email = email;
                u.password = password;
                u.birthDay = birthDay != null ? birthDay.toString() : null;
                break;
            }
        }
        repo.saveUsers(users);
        logger.info("User updated successfully");
        currentUser.email = email;
        currentUser.password = password;
        currentUser.birthDay = birthDay != null ? birthDay.toString() : null;
        
        // Refresh the profile image in case it was updated
        loadCurrentUserProfileImage();
    }

    @FXML
    public void onDelete(javafx.event.ActionEvent event) {
        try {
            // Remove user from UserDataRepository using the new deleteUser method
            com.starlight.repository.UserDataRepository repo = new com.starlight.repository.UserDataRepository();
            boolean userDeleted = repo.deleteUser(currentUser.username);
            
            if (!userDeleted) {
                logger.warning("User could not be deleted.");
                showResultDialog("account_deletion_failed");
                return;
            }
            
            // Clear current user session
            com.starlight.util.Session.setCurrentUser(null);
            
            // Execute logout callback if provided
            if (onLogout != null) {
                onLogout.run();
            }
            
            // Show success message first
            showResultDialog("account_deleted_success");
            
            // Load the authorization view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/Authorization.fxml"));
            Parent authView = loader.load();
            
            // Get the current stage
            Stage currentStage = (Stage) deleteaccbutton.getScene().getWindow();
            
            // Set the authorization view in the current stage
            javafx.scene.Scene scene = new javafx.scene.Scene(authView);
            currentStage.setScene(scene);
            currentStage.setTitle("Login/Register");
            currentStage.show();
            
            // Display a confirmation message
            logger.info("User account deleted successfully");
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load authorization view: " + e.getMessage(), e);
            showResultDialog("account_deletion_failed");
            
            // Fallback to previous behavior if authorization view can't be loaded
            Stage currentStage = (Stage) deleteaccbutton.getScene().getWindow();
            currentStage.hide();
            
            if (previousStage != null) {
                previousStage.show();
                previousStage.toFront();
                previousStage.requestFocus();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete account: " + e.getMessage(), e);
            showResultDialog("account_deletion_failed");
        }
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
                case "account_updated_success":
                    controller.setAccountUpdatedSuccess();
                    break;
                case "account_update_failed":
                    controller.setAccountUpdateFailed();
                    break;
                case "account_deleted_success":
                    controller.setAccountDeletedSuccess();
                    break;
                case "account_deletion_failed":
                    controller.setAccountDeletionFailed();
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
