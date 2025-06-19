package com.starlight.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    private MFXTextField ingredients;

    @FXML
    private MFXTextField directions;

    @FXML
    private MFXButton submit;

    @FXML
    private MFXButton cancel;

    private File selectedImage;

    private final String XML_PATH = "src/main/java/com/starlight/models/PostData.xml";

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

            boolean success = savePostToXML(postTitle, postDescription, postIngredients, postDirections, selectedImage.getAbsolutePath());
            if (success) {
                title.clear();
                description.clear();
                ingredients.clear();
                directions.clear();
                selectedImage = null;
                status.setText("Post submitted and saved!");
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

    private boolean savePostToXML(String title, String description, String ingredients, String directions, String imagePath) {
        try {
            File xmlFile = new File(XML_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;

            if (!xmlFile.exists() || xmlFile.length() == 0) {
                doc = dBuilder.newDocument();
                Element root = doc.createElement("posts");
                doc.appendChild(root);
            } else {
                try {
                    doc = dBuilder.parse(xmlFile);
                } catch (Exception ex) {
                    doc = dBuilder.newDocument();
                    Element root = doc.createElement("posts");
                    doc.appendChild(root);
                }
            }

            Element root = doc.getDocumentElement();

            Element post = doc.createElement("post");

            Element titleElem = doc.createElement("title");
            titleElem.appendChild(doc.createTextNode(title));
            post.appendChild(titleElem);

            Element descElem = doc.createElement("description");
            descElem.appendChild(doc.createTextNode(description));
            post.appendChild(descElem);

            Element ingElem = doc.createElement("ingredients");
            ingElem.appendChild(doc.createTextNode(ingredients));
            post.appendChild(ingElem);

            Element dirElem = doc.createElement("directions");
            dirElem.appendChild(doc.createTextNode(directions));
            post.appendChild(dirElem);

            Element imgElem = doc.createElement("image");
            imgElem.appendChild(doc.createTextNode(imagePath));
            post.appendChild(imgElem);

            root.appendChild(post);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
