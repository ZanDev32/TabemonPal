package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class RecipeItemController {
    @FXML
    private ImageView image;

    @FXML
    private Label title;

    @FXML
    private Label rating;

    @FXML
    private Label likecount;

    @FXML
    private MFXButton editRecipe;

    @FXML
    private MFXButton deletePost;

    @FXML
    private void initialize() {
        
    }
    
    /**
     * Sets the recipe data for this recipe item
     */
    public void setRecipeData(String title, String rating, String likecount) {
        if (this.title != null) {
            this.title.setText(title);
        }
        if (this.rating != null) {
            this.rating.setText(rating);
        }
        if (this.likecount != null) {
            this.likecount.setText(likecount);
        }
    }
    
    /**
     * Gets the image view for external image loading
     */
    public ImageView getImageView() {
        return image;
    }
}
