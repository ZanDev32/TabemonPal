package com.starlight.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.models.User;
import com.starlight.util.Session;

/**
 * Controller for the user settings dialog where profile information can be
 * edited.
 * 
 * Architecture: Uses CommunityController's public utility methods for image loading
 * and scaling to maintain consistency and reduce code redundancy across the application.
 */
public class EditProfileController implements Initializable {
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
    
    // Use CommunityController for shared utility methods (image loading and scaling)
    private final CommunityController communityController = new CommunityController();

    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) welcomeLabel.setText("Hello, " + user.username);
        if (emailField != null) emailField.setText(user.email);
        if (passwordField != null) passwordField.setText(user.password);
        if (birthDayPicker != null && user.birthDay != null && !user.birthDay.isEmpty()) {
            birthDayPicker.setValue(java.time.LocalDate.parse(user.birthDay));
        }
        
        // Load and scale profile image using CommunityController methods
        if (Image != null) {
            communityController.loadImage(Image, user.profilepicture, "/com/starlight/images/missing.png");
            communityController.scaleToFit(Image, 170, 170, 200);
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
     * Loads the current user's profile image from UserData XML using CommunityController methods
     */
    private void loadCurrentUserProfileImage() {
        User currentSessionUser = Session.getCurrentUser();
        if (currentSessionUser != null && Image != null) {
            // Load profile image using CommunityController for consistency
            communityController.loadImage(Image, currentSessionUser.profilepicture, "/com/starlight/images/missing.png");
            communityController.scaleToFit(Image, 170, 170, 85); // Circular profile image with rounded corners
        }
    }
    
    /**
     * Handles the image picker button click to allow user to select a new profile image
     */
    @FXML
    private void onImagePickerClick(javafx.event.ActionEvent event) {
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
                System.err.println("No current session user found");
                return;
            }
            
            // Copy the selected file to the user's directory
            String copiedFilePath = com.starlight.util.FileSystemManager.copyFileToUserDirectoryWithUniqueFilename(
                selectedFile, currentSessionUser.username);
            
            if (copiedFilePath == null) {
                System.err.println("Failed to copy image file to user directory");
                return;
            }
            
            // Update the current user's profile picture path
            currentSessionUser.profilepicture = copiedFilePath;
            
            // Also update currentUser if it's set
            if (currentUser != null) {
                currentUser.profilepicture = copiedFilePath;
            }
            
            // Update the UserData XML file
            com.starlight.models.UserDataRepository repo = new com.starlight.models.UserDataRepository();
            java.util.List<com.starlight.models.User> users = repo.loadUsers();
            for (com.starlight.models.User u : users) {
                if (u.username.equals(currentSessionUser.username)) {
                    u.profilepicture = copiedFilePath;
                    break;
                }
            }
            repo.saveUsers(users);
            
            // Refresh the profile image display
            loadCurrentUserProfileImage();
            
            System.out.println("Profile image updated successfully: " + copiedFilePath);
        } catch (Exception e) {
            System.err.println("Failed to update profile image: " + e.getMessage());
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
                xs.allowTypesByWildcard(new String[]{"com.starlight.models.*"});
                xs.alias("user", User.class);
                String xml = xs.toXML(u);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(xml.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) {
                    System.out.println("User updated");
                } else {
                    System.out.println("Update failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            System.err.println("All fields are required.");
            return;
        }
        com.starlight.models.UserDataRepository repo = new com.starlight.models.UserDataRepository();
        java.util.List<com.starlight.models.User> users = repo.loadUsers();
        for (com.starlight.models.User u : users) {
            if (u.username.equals(currentUser.username)) {
                u.email = email;
                u.password = password;
                u.birthDay = birthDay != null ? birthDay.toString() : null;
                break;
            }
        }
        repo.saveUsers(users);
        System.out.println("User updated successfully");
        currentUser.email = email;
        currentUser.password = password;
        currentUser.birthDay = birthDay != null ? birthDay.toString() : null;
        
        // Refresh the profile image in case it was updated
        loadCurrentUserProfileImage();
    }

    @FXML
    private void onDelete(javafx.event.ActionEvent event) {
        // Remove user from UserDataRepository using the new deleteUser method
        com.starlight.models.UserDataRepository repo = new com.starlight.models.UserDataRepository();
        boolean userDeleted = repo.deleteUser(currentUser.username);
        
        if (!userDeleted) {
            System.err.println("User could not be deleted.");
            return;
        }
        
        // Clear current user session
        com.starlight.util.Session.setCurrentUser(null);
        
        // Execute logout callback if provided
        if (onLogout != null) {
            onLogout.run();
        }
        
        try {
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
            System.out.println("User account deleted successfully");
            
        } catch (IOException e) {
            System.err.println("Failed to load authorization view: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to previous behavior if authorization view can't be loaded
            Stage currentStage = (Stage) deleteaccbutton.getScene().getWindow();
            currentStage.hide();
            
            if (previousStage != null) {
                previousStage.show();
                previousStage.toFront();
                previousStage.requestFocus();
            }
        }
    }

}
