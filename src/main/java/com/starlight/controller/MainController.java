package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;

import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.concurrent.Task;

/**
 * Controller for the main application shell which loads sub-pages and keeps
 * track of the active navigation button.
 */
public class MainController implements Initializable {
    
    private NavbarController navbarController;
    
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
    
    /** Handles navigation to the home view. */
    @FXML
    void home(MouseEvent event) {
        loadPage("home");
        selected(home);
    }

    /** Handles navigation to the achievement view. */
    @FXML
    void achievement(MouseEvent event) {
        loadPage("achievement");
        selected(achievement);
    }

    /** Handles navigation to the community view. */
    @FXML
    void community(MouseEvent event) {
        loadPage("community");
        selected(community);
    }

    /** Handles navigation to the consult view. */
    @FXML
    void consult(MouseEvent event) {
        loadPage("consult");
        selected(consult);
    }

    /** Handles navigation to the games view. */
    @FXML
    void games(MouseEvent event) {
        loadPage("games");
        selected(games);
    }

    /** Handles navigation to the mission view. */
    @FXML
    void mission(MouseEvent event) {
        loadPage("mission");
        selected(mission);
    }

    /** Handles navigation to the wiki view. */
    @FXML
    void wiki(MouseEvent event) {
        loadPage("wiki");
        selected(wiki);
    }

    /**
     * Loads the given FXML page into the grid pane container.
    */
    void loadPage(String page) {
        String fxmlPath = "/com/starlight/view/" + page + ".fxml";

        try {
            // Show the loading screen inside the main grid pane while loading
            Parent loadingRoot = FXMLLoader.load(getClass().getResource("/com/starlight/view/loading.fxml"));
            gp.getChildren().removeIf(node ->
                GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null &&
                GridPane.getColumnIndex(node) == 0 && GridPane.getRowIndex(node) == 1
            );
            gp.add(loadingRoot, 0, 1);
            GridPane.setValignment(loadingRoot, VPos.CENTER);
            GridPane.setHalignment(loadingRoot, HPos.CENTER);

            Task<Parent> task = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                
                // Get the controller and pass user data if applicable
                Object controller = loader.getController();
                if (controller instanceof UserSettingController) {
                    ((UserSettingController) controller).setUser(
                        Session.getCurrentUser()
                    );
                }
                
                return root;
            }
        };

            task.setOnSucceeded(ev -> {
                Parent root = task.getValue();
                gp.getChildren().remove(loadingRoot);
                gp.getChildren().removeIf(node ->
                    GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) != null &&
                    GridPane.getColumnIndex(node) == 0 && GridPane.getRowIndex(node) == 1
                );
                gp.add(root, 0, 1);
                GridPane.setValignment(root, VPos.CENTER);
                GridPane.setHalignment(root, HPos.CENTER);
            });

            task.setOnFailed(ev -> {
                gp.getChildren().remove(loadingRoot);
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE,
                        "Failed to load FXML page: " + fxmlPath, task.getException());
            });

            new Thread(task).start();
        } catch (IOException e) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE,
                    "Failed to load loading.fxml", e);
        }
    }

    /**
     * Updates the navigation bar to mark the given button as active.
     */
    void selected(MFXButton button) {

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

    /** Initializes the controller by showing the home view. */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load navbar and set up controller communication
        try {
            FXMLLoader navbarLoader = new FXMLLoader(getClass().getResource("/com/starlight/view/navbar.fxml"));
            HBox navbarNode = navbarLoader.load();
            navbarController = navbarLoader.getController();
            navbarController.setMainController(this);
            
            // Add navbar to the GridPane at row 0
            gp.add(navbarNode, 0, 0);
            navbarNode.setMaxWidth(Double.MAX_VALUE);

        } catch (IOException e) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE,
                    "Failed to load navbar.fxml", e);
        }
        
        loadPage("home");
        selected(home);
    }
}
