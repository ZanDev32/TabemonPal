package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import com.starlight.util.DoughnutChart;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import com.starlight.models.Post;
import com.starlight.models.Nutrition;
import com.starlight.util.ImageUtils;
import com.starlight.repository.UserDataRepository;
import com.starlight.repository.PostDataRepository;
import com.starlight.api.ChatbotAPI;
import com.starlight.util.NutritionParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PostController implements Initializable {
    @FXML
    private MFXButton doAnalysis;

    @FXML
    private MFXButton goBack;

    @FXML
    private MFXButton verdict;

    @FXML
    private VBox recipeContainer;

    @FXML
    private DoughnutChart nutritionFacts;

    @FXML
    private VBox postTemplate;

    @FXML
    private VBox post1;

    @FXML
    private ImageView profile1;

    @FXML
    private Label username;

    @FXML
    private Label uploadtime;


    @FXML
    private Label description;

    @FXML
    private ImageView recentphoto1;
    
    private Post currentPost;
    private final UserDataRepository userRepository = new UserDataRepository();
    private final PostDataRepository postRepository = new PostDataRepository();
    private final CommunityController communityController = new CommunityController();
    private final ChatbotAPI chatbotAPI = new ChatbotAPI();
    private final NutritionParser nutritionParser = new NutritionParser();
    private MainController mainController; // Reference to main controller for navigation
    private static final Logger logger = Logger.getLogger(PostController.class.getName());
    private static final int MAX_RETRIES = 3;

    @FXML
    private void initialize() {
        // Initialize with placeholder data or wait for post data to be set
    }
    
    /**
     * Handles the go back button click - navigates back to community view
     */
    @FXML
    private void goBack(javafx.scene.input.MouseEvent event) {
        if (mainController != null) {
            // Navigate back to community view with proper button selection
            mainController.navigateToCommunity();
        }
    }
    
    /**
     * Handles the do analysis button click - performs nutrition analysis on current recipe
     */
    @FXML
    private void doAnalysis(javafx.event.ActionEvent event) {
        if (currentPost == null) {
            logger.warning("Cannot perform analysis - no current post available");
            return;
        }
        
        if (currentPost.ingredients == null || currentPost.ingredients.trim().isEmpty()) {
            logger.warning("Cannot perform analysis - no ingredients available");
            showAnalysisErrorDialog("No ingredients found in this recipe to analyze.");
            return;
        }
        
        // Show processing dialog and start analysis
        showProcessingAndAnalyzeNutrition();
    }
    
    /**
     * Sets the main controller reference for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
    
    /**
     * Sets the post data and updates the UI
     */
    public void setPost(Post post) {
        this.currentPost = post;
        updateUIFromPost();
    }
    
    /**
     * Updates all UI elements with the current post data
     */
    private void updateUIFromPost() {
        if (currentPost == null) return;
        
        if (description != null) {
            description.setText(currentPost.description != null ? currentPost.description : "");
        }
        
        if (uploadtime != null) {
            uploadtime.setText(formatRelativeTime(currentPost.uploadtime));
        }
        
        // Update username with display name from UserData
        if (username != null) {
            String displayUsername = getDisplayUsernameForUser(currentPost.username);
            username.setText(displayUsername != null ? displayUsername : currentPost.username);
        }
        
        // Update verdict button if available
        if (verdict != null && currentPost.nutrition != null) {
            String verdictText = currentPost.nutrition.verdict != null ? 
                currentPost.nutrition.verdict : "Unknown";
            verdict.setText(verdictText);
            
            // Apply different styling based on verdict
            verdict.getStyleClass().clear();
            verdict.getStyleClass().add("food-tag");
            switch (verdictText) {
                case "Healthy":
                    verdict.getStyleClass().add("verdict-healthy");
                    break;
                case "Moderate":
                    verdict.getStyleClass().add("verdict-moderate");
                    break;
                case "Unhealthy":
                    verdict.getStyleClass().add("verdict-unhealthy");
                    break;
                case "Junk Food":
                    verdict.getStyleClass().add("verdict-junk");
                    break;
                case "Unknown":
                    verdict.getStyleClass().add("verdict-unknown");
                    break;
                default:
                    verdict.getStyleClass().add("verdict-unknown");
                    break;
            }
        } else if (verdict != null) {
            verdict.setText("Unknown");
            verdict.getStyleClass().clear();
            verdict.getStyleClass().add("food-tag");
            verdict.getStyleClass().add("verdict-unknown");
        }
        
        // Update doAnalysis button state based on current nutrition status
        if (doAnalysis != null) {
            if (currentPost.nutrition != null && currentPost.nutrition.verdict != null && !currentPost.nutrition.verdict.equals("Unknown")) {
                // Recipe has been analyzed - button can re-analyze
                doAnalysis.setText("Re-analyze");
                doAnalysis.setDisable(false);
            } else {
                // Recipe hasn't been analyzed - button can analyze
                doAnalysis.setText("Analyze");
                doAnalysis.setDisable(false);
            }
            
            // Disable if no ingredients available
            if (currentPost.ingredients == null || currentPost.ingredients.trim().isEmpty()) {
                doAnalysis.setText("No Ingredients");
                doAnalysis.setDisable(true);
            }
        }
        
        // Load profile picture
        if (profile1 != null) {
            String profilePicture = getProfilePictureForUser(currentPost.username);
            ImageUtils.loadImage(profile1, profilePicture, "/com/starlight/images/missing.png");
            ImageUtils.scaleToFit(profile1, 80, 80, 500);
        }
        
        // Load post image
        if (recentphoto1 != null) {
            ImageUtils.loadImage(recentphoto1, currentPost.image, "/com/starlight/images/missing.png");
            ImageUtils.scaleToFit(recentphoto1, 1270, 990, 20);
        }
        
        // Populate recipe container with formatted text
        populateRecipeContainer();
        
        // Populate nutrition facts chart
        populateNutritionFacts();
    }
    
    /**
     * Populates the recipe container with formatted ingredients and directions
     */
    private void populateRecipeContainer() {
        if (recipeContainer == null || currentPost == null) return;
        
        recipeContainer.getChildren().clear();
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("post-recipe");
        
        // Add title section
        if (currentPost.title != null && !currentPost.title.trim().isEmpty()) {
            addSectionTitle(textFlow, currentPost.title);
            addNewLine(textFlow, 2);
        }
        
        // Add ingredients section
        if (currentPost.ingredients != null && !currentPost.ingredients.trim().isEmpty()) {
            addSectionTitle(textFlow, "Ingredients :");
            addNewLine(textFlow, 1);
            // Parse vertical line-separated ingredients as newlines
            String ingredientsWithNewlines = currentPost.ingredients.replace("|", "\n");
            addBulletList(textFlow, ingredientsWithNewlines);
            addNewLine(textFlow, 1);
        }
        
        // Add directions section
        if (currentPost.directions != null && !currentPost.directions.trim().isEmpty()) {
            addSectionTitle(textFlow, "Directions :");
            addNewLine(textFlow, 1);
            // Parse vertical line-separated directions as newlines
            String directionsWithNewlines = currentPost.directions.replace("|", "\n");
            addNumberedList(textFlow, directionsWithNewlines);
        }
        
        recipeContainer.getChildren().add(textFlow);
    }
    
    /**
     * Adds a bold section title to the TextFlow
     */
    private void addSectionTitle(TextFlow textFlow, String titleText) {
        Text title = new Text("**" + titleText + "**");
        parseAndAddFormattedText(textFlow, title.getText());
    }
    
    /**
     * Adds bullet list items to the TextFlow
     */
    private void addBulletList(TextFlow textFlow, String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                Text bullet = new Text("• " + line);
                bullet.setFont(Font.font("Poppins", 24));
                bullet.setFill(Color.rgb(63, 63, 91)); // #3F3F5B
                textFlow.getChildren().add(bullet);
                addNewLine(textFlow, 1);
            }
        }
    }
    
    /**
     * Adds numbered list items to the TextFlow
     */
    private void addNumberedList(TextFlow textFlow, String content) {
        String[] lines = content.split("\n");
        int counter = 1;
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                Text numberedItem = new Text(counter + ". " + line);
                numberedItem.setFont(Font.font("Poppins", 24));
                numberedItem.setFill(Color.rgb(63, 63, 91)); // #3F3F5B
                textFlow.getChildren().add(numberedItem);
                addNewLine(textFlow, 1);
                counter++;
            }
        }
    }
    
    /**
     * Adds newline characters to the TextFlow
     */
    private void addNewLine(TextFlow textFlow, int count) {
        for (int i = 0; i < count; i++) {
            Text newLine = new Text("\n");
            textFlow.getChildren().add(newLine);
        }
    }
    
    /**
     * Parses message text and adds formatted Text nodes to the TextFlow.
     * Supports **bold** markdown formatting.
     * Based on ConsultController implementation
     */
    private void parseAndAddFormattedText(TextFlow textFlow, String message) {
        // Base text color and fonts
        Color textColor = Color.rgb(63, 63, 91); // #3F3F5B
        Font baseFont = Font.font("Poppins", 24);
        Font boldFont = Font.font("Poppins", FontWeight.BOLD, 24);
        
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
     * Gets the profile picture path for a given username from UserData.xml
     * Reuses logic from CommunityController
     */
    private String getProfilePictureForUser(String username) {
        if (username == null) return null;
        
        var users = userRepository.loadUsers();
        for (var user : users) {
            if (username.equals(user.username)) {
                return user.profilepicture;
            }
        }
        return null;
    }
    
    /**
     * Gets the display username from UserData.xml for a given username
     * Reuses logic from CommunityController
     */
    private String getDisplayUsernameForUser(String username) {
        if (username == null) return null;
        
        var users = userRepository.loadUsers();
        for (var user : users) {
            if (username.equals(user.username)) {
                // Return fullname if available, otherwise return username
                return user.fullname != null && !user.fullname.trim().isEmpty() 
                    ? user.fullname : user.username;
            }
        }
        return username; // fallback to original username if not found
    }
    
    /**
     * Formats a timestamp string into a relative time format like "2h ago", "1d ago"
     * Reuses logic from CommunityController
     */
    private String formatRelativeTime(String timeString) {
        return communityController.formatRelativeTime(timeString);
    }
    
    /**
     * Populates the nutrition facts DoughnutChart with data from the current post.
     */
    private void populateNutritionFacts() {
        if (nutritionFacts == null || currentPost == null) {
            return;
        }
        
        // Show placeholder data when no nutrition analysis available
        if (currentPost.nutrition == null) {
            ObservableList<PieChart.Data> placeholderData = FXCollections.observableArrayList(
                new PieChart.Data("No nutrition analysis available", 1)
            );
            nutritionFacts.setData(placeholderData);
            nutritionFacts.setTitle("Nutrition Facts");
            nutritionFacts.setLegendVisible(true);
            nutritionFacts.getStyleClass().add("nutrition-chart");
            
            // Force layout update to ensure DoughnutChart inner circle is properly displayed
            refreshDoughnutChart();
            return;
        }
        
        try {
            Nutrition nutrition = currentPost.nutrition;
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            // Get total values for all nutrients
            double totalProtein = nutrition.getTotalProtein();
            double totalFat = nutrition.getTotalFat();
            double totalCarbs = nutrition.getTotalCarbohydrates();
            double totalFiber = nutrition.getTotalFiber();
            double totalSugar = nutrition.getTotalSugar();
            double totalSalt = nutrition.getTotalSalt();
            
            // Create pie chart data for macronutrients and key micronutrients
            if (totalProtein > 0) {
                pieChartData.add(new PieChart.Data("Protein (" + String.format("%.1f", totalProtein) + "g)", totalProtein));
            }
            
            if (totalFat > 0) {
                pieChartData.add(new PieChart.Data("Fat (" + String.format("%.1f", totalFat) + "g)", totalFat));
            }
            
            if (totalCarbs > 0) {
                pieChartData.add(new PieChart.Data("Carbohydrates (" + String.format("%.1f", totalCarbs) + "g)", totalCarbs));
            }
            
            if (totalFiber > 0) {
                pieChartData.add(new PieChart.Data("Fiber (" + String.format("%.1f", totalFiber) + "g)", totalFiber));
            }
            
            if (totalSugar > 0) {
                pieChartData.add(new PieChart.Data("Sugar (" + String.format("%.1f", totalSugar) + "g)", totalSugar));
            }
            
            if (totalSalt > 0) {
                // Convert mg to g for display consistency, or keep as mg if preferred
                if (totalSalt >= 1000) {
                    pieChartData.add(new PieChart.Data("Salt (" + String.format("%.1f", totalSalt/1000) + "g)", totalSalt/100)); // Scale down for chart
                } else {
                    pieChartData.add(new PieChart.Data("Salt (" + String.format("%.0f", totalSalt) + "mg)", totalSalt/100)); // Scale down for chart
                }
            }
            
            // If no macronutrient data, show calories breakdown
            if (pieChartData.isEmpty()) {
                double totalCalories = nutrition.getTotalCalories();
                if (totalCalories > 0) {
                    pieChartData.add(new PieChart.Data("Calories (" + String.format("%.0f", totalCalories) + " kcal)", totalCalories));
                } else {
                    // Fallback: show a placeholder
                    pieChartData.add(new PieChart.Data("No nutrition data available", 1));
                }
            }
            
            nutritionFacts.setData(pieChartData);
            nutritionFacts.setTitle("Nutrition Facts");
            nutritionFacts.setLegendVisible(true);
            
            // Apply custom styling if needed
            nutritionFacts.getStyleClass().add("nutrition-chart");
            
            // Force layout update to ensure DoughnutChart inner circle is properly displayed
            refreshDoughnutChart();
            
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(PostController.class.getName())
                    .warning("Failed to populate nutrition facts: " + e.getMessage());
            
            // Show fallback data
            ObservableList<PieChart.Data> fallbackData = FXCollections.observableArrayList(
                new PieChart.Data("Nutrition data unavailable", 1)
            );
            nutritionFacts.setData(fallbackData);
            nutritionFacts.setTitle("Nutrition Facts");
            
            // Force layout update to ensure DoughnutChart inner circle is properly displayed
            refreshDoughnutChart();
        }
    }
    
    /**
     * Forces a layout refresh on the DoughnutChart to ensure the inner circle is properly displayed.
     * This is needed after data updates to maintain the doughnut appearance.
     */
    private void refreshDoughnutChart() {
        if (nutritionFacts != null) {
            // Use the DoughnutChart's built-in refresh method
            nutritionFacts.refreshDoughnut();
            
            // Additional delayed refresh to ensure proper rendering
            Platform.runLater(() -> {
                nutritionFacts.refreshDoughnut();
            });
        }
    }
    
    /**
     * Shows processing dialog and performs nutrition analysis asynchronously.
     * Based on CreatePostController implementation.
     */
    private void showProcessingAndAnalyzeNutrition() {
        try {
            // Load processing dialog
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/starlight/view/processingDialog.fxml"));
            Parent parent = fxmlLoader.load();
            ProcessingDialogController processingController = fxmlLoader.getController();
            
            // Create modal stage
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Analyzing Nutrition...");
            dialogStage.setScene(new Scene(parent));
            dialogStage.setResizable(false);
            
            // Set up the dialog controller
            processingController.setDialogStage(dialogStage);
            processingController.updateStatus("Starting nutrition analysis...");
            
            // Show dialog
            dialogStage.show();
            
            // Create and configure the analysis task
            Task<Nutrition> nutritionTask = createNutritionAnalysisTask(processingController);
            
            // Handle task completion
            nutritionTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    dialogStage.close();
                    
                    Nutrition newNutrition = nutritionTask.getValue();
                    if (newNutrition != null && !newNutrition.ingredient.isEmpty()) {
                        // Update the current post's nutrition
                        currentPost.nutrition = newNutrition;
                        
                        // Save the updated post to XML
                        saveUpdatedPost();
                        
                        // Update UI to reflect new nutrition data
                        updateUIFromPost();
                        
                        // Show success dialog
                        showResultDialog(true, "Nutrition analysis completed successfully!");
                        
                        logger.info("Nutrition analysis completed and saved successfully");
                    } else {
                        showResultDialog(false, "Nutrition analysis failed. Please try again.");
                        logger.warning("Nutrition analysis returned empty or invalid results");
                    }
                });
            });
            
            nutritionTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    dialogStage.close();
                    
                    Throwable exception = nutritionTask.getException();
                    String errorMessage = "Nutrition analysis failed";
                    if (exception != null) {
                        errorMessage += ": " + exception.getMessage();
                    }
                    
                    showResultDialog(false, errorMessage);
                    logger.log(Level.SEVERE, "Nutrition analysis task failed", exception);
                });
            });
            
            // Start the task in a background thread
            Thread taskThread = new Thread(nutritionTask);
            taskThread.setDaemon(true);
            taskThread.start();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show processing dialog", e);
            showAnalysisErrorDialog("Failed to start nutrition analysis: " + e.getMessage());
        }
    }
    
    /**
     * Creates the nutrition analysis task.
     * Based on CreatePostController implementation.
     */
    private Task<Nutrition> createNutritionAnalysisTask(ProcessingDialogController processingController) {
        return new Task<Nutrition>() {
            @Override
            protected Nutrition call() throws Exception {
                String ingredients = currentPost.ingredients;
                Exception lastException = null;
                
                // Try analysis up to MAX_RETRIES times
                for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                    final int currentAttempt = attempt; // Make effectively final for lambda
                    try {
                        Platform.runLater(() -> 
                            processingController.updateStatus("Analyzing nutrition... (Attempt " + currentAttempt + "/" + MAX_RETRIES + ")")
                        );
                        
                        // Perform nutrition analysis using ChatGPT API
                        String nutritionResponse = chatbotAPI.analyzeNutritionFacts(ingredients);
                        
                        // Parse the AI response into Nutrition object
                        Nutrition nutrition = nutritionParser.parseNutritionFromResponse(nutritionResponse);
                        
                        if (nutrition != null && !nutrition.ingredient.isEmpty()) {
                            logger.info("Nutrition analysis completed successfully on attempt " + currentAttempt);
                            Platform.runLater(() -> 
                                processingController.updateStatus("Analysis completed successfully!")
                            );
                            return nutrition;
                        } else {
                            throw new Exception("AI returned empty or invalid nutrition data");
                        }
                        
                    } catch (Exception e) {
                        lastException = e;
                        logger.log(Level.WARNING, "Nutrition analysis attempt " + currentAttempt + " failed: " + e.getMessage(), e);
                        
                        if (currentAttempt < MAX_RETRIES) {
                            Platform.runLater(() -> 
                                processingController.updateStatus("Attempt " + currentAttempt + " failed, retrying...")
                            );
                            Thread.sleep(1000); // Brief pause between attempts
                        }
                    }
                }
                
                // All attempts failed
                Platform.runLater(() -> 
                    processingController.updateStatus("Analysis failed after " + MAX_RETRIES + " attempts")
                );
                throw lastException != null ? lastException : new Exception("All nutrition analysis attempts failed");
            }
        };
    }
    
    /**
     * Saves the updated post with new nutrition data to XML.
     */
    private void saveUpdatedPost() {
        try {
            // Load all posts
            List<Post> allPosts = postRepository.loadPosts();
            
            // Find and update the current post in the list
            boolean updated = false;
            for (int i = 0; i < allPosts.size(); i++) {
                Post post = allPosts.get(i);
                if (post.uuid != null && post.uuid.equals(currentPost.uuid)) {
                    // Update the post in the list
                    allPosts.set(i, currentPost);
                    updated = true;
                    break;
                }
            }
            
            if (!updated) {
                logger.warning("Could not find post to update in XML data");
                return;
            }
            
            // Save the updated posts list back to XML
            postRepository.savePosts(allPosts);
            logger.info("Post nutrition data updated and saved to XML successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save updated post to XML", e);
            throw new RuntimeException("Failed to save nutrition analysis results", e);
        }
    }
    
    /**
     * Shows a result dialog with success or failure message.
     * Based on CreatePostController pattern.
     */
    private void showResultDialog(boolean success, String message) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/starlight/view/popupDialog.fxml"));
            Parent parent = fxmlLoader.load();
            PopupDialogController controller = fxmlLoader.getController();
            
            // Set appropriate message
            if (success) {
                controller.setNutritionAnalysisSuccess();
            } else {
                controller.setMessage(message);
            }
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(success ? "Analysis Complete" : "Analysis Failed");
            dialogStage.setScene(new Scene(parent));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show result dialog", e);
        }
    }
    
    /**
     * Shows an error dialog for analysis issues.
     */
    private void showAnalysisErrorDialog(String errorMessage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/starlight/view/popupDialog.fxml"));
            Parent parent = fxmlLoader.load();
            PopupDialogController controller = fxmlLoader.getController();
            
            controller.setMessage("Analysis Error\n\n" + errorMessage);
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Analysis Error");
            dialogStage.setScene(new Scene(parent));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show error dialog", e);
        }
    }
}
