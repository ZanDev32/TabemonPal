package com.starlight.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.starlight.models.Post;
import com.starlight.models.PostDataRepository;

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
    private Label title;

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
    private MFXButton likebutton;

    @FXML
    private Label likecounter;

    @FXML
    private VBox postlist;

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

    private final PostDataRepository repository = new PostDataRepository();

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

    private void loadPosts() {
        postlist.getChildren().clear();
        postlist.getChildren().add(post1);

        List<Post> posts = repository.loadPosts();
        if (posts.isEmpty()) {
            username.setText("");
            uploadtime.setText("");
            title.setText("");
            description.setText("");
            recentphoto1.setImage(null);
            likecounter.setText("");
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            String tits = p.title;
            String pp = p.profilepicture;
            String usr = p.username;
            String desc = p.description;
            String image = p.image;
            String time = p.uploadtime;
            String likes = p.likecount;

            if (i == 0) {
                username.setText(usr);
                title.setText(tits);
                description.setText(desc);
                uploadtime.setText(time);
                likecounter.setText(likes);
                File fp = new File(pp);
                if (fp.exists()) {
                    profile1.setImage(new Image(fp.toURI().toString()));
                } else {
                    profile1.setImage(new Image(getClass().getResource("/com/starlight/images/missing.png").toExternalForm()));
                }
                File f = new File(image);
                if (f.exists()) {
                    recentphoto1.setImage(new Image(f.toURI().toString()));
                } else {
                    recentphoto1.setImage(new Image(getClass().getResource("/com/starlight/images/missing.png").toExternalForm()));
                }
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/postItem.fxml"));
                    VBox node = loader.load();
                    Label u = (Label) node.lookup("#username");
                    Label ut = (Label) node.lookup("#uploadtime");
                    Label t = (Label) node.lookup("#title");
                    Label d = (Label) node.lookup("#description");
                    ImageView img = (ImageView) node.lookup("#image");
                    Label lc = (Label) node.lookup("#likecounter");
                    u.setText(usr);
                    t.setText(tits);
                    ut.setText(time);
                    d.setText(desc);
                    lc.setText(likes);
                    File fi = new File(image);
                    if (fi.exists()) {
                        img.setImage(new Image(fi.toURI().toString()));
                    } else {
                        img.setImage(new Image(getClass().getResource("/com/starlight/images/missing.png").toExternalForm()));
                    }
                    postlist.getChildren().add(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        repository.ensureDummyData();
        loadPosts();

        CreatePost.setOnAction(event -> showCreatePostPopup());
    }

    private void showCreatePostPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/createPost.fxml"));
            Parent popupRoot = loader.load();

            CreatePostController controller = loader.getController();

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
            if (controller.isSuccess()) {
                loadPosts();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}