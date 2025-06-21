package com.starlight.controller;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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
    private Label status;

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

    private final String XML_PATH = "src/main/java/com/starlight/models/PostData.xml";

    private static class Post {
        String title;
        String description;
        String ingredients;
        String directions;
        String image;
        String rating;
        String uploadtime;
        String likecount;
    }

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
                status.setText("Please complete all fields and select an image.");
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
                status.setText("Post submitted and saved!");
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                status.setText("Error saving post.");
            }
        });

        cancel.setOnAction(event -> {
            title.clear();
            description.clear();
            ingredients.clear();
            directions.clear();
            selectedImage = null;
            status.setText("Post creation canceled.");

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.close();
        });
    }

    private boolean savePostToXML(String title, String description, String ingredients,
                                  String directions, String imagePath) {
        try {
            List<Post> posts = readPosts();
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
            writePosts(posts);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<Post> readPosts() {
        List<Post> list = new ArrayList<>();
        File xmlFile = new File(XML_PATH);
        if (!xmlFile.exists()) return list;
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(fis);
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
                                case "ingredients":
                                    current.ingredients = text;
                                    break;
                                case "directions":
                                    current.directions = text;
                                    break;
                                case "image":
                                    current.image = text;
                                    break;
                                case "rating":
                                    current.rating = text;
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

    private void writePosts(List<Post> posts) throws Exception {
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = outFactory.createXMLStreamWriter(new FileOutputStream(XML_PATH), "UTF-8");
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("posts");
        for (Post p : posts) {
            writer.writeStartElement("post");

            writer.writeStartElement("title");
            writer.writeCharacters(p.title);
            writer.writeEndElement();

            writer.writeStartElement("description");
            writer.writeCharacters(p.description);
            writer.writeEndElement();

            writer.writeStartElement("ingredients");
            writer.writeCharacters(p.ingredients);
            writer.writeEndElement();

            writer.writeStartElement("directions");
            writer.writeCharacters(p.directions);
            writer.writeEndElement();

            writer.writeStartElement("image");
            writer.writeCharacters(p.image);
            writer.writeEndElement();

            writer.writeStartElement("rating");
            writer.writeCharacters(p.rating);
            writer.writeEndElement();

            writer.writeStartElement("uploadtime");
            writer.writeCharacters(p.uploadtime);
            writer.writeEndElement();

            writer.writeStartElement("likecount");
            writer.writeCharacters(p.likecount);
            writer.writeEndElement();

            writer.writeEndElement(); // post
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }
}
