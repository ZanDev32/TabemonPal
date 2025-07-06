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
import javafx.stage.Stage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.models.User;
import com.starlight.util.Session;

/**
 * Controller for the user settings dialog where profile information can be
 * edited.
 */
public class UserSettingController implements Initializable {
    @FXML
    private ImageView profileimage;

    @FXML
    private MFXButton imagepicker;

    @FXML
    private Label welcomeLabel;

    @FXML
    private MFXTextField emailField;
    
    @FXML 
    private MFXTextField passwordField;
    
    @FXML 
    private MFXDatePicker birthDayPicker;

    @FXML
    private MFXButton savebutton;

    @FXML
    private MFXButton cancelbutton;

    @FXML
    private MFXButton deleteaccbutton;

    private User currentUser;
    private Runnable onLogout;

    // Remove usernameField, use currentUser.username for all username logic
    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) welcomeLabel.setText("Hello, " + user.username);
        if (emailField != null) emailField.setText(user.email);
        if (passwordField != null) passwordField.setText(user.password);
        if (birthDayPicker != null && user.birthDay != null && !user.birthDay.isEmpty()) {
            birthDayPicker.setValue(java.time.LocalDate.parse(user.birthDay));
        }
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    /**
     * Wires up the save and cancel buttons for the dialog.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        savebutton.setOnAction(event -> {
            String newEmail = emailField.getText();
            String newPass = passwordField.getText();
            String birth = birthDayPicker.getValue() != null ? birthDayPicker.getValue().toString() : null;
            try {
                URL endpoint = new URL("http://localhost:8000/users/" + currentUser.username);
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
        // Add delete logic
        if (cancelbutton.getParent() != null && cancelbutton.getParent().lookup("#deleteBtn") != null) {
            MFXButton deleteBtn = (MFXButton) cancelbutton.getParent().lookup("#deleteBtn");
            deleteBtn.setOnAction(event -> {
                // Remove user from UserDataRepository
                if (currentUser != null) {
                    com.starlight.models.UserDataRepository repo = new com.starlight.models.UserDataRepository();
                    java.util.List<com.starlight.models.User> users = repo.loadUsers();
                    users.removeIf(u -> u.username.equals(currentUser.username));
                    repo.saveUsers(users);
                    if (onLogout != null) onLogout.run();
                    Stage stage = (Stage) deleteBtn.getScene().getWindow();
                    stage.close();
                }
            });
        }
        User currentSessionUser = Session.getCurrentUser();
        if (currentSessionUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Hello, " + currentSessionUser.username);
        }
    }

    @FXML
    private void onUpdate() {
        String email = emailField.getText();
        String password = passwordField.getText();
        java.time.LocalDate birthDay = birthDayPicker.getValue();
        if (email.isEmpty() || password.isEmpty()) {
            System.err.println("All fields are required.");
            return;
        }
        com.starlight.models.UserDataRepository repo = new com.starlight.models.UserDataRepository();
        java.util.List<com.starlight.models.User> users = repo.loadUsers();
        for (com.starlight.models.User u : users) {
            if (u.username.equals(currentUser.username)) {
                u.email = email;
                u.password = password;
                u.birthDay = birthDay != null ? birthDay.toString() : null;
                break;
            }
        }
        repo.saveUsers(users);
        System.out.println("User updated successfully");
        currentUser.email = email;
        currentUser.password = password;
        currentUser.birthDay = birthDay != null ? birthDay.toString() : null;
    }

    @FXML
    private void onDelete() {
        com.starlight.models.UserDataRepository repo = new com.starlight.models.UserDataRepository();
        java.util.List<com.starlight.models.User> users = repo.loadUsers();
        users.removeIf(u -> u.username.equals(currentUser.username));
        repo.saveUsers(users);
        if (onLogout != null) onLogout.run();
        Stage stage = (Stage) deleteaccbutton.getScene().getWindow();
        stage.close();
    }

}
