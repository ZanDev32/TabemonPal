package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Controller for the authorization panel that can switch between login and register views.
 */
public class AuthorizationController implements Initializable {
    
    @FXML
    private BorderPane authPanel;
    
    private Parent loginView;
    private Parent registerView;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Load both views
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/com/starlight/view/loginView.fxml"));
            loginView = loginLoader.load();
            
            FXMLLoader registerLoader = new FXMLLoader(getClass().getResource("/com/starlight/view/registerView.fxml"));
            registerView = registerLoader.load();
            
            // Get controllers and set their parent reference
            LoginViewController loginController = loginLoader.getController();
            RegisterViewController registerController = registerLoader.getController();
            
            loginController.setAuthorizationController(this);
            registerController.setAuthorizationController(this);
            
            // Show login view by default
            showLoginView();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Shows the login view in the right section of the BorderPane.
     */
    public void showLoginView() {
        authPanel.setRight(loginView);
    }
    
    /**
     * Shows the register view in the right section of the BorderPane.
     */
    public void showRegisterView() {
        authPanel.setRight(registerView);
    }
}