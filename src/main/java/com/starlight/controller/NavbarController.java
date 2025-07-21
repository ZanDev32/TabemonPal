package com.starlight.controller;

import com.starlight.model.User;
import com.starlight.util.ImageUtils;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class NavbarController {
    @FXML
    private ImageView photoprofile;

    @FXML
    private HBox navbar;

    @FXML
    private MFXTextField searchbar;

    @FXML
    private MFXButton setting;

    @FXML
    private MFXButton inbox;

    @FXML
    private MFXButton notification;

    @FXML
    private MFXButton profile;

    private MainController main;

    public void setMainController(MainController main) {
        this.main = main;
    }

    @FXML
    void profile(MouseEvent event) {
        main.loadPage("editAccount");
    }

    @FXML
    void setting(MouseEvent event) {
        main.loadPage("setting");
    }

    @FXML
    void notification(MouseEvent event) {
        main.loadPage("notification");
    }

    @FXML
    void inbox(MouseEvent event) {
        main.loadPage("inbox");
    }

    @FXML
    private void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.profilepicture != null && !currentUser.profilepicture.isEmpty()) {
                ImageUtils.loadImage(photoprofile, currentUser.profilepicture, "/com/starlight/images/default-profile.png");
            } else {
                ImageUtils.loadImage(photoprofile, "/com/starlight/images/default-profile.png");
            }
            ImageUtils.scaleToFit(photoprofile, 40, 40, 80);
        } else {
            ImageUtils.loadImage(photoprofile, "/com/starlight/images/missing.png");
            ImageUtils.scaleToFit(photoprofile, 40, 40, 80);
        }
    }
}