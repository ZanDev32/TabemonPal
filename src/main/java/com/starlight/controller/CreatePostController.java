package com.starlight.controller;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.starlight.models.Post;
import com.starlight.repository.PostDataRepository;
import com.starlight.util.Session;
import com.starlight.api.ChatbotAPI;
import com.starlight.util.NutritionParser;
import com.starlight.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.concurrent.Task;


/**
 * Controller responsible for the "create post" dialog.
 */
public class CreatePostController implements Initializable {
    private static final Logger logger = Logger.getLogger(CreatePostController.class.getName());

    @FXML
    private MFXTextField title;

    @FXML
    private MFXButton imagepicker;

    @FXML
    private Label pickerstatus;

    @FXML
    private MFXTextField description;

    @FXML
    private TextArea ingredients;

    @FXML
    private TextArea directions;

    @FXML
    private MFXButton submit;

    @FXML
    private MFXButton cancel;

    private File selectedImage;

    private boolean success;

    private final PostDataRepository repository = new PostDataRepository();
    private final ChatbotAPI chatbotAPI = new ChatbotAPI();
    private final NutritionParser nutritionParser = new NutritionParser();

    /**
     * Formats text by replacing newlines with vertical lines and cleaning up spacing.
     * This ensures ingredients and directions are stored as vertical line-separated single lines.
     *
     * @param text the text to format
     * @return formatted text with vertical lines instead of newlines
     */
    private String formatTextAsVerticalLineSeparated(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // Replace newlines with vertical lines, trim whitespace, and remove empty entries
        return text.trim()
                  .replaceAll("\\r?\\n", "| ")  // Replace newlines with "| "
                  .replaceAll("\\|\\s*\\|", "|")    // Remove duplicate vertical lines
                  .replaceAll("^\\|\\s*|\\|\\s*$", "") // Remove leading/trailing vertical lines
                  .replaceAll("\\s{2,}", " ");  // Replace multiple spaces with single space
    }

