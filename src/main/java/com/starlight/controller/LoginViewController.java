package com.starlight.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import com.starlight.App;
import com.starlight.api.ApiClient;
import com.starlight.api.ApiClient.ApiException;
import com.starlight.models.User;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Controller handling user login and transition to the main application.
 */
public class LoginViewController implements Initializable {
    private static final Logger logger = Logger.getLogger(LoginViewController.class.getName());
    @FXML
    private VBox loginView;

    @FXML
    private ImageView AppLogo;

    @FXML
    private Label username;
    
    @FXML
    private Label errorMessage;

    @FXML
    private MFXTextField emailOrUsername;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXButton loginButton;

    @FXML
    private MFXButton register;
    
    private final ApiClient apiClient = new ApiClient();
    
    private AuthorizationController authController;
    
    /**
     * Sets the parent authorization controller for view switching.
     */
    public void setAuthorizationController(AuthorizationController controller) {
        this.authController = controller;
    }

    /**
     * Handles login button click event.
     */
    @FXML
    void handleLogin(MouseEvent event) {
        performLogin();
    }
    
    /**
     * Handles register button click event by switching to register view.
     */
    @FXML
    void handleRegister(MouseEvent event) {
        if (authController != null) {
            authController.showRegisterView();
        }
    }
    
    /**
     * Common login logic shared between mouse click and Enter key press.
     */
    private void performLogin() {
        String em = emailOrUsername.getText();
        String pass = password.getText();
        
        // Clear any previous error message
        hideError();
        
        // Basic validation
        if (em.isEmpty() || pass.isEmpty()) {
            showError("Please enter both username/email and password");
            return;
        }
        
        try {
            User logged = apiClient.login(em, pass);
            Session.setCurrentUser(logged);
            logger.info("Login success for user: " + logged.username);
            
            // Ensure we redirect to main.fxml after successful login
            App.loadMainWithSplash();
        } catch (ApiException e) {
            logger.warning("Login failed: " + e.getMessage());
            
            // Show user-friendly error message
            if (e.getStatusCode() == 401) {
                showError("Login failed: The username/email or password is incorrect");
            } else {
                showError(e.getMessage());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add key event handlers
        password.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performLogin();
            }
        });

        emailOrUsername.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performLogin();
            }
        });
        
        // Initialize error message
        if (errorMessage != null) {
            errorMessage.setVisible(false);
            errorMessage.setManaged(false);
            errorMessage.setTextFill(Color.RED);
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
}