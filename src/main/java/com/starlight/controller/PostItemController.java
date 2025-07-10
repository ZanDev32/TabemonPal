package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Simple controller used for dynamically loaded post items.
 */
public class PostItemController {
    @FXML
    private VBox postTemplate;

    @FXML
    private VBox post1;

    @FXML
    private ImageView likebutton;

    @FXML
    private MFXButton commentcounter;

    @FXML
    private ImageView likebutton1;

    @FXML
    private MFXButton sharebutton;

    @FXML
    private ImageView likebutton11;

    @FXML 
    public Label username;
    @FXML 
    public Label uploadtime;
    @FXML 
    public Label title;
    @FXML 
    public Label description;
    @FXML 
    public ImageView recentphoto1;
    @FXML 
    public ImageView profile1;
    @FXML 
    public MFXButton likecounter;

    @FXML
    private void initialize() {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
    }
}
