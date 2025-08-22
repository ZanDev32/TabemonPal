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

import com.starlight.repository.PostDataRepository;
import com.starlight.util.Session;
import com.starlight.api.ChatbotAPI;
import com.starlight.model.Post;
import com.starlight.util.NutritionParser;
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
import javafx.stage.FileChooser;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.concurrent.Task;


/**
 * Controller responsible for the "create post" dialog.
 */
public class CreatePostController implements Initializable {
    private static final Logger logger = Logger.getLogger(CreatePostController.class.getName());
    private static final String EXCEPTION_DETAILS = "Exception details";

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
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private MainController mainController;

    /**
     * Sets the main controller for navigation.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
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
            newPost.setUuid(UUID.randomUUID().toString());
            newPost.setUsername(Session.getCurrentUser().getUsername());
            newPost.setProfilepicture("src/main/resources/com/starlight/images/dummy/2.png");
            newPost.setTitle(postTitle);
            newPost.setDescription(postDescription);
            newPost.setIngredients(postIngredients);
            newPost.setDirections(postDirections);
            newPost.setUploadtime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            try {
                String storedPath = copyImageToUserDir(selectedImage);
                newPost.setImage(storedPath != null ? storedPath : selectedImage.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                newPost.setImage(selectedImage.getAbsolutePath());
            }
            newPost.setLikecount("0");
            newPost.setCommentcount("0");
            newPost.setIsLiked("false");
            newPost.setRating("0.0");

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
                  .replaceAll("(^\\|\\s*)|(\\|\\s*$)", "") // Remove leading/trailing vertical lines
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
            String username = Session.getCurrentUser() != null ? Session.getCurrentUser().getUsername() : "unknown";
            return com.starlight.util.FileSystemManager.copyFileToUserDirectoryWithUniqueFilename(image, username);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to copy image to user directory: {0}", new Object[]{e.getMessage()});
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
            return null;
        }
    }

    /**
     * Shows processing dialog and performs nutrition analysis asynchronously,
     * as a modal popup dialog
     */
    private void showProcessingAndAnalyzeNutrition(Post newPost, String ingredients) {
        try {
            ProcessingDialogController processingController = createAndShowProcessingDialog();
            startNutritionAnalysis(newPost, ingredients, processingController);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show processing screen: {0}", new Object[]{e.getMessage()});
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
            fallbackSavePost(newPost);
        }
    }

