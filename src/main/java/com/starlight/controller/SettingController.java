package com.starlight.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.starlight.api.ChatbotAPI;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class SettingController {
    private static final Logger logger = Logger.getLogger(SettingController.class.getName());
    
    @FXML
    private VBox loginView;

    @FXML
    private MFXButton goBack;

    @FXML
    private MFXPasswordField secret_key;

    @FXML
    private MFXButton saveButton;

    private MainController mainController;
    private XStream xstream;

    @FXML
    private void initialize() {
        // Initialize XStream for XML serialization
        this.xstream = new XStream(new DomDriver());
        this.xstream.allowTypesByWildcard(new String[]{"com.starlight.api.*"});
        this.xstream.alias("config", ChatbotAPI.Config.class);
        this.xstream.aliasField("openai-key", ChatbotAPI.Config.class, "openaiKey");
        
        // Load existing API key if available
        loadExistingApiKey();
        
        // Add key event handler for Enter key
        secret_key.setOnKeyPressed(this::handleKeyPressed);
    }
    
    /**
     * Sets the main controller reference for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    /**
     * Handles the go back button click - navigates back to previous view
     */
    @FXML
    private void goBack(MouseEvent event) {
        if (mainController != null) {
            // Navigate back to home view using the same pattern as navigateToCommunity
            mainController.loadPage("home");
        }
    }
    
    /**
     * Handles key press events on the secret key field
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            saveApiKey();
        }
    }
    
    /**
     * Saves the API key to the configuration file
     */
    @FXML
    private void saveApiKey() {
        String apiKey = secret_key.getText();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            showAlert("Error", "Please enter a valid API key.", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            // Create the directory if it doesn't exist
            String userHome = System.getProperty("user.home");
            Path configDir = Paths.get(userHome, ".tabemonpal", "Database");
            Files.createDirectories(configDir);
            
            // Create config object
            ChatbotAPI.Config config = new ChatbotAPI.Config();
            config.setOpenaiKey(apiKey.trim());
            
            // Save to XML file
            File configFile = configDir.resolve(".SECRET_KEY.xml").toFile();
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                xstream.toXML(config, fos);
            }
            
            showAlert("Success", "API key saved successfully! The chatbot functionality is now available.", Alert.AlertType.INFORMATION);
            logger.info("API key saved successfully to: " + configFile.getAbsolutePath());
            
            // Clear the password field for security
            secret_key.clear();
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save API key: " + e.getMessage(), e);
            showAlert("Error", "Failed to save API key: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Loads existing API key from the configuration file if it exists
     */
    private void loadExistingApiKey() {
        try {
            String userHome = System.getProperty("user.home");
            Path configFile = Paths.get(userHome, ".tabemonpal", "Database", ".SECRET_KEY.xml");
            
            if (Files.exists(configFile)) {
                ChatbotAPI.Config config = (ChatbotAPI.Config) xstream.fromXML(configFile.toFile());
                if (config.getOpenaiKey() != null && !config.getOpenaiKey().trim().isEmpty() 
                    && !config.getOpenaiKey().equals("your-openai-api-key-here")) {
                    // Show placeholder text to indicate key is already set
                    secret_key.setPromptText("API key is already configured");
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load existing API key: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shows an alert dialog with the specified message
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
