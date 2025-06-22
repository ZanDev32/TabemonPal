package com.starlight.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.models.User;

public class RegisterController implements Initializable{
    @FXML
    private ImageView AppLogo;

    @FXML
    private Label username;

    @FXML
    private MFXTextField email;

    @FXML
    private MFXTextField email1;

    @FXML
    private MFXTextField password;

    @FXML
    private MFXButton register;

    @FXML
    private MFXButton login;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        register.setOnAction(event -> {
            String user = email.getText();
            String em = email1.getText();
            String pass = password.getText();
            try {
                URL url = new URL("http://localhost:8000/register");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/xml");
                conn.setDoOutput(true);
                User u = new User();
                u.username = user;
                u.email = em;
                u.password = pass;
                XStream xs = new XStream(new DomDriver());
                xs.allowTypesByWildcard(new String[]{"com.starlight.models.*"});
                xs.alias("user", User.class);
                String xml = xs.toXML(u);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(xml.getBytes(StandardCharsets.UTF_8));
                }
                if (conn.getResponseCode() == 201) {
                    System.out.println("Register success");
                } else {
                    System.out.println("Register failed: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        login.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/LoginDialog.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