    private ProcessingDialogController createAndShowProcessingDialog() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/processingDialog.fxml"));
        Parent root = loader.load();
        ProcessingDialogController controller = loader.getController();
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Creating Post");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root));
        dialogStage.setResizable(false);
        controller.setDialogStage(dialogStage);
        dialogStage.centerOnScreen();
        dialogStage.show();
        return controller;
    }

    private void startNutritionAnalysis(Post newPost, String ingredients, ProcessingDialogController processingController) {
        Task<Void> analysisTask = new Task<>() {
            @Override
            protected Void call() {
                AnalysisOutcome outcome = performNutritionAnalysisWithRetries(newPost, ingredients, processingController);
                handlePostAnalysisOutcome(outcome, newPost, processingController);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> finalizeSuccessfulPostCreation(newPost, processingController));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> finalizeFailedAnalysis(newPost, processingController, getException()));
            }
        };
        Thread analysisThread = new Thread(analysisTask);
        analysisThread.setDaemon(true);
        analysisThread.start();
    }

    private AnalysisOutcome performNutritionAnalysisWithRetries(Post newPost, String ingredients, ProcessingDialogController processingController) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            AnalysisOutcome outcome = attemptNutritionAnalysisAttempt(newPost, ingredients, processingController, attempt);
            if (outcome.type == AnalysisOutcome.Type.SUCCESS) return outcome;
            if (outcome.type == AnalysisOutcome.Type.API_KEY || outcome.type == AnalysisOutcome.Type.NETWORK || attempt == MAX_RETRIES) {
                return outcome;
            }
            lastException = outcome.exception;
            scheduleRetryStatusUpdate(processingController, attempt + 1);
            sleepQuietly(RETRY_DELAY_MS);
        }
        return AnalysisOutcome.failure(lastException);
    }

    private AnalysisOutcome attemptNutritionAnalysisAttempt(Post newPost, String ingredients, ProcessingDialogController controller, int attempt) {
        try {
            logInfo("Analyzing nutrition facts for ingredients (attempt {0}/{1}): {2}", attempt, MAX_RETRIES, ingredients);
            updateStatus(controller, attempt == 1 ? "Analyzing nutrition facts..." : java.text.MessageFormat.format("Analyzing nutrition facts... (retry {0}/{1})", attempt, MAX_RETRIES));
            String response = chatbotAPI.analyzeNutritionFacts(ingredients);
            newPost.setNutrition(nutritionParser.parseNutritionFromResponse(response));
            if (hasValidNutrition(newPost)) {
                logInfo("Nutrition analysis completed successfully on attempt {0}", attempt);
                updateStatus(controller, "Nutrition analysis completed successfully!");
                return AnalysisOutcome.success();
            }
            throw new InvalidNutritionDataException("Empty or invalid nutrition data");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (isApiKeyIssue(msg)) return AnalysisOutcome.apiKeyIssue(e);
            if (isNetworkIssue(msg)) return AnalysisOutcome.networkIssue(e);
            return AnalysisOutcome.failure(e);
        }
    }

    private void handlePostAnalysisOutcome(AnalysisOutcome outcome, Post newPost, ProcessingDialogController controller) {
        if (outcome.type == AnalysisOutcome.Type.SUCCESS) {
            return;
        }
        Platform.runLater(() -> {
            switch (outcome.type) {
                case API_KEY:
                    logWarn("Skipping nutrition analysis - API key not configured");
                    controller.updateStatus("API key not configured. Using default values...");
                    showPopupDialog(POPUP_APIKEY);
                    break;
                case NETWORK:
                    logWarn("Skipping nutrition analysis - network issues detected");
                    controller.updateStatus("Network issues detected. Using default values...");
                    showPopupDialog(POPUP_NETWORK);
                    break;
                case FAILURE:
                    logSevere("All nutrition analysis attempts failed. Last error: {0}", outcome.exception != null ? outcome.exception.getMessage() : "Unknown");
                    controller.updateStatus("Nutrition analysis failed. Using default values...");
                    showPopupDialog(POPUP_ANALYSIS_FAILED);
                    break;
                case SUCCESS:
                default:
                    break; // no action
            }
            newPost.setNutrition(nutritionParser.parseNutritionFromResponse(null));
        });
    }

    private boolean hasValidNutrition(Post post) {
        return post.getNutrition() != null && post.getNutrition().getIngredient() != null && !post.getNutrition().getIngredient().isEmpty();
    }

    private boolean isApiKeyIssue(String msg) {
        return msg.contains("api key") || msg.contains("configure your openai api key");
    }

    private boolean isNetworkIssue(String msg) {
    return msg.contains(POPUP_NETWORK) || msg.contains("connection") || msg.contains("timeout") || msg.contains("unreachable") || msg.contains("failed to get response");
    }

    private void scheduleRetryStatusUpdate(ProcessingDialogController controller, int nextAttempt) {
        Platform.runLater(() -> updateStatus(controller, java.text.MessageFormat.format("Analysis failed, retrying... ({0}/{1})", nextAttempt, MAX_RETRIES)));
    }

    private void sleepQuietly(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private void finalizeSuccessfulPostCreation(Post newPost, ProcessingDialogController controller) {
        try {
            updateStatus(controller, "Saving post...");
            saveNewPost(newPost);
            success = true;
            controller.closeDialog();
            showResultDialog(RESULT_POST_CREATED_SUCCESS);
            refreshCommunity();
        } catch (Exception e) {
            logger.log(Level.SEVERE, () -> "Failed to save post or refresh community page: " + e.getMessage());
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
            handleFinalizeFailure(controller);
        }
    }

    private void finalizeFailedAnalysis(Post newPost, ProcessingDialogController controller, Throwable error) {
        logger.log(Level.SEVERE, "Nutrition analysis task failed completely", error);
        try {
            updateStatus(controller, "Saving post with default values...");
            newPost.setNutrition(nutritionParser.parseNutritionFromResponse(null));
            saveNewPost(newPost);
            success = true;
            controller.closeDialog();
            showResultDialog(RESULT_POST_CREATED_SUCCESS);
            refreshCommunity();
        } catch (Exception e) {
            logger.log(Level.SEVERE, () -> "Failed to save post: " + e.getMessage());
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
            handleFinalizeFailure(controller);
        }
    }

    private void handleFinalizeFailure(ProcessingDialogController controller) {
        try {
            controller.closeDialog();
            showResultDialog(RESULT_POST_CREATION_FAILED);
            refreshCommunity();
        } catch (Exception fallbackEx) {
            logger.log(Level.SEVERE, () -> "Fallback navigation failed: " + fallbackEx.getMessage());
        }
    }

    private void saveNewPost(Post post) {
        List<Post> posts = repository.loadPosts();
        posts.add(0, post);
        repository.savePosts(posts);
    }

    private void refreshCommunity() {
        if (mainController != null) {
            mainController.refreshCommunityPage();
        }
    }

    private void fallbackSavePost(Post newPost) {
        try {
            newPost.setNutrition(nutritionParser.parseNutritionFromResponse(null));
            saveNewPost(newPost);
            success = true;
            refreshCommunity();
        } catch (Exception fallbackEx) {
            logger.log(Level.SEVERE, () -> "Fallback save and navigation failed: " + fallbackEx.getMessage());
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, fallbackEx);
        }
    }

    private void logWarn(String pattern, Object... args) { if (logger.isLoggable(Level.WARNING)) logger.log(Level.WARNING, java.text.MessageFormat.format(pattern, args)); }
    private void logSevere(String pattern, Object... args) { if (logger.isLoggable(Level.SEVERE)) logger.log(Level.SEVERE, java.text.MessageFormat.format(pattern, args)); }
    private void logInfo(String pattern, Object... args) { if (logger.isLoggable(Level.INFO)) logger.log(Level.INFO, java.text.MessageFormat.format(pattern, args)); }

    private void updateStatus(ProcessingDialogController controller, String message) {
        try { controller.updateStatus(message); } catch (Exception ignored) { /* dialog may be closed */ }
    }

    private static class AnalysisOutcome {
        enum Type { SUCCESS, API_KEY, NETWORK, FAILURE }
        final Type type;
        final Exception exception;
        private AnalysisOutcome(Type type, Exception exception) { this.type = type; this.exception = exception; }
        static AnalysisOutcome success() { return new AnalysisOutcome(Type.SUCCESS, null); }
        static AnalysisOutcome apiKeyIssue(Exception e) { return new AnalysisOutcome(Type.API_KEY, e); }
        static AnalysisOutcome networkIssue(Exception e) { return new AnalysisOutcome(Type.NETWORK, e); }
        static AnalysisOutcome failure(Exception e) { return new AnalysisOutcome(Type.FAILURE, e); }
    }
    private static class InvalidNutritionDataException extends Exception { InvalidNutritionDataException(String msg) { super(msg); } }
    private static final String POPUP_APIKEY = "apikey";
    private static final String POPUP_NETWORK = "network";
    private static final String POPUP_ANALYSIS_FAILED = "analysis_failed";
    private static final String RESULT_POST_CREATED_SUCCESS = "post_created_success";
    private static final String RESULT_POST_CREATION_FAILED = "post_creation_failed";
    private static final String RESULT_NUTRITION_ANALYSIS_SUCCESS = "nutrition_analysis_success";
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
                case POPUP_APIKEY -> controller.setApiKeyWarning();
                case POPUP_NETWORK -> controller.setNetworkWarning();
                case POPUP_ANALYSIS_FAILED -> controller.setAnalysisFailedWarning();
                default -> controller.setMessage("An error occurred during nutrition analysis. Using default values.");
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
            logger.log(Level.WARNING, "Failed to show warning dialog: {0}", new Object[]{e.getMessage()});
            logger.log(Level.WARNING, EXCEPTION_DETAILS, e);
            // Fallback: just log the warning
            logger.warning("WARNING: " + warningType + " - Nutrition analysis skipped, using default values");
        }
    }
    
    /**
     * Shows a popup dialog with success or failure message
     */
    private void showResultDialog(String resultType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/popupDialog.fxml"));
            Parent root = loader.load();
            
            PopupDialogController controller = loader.getController();
            
            // Set appropriate message based on result type
            switch (resultType) {
                case RESULT_POST_CREATED_SUCCESS -> controller.setPostCreatedSuccess();
                case RESULT_POST_CREATION_FAILED -> controller.setPostCreationFailed();
                case RESULT_NUTRITION_ANALYSIS_SUCCESS -> controller.setNutritionAnalysisSuccess();
                default -> controller.setMessage("Operation completed.");
            }
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resultType.contains("success") ? "Success" : "Error");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            
            // Center the dialog
            dialogStage.centerOnScreen();
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to show result dialog: {0}", new Object[]{e.getMessage()});
            logger.log(Level.SEVERE, EXCEPTION_DETAILS, e);
        }
    }
}