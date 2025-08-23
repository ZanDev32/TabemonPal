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

import com.starlight.util.ImageUtils;
import com.starlight.repository.UserDataRepository;
import com.starlight.repository.PostDataRepository;
import com.starlight.api.ChatbotAPI;
import com.starlight.model.Nutrition;
import com.starlight.model.Post;
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
    // Duplicated literal constants (Sonar S1192)
    private static final String UNKNOWN = "Unknown";
    private static final String STYLE_VERDICT_UNKNOWN = "verdict-unknown";
    private static final String FONT_POPPINS = "Poppins";
    private static final String TITLE_NUTRITION_FACTS = "Nutrition Facts";

    // Dedicated exception for analysis retries
    private static class NutritionAnalysisException extends Exception {
        NutritionAnalysisException(String message, Throwable cause) { super(message, cause); }
    }

    // @FXML initialize method intentionally omitted; Initializable#initialize used instead.

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
        
    if (currentPost.getIngredients() == null || currentPost.getIngredients().trim().isEmpty()) {
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
        // Intentionally left blank (S1186): UI is populated only after a Post is injected via setPost()
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
        if (currentPost == null) {
            return;
        }
        updateBasicTextFields();
        updateVerdictButton();
        updateAnalysisButtonState();
        loadUserImages();
        populateRecipeContainer();
        populateNutritionFacts();
    }

    private void updateBasicTextFields() {
        if (description != null) {
            description.setText(currentPost.getDescription() != null ? currentPost.getDescription() : "");
        }
        if (uploadtime != null) {
            uploadtime.setText(formatRelativeTime(currentPost.getUploadtime()));
        }
        if (username != null) {
            String displayUsername = getDisplayUsernameForUser(currentPost.getUsername());
            username.setText(displayUsername != null ? displayUsername : currentPost.getUsername());
        }
    }

    private void updateVerdictButton() {
        if (verdict == null) {
            return;
        }
        if (currentPost.getNutrition() != null) {
            String verdictText = currentPost.getNutrition().getVerdict() != null ? currentPost.getNutrition().getVerdict() : UNKNOWN;
            verdict.setText(verdictText);
            verdict.getStyleClass().clear();
            verdict.getStyleClass().add("food-tag");
            switch (verdictText) {
                case "Healthy" -> verdict.getStyleClass().add("verdict-healthy");
                case "Moderate" -> verdict.getStyleClass().add("verdict-moderate");
                case "Unhealthy" -> verdict.getStyleClass().add("verdict-unhealthy");
                case "Junk Food" -> verdict.getStyleClass().add("verdict-junk");
                case UNKNOWN -> verdict.getStyleClass().add(STYLE_VERDICT_UNKNOWN);
                default -> verdict.getStyleClass().add(STYLE_VERDICT_UNKNOWN);
            }
        } else {
            verdict.setText(UNKNOWN);
            verdict.getStyleClass().clear();
            verdict.getStyleClass().add("food-tag");
            verdict.getStyleClass().add(STYLE_VERDICT_UNKNOWN);
        }
    }

    private void updateAnalysisButtonState() {
        if (doAnalysis == null) {
            return;
        }
        boolean analyzed = currentPost.getNutrition() != null && currentPost.getNutrition().getVerdict() != null && !UNKNOWN.equals(currentPost.getNutrition().getVerdict());
        doAnalysis.setText(analyzed ? "Re-analyze" : "Analyze");
        doAnalysis.setDisable(false);
        if (currentPost.getIngredients() == null || currentPost.getIngredients().trim().isEmpty()) {
            doAnalysis.setText("No Ingredients");
            doAnalysis.setDisable(true);
        }
    }

    private void loadUserImages() {
        if (profile1 != null) {
            String profilePicture = getProfilePictureForUser(currentPost.getUsername());
            ImageUtils.loadImage(profile1, profilePicture, ImageUtils.DEFAULT_MISSING_IMAGE);
            ImageUtils.scaleToFit(profile1, 80, 80, 500);
        }
        if (recentphoto1 != null) {
            ImageUtils.loadImage(recentphoto1, currentPost.getImage(), ImageUtils.DEFAULT_MISSING_IMAGE);
            ImageUtils.scaleToFit(recentphoto1, 1270, 990, 20);
        }
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
        if (currentPost.getTitle() != null && !currentPost.getTitle().trim().isEmpty()) {
            addSectionTitle(textFlow, currentPost.getTitle());
            addNewLine(textFlow, 2);
        }
        
        // Add ingredients section
        if (currentPost.getIngredients() != null && !currentPost.getIngredients().trim().isEmpty()) {
            addSectionTitle(textFlow, "Ingredients :");
            addNewLine(textFlow, 1);
            // Parse vertical line-separated ingredients as newlines
            String ingredientsWithNewlines = currentPost.getIngredients().replace("|", "\n");
            addBulletList(textFlow, ingredientsWithNewlines);
            addNewLine(textFlow, 1);
        }
        
        // Add directions section
        if (currentPost.getDirections() != null && !currentPost.getDirections().trim().isEmpty()) {
            addSectionTitle(textFlow, "Directions :");
            addNewLine(textFlow, 1);
            // Parse vertical line-separated directions as newlines
            String directionsWithNewlines = currentPost.getDirections().replace("|", "\n");
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
                bullet.setFont(Font.font(FONT_POPPINS, 24));
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
                numberedItem.setFont(Font.font(FONT_POPPINS, 24));
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
    Font baseFont = Font.font(FONT_POPPINS, 24);
    Font boldFont = Font.font(FONT_POPPINS, FontWeight.BOLD, 24);
        
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
            if (username.equals(user.getUsername())) {
                return user.getProfilepicture();
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
            if (username.equals(user.getUsername())) {
                // Return fullname if available, otherwise return username
                return user.getFullname() != null && !user.getFullname().trim().isEmpty()
                    ? user.getFullname() : user.getUsername();
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
        if (nutritionFacts == null || currentPost == null) return;
        if (currentPost.getNutrition() == null) {
            setPlaceholderNutritionFacts();
            return;
        }
        try {
            ObservableList<PieChart.Data> data = buildNutritionPieData(currentPost.getNutrition());
            nutritionFacts.setData(data);
            nutritionFacts.setTitle(TITLE_NUTRITION_FACTS);
            nutritionFacts.setLegendVisible(true);
            nutritionFacts.getStyleClass().add("nutrition-chart");
            refreshDoughnutChart();
        } catch (Exception e) {
            // Defer message construction (Sonar S3457) while keeping stack trace
            logger.log(Level.WARNING, e, () -> "Failed to populate nutrition facts: " + e.getMessage());
            setFallbackNutritionFacts();
        }
    }

    private void setPlaceholderNutritionFacts() {
        ObservableList<PieChart.Data> placeholderData = FXCollections.observableArrayList(
            new PieChart.Data("No nutrition analysis available", 1)
        );
        nutritionFacts.setData(placeholderData);
        nutritionFacts.setTitle(TITLE_NUTRITION_FACTS);
        nutritionFacts.setLegendVisible(true);
        nutritionFacts.getStyleClass().add("nutrition-chart");
        refreshDoughnutChart();
    }

    private void setFallbackNutritionFacts() {
        ObservableList<PieChart.Data> fallbackData = FXCollections.observableArrayList(
            new PieChart.Data("Nutrition data unavailable", 1)
        );
        nutritionFacts.setData(fallbackData);
        nutritionFacts.setTitle(TITLE_NUTRITION_FACTS);
        refreshDoughnutChart();
    }

    private ObservableList<PieChart.Data> buildNutritionPieData(Nutrition nutrition) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        addIfPositive(pieChartData, "Protein", nutrition.getTotalProtein(), "g");
        addIfPositive(pieChartData, "Fat", nutrition.getTotalFat(), "g");
        addIfPositive(pieChartData, "Carbohydrates", nutrition.getTotalCarbohydrates(), "g");
        addIfPositive(pieChartData, "Fiber", nutrition.getTotalFiber(), "g");
        addIfPositive(pieChartData, "Sugar", nutrition.getTotalSugar(), "g");
        addSaltData(pieChartData, nutrition.getTotalSalt());
        if (pieChartData.isEmpty()) addCaloriesOrPlaceholder(pieChartData, nutrition.getTotalCalories());
        return pieChartData;
    }

    private void addIfPositive(ObservableList<PieChart.Data> list, String label, double value, String unit) {
        if (value > 0) list.add(new PieChart.Data(label + " (" + String.format("%.1f", value) + unit + ")", value));
    }

    private void addSaltData(ObservableList<PieChart.Data> list, double totalSalt) {
        if (totalSalt > 0) {
            if (totalSalt >= 1000) {
                list.add(new PieChart.Data("Salt (" + String.format("%.1f", totalSalt/1000) + "g)", totalSalt/100));
            } else {
                list.add(new PieChart.Data("Salt (" + String.format("%.0f", totalSalt) + "mg)", totalSalt/100));
            }
        }
    }

    private void addCaloriesOrPlaceholder(ObservableList<PieChart.Data> list, double totalCalories) {
        if (totalCalories > 0) {
            list.add(new PieChart.Data("Calories (" + String.format("%.0f", totalCalories) + " kcal)", totalCalories));
        } else {
            list.add(new PieChart.Data("No nutrition data available", 1));
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
            Platform.runLater(() -> nutritionFacts.refreshDoughnut());
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
            nutritionTask.setOnSucceeded(e -> Platform.runLater(() -> {
                dialogStage.close();
                Nutrition newNutrition = nutritionTask.getValue();
                if (newNutrition != null && !newNutrition.getIngredient().isEmpty()) {
                    currentPost.setNutrition(newNutrition);
                    saveUpdatedPost();
                    updateUIFromPost();
                    showResultDialog(true, "Nutrition analysis completed successfully!");
                    logger.info("Nutrition analysis completed and saved successfully");
                } else {
                    showResultDialog(false, "Nutrition analysis failed. Please try again.");
                    logger.warning("Nutrition analysis returned empty or invalid results");
                }
            }));
            
            nutritionTask.setOnFailed(e -> Platform.runLater(() -> {
                dialogStage.close();
                Throwable exception = nutritionTask.getException();
                String errorMessage = "Nutrition analysis failed" + (exception != null ? ": " + exception.getMessage() : "");
                showResultDialog(false, errorMessage);
                logger.log(Level.SEVERE, "Nutrition analysis task failed", exception);
            }));
            
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
        return new Task<>() {
            @Override
            protected Nutrition call() throws NutritionAnalysisException {
                return performNutritionAnalysisWithRetries(currentPost.getIngredients(), processingController);
            }
        };
    }

    private Nutrition performNutritionAnalysisWithRetries(String ingredients, ProcessingDialogController controller) throws NutritionAnalysisException {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            int currentAttempt = attempt;
            Platform.runLater(() -> controller.updateStatus("Analyzing nutrition... (Attempt " + currentAttempt + "/" + MAX_RETRIES + ")"));
            try {
                String response = chatbotAPI.analyzeNutritionFacts(ingredients);
                Nutrition nutrition = nutritionParser.parseNutritionFromResponse(response);
                if (nutrition != null && !nutrition.getIngredient().isEmpty()) {
                    logger.info(() -> "Nutrition analysis completed successfully on attempt " + currentAttempt);
                    Platform.runLater(() -> controller.updateStatus("Analysis completed successfully!"));
                    return nutrition;
                }
                throw new IllegalStateException("AI returned empty or invalid nutrition data");
            } catch (Exception e) {
                lastException = e;
                logger.log(Level.WARNING, e, () -> "Nutrition analysis attempt " + currentAttempt + " failed: " + e.getMessage());
                if (currentAttempt < MAX_RETRIES) {
                    Platform.runLater(() -> controller.updateStatus("Attempt " + currentAttempt + " failed, retrying..."));
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        Platform.runLater(() -> controller.updateStatus("Analysis failed after " + MAX_RETRIES + " attempts"));
        throw new NutritionAnalysisException("Analysis failed after retries", lastException);
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
                        if (post.getUuid() != null && post.getUuid().equals(currentPost.getUuid())) {
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
            // Handle locally: log only (Sonar S2139 - do not log then rethrow)
            logger.log(Level.SEVERE, "Failed to save updated post to XML", e);
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
            // Log and swallow (dialog already failed, no further recovery path). S2139: choose logging only.
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
            // Log and swallow (cannot show error dialog about failing to show error dialog). S2139 compliance.
            logger.log(Level.SEVERE, "Failed to show error dialog", e);
        }
    }
}
