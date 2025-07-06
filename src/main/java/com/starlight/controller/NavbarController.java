package com.starlight.controller;

import com.starlight.models.User;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class NavbarController {
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
        main.loadPage("profile");
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
            profile.setText(currentUser.username);
        } else {
            profile.setText("Guest");
        }
    }
}