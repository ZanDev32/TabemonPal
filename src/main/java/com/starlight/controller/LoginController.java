package com.starlight.controller;

import java.net.URL;
import java.util.ResourceBundle;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class LoginController implements Initializable{
    @FXML
    private ImageView AppLogo;

    @FXML
    private Label username;

    @FXML
    private MFXTextField email;

    @FXML
    private MFXTextField password;

    @FXML
    private MFXButton Loginbutton;

    @FXML
    private MFXButton register;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

}
