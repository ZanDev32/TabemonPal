package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class MainController implements Initializable {

    @FXML
    private BorderPane bp;
    
    @FXML
    private AnchorPane ap;
    
    @FXML
    void home(MouseEvent event) {
        loadPage("home");
    }

    @FXML
    void achivement(MouseEvent event) {
        loadPage("achivement");
    }

    @FXML
    void community(MouseEvent event) {
        loadPage("community");
    }

    @FXML
    void consult(MouseEvent event) {
        loadPage("consult");
    }

    @FXML
    void games(MouseEvent event) {
        loadPage("games");
    }

    @FXML
    void mission(MouseEvent event) {
        loadPage("mission");
    }

    @FXML
    void wiki(MouseEvent event) {
        loadPage("wiki");
    }

    private void loadPage (String page) {
        Parent root = null;

        page = "/com/starlight/view/" + page;

        try {
            root = FXMLLoader.load(getClass().getResource(page + ".fxml"));
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Failed to load FXML page: " + page, ex);
        }

        bp.setCenter(root);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPage("home");
    }
}
