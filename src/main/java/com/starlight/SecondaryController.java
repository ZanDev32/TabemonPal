package com.starlight;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SecondaryController {
    @FXML
    private Button secondaryButton;


    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}