package com.starlight.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.starlight.api.ChatbotAPI;
import com.starlight.api.ChatbotAPI.ChatbotException;
import com.starlight.models.User;
import com.starlight.util.ImageUtils;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Controller for the consult view with chatbot functionality.
 */
public class ConsultController implements Initializable {
    @FXML
    private Label attachmentnama;

    @FXML
    private MFXButton attachImage;

    @FXML
    private MFXButton send;

    @FXML
    private VBox bubblelist;

    @FXML
    private TextArea prompt;
    
    private ChatbotAPI chatbotAPI;
    private User currentUser;
    
    // Properties for FXML binding
    private final BooleanProperty isProcessing = new SimpleBooleanProperty(false);
    private final StringProperty attachmentStatus = new SimpleStringProperty("No attachment");
    private final BooleanProperty hasTextInput = new SimpleBooleanProperty(false);
    
    public ConsultController() {
        try {
            this.chatbotAPI = new ChatbotAPI();
            loadCurrentUser();
        } catch (Exception e) {
            System.err.println("Failed to initialize chatbot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the current user from the session.
     */
    private void loadCurrentUser() {
        try {
            // Get the current user from the session instead of loading from repository
            currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                System.err.println("No current user found in session");
            }
        } catch (Exception e) {
            System.err.println("Failed to load current user: " + e.getMessage());
        }
    }

    /**
     * Handles the user's message input.
     * This method can be bound directly to FXML using onAction="#handleUserMessage"
     */
    @FXML
    private void handleUserMessage() {
        String userInput = prompt.getText().trim();
        if (userInput.isEmpty()) return;

        // Set processing state
        isProcessing.set(true);
        
        addBubble(userInput, true); // user
        prompt.clear();
        
        // Make the TextArea transparent
        prompt.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        new Thread(() -> {
            try {
                String response = chatbotAPI.askNutritionQuestion(userInput);
                Platform.runLater(() -> {
                    addBubble(response, false); // bot
                    isProcessing.set(false);
                });
            } catch (ChatbotException e) {
                Platform.runLater(() -> {
                    addBubble("Sorry, I'm having trouble connecting right now. Please try again later.", false);
                    isProcessing.set(false);
                });
                System.err.println("Chatbot error: " + e.getMessage());
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addBubble("An unexpected error occurred. Please try again.", false);
                    isProcessing.set(false);
                });
                System.err.println("Unexpected error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Adds a chat bubble to the UI 
     *
     * @param message The message to display.
     * @param isUser  True if the message is from the user, false if from the assistant.
     */
    private void addBubble(String message, boolean isUser) {
        if (message == null || message.trim().isEmpty()) {
            return; // Don't add empty messages
        }

        // Create main container for the bubble row
        HBox bubble = new HBox(18);
        bubble.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        bubble.setPadding(new Insets(5, 10, 5, 10));

        // Create and configure avatar
        ImageView avatar = createAvatar(isUser);
        
        // Create and configure text bubble
        Label textBubble = createTextBubble(message, isUser);
        
        // Add components to the row in correct order
        if (isUser) {
            bubble.getChildren().addAll(textBubble, avatar);
        } else {
            bubble.getChildren().addAll(avatar, textBubble);
        }

        // Add to the chat container
        bubblelist.getChildren().add(bubble);

        // Auto-scroll to bottom
        Platform.runLater(() -> {
            if (bubblelist.getParent() instanceof javafx.scene.control.ScrollPane) {
                javafx.scene.control.ScrollPane scrollPane = (javafx.scene.control.ScrollPane) bubblelist.getParent();
                scrollPane.setVvalue(1.0);
            }
        });
    }
    
    /**
     * Creates and configures an avatar ImageView for the chat bubble.
     *
     * @param isUser True if this is a user avatar, false for bot avatar
     * @return Configured ImageView for the avatar
     */
    private ImageView createAvatar(boolean isUser) {
        ImageView avatar = new ImageView();
        
        // Set up the avatar image - mimic CommunityController approach
        String avatarPath = isUser ? getUserAvatarPath() : getBotAvatarPath();
        
        // Load image with fallback, using the same format as UserData.xml
        ImageUtils.loadImage(avatar, avatarPath, "src/main/resources/com/starlight/images/missing.png");
        
        // Apply scaling and styling
        ImageUtils.scaleToFit(avatar, 32, 32, 16);
        
        return avatar;
    }
    
    /**
     * Creates and configures a text bubble Label for the chat message.
     *
     * @param message The message text to display
     * @param isUser True if this is a user message, false for bot message
     * @return Configured Label for the text bubble
     */
    private Label createTextBubble(String message, boolean isUser) {
        Label textBubble = new Label(message);
        
        // Basic text configuration
        textBubble.setWrapText(true);
        textBubble.setMaxWidth(640);
        textBubble.setPadding(new Insets(12, 15, 12, 15));
        textBubble.setTextAlignment(TextAlignment.LEFT);
        
        // Apply styling based on sender
        if (isUser) {
            textBubble.setStyle(
                "-fx-background-color: #B8145B; " +
                "-fx-background-radius: 18 5 18 18; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Poppins'; " +
                "-fx-text-fill: #ffffffff;"
            );
        } else {
            textBubble.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 5 18 18 18; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Poppins'; " +
                "-fx-text-fill: #3F3F5B;"
            );
        }
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(4);
        textBubble.setEffect(dropShadow);
        
        return textBubble;
    }
    
    /**
     * Gets the bot avatar path.
     *
     * @return Path to the bot avatar image
     */
    private String getBotAvatarPath() {
        // Return the same format as used in UserData.xml
        return "src/main/resources/com/starlight/images/dummy/profilewoman.jpg";
    }
    
    /**
     * Gets the avatar path for the current user.
     */
    private String getUserAvatarPath() {
        if (currentUser != null && currentUser.profilepicture != null && !currentUser.profilepicture.isEmpty()) {
            return currentUser.profilepicture;
        }
        
        // Return the same format as used in UserData.xml for default avatar
        return "src/main/resources/com/starlight/images/dummy/profiledefault.png";
    }
    
    /**
     * Handles image attachment functionality.
     * This method can be bound directly to FXML using onAction="#handleAttachImage"
     */
    @FXML
    private void handleAttachImage() {
        // TODO: Implement image attachment functionality
        attachmentStatus.set("Image attachment not yet implemented");
        attachmentnama.setText(attachmentStatus.get());
    }
    
    /**
     * Handles key events on the prompt TextArea.
     * This method can be bound directly to FXML using onKeyPressed="#handlePromptKeyPress"
     */
    @FXML
    private void handlePromptKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            event.consume();
            handleUserMessage();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize FXML bindings
        setupBindings();
        
        // Make the TextArea transparent
        prompt.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        // Add welcome message
        addBubble("Hello! I'm Fumo, your nutrition assistant. How can I help you today?", false);
    }
    
    /**
     * Sets up property bindings for reactive UI updates.
     * This replaces manual event handler setup with declarative bindings.
     */
    private void setupBindings() {
        // Bind hasTextInput property to prompt text property
        hasTextInput.bind(prompt.textProperty().isNotEmpty());
        
        // Bind send button disable property to processing state and text input
        send.disableProperty().bind(isProcessing.or(hasTextInput.not()));
        
        // Bind attachment button disable property to processing state
        attachImage.disableProperty().bind(isProcessing);
        
        // Bind attachment label text to attachment status property
        attachmentnama.textProperty().bind(attachmentStatus);
        
        // Note: For FXML binding, you would typically use these in the FXML file:
        // <MFXButton fx:id="send" onAction="#handleUserMessage" />
        // <MFXButton fx:id="attachImage" onAction="#handleAttachImage" />
        // <TextArea fx:id="prompt" onKeyPressed="#handlePromptKeyPress" />
        
        // Fallback programmatic bindings for compatibility
        if (send.getOnAction() == null) {
            send.setOnAction(e -> handleUserMessage());
        }
        if (attachImage.getOnAction() == null) {
            attachImage.setOnAction(e -> handleAttachImage());
        }
        if (prompt.getOnKeyPressed() == null) {
            prompt.setOnKeyPressed(this::handlePromptKeyPress);
        }
    }
    
    // Getter methods for property access (useful for advanced bindings)
    public BooleanProperty isProcessingProperty() {
        return isProcessing;
    }
    
    public StringProperty attachmentStatusProperty() {
        return attachmentStatus;
    }
    
    public BooleanProperty hasTextInputProperty() {
        return hasTextInput;
    }
}
    