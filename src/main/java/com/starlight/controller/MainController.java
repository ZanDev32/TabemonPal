package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.starlight.model.Post;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;

import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML
    private ImageView recipeManagerIcon;

    @FXML
    private ImageView userManagerIcon;

    @FXML
    private MFXButton recipeManager;

    @FXML
    private Label adminMenu;

    @FXML
    private MFXButton userManager;

    @FXML
    private ImageView homeIcon;

    @FXML
    private ImageView communityIcon;

    @FXML
    private ImageView wikiIcon;

    @FXML
    private ImageView consultIcon;

    @FXML
    private ImageView missionIcon;

    @FXML
    private ImageView gameIcon;

    @FXML
    private ImageView achievemntIcon;

    @FXML
    private ImageView appLogo;

    
    private NavbarController navbarController;
    
    private MFXButton currentActiveButton;
    
    private Post currentPost; // Current post for navigation to post view
    
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
        loadPage("underDevelopment");
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
        loadPage("underDevelopment");
        selected(games);
    }

    /** Handles navigation to the mission view. */
    @FXML
    void mission(MouseEvent event) {
        loadPage("underDevelopment");
        selected(mission);
    }

    /** Handles navigation to the wiki view. */
    @FXML
    void wiki(MouseEvent event) {
        loadPage("underDevelopment");
        selected(wiki);
    }

    /** Handles navigation to the recipe manager view (only for admin users). */
    @FXML
    void recipeManager(MouseEvent event) {
        // This should only be called if button is visible (admin user logged in)
        loadPage("recipeManager");
        selected(recipeManager);
    }

    /** Handles navigation to the user manager view (only for admin users). */
    @FXML
    void userManager(MouseEvent event) {
        // This should only be called if button is visible (admin user logged in)
        loadPage("userManager");
        selected(userManager);
    }
    
    /**
     * Checks if the current user has admin privileges
     */
    private boolean isCurrentUserAdmin() {
        if (Session.getCurrentUser() == null) {
            return false;
        }
        
        String username = Session.getCurrentUser().username;
        return username != null && username.toLowerCase().equals("admin");
    }
    
    /**
     * Updates the visibility of navigation elements based on user permissions
     */
    public void updateNavigationForUser() {
        boolean isAdmin = isCurrentUserAdmin();
        adminMenu.setVisible(isAdmin);
        adminMenu.setManaged(isAdmin); // This makes the label not take up space when hidden
        recipeManager.setVisible(isAdmin);
        recipeManager.setManaged(isAdmin); // This makes the button not take up space when hidden
        userManager.setVisible(isAdmin);
        userManager.setManaged(isAdmin); // This makes the button not take up space when hidden

        // If recipe manager button was active and user is no longer admin, switch to home
        if (!isAdmin && currentActiveButton == recipeManager) {
            home(null);
        }
        // If user manager button was active and user is no longer admin, switch to home
        if (!isAdmin && currentActiveButton == userManager) {
            home(null);
        }
    }
    
    /**
     * Public method to refresh navigation state - can be called by other controllers
     * when user login state changes without a full application reload
     */
    public void refreshNavigationState() {
        updateNavigationForUser();
        
        // Also refresh the navbar if needed
        if (navbarController != null) {
            // The navbar controller will handle its own user state updates
        }
    }

    /**
     * Refreshes the community page to show new posts.
     */
    public void refreshCommunityPage() {
        if (bp.getCenter() != null) {
            Object controller = bp.getCenter().getProperties().get("controller");
            if (controller instanceof CommunityController) {
                ((CommunityController) controller).refreshPosts();
            }
        }
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
                if (controller instanceof EditProfileController) {
                    ((EditProfileController) controller).setUser(
                        Session.getCurrentUser()
                    );
                }
                if (controller instanceof CommunityController) {
                    ((CommunityController) controller).setMainController(MainController.this);
                }
                if (controller instanceof ProfileController) {
                    ((ProfileController) controller).setMainController(MainController.this);
                }
                if (controller instanceof PostController) {
                    PostController postController = (PostController) controller;
                    postController.setPost(currentPost); 
                    postController.setMainController(MainController.this);
                }
                if (controller instanceof SettingController) {
                    ((SettingController) controller).setMainController(MainController.this);
                }
                
                return root;
            }
        };

            task.setOnSucceeded(ev -> {
                Parent root = task.getValue();
                root.getProperties().put("controller", task.getValue().getProperties().get("controller"));
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
        
        activeBar.setTranslateX(2);
        activeBar.getStyleClass().clear();
        activeBar.getStyleClass().add("black");
        

        // Reset previous active button to inactive state
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("button-active");
            currentActiveButton.getStyleClass().add("button-sidebar");
            setButtonIcon(currentActiveButton, false); // Set to inactive icon
        }

        // Set new active button state
        button.getStyleClass().remove("button-sidebar");
        button.getStyleClass().add("button-active");
        setButtonIcon(button, true); // Set to active icon

        // Track the current active button
        currentActiveButton = button;
    }

    /**
     * Sets the icon for a button based on its active state.
     * @param button The button to update
     * @param isActive Whether the button is active or not
     */
    private void setButtonIcon(MFXButton button, boolean isActive) {
        String buttonId = getButtonIdentifier(button);
        if (buttonId == null) return;

        ImageView iconView;
        String iconBaseName;

        switch (buttonId) {
            case "home":
                iconView = homeIcon;
                iconBaseName = "Home";
                break;
            case "community":
                iconView = communityIcon;
                iconBaseName = "Community";
                break;
            case "wiki":
                iconView = wikiIcon;
                iconBaseName = "Wiki";
                break;
            case "consult":
                iconView = consultIcon;
                iconBaseName = "Consult";
                break;
            case "mission":
                iconView = missionIcon;
                iconBaseName = "Mission";
                break;
            case "games":
                iconView = gameIcon;
                iconBaseName = "Game";
                break;
            case "achievement":
                iconView = achievemntIcon;
                iconBaseName = "Achievements";
                break;
            case "recipeManager":
                iconView = recipeManagerIcon;
                iconBaseName = "RecipeManager";
                break;
            case "userManager":
                iconView = userManagerIcon;
                iconBaseName = "UserManager";
                break;
            default:
                return; // Unknown button, do nothing
        }

        // Update the icon
        try {
            String iconName = isActive ? iconBaseName + ".png" : iconBaseName + "_1.png";
            String iconPath = "/com/starlight/icon/" + iconName;
            Image newImage = new Image(getClass().getResourceAsStream(iconPath));
            iconView.setImage(newImage);
        } catch (Exception e) {
            Logger.getLogger(MainController.class.getName()).log(Level.WARNING,
                    "Failed to load icon for button: " + buttonId, e);
        }
    }

    /**
     * Helper method to get a string identifier for a button
     * @param button The button to identify
     * @return String identifier for the button, or null if unknown
     */
    private String getButtonIdentifier(MFXButton button) {
        if (button == home) return "home";
        if (button == community) return "community";
        if (button == wiki) return "wiki";
        if (button == consult) return "consult";
        if (button == mission) return "mission";
        if (button == games) return "games";
        if (button == achievement) return "achievement";
        if (button == recipeManager) return "recipeManager";
        if (button == userManager) return "userManager";
        return null;
    }

    /** Initializes the controller by showing the home view. */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize all buttons with inactive state and icons
        initializeButtons();
        
        
        // Load the bot icon for the app logo
        try {
            Image botIcon = new Image(getClass().getResourceAsStream("/com/starlight/images/botIcon.png"));
            appLogo.setImage(botIcon);
        } catch (Exception e) {
            Logger.getLogger(MainController.class.getName()).log(Level.WARNING,
                    "Failed to load botIcon.png", e);
        }
        
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
        selected(home); // This will set home as active and update its icon accordingly
        
        // Update navigation visibility based on current user
        updateNavigationForUser();
    }

    /**
     * Initializes all navigation buttons with their inactive state and icons.
     */
    private void initializeButtons() {
        // Set all buttons to inactive style class
        MFXButton[] buttons = {home, community, wiki, consult, mission, games, achievement, recipeManager, userManager};
        for (MFXButton button : buttons) {
            button.getStyleClass().remove("button-active");
            button.getStyleClass().add("button-sidebar");
        }

        // Set all icons to inactive state
        setButtonIcon(home, false);
        setButtonIcon(community, false);
        setButtonIcon(wiki, false);
        setButtonIcon(consult, false);
        setButtonIcon(mission, false);
        setButtonIcon(games, false);
        setButtonIcon(achievement, false);
        setButtonIcon(recipeManager, false);
        setButtonIcon(userManager, false);
        
        // Hide admin button initially - will be shown if admin user logs in
        adminMenu.setVisible(false);
        adminMenu.setManaged(false);
        recipeManager.setVisible(false);
        recipeManager.setManaged(false);
        userManager.setVisible(false);
        userManager.setManaged(false); 
    }
    
    /**
     * Public method to navigate to community page with proper button selection
     * Used by other controllers like PostController for navigation
     */
    public void navigateToCommunity() {
        loadPage("community");
        selected(community);
    }
    
    /**
     * Sets the current post for navigation to post view
     */
    public void setCurrentPost(Post post) {
        this.currentPost = post;
    }
}
