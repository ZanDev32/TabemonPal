package com.starlight.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

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

    private final String XML_PATH = "src/main/java/com/starlight/models/PostData.xml";

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

    private static class Post {
        String title;
        String description;
        String image;
        String uploadtime;
        String likecount;
    }

    private void loadPosts() {
        postlist.getChildren().clear();
        postlist.getChildren().add(post1);

        List<Post> posts = parsePosts();
        if (posts.isEmpty()) {
            username.setText("");
            uploadtime.setText("");
            description.setText("");
            recentphoto1.setImage(null);
            likecounter.setText("");
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            String title = p.title;
            String desc = p.description;
            String image = p.image;
            String time = p.uploadtime;
            String likes = p.likecount;

            if (i == 0) {
                username.setText(title);
                description.setText(desc);
                uploadtime.setText(time);
                likecounter.setText(likes);
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
                    Label d = (Label) node.lookup("#description");
                    ImageView img = (ImageView) node.lookup("#image");
                    Label lc = (Label) node.lookup("#likecounter");
                    u.setText(title);
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

    private List<Post> parsePosts() {
        List<Post> list = new ArrayList<>();
        File xmlFile = new File(XML_PATH);
        if (!xmlFile.exists()) return list;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new java.io.FileInputStream(xmlFile));
            Post current = null;
            String currentTag = null;
            StringBuilder buffer = new StringBuilder();
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        currentTag = reader.getLocalName();
                        if ("post".equals(currentTag)) {
                            current = new Post();
                        } else {
                            buffer.setLength(0);
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (currentTag != null) {
                            buffer.append(reader.getText());
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        String end = reader.getLocalName();
                        if (current != null) {
                            String text = buffer.toString().trim();
                            switch (end) {
                                case "title":
                                    current.title = text;
                                    break;
                                case "description":
                                    current.description = text;
                                    break;
                                case "image":
                                    current.image = text;
                                    break;
                                case "uploadtime":
                                    current.uploadtime = text;
                                    break;
                                case "likecount":
                                    current.likecount = text;
                                    break;
                                case "post":
                                    list.add(current);
                                    current = null;
                                    break;
                            }
                        }
                        currentTag = null;
                        break;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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
