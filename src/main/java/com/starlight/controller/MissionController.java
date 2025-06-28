package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

/**
 * Controller for the missions and rewards view.
 */
public class MissionController implements Initializable {
    @FXML
    private Button missionButton;
    @FXML
    private Button streakButton;
    @FXML
    private Button dailyButton;
    @FXML
    private Button rewardButton;
    
    /**
     * Initializes the controller. Currently does nothing.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // no-op
    }
    
}
