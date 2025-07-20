package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PostController implements Initializable {
    @FXML
    private MFXButton goBack;

    @FXML
    private MFXButton verdict;

    @FXML
    private VBox recipeContainer;

    @FXML
    private PieChart nutritionFacts;

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
    private final CommunityController communityController = new CommunityController();
    private MainController mainController; // Reference to main controller for navigation

    @FXML
    private void initialize() {
        // Initialize with placeholder data or wait for post data to be set
    }
    
    /**
     * Handles the go back button click - navigates back to community view
     */
    @FXML
    private void goBack() {
        if (mainController != null) {
            // Navigate back to community view with proper button selection
            mainController.navigateToCommunity();
        }
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
                currentPost.nutrition.verdict : "Moderate";
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
                default:
                    verdict.getStyleClass().add("verdict-moderate");
                    break;
            }
        } else if (verdict != null) {
            verdict.setText("No Analysis");
            verdict.getStyleClass().clear();
            verdict.getStyleClass().add("food-tag");
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
     * Populates the nutrition facts PieChart with data from the current post.
     */
    private void populateNutritionFacts() {
        if (nutritionFacts == null || currentPost == null || currentPost.nutrition == null) {
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
            
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(PostController.class.getName())
                    .warning("Failed to populate nutrition facts: " + e.getMessage());
            
            // Show fallback data
            ObservableList<PieChart.Data> fallbackData = FXCollections.observableArrayList(
                new PieChart.Data("Nutrition data unavailable", 1)
            );
            nutritionFacts.setData(fallbackData);
            nutritionFacts.setTitle("Nutrition Facts");
        }
    }
}
