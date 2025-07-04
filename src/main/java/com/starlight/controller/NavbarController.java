package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.starlight.models.User;

import java.io.IOException;

public class NavbarController {
    @FXML
    private HBox navbar;

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

    private User currentUser; // Add a field to hold the current user

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        profile.setOnAction(event -> openUserSetting());
    }

    private void openUserSetting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/userSetting.fxml"));
            Parent root = loader.load();
            UserSettingController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            controller.setOnLogout(() -> {
                // Call App.showLogin() to logout
                com.starlight.App.showLogin();
            });
            // Show as a modal dialog, but you can also swap the center if needed
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Account Settings");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
