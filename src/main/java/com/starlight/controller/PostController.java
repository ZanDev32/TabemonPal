package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class PostController {
    @FXML
    private MFXButton verdict;

    @FXML
    private VBox recipeContainer;

    @FXML
    private PieChart nutritionFacts;

    @FXML
    private VBox postTemplate;

    @FXML
    private VBox post1;

    @FXML
    private ImageView profile1;

    @FXML
    private Label username;

    @FXML
    private Label uploadtime;

    @FXML
    private Label title;

    @FXML
    private Label description;

    @FXML
    private ImageView recentphoto1;




    @FXML
    private void initialize() {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
    }
}
