package com.starlight.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.starlight.api.ApiClient;
import com.starlight.api.ApiClient.ApiException;
import com.starlight.models.User;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Controller used for registering a new user account within the Authorization panel.
 */
public class RegisterViewController implements Initializable {
    @FXML
    private VBox registerView;

    @FXML
    private ImageView AppLogo;
    
    @FXML
    private Label errorMessage;

    @FXML
    private MFXTextField username;

    @FXML
    private MFXTextField email;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXButton register;

    @FXML
    private MFXButton login;
    
    private final ApiClient apiClient = new ApiClient();
    
    private AuthorizationController authController;
    
    /**
     * Sets the parent authorization controller for view switching.
     */
    public void setAuthorizationController(AuthorizationController controller) {
        this.authController = controller;
    }

    /**
     * Handles register button click event.
     */
    @FXML
    void handleRegister(MouseEvent event) {
        performRegister();
    }
    
    /**
     * Handles Enter key press in any field.
     */
    @FXML
    void handleRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            performRegister();
        }
    }
    
    /**
     * Common registration logic shared between mouse click and Enter key press.
     */
    private void performRegister() {
        String user = username.getText();
        String em = email.getText();
        String pass = password.getText();
        
        // Clear any previous error
        hideError();
        
        // More detailed validation
        if (user.isEmpty()) {
            showError("Please enter a username");
            return;
        }
        
        if (em.isEmpty()) {
            showError("Please enter an email address");
            return;
        }
        
        if (pass.isEmpty()) {
            showError("Please enter a password");
            return;
        }
        
        if (pass.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }
        
        try {
            User registered = apiClient.register(user, em, pass);
            System.out.println("Register success for user: " + registered.username);
            
            // Show success message before switching view
            showSuccess("Account created successfully! Please log in.");
            
            // Switch back to login view after successful registration
            if (authController != null) {
                authController.showLoginView();
            }
        } catch (ApiException e) {
            System.out.println("Register failed: " + e.getMessage());
            
            // Show user-friendly error message
            if (e.getStatusCode() == 409) {
                showError("An account with this email already exists. Please use a different email.");
            } else {
                showError(e.getMessage());
            }
        }
    }
    
    /**
     * Handles login button click by switching to login view.
     */
    @FXML
    void handleLogin(MouseEvent event) {
        if (authController != null) {
            authController.showLoginView();
        }
    }

    
    /**
     * Shows an error message to the user
     */
    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText(message);
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
        }
    }
    
    /**
     * Hides the error message
     */
    private void hideError() {
        if (errorMessage != null) {
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
        }
    }
    
    /**
     * Shows a success message to the user
     */
    private void showSuccess(String message) {
        if (errorMessage != null) {
            errorMessage.setText(message);
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
            errorMessage.setTextFill(Color.GREEN); // Change color for success
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add key event handlers
        username.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performRegister();
            }
        });
    
        email.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performRegister();
            }
        });
    
        password.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performRegister();
            }
        });
        
        // Initialize error message
        if (errorMessage != null) {
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
            errorMessage.setTextFill(Color.RED);
        }
    }
}
