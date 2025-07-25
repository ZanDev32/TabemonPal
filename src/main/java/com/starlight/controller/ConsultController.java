package com.starlight.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.starlight.api.ChatbotAPI;
import com.starlight.api.ChatbotAPI.ChatbotException;
import com.starlight.model.User;
import com.starlight.util.ChatHistoryManager;
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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * Controller for the consult view with chatbot functionality.
 */
public class ConsultController implements Initializable {
    @FXML
    private MFXButton quickMessage1;

    @FXML
    private MFXButton quickMessage2;

    @FXML
    private MFXButton quickMessage3;

    private static final Logger logger = Logger.getLogger(ConsultController.class.getName());

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
    private ChatHistoryManager historyManager;
    
    // Properties for FXML binding
    private final BooleanProperty isProcessing = new SimpleBooleanProperty(false);
    private final BooleanProperty hasTextInput = new SimpleBooleanProperty(false);
    
    // Loading animation variables
    private Timeline loadingAnimation;
    private HBox loadingBubble;
    
    public ConsultController() {
        try {
            this.chatbotAPI = new ChatbotAPI();
            this.historyManager = new ChatHistoryManager();
            loadCurrentUser();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize chatbot: " + e.getMessage(), e);
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
                logger.warning("No current user found in session");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load current user: " + e.getMessage(), e);
        }
    }

    /**
     * Handles the user's message input.
     * This method can be bound directly to FXML using onAction="#handleUserMessage"
     */
    @FXML
    public void handleUserMessage(javafx.event.ActionEvent event) {
        handleUserMessage();
    }
    
    public void handleUserMessage() {
        String userInput = prompt.getText().trim();
        if (userInput.isEmpty()) return;

        // Set processing state
        isProcessing.set(true);
        
        addBubble(userInput, true); // user
        prompt.clear();
        
        // Make the TextArea transparent
        prompt.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        new Thread(() -> {
            // Add loading animation
            Platform.runLater(this::showLoadingBubble);
            
            try {
                String response = chatbotAPI.askNutritionQuestion(userInput);
                Platform.runLater(() -> {
                    hideLoadingBubble();
                    addBubble(response, false); // bot
                    isProcessing.set(false);
                });
            } catch (ChatbotException e) {
                Platform.runLater(() -> {
                    hideLoadingBubble();
                    addBubble("Sorry, I'm having trouble connecting right now. Please try again later.", false);
                    isProcessing.set(false);
                });
                logger.log(Level.WARNING, "Chatbot error: " + e.getMessage(), e);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoadingBubble();
                    addBubble("An unexpected error occurred. Please try again.", false);
                    isProcessing.set(false);
                });
                logger.log(Level.SEVERE, "Unexpected error: " + e.getMessage(), e);
            }
        }).start();
    }
    
    /**
     * Adds a chat bubble to the UI and saves the message to history
     *
     * @param message The message to display.
     * @param isUser  True if the message is from the user, false if from the assistant.
     */
    private void addBubble(String message, boolean isUser) {
        addBubble(message, isUser, true);
    }
    
    /**
     * Adds a chat bubble to the UI with option to save to history
     *
     * @param message The message to display.
     * @param isUser  True if the message is from the user, false if from the assistant.
     * @param saveToHistory Whether to save this message to chat history
     */
    private void addBubble(String message, boolean isUser, boolean saveToHistory) {
        if (message == null || message.trim().isEmpty()) {
            return; // Don't add empty messages
        }

        // Save to chat history if enabled and not a welcome message
        if (saveToHistory && historyManager != null && currentUser != null) {
            // Skip saving welcome messages (messages that contain "Welcome Pal!")
            if (!message.contains("Welcome Pal!")) {
                historyManager.addMessage(message, isUser, currentUser.username);
            }
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
    public void handleAttachImage(javafx.event.ActionEvent event) {
        handleAttachImage();
    }
    
    public void handleAttachImage() {
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

    /**
     * Array of predefined quick questions that rotate randomly
     */
    private static final String[] QUICK_QUESTIONS = {
        "What is TabemonPal?",
        "Why is healthy eating important?", 
        "What are superfoods?",
        "What are the top 5 Indonesian superfoods?",
        "How can I start eating healthier?",
        "What nutrients should I focus on daily?",
        "How do I plan balanced meals?",
        "What are some healthy Indonesian dishes?"
    };
    
    /**
     * Handles quick message button 1 click
     */
    @FXML
    public void handleQuickMessage1(javafx.event.ActionEvent event) {
        sendQuickMessage(quickMessage1.getText());
    }
    
    /**
     * Handles quick message button 2 click
     */
    @FXML
    public void handleQuickMessage2(javafx.event.ActionEvent event) {
        sendQuickMessage(quickMessage2.getText());
    }
    
    /**
     * Handles quick message button 3 click
     */
    @FXML
    public void handleQuickMessage3(javafx.event.ActionEvent event) {
        sendQuickMessage(quickMessage3.getText());
    }
    
    /**
     * Sends a predefined question to the AI chatbot
     * @param question The question to send
     */
    private void sendQuickMessage(String question) {
        if (question == null || question.trim().isEmpty()) return;
        
        // Disable processing if already in progress
        if (isProcessing.get()) {
            logger.info("Already processing a message, ignoring quick message");
            return;
        }
        
        // Set the question in the prompt area (optional, for user feedback)
        prompt.setText(question);
        
        // Send the message using existing logic
        handleUserMessage();
        
        // Update quick message buttons with new random questions after sending
        Platform.runLater(this::updateQuickMessageButtons);
    }
    
    /**
     * Updates quick message buttons with random questions from the predefined list
     */
    private void updateQuickMessageButtons() {
        java.util.Random random = new java.util.Random();
        java.util.List<String> availableQuestions = new java.util.ArrayList<>(java.util.Arrays.asList(QUICK_QUESTIONS));
        
        // Remove current button texts to avoid duplicates
        availableQuestions.remove(quickMessage1.getText());
        availableQuestions.remove(quickMessage2.getText());
        availableQuestions.remove(quickMessage3.getText());
        
        // If we have enough questions left, pick 3 random ones
        if (availableQuestions.size() >= 3) {
            quickMessage1.setText(availableQuestions.remove(random.nextInt(availableQuestions.size())));
            quickMessage2.setText(availableQuestions.remove(random.nextInt(availableQuestions.size())));
            quickMessage3.setText(availableQuestions.remove(random.nextInt(availableQuestions.size())));
        } else {
            // Fallback to shuffling all questions if we don't have enough unique ones left
            java.util.Collections.shuffle(availableQuestions);
            if (availableQuestions.size() > 0) quickMessage1.setText(availableQuestions.get(0));
            if (availableQuestions.size() > 1) quickMessage2.setText(availableQuestions.get(1));
            if (availableQuestions.size() > 2) quickMessage3.setText(availableQuestions.get(2));
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
        
        // Add welcome message (don't save to history)
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
            false, false);
            
        // Initialize quick message buttons with random questions
        updateQuickMessageButtons();
        
        // Start chat history session if user is available
        if (currentUser != null && historyManager != null) {
            historyManager.startSession(currentUser.username);
        }
        
        // Load previous chat history if available
        loadRecentChatHistory();
    }
    
    /**
     * Shows a loading bubble with animated dots to indicate processing.
     */
    private void showLoadingBubble() {
        if (loadingBubble != null) {
            hideLoadingBubble(); // Remove any existing loading bubble
        }
        
        // Create main container for the loading bubble row
        loadingBubble = new HBox(18);
        loadingBubble.setAlignment(Pos.TOP_LEFT);
        loadingBubble.setPadding(new Insets(5, 10, 5, 10));

        // Create and configure bot avatar
        ImageView avatar = createAvatar(false);
        
        // Create text bubble for loading animation
        TextFlow textBubble = new TextFlow();
        textBubble.setMaxWidth(640);
        textBubble.setPadding(new Insets(12, 15, 12, 15));
        textBubble.setTextAlignment(TextAlignment.LEFT);
        
        // Create the processing text
        Text processingText = new Text("Processing");
        processingText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        processingText.setFill(Color.rgb(51, 51, 51));
        
        Text dotsText = new Text("");
        dotsText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        dotsText.setFill(Color.rgb(51, 51, 51));
        
        textBubble.getChildren().addAll(processingText, dotsText);
        
        // Apply bot styling
        textBubble.setStyle(
            "-fx-background-color: #FFFFFF; " +
            "-fx-background-radius: 5 18 18 18;"
        );
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(4);
        textBubble.setEffect(dropShadow);

        // Add components to the row
        loadingBubble.getChildren().addAll(avatar, textBubble);

        // Add to the chat container
        bubblelist.getChildren().add(loadingBubble);

        // Auto-scroll to bottom
        Platform.runLater(() -> {
            if (bubblelist.getParent() instanceof javafx.scene.control.ScrollPane) {
                javafx.scene.control.ScrollPane scrollPane = (javafx.scene.control.ScrollPane) bubblelist.getParent();
                scrollPane.setVvalue(1.0);
            }
        });
        
        // Start the animation
        startLoadingAnimation(dotsText);
    }
    
    /**
     * Hides the loading bubble and stops the animation.
     */
    private void hideLoadingBubble() {
        if (loadingAnimation != null) {
            loadingAnimation.stop();
            loadingAnimation = null;
        }
        
        if (loadingBubble != null) {
            bubblelist.getChildren().remove(loadingBubble);
            loadingBubble = null;
        }
    }
    
    /**
     * Starts the loading animation with cycling dots.
     */
    private void startLoadingAnimation(Text dotsText) {
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }
        
        loadingAnimation = new Timeline(
            new KeyFrame(Duration.millis(500), e -> dotsText.setText("")),
            new KeyFrame(Duration.millis(1000), e -> dotsText.setText(".")),
            new KeyFrame(Duration.millis(1500), e -> dotsText.setText("..")),
            new KeyFrame(Duration.millis(2000), e -> dotsText.setText("..."))
        );
        
        loadingAnimation.setCycleCount(Timeline.INDEFINITE);
        loadingAnimation.play();
    }
    
    /**
     * Loads recent chat history and displays it in the chat view.
     * This method loads the most recent chat session (excluding the current one)
     * to show conversation continuity.
     */
    private void loadRecentChatHistory() {
        if (currentUser == null || historyManager == null) {
            return;
        }
        
        try {
            var histories = historyManager.loadUserHistory(currentUser.username);
            
            // Skip the first history if it's the current session
            int startIndex = 0;
            if (!histories.isEmpty() && historyManager.hasActiveSession()) {
                var currentSession = historyManager.getCurrentSession();
                if (currentSession != null && 
                    histories.get(0).sessionId.equals(currentSession.sessionId)) {
                    startIndex = 1;
                }
            }
            
            // Load messages from the most recent completed session (limit to avoid UI overload)
            if (histories.size() > startIndex) {
                var recentHistory = histories.get(startIndex);
                if (recentHistory.messages != null && !recentHistory.messages.isEmpty()) {
                    
                    // Add a separator message to show this is from previous session
                    addBubble("─── *Previous conversation from " + 
                             recentHistory.sessionStart.substring(0, 10) + "* ───", 
                             false, false);
                    
                    // Load up to 10 most recent messages to avoid overwhelming the UI
                    var messages = recentHistory.messages;
                    int startMessageIndex = Math.max(0, messages.size() - 10);
                    
                    for (int i = startMessageIndex; i < messages.size(); i++) {
                        var msg = messages.get(i);
                        if (msg != null && msg.content != null && !msg.content.trim().isEmpty()) {
                            addBubble(msg.content, msg.isUser, false);
                        }
                    }
                    
                    // Add another separator to distinguish from new conversation
                    addBubble("─── *New conversation* ───", false, false);
                    
                    logger.info("Loaded " + (messages.size() - startMessageIndex) + 
                               " messages from previous session: " + recentHistory.sessionId);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load recent chat history: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup method to stop any running animations and end chat session when the controller is destroyed.
     * This should be called when switching views or closing the application.
     */
    public void cleanup() {
        hideLoadingBubble();
        
        // End the current chat session
        if (historyManager != null && historyManager.hasActiveSession()) {
            historyManager.endSession();
            logger.info("Chat session ended during cleanup");
        }
    }
    
    // Getter methods for property access (useful for advanced bindings)
    public BooleanProperty isProcessingProperty() {
        return isProcessing;
    }
    
    public BooleanProperty hasTextInputProperty() {
        return hasTextInput;
    }
    
    /**
     * Gets the chat history manager for external access.
     * 
     * @return The chat history manager instance
     */
    public ChatHistoryManager getHistoryManager() {
        return historyManager;
    }
    
    /**
     * Manually ends the current chat session.
     * This can be called from external controllers or when user explicitly wants to end session.
     */
    public void endCurrentSession() {
        if (historyManager != null && historyManager.hasActiveSession()) {
            historyManager.endSession();
            logger.info("Chat session ended manually");
        }
    }
    
    /**
     * Starts a new chat session, ending the current one if active.
     */
    public void startNewSession() {
        if (currentUser != null && historyManager != null) {
            historyManager.startSession(currentUser.username);
            logger.info("New chat session started");
        }
    }
    
    /**
     * Clears the current chat UI and optionally starts a new session.
     * 
     * @param startNewSession Whether to start a new chat session after clearing
     */
    public void clearChat(boolean startNewSession) {
        // Clear the chat UI
        bubblelist.getChildren().clear();
        
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
            false, false);
        
        if (startNewSession) {
            startNewSession();
        }
    }
}
