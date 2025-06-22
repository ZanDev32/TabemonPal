package com.starlight.controller;

import java.net.URL;
import java.util.ResourceBundle;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class UserDialogController implements Initializable{
    @FXML
    private ImageView profile;

    @FXML
    private MFXButton imagepicker;

    @FXML
    private Label username;

    @FXML
    private MFXTextField email;

    @FXML
    private MFXTextField password;

    @FXML
    private MFXDatePicker birthday;

    @FXML
    private MFXButton savebutton;

    @FXML
    private MFXButton cancelbutton;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

}
