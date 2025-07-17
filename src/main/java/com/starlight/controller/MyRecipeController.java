package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class MyRecipeController {
    @FXML
    private ImageView RecipePhoto;

    @FXML
    private MFXButton editRecipe;

    @FXML
    private MFXButton deletePost;

    @FXML
    private Label RecipeTitle;

    @FXML
    private Label starrating;

    @FXML
    private Label likecounter;



    @FXML
    private void initialize() {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
    }
}
