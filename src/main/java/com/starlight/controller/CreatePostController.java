package com.starlight.controller;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;

import com.starlight.models.Post;
import com.starlight.models.PostDataRepository;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;


public class CreatePostController implements Initializable {

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

    public boolean isSuccess() {
        return success;
    }

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
            String postIngredients = ingredients.getText();
            String postDirections = directions.getText();

            if (postTitle.isEmpty() || postDescription.isEmpty() || postIngredients.isEmpty() || postDirections.isEmpty() || selectedImage == null) {
                System.err.println("Please complete all fields and select an image.");
                return;
            }

            boolean result = savePostToXML(postTitle, postDescription, postIngredients, postDirections, selectedImage.getAbsolutePath());
            success = result;
            if (result) {
                title.clear();
                description.clear();
                ingredients.clear();
                directions.clear();
                selectedImage = null;
                System.out.println("Post submitted and saved!");
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                System.err.println("Error saving post.");
            }
        });

        cancel.setOnAction(event -> {
            title.clear();
            description.clear();
            ingredients.clear();
            directions.clear();
            selectedImage = null;
            System.err.println("Post creation canceled.");

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    private boolean savePostToXML(String title, String description, String ingredients, String directions, String imagePath) {
        try {
            List<Post> posts = repository.loadPosts();
            Post post = new Post();
            post.title = title;
            post.description = description;
            post.ingredients = ingredients;
            post.directions = directions;
            post.image = imagePath;
            post.rating = "0";
            post.uploadtime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            post.likecount = "0";
            posts.add(post);
            repository.savePosts(posts);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}