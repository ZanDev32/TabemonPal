package com.starlight.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class ProcessingDialogController implements Initializable {
    @FXML
    private Label status;
    
    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set initial processing message
        if (status != null) {
            status.setText("Creating your post...");
        }
    }
    
    /**
     * Sets the dialog stage so it can be closed programmatically
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Updates the status text displayed to the user
     */
    public void updateStatus(String newStatus) {
        if (status != null) {
            status.setText(newStatus);
        }
    }
    
    /**
     * Closes the processing dialog
     */
    public void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
