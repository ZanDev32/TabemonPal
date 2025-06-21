package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CommunityController implements Initializable {
    @FXML
    private Label dailytitle1;

    @FXML
    private Label starrating;

    @FXML
    private Label dailylikecounter;

    @FXML
    private Label dailytitle2;

    @FXML
    private Label starrating2;

    @FXML
    private Label dailylikecounter2;

    @FXML
    private Label dailytitle3;

    @FXML
    private Label starrating3;

    @FXML
    private Label dailylikecounter3;

    @FXML
    private Label dailytitle4;

    @FXML
    private Label starrating4;

    @FXML
    private Label dailylikecounter4;

    @FXML
    private VBox post1;

    @FXML
    private Label username;

    @FXML
    private Label uploadtime;

    @FXML
    private Label description;

    @FXML
    private ImageView likebutton1;

    @FXML
    private Label likecounter;

    @FXML
    private ImageView profile1;
    @FXML
    private ImageView dailyphoto1;
    @FXML
    private ImageView dailyphoto2;
    @FXML
    private ImageView dailyphoto3;
    @FXML
    private ImageView dailyphoto4;
    @FXML
    private ImageView recentphoto1;
    @FXML
    private MFXButton CreatePost;

    private void cropToFit(ImageView imageView, double frameWidth, double frameHeight, double arcRadius) {
        if (imageView.getImage() == null) return;
        Image image = imageView.getImage();
        double scaleX = frameWidth / image.getWidth();
        double scaleY = frameHeight / image.getHeight();
        double scale = Math.max(scaleX, scaleY);
        imageView.setFitWidth(image.getWidth() * scale);
        imageView.setFitHeight(image.getHeight() * scale);
        Rectangle clip = new Rectangle(frameWidth, frameHeight);
        clip.setArcWidth(arcRadius);
        clip.setArcHeight(arcRadius);
        imageView.setClip(clip);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dailyphoto1.setImage(new Image(getClass().getResource("/com/starlight/images/image_1.jpg").toExternalForm()));
        dailyphoto2.setImage(new Image(getClass().getResource("/com/starlight/images/image_2.jpg").toExternalForm()));
        dailyphoto3.setImage(new Image(getClass().getResource("/com/starlight/images/image_3.jpg").toExternalForm()));
        dailyphoto4.setImage(new Image(getClass().getResource("/com/starlight/images/image_4.png").toExternalForm()));

        cropToFit(dailyphoto1, 178, 110, 20);
        cropToFit(dailyphoto2, 178, 110, 20);
        cropToFit(dailyphoto3, 178, 110, 20);
        cropToFit(dailyphoto4, 178, 110, 20);

        recentphoto1.setImage(new Image(getClass().getResource("/com/starlight/images/recent_1.png").toExternalForm()));

        cropToFit(recentphoto1, 766, 150, 20);

        CreatePost.setOnAction(event -> showCreatePostPopup());
    }

    private void showCreatePostPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/createPost.fxml"));
            Parent popupRoot = loader.load();

            // Scale in animation
            popupRoot.setScaleX(0.7);
            popupRoot.setScaleY(0.7);

            Scene popupScene = new Scene(popupRoot);
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(popupScene);
            popupStage.setTitle("Create Post");
            popupStage.setResizable(false);

            // Center animation after showing
            popupStage.setOnShown(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(220), popupRoot);
                st.setFromX(0.7);
                st.setFromY(0.7);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
