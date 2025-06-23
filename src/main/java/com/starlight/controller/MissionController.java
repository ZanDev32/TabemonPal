package com.starlight.controller;

<<<<<<< HEAD
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;

public class MissionController implements Initializable{

    @Override
    public void initialize(URL url, ResourceBundle rb) {

=======
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class MissionController implements Initializable {
    @FXML
    private Button missionButton;
    @FXML
    private Button streakButton;
    @FXML
    private Button dailyButton;
    @FXML
    private Button rewardButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
>>>>>>> e394bb1 (Aku nambahin dikit di bagian mission)
    }
    
}
