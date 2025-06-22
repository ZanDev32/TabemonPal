package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import io.github.palexdev.materialfx.controls.MFXButton;

import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class MainController implements Initializable {
    
    private MFXButton currentActiveButton;
    
    @FXML
    private BorderPane bp;
    
    @FXML
    private GridPane gp;

    @FXML 
    private Pane activeBar;
   
    @FXML 
    private MFXButton home; 
    
    @FXML 
    private MFXButton community;
    
    @FXML 
    private MFXButton wiki;
    
    @FXML 
    private MFXButton consult;
    
    @FXML 
    private MFXButton mission;
    
    @FXML 
    private MFXButton games;
    @FXML 
    private MFXButton achievement;
    
    @FXML
    void home(MouseEvent event) {
        loadPage("home");
        selected(home);
    }

    @FXML
    void achievement(MouseEvent event) {
        loadPage("achievement");
        selected(achievement);
    }

    @FXML
    void community(MouseEvent event) {
        loadPage("community");
        selected(community);
    }

    @FXML
    void consult(MouseEvent event) {
        loadPage("consult");
        selected(consult);
    }

    @FXML
    void games(MouseEvent event) {
        loadPage("games");
        selected(games);
    }

    @FXML
    void mission(MouseEvent event) {
        loadPage("mission");
        selected(mission);
    }

    @FXML
    void wiki(MouseEvent event) {
        loadPage("wiki");
        selected(wiki);
    }

    private void loadPage (String page) {
        Parent root = null;

        page = "/com/starlight/view/" + page;

        try {
            root = FXMLLoader.load(getClass().getResource(page + ".fxml"));
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Failed to load FXML page: " + page, ex);
        }

        if (root != null) {
            // Optional: remove any existing node in the target cell
            gp.getChildren().removeIf(node ->
                GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null &&
                GridPane.getColumnIndex(node) == 0 && GridPane.getRowIndex(node) == 1
            );
            // Add the new node to the specified cell
            gp.add(root, 0, 1);
        }
    }

    private void selected(MFXButton button) {

        Integer row = GridPane.getRowIndex(button);
        if (row == null) row = 0;
        GridPane.setRowIndex(activeBar, row);

        // Change button background
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("button-active");
            if (!currentActiveButton.getStyleClass().contains("button")) {
                currentActiveButton.getStyleClass().add("button");
            }
        }
        button.getStyleClass().add("button-active");

        // Track the current active button
        currentActiveButton = button;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPage("home");
        selected(home);
    }
}
