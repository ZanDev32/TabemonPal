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

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;


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

    /**
     * Formats text by replacing newlines with commas and cleaning up spacing.
     * This ensures ingredients and directions are stored as comma-separated single lines.
     *
     * @param text the text to format
     * @return formatted text with commas instead of newlines
     */
    private String formatTextAsCommaSeparated(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // Replace newlines with commas, trim whitespace, and remove empty entries
        return text.trim()
                  .replaceAll("\\r?\\n", ", ")  // Replace newlines with ", "
                  .replaceAll(",\\s*,", ",")    // Remove duplicate commas
                  .replaceAll("^,\\s*|,\\s*$", "") // Remove leading/trailing commas
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
            String postIngredients = formatTextAsCommaSeparated(ingredients.getText());
            String postDirections = formatTextAsCommaSeparated(directions.getText());

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
            newPost.rating = "0.0";

            // Save the new post
            List<Post> posts = repository.loadPosts();
            posts.add(0, newPost);
            repository.savePosts(posts);

            success = true;

            // Close the dialog
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.close();
        });

        cancel.setOnAction(event -> {
            // Close the dialog without saving
            Stage stage = (Stage) cancel.getScene().getWindow();
            stage.close();
        });
    }
}