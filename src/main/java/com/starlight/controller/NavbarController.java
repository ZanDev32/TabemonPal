package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;

public class NavbarController {
    @FXML
    private MFXTextField searchbar;

    @FXML
    private MFXButton setting;

    @FXML
    private MFXButton message;

    @FXML
    private MFXButton notification;

    @FXML
    private MFXButton profile;

    

    @FXML
    private void initialize() {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
    }
}
