package com.starlight.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.io.InputStream;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.models.User;
import com.starlight.util.Session;
import com.starlight.App;

/**
 * Controller handling user login and transition to the main application.
 */
public class LoginController implements Initializable {
    @FXML
    private ImageView AppLogo;

    @FXML
    private Label username;

    @FXML
    private MFXTextField emailOrUsername;

    @FXML
    private MFXTextField password;

    @FXML
    private MFXButton loginButton;

    @FXML
    private MFXButton register;

    /**
     * Handles login button click event.
     */
    @FXML
    void handleLogin(MouseEvent event) {
        performLogin();
    }
    
    /**
     * Handles Enter key press in password field.
     */
    @FXML
    void handleLogin(KeyEvent event) {
        // Only proceed if Enter key is pressed
        if (event.getCode() == KeyCode.ENTER) {
            performLogin();
        }
    }
    
    /**
     * Common login logic shared between mouse click and Enter key press.
     */
    private void performLogin() {
        String em = emailOrUsername.getText();
        String pass = password.getText();
        try {
            URL url = new URL("http://localhost:8000/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setDoOutput(true);

            User creds = new User();
            creds.email = em;
            creds.password = pass;

            XStream xs = new XStream(new DomDriver());
            xs.allowTypesByWildcard(new String[]{"com.starlight.models.*"});
            xs.alias("user", User.class);

            String xml = xs.toXML(creds);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(xml.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 200) {
                try (InputStream is = conn.getInputStream()) {
                    User logged = (User) xs.fromXML(is);
                    Session.setCurrentUser(logged);
                }
                System.out.println("Login success");
                App.loadMainWithSplash();
            } else {
                System.out.println("Login failed: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles register button click event.
     */
    @FXML
    void handleRegister(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/RegisterDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        password.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performLogin();
            }
        });

        emailOrUsername.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performLogin();
            }
        });
    }
}
