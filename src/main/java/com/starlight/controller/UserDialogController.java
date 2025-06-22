package com.starlight.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.models.User;

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
        savebutton.setOnAction(event -> {
            String user = username.getText().replace("Hello, ", "").trim();
            String newEmail = email.getText();
            String newPass = password.getText();
            String birth = birthday.getValue() != null ? birthday.getValue().toString() : null;
            try {
                URL endpoint = new URL("http://localhost:8000/users/" + user);
                HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/xml");
                conn.setDoOutput(true);
                User u = new User();
                u.email = newEmail;
                u.password = newPass;
                u.birthDay = birth;
                XStream xs = new XStream(new DomDriver());
                xs.allowTypesByWildcard(new String[]{"com.starlight.models.*"});
                xs.alias("user", User.class);
                String xml = xs.toXML(u);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(xml.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 200) {
                    System.out.println("User updated");
                } else {
                    System.out.println("Update failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        cancelbutton.setOnAction(event -> {
            Stage stage = (Stage) cancelbutton.getScene().getWindow();
            stage.close();
        });
    }

}