    /**
     * Copies the selected image to the user's data directory.
     *
     * @param image the image file selected by the user
     * @return the copied image file path
     */
    private String copyImageToUserDir(File image) {
        try {
            String username = Session.getCurrentUser() != null ? Session.getCurrentUser().username : "unknown";
            return com.starlight.util.FileSystemManager.copyFileToUserDirectoryWithUniqueFilename(image, username);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to copy image to user directory: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Indicates whether the user successfully created a post.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Initializes the controller by wiring up button actions and the image
     * picker logic.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pickerstatus.setText("No image selected");

        // Image picker logic
        imagepicker.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose an image");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedImage = file;
                pickerstatus.setText("Selected: " + file.getName());
            } else {
                pickerstatus.setText("No image selected");
            }
        });

        submit.setOnAction(event -> {
            String postTitle = title.getText();
            String postDescription = description.getText();
            String postIngredients = formatTextAsVerticalLineSeparated(ingredients.getText());
            String postDirections = formatTextAsVerticalLineSeparated(directions.getText());

            if (postTitle.isEmpty() || postDescription.isEmpty() || postIngredients.isEmpty() || postDirections.isEmpty() || selectedImage == null) {
                logger.warning("Please complete all fields and select an image.");
                return;
            }

            // Create a new Post object
            Post newPost = new Post();
            newPost.uuid = UUID.randomUUID().toString();
            newPost.username = Session.getCurrentUser().username;
            newPost.profilepicture = "src/main/resources/com/starlight/images/dummy/2.png";
            newPost.title = postTitle;
            newPost.description = postDescription;
            newPost.ingredients = postIngredients;
            newPost.directions = postDirections;
            newPost.uploadtime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            try {
                String storedPath = copyImageToUserDir(selectedImage);
                newPost.image = storedPath != null ? storedPath : selectedImage.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                newPost.image = selectedImage.getAbsolutePath();
            }
            newPost.likecount = "0";
            newPost.commentcount = "0";
            newPost.isLiked = "false";
            newPost.rating = "0.0";

            // Close the current dialog first
            Stage currentStage = (Stage) submit.getScene().getWindow();
            currentStage.close();

            // Show processing screen and perform AI analysis
            showProcessingAndAnalyzeNutrition(newPost, postIngredients);
        });

        cancel.setOnAction(event -> {
            // Close the dialog without saving
            Stage stage = (Stage) cancel.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Shows processing dialog and performs nutrition analysis asynchronously,
     * using MaterialFX dialog as embedded overlay within the current scene.
     */
    private void showProcessingAndAnalyzeNutrition(Post newPost, String ingredients) {
        try {
            // Create MaterialFX processing dialog
            io.github.palexdev.materialfx.dialogs.MFXGenericDialog processingDialog = 
                new io.github.palexdev.materialfx.dialogs.MFXGenericDialog();
            processingDialog.getStylesheets().add(getClass().getResource("/com/starlight/style.css").toExternalForm());
            processingDialog.getStyleClass().add("background-window");
            processingDialog.setPrefSize(400, 200);
            
            // Create status label
            Label statusLabel = new Label("Creating your post...");
            statusLabel.getStyleClass().add("post-username");
            statusLabel.setWrapText(true);
            statusLabel.setPrefWidth(350);
            
            // Create progress spinner
            io.github.palexdev.materialfx.controls.MFXProgressSpinner spinner = 
                new io.github.palexdev.materialfx.controls.MFXProgressSpinner();
            spinner.setPrefSize(80, 80);
            
            // Create container
            VBox container = new VBox(20);
            container.setAlignment(javafx.geometry.Pos.CENTER);
            container.getChildren().addAll(statusLabel, spinner);
            container.setPadding(new javafx.geometry.Insets(40));
            
            processingDialog.setContent(container);
            
            // Find the scene root to add dialog overlay
            javafx.scene.Parent sceneRoot = submit.getScene().getRoot();
            
            // Get or create StackPane overlay
            javafx.scene.layout.StackPane dialogOverlay;
            if (sceneRoot instanceof javafx.scene.layout.StackPane) {
                dialogOverlay = (javafx.scene.layout.StackPane) sceneRoot;
            } else {
                dialogOverlay = new javafx.scene.layout.StackPane();
                dialogOverlay.getChildren().add(sceneRoot);
                submit.getScene().setRoot(dialogOverlay);
            }
            
            // Create semi-transparent background overlay
            javafx.scene.layout.StackPane backgroundOverlay = new javafx.scene.layout.StackPane();
            backgroundOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
            backgroundOverlay.setPrefSize(javafx.scene.layout.Region.USE_COMPUTED_SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            backgroundOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            // Center the dialog in the overlay
            javafx.scene.layout.StackPane.setAlignment(processingDialog, javafx.geometry.Pos.CENTER);
            backgroundOverlay.getChildren().add(processingDialog);
            
            // Add the overlay to scene
            dialogOverlay.getChildren().add(backgroundOverlay);
            
            // Create background task for AI analysis
            Task<Void> analysisTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    final int MAX_RETRIES = 3;
                    int attempt = 0;
                    Exception lastException = null;
                    boolean isApiKeyIssue = false;
                    boolean isNetworkIssue = false;
                    
                    while (attempt < MAX_RETRIES) {
                        attempt++;
                        final int currentAttempt = attempt; // Final copy for lambda use
                        
                        try {
                            logger.info("Analyzing nutrition facts for ingredients (attempt " + currentAttempt + "/" + MAX_RETRIES + "): " + ingredients);
                            
                            // Update status on UI thread
                            Platform.runLater(() -> {
                                try {
                                    if (currentAttempt == 1) {
                                        statusLabel.setText("Analyzing nutrition facts...");
                                    } else {
                                        statusLabel.setText("Analyzing nutrition facts... (retry " + currentAttempt + "/" + MAX_RETRIES + ")");
                                    }
                                } catch (Exception e) {
                                    logger.log(Level.WARNING, "Failed to update processing status: " + e.getMessage());
                                }
                            });
                            
                            String nutritionResponse = chatbotAPI.analyzeNutritionFacts(ingredients);
                            newPost.nutrition = nutritionParser.parseNutritionFromResponse(nutritionResponse);
                            
            // Check if we got valid nutrition data
            if (newPost.nutrition != null && newPost.nutrition.ingredient != null && !newPost.nutrition.ingredient.isEmpty()) {
                logger.info("Nutrition analysis completed successfully on attempt " + currentAttempt);
                
                // Update status to show success  
                Platform.runLater(() -> {
                    statusLabel.setText("Nutrition analysis completed successfully!");
                });
                
                return null; // Success - exit the retry loop
            } else {
                throw new Exception("Received empty or invalid nutrition data from AI");
            }                        } catch (Exception e) {
                            lastException = e;
                            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                            
                            // Detect specific error types
                            if (errorMessage.contains("api key") || errorMessage.contains("configure your openai api key")) {
                                isApiKeyIssue = true;
                                logger.log(Level.WARNING, "API Key issue detected: " + e.getMessage(), e);
                                break; // No point retrying API key issues
                            } else if (errorMessage.contains("network") || errorMessage.contains("connection") || 
                                     errorMessage.contains("timeout") || errorMessage.contains("unreachable") ||
                                     errorMessage.contains("failed to get response")) {
                                isNetworkIssue = true;
                                logger.log(Level.WARNING, "Network issue detected on attempt " + currentAttempt + ": " + e.getMessage(), e);
                            } else {
                                logger.log(Level.WARNING, "Nutrition analysis attempt " + currentAttempt + " failed: " + e.getMessage(), e);
                            }
                            
                            if (currentAttempt < MAX_RETRIES && !isApiKeyIssue) {
                                // Wait a bit before retrying (unless it's an API key issue)
                                Platform.runLater(() -> {
                                    try {
                                        statusLabel.setText("Analysis failed, retrying... (" + (currentAttempt + 1) + "/" + MAX_RETRIES + ")");
                                    } catch (Exception ex) {
                                        logger.log(Level.WARNING, "Failed to update retry status: " + ex.getMessage());
                                    }
                                });
                                
                                try {
                                    Thread.sleep(2000); // Wait 2 seconds before retry
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new Exception("Analysis interrupted", ie);
                                }
                            }
                        }
                    }
                    
                    // All retries failed or specific issue detected
                    final boolean finalIsApiKeyIssue = isApiKeyIssue;
                    final boolean finalIsNetworkIssue = isNetworkIssue;
                    final Exception finalException = lastException;
                    
                    Platform.runLater(() -> {
                        try {
                            if (finalIsApiKeyIssue) {
                                logger.log(Level.WARNING, "Skipping nutrition analysis - API key not configured");
                                statusLabel.setText("API key not configured. Using default values...");
                                showPopupDialog("apikey");
                            } else if (finalIsNetworkIssue) {
                                logger.log(Level.WARNING, "Skipping nutrition analysis - network issues detected");
                                statusLabel.setText("Network issues detected. Using default values...");
                                showPopupDialog("network");
                            } else {
                                logger.log(Level.SEVERE, "All " + MAX_RETRIES + " nutrition analysis attempts failed. Last error: " + 
                                          (finalException != null ? finalException.getMessage() : "Unknown error"));
                                statusLabel.setText("Nutrition analysis failed. Using default values...");
                                showPopupDialog("analysis_failed");
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Failed to update error status or show warning: " + e.getMessage());
                        }
                    });
                    
                    // Use fallback nutrition data
                    newPost.nutrition = nutritionParser.parseNutritionFromResponse(null); // Creates fallback nutrition
                    
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        try {
                            // Update final status
                            statusLabel.setText("Saving post...");
                            
                            // Save the new post
                            List<Post> posts = repository.loadPosts();
                            posts.add(0, newPost);
                            repository.savePosts(posts);
                            
                            success = true;
                            logger.info("Post created and saved successfully");
                            
                            // Close the processing dialog
                            dialogOverlay.getChildren().remove(backgroundOverlay);
                            
                            // Show success popup
                            showResultDialog("post_created_success");
                            
                            // Navigate back to main view
                            App.setRoot("main");
                            
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Failed to save post or navigate to main: " + e.getMessage(), e);
                            
                            // Close dialog and show failure message
                            try {
                                dialogOverlay.getChildren().remove(backgroundOverlay);
                                showResultDialog("post_creation_failed");
                                App.setRoot("main");
                            } catch (Exception fallbackEx) {
                                logger.log(Level.SEVERE, "Fallback navigation failed: " + fallbackEx.getMessage(), fallbackEx);
                            }
                        }
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        logger.log(Level.SEVERE, "Nutrition analysis task failed completely", getException());
                        
                        // Still try to save post without nutrition data
                        try {
                            statusLabel.setText("Saving post with default values...");
                            newPost.nutrition = nutritionParser.parseNutritionFromResponse(null); // Creates fallback nutrition
                            List<Post> posts = repository.loadPosts();
                            posts.add(0, newPost);
                            repository.savePosts(posts);
                            success = true;
                            
                            // Close the processing dialog
                            dialogOverlay.getChildren().remove(backgroundOverlay);
                            
                            // Show success popup (post was still created)
                            showResultDialog("post_created_success");
                            
                            // Navigate back to main view
                            App.setRoot("main");
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Failed to save post: " + e.getMessage(), e);
                            
                            // Close the processing dialog and show failure message
                            try {
                                dialogOverlay.getChildren().remove(backgroundOverlay);
                                showResultDialog("post_creation_failed");
                                App.setRoot("main");
                            } catch (Exception ex) {
                                logger.log(Level.SEVERE, "Failed to close dialog or navigate to main view: " + ex.getMessage(), ex);
                            }
                        }
                    });
                }
            };
            
            // Start the task in a background thread
            Thread analysisThread = new Thread(analysisTask);
            analysisThread.setDaemon(true);
            analysisThread.start();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show processing screen: " + e.getMessage(), e);
            
            // Fallback: try to save post and navigate directly
            try {
                newPost.nutrition = nutritionParser.parseNutritionFromResponse(null); // Creates fallback nutrition
                List<Post> posts = repository.loadPosts();
                posts.add(0, newPost);
                repository.savePosts(posts);
                success = true;
                App.setRoot("main");
            } catch (Exception fallbackEx) {
                logger.log(Level.SEVERE, "Fallback save and navigation failed: " + fallbackEx.getMessage(), fallbackEx);
            }
        }
    }
    
    
    /**
     * Shows a popup dialog for nutrition analysis issues
     */
    private void showPopupDialog(String warningType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/popupDialog.fxml"));
            Parent root = loader.load();
            
            PopupDialogController controller = loader.getController();
            
            // Set appropriate warning message based on type
            switch (warningType) {
                case "apikey":
                    controller.setApiKeyWarning();
                    break;
                case "network":
                    controller.setNetworkWarning();
                    break;
                case "analysis_failed":
                    controller.setAnalysisFailedWarning();
                    break;
                default:
                    controller.setMessage("An error occurred during nutrition analysis. Using default values.");
                    break;
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nutrition Analysis Warning");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            
            // Center the dialog
            dialogStage.centerOnScreen();
            
            // Show and wait for user to close
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to show warning dialog: " + e.getMessage(), e);
            // Fallback: just log the warning
            logger.warning("WARNING: " + warningType + " - Nutrition analysis skipped, using default values");
        }
    }
    
    /**
     * Shows a popup dialog with success or failure message using MaterialFX dialog.
     */
    private void showResultDialog(String resultType) {
        try {
            // Create MaterialFX result dialog
            io.github.palexdev.materialfx.dialogs.MFXGenericDialog resultDialog = 
                new io.github.palexdev.materialfx.dialogs.MFXGenericDialog();
            resultDialog.getStylesheets().add(getClass().getResource("/com/starlight/style.css").toExternalForm());
            resultDialog.getStyleClass().add("background-window");
            resultDialog.setPrefSize(400, 200);
            
            // Create message label
            Label messageLabel = new Label();
            messageLabel.getStyleClass().add("post-username");
            messageLabel.setWrapText(true);
            messageLabel.setPrefWidth(350);
            
            // Set appropriate message based on result type
            switch (resultType) {
                case "post_created_success":
                    messageLabel.setText("Great! Your recipe has been posted successfully and is now live in the community feed!");
                    break;
                case "post_creation_failed":
                    messageLabel.setText("Sorry, we couldn't create your post right now. Please check your inputs and try again.");
                    break;
                case "nutrition_analysis_success":
                    messageLabel.setText("Nutrition analysis completed successfully! Your recipe now has detailed nutritional information.");
                    break;
                default:
                    messageLabel.setText("Operation completed.");
                    break;
            }
            
            // Create OK button
            io.github.palexdev.materialfx.controls.MFXButton okButton = 
                new io.github.palexdev.materialfx.controls.MFXButton("OK");
            okButton.getStyleClass().add("food-tag");
            
            // Create container
            VBox container = new VBox(20);
            container.setAlignment(javafx.geometry.Pos.CENTER);
            container.getChildren().addAll(messageLabel, okButton);
            container.setPadding(new javafx.geometry.Insets(40));
            
            resultDialog.setContent(container);
            
            // Find the scene root to add dialog overlay
            javafx.scene.Parent sceneRoot = submit.getScene().getRoot();
            
            // Get or create StackPane overlay
            javafx.scene.layout.StackPane dialogOverlay;
            if (sceneRoot instanceof javafx.scene.layout.StackPane) {
                dialogOverlay = (javafx.scene.layout.StackPane) sceneRoot;
            } else {
                dialogOverlay = new javafx.scene.layout.StackPane();
                dialogOverlay.getChildren().add(sceneRoot);
                submit.getScene().setRoot(dialogOverlay);
            }
            
            // Create semi-transparent background overlay
            javafx.scene.layout.StackPane backgroundOverlay = new javafx.scene.layout.StackPane();
            backgroundOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
            backgroundOverlay.setPrefSize(javafx.scene.layout.Region.USE_COMPUTED_SIZE, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            backgroundOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            // Center the dialog in the overlay
            javafx.scene.layout.StackPane.setAlignment(resultDialog, javafx.geometry.Pos.CENTER);
            backgroundOverlay.getChildren().add(resultDialog);
            
            // Add the overlay to scene
            dialogOverlay.getChildren().add(backgroundOverlay);
            
            // Set up OK button to close dialog
            okButton.setOnAction(e -> dialogOverlay.getChildren().remove(backgroundOverlay));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show result dialog: " + e.getMessage(), e);
        }
    }
}