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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

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
        
        // Create and configure text bubble using TextFlow
        TextFlow textBubble = createTextBubble(message, isUser);
        
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
        ImageUtils.scaleToFit(avatar, 32, 32, 40);
        
        return avatar;
    }
    
    /**
     * Creates and configures a text bubble TextFlow for the chat message.
     *
     * @param message The message text to display
     * @param isUser True if this is a user message, false for bot message
     * @return Configured TextFlow for the text bubble
     */
    private TextFlow createTextBubble(String message, boolean isUser) {
        TextFlow textBubble = new TextFlow();
        
        // Basic configuration
        textBubble.setMaxWidth(640);
        textBubble.setPadding(new Insets(12, 15, 12, 15));
        textBubble.setTextAlignment(TextAlignment.LEFT);
        
        // Parse and add formatted text
        parseAndAddFormattedText(textBubble, message, isUser);
        
        // Apply styling based on sender
        if (isUser) {
            textBubble.setStyle(
                "-fx-background-color: #B8145B; " +
                "-fx-background-radius: 18 5 18 18;"
            );
        } else {
            textBubble.setStyle(
                "-fx-background-color: #FFFFFF; " +
                "-fx-background-radius: 5 18 18 18;"
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
     * Parses message text and adds formatted Text nodes to the TextFlow.
     * Supports **bold** markdown formatting.
     *
     * @param textFlow The TextFlow to add Text nodes to
     * @param message The message to parse
     * @param isUser True if this is a user message, false for bot message
     */
    private void parseAndAddFormattedText(TextFlow textFlow, String message, boolean isUser) {
        // Base text color
        Color textColor = isUser ? Color.WHITE : Color.rgb(63, 63, 91);
        Font baseFont = Font.font("Poppins", 14);
        Font boldFont = Font.font("Poppins", FontWeight.BOLD, 14);
        
        String[] parts = message.split("\\*\\*");
        
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                Text text = new Text(parts[i]);
                text.setFill(textColor);
                
                // Apply bold formatting to odd-indexed parts (text between **)
                if (i % 2 == 1) {
                    text.setFont(boldFont);
                } else {
                    text.setFont(baseFont);
                }
                
                textFlow.getChildren().add(text);
            }
        }
        
        // If no text was added (empty message), add a space
        if (textFlow.getChildren().isEmpty()) {
            Text text = new Text(" ");
            text.setFill(textColor);
            text.setFont(baseFont);
            textFlow.getChildren().add(text);
        }
    }
    
    /**
     * Gets the bot avatar path.
     *
     * @return Path to the bot avatar image
     */
    private String getBotAvatarPath() {
        // Return the same format as used in UserData.xml
        return "src/main/resources/com/starlight/images/botIcon.png";
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
        attachmentnama.setText("Image attachment not yet implemented");
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
        // Bind hasTextInput property to prompt text property
        hasTextInput.bind(prompt.textProperty().isNotEmpty());
        
        // Bind send button disable property to processing state and text input
        send.disableProperty().bind(isProcessing.or(hasTextInput.not()));
        
        // Bind attachment button disable property to processing state
        attachImage.disableProperty().bind(isProcessing);
        
        // Set up event handlers
        send.setOnAction(e -> handleUserMessage());
        attachImage.setOnAction(e -> handleAttachImage());
        prompt.setOnKeyPressed(this::handlePromptKeyPress);
        
        // Make the TextArea transparent
        prompt.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        // Add welcome message
        addBubble("**Welcome Pal!** 🌱\n" + 
                  "\n" + 
                  "**I'm here to help you with:** \n" + 
                  "🍏 Nutrition advice (meal planning, healthy eating) \n" + 
                  "🍎 Nutritious food choices (e.g., balanced meals, superfoods, recipes) \n" + 
                  "💪 Healthy lifestyle habits (exercise, sleep, stress management) \n" + 
                  "🔍 Tips & tricks (meal prep, portion control, eating out) \n" + 
                  "🚀 Motivation & mindset (staying consistent, goal-setting) \n" + 
                  "\n" + 
                  "**Ask me anything!** Examples:  \n" + 
                  "• \"What's a quick healthy breakfast?\" \n" + 
                  "• \"How can I reduce sugar cravings?\" \n" + 
                  "\n" + 
                  "Let's make healthy living simple and fun! 😊\n" + 
                  "\n" + 
                  "**What's your question today?**", 
            false);
    }
    
    // Getter methods for property access (useful for advanced bindings)
    public BooleanProperty isProcessingProperty() {
        return isProcessing;
    }
    
    public BooleanProperty hasTextInputProperty() {
        return hasTextInput;
    }
}
    