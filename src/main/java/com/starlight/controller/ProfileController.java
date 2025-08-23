package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.starlight.model.Post;
import com.starlight.model.User;
import com.starlight.repository.PostDataRepository;
import com.starlight.util.Session;
import com.starlight.util.ImageUtils;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller backing the profile page. It displays user profile information and user's recipes/posts.
 * 
 * Architecture: This controller uses dependency injection pattern by utilizing CommunityController's
 * public utility methods to reduce code redundancy and maintain consistency across the application.
 * Instead of duplicating image loading, scaling, and user data retrieval logic, we delegate these
 * operations to the CommunityController which serves as a shared utility provider.
 */
public class ProfileController implements Initializable {
    private static final Logger logger = Logger.getLogger(ProfileController.class.getName());
    
    @FXML
    private MFXButton editBio;

    @FXML
    private MFXButton addRecipe;

    @FXML
    private VBox badge;

    @FXML
    private ImageView badgeICon;

    @FXML
    private Label badgeTitle;

    @FXML
    private MFXButton swapToCommunity;

    @FXML
    private MFXScrollPane myrepiceList;
    
    @FXML
    private ImageView profile;
    
    @FXML
    private Label username;
    
    @FXML
    private Label bio;
    
    @FXML
    private Label recipes;
    
    @FXML
    private Label followers;
    
    @FXML
    private Label following;
    
    private MainController main;
    
    private final PostDataRepository repository = new PostDataRepository();
    
    // Use CommunityController for getDisplayUsernameForUser method and ImageUtils for image operations
    private final CommunityController communityController = new CommunityController();

    public void setMainController(MainController main) {
        this.main = main;
        // Also set the main controller for the community controller if needed
        // No need to set main controller for utility class
        
        // Update any existing RecipeItemControllers with the MainController reference
        updateRecipeItemControllersMainController();
    }
    
    /**
     * Updates all existing RecipeItemControllers with MainController reference
     * This handles the timing issue where recipes are loaded before MainController is set
     */
    private void updateRecipeItemControllersMainController() {
        if (main == null || myrepiceList == null) {
            return;
        }
        
        // Since we can't easily access individual controllers after they're created,
        // the best approach is to reload recipes if they were loaded before MainController was set
        javafx.scene.Node content = myrepiceList.getContent();
        if (content instanceof javafx.scene.Parent) {
            javafx.scene.Parent parentContent = (javafx.scene.Parent) content;
            if (parentContent.getChildrenUnmodifiable().size() > 0) {
                logger.info("Reloading recipes with MainController reference");
                loadUserRecipes();
            }
        }
    }
    
    @FXML
    void swapToCommunity(MouseEvent event) {
        main.loadPage("community");
    }

    /**
     * Loads the current user's profile information using CommunityController's methods
     */
    private void loadUserProfile() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            // Set username using display name logic from CommunityController
            if (username != null) {
                String displayName = communityController.getDisplayUsernameForUser(currentUser.getUsername());
                username.setText(displayName != null ? displayName : currentUser.getUsername());
            }
            
            // Load profile picture using ImageUtils
            if (profile != null) {
                ImageUtils.loadImage(profile, currentUser.getProfilepicture(), ImageUtils.DEFAULT_MISSING_IMAGE);
                ImageUtils.scaleToFit(profile, 170, 170, 200); // Adjust size as needed
            }
            
            // Set followers and following counts (placeholder values since not in User model)
            if (followers != null) {
                followers.setText("125"); // Placeholder - could be calculated from user relationships
            }
            
            if (following != null) {
                following.setText("89"); // Placeholder - could be calculated from user relationships
            }
            
            // Set bio if available (though not in current User model, prepare for future)
            if (bio != null) {
                bio.setText("Food enthusiast and recipe creator"); // Placeholder bio
            }
        }
    }
    
    /**
     * Loads and displays the current user's recipes/posts using CommunityController's approach
     */
    private void loadUserRecipes() {
        if (myrepiceList == null) return;
        
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;
        
        List<Post> userPosts = getUserPosts(currentUser);
        if (userPosts.isEmpty()) {
            clearRecipeContainer();
            return;
        }
        
        HBox recipeContainer = createRecipeContainer(userPosts);
        myrepiceList.setContent(recipeContainer);
        updateRecipeCount(userPosts.size());
    }

    /**
     * Retrieves posts for the current user
     */
    private List<Post> getUserPosts(User currentUser) {
        List<Post> allPosts = repository.loadPosts();
        if (allPosts == null || allPosts.isEmpty()) {
            return List.of();
        }
        
        return allPosts.stream()
            .filter(post -> currentUser.getUsername().equals(post.getUsername()))
            .toList();
    }

    /**
     * Clears the recipe container and resets count
     */
    private void clearRecipeContainer() {
        myrepiceList.setContent(new HBox());
        updateRecipeCount(0);
    }

    /**
     * Creates the recipe container with all user posts
     */
    private HBox createRecipeContainer(List<Post> userPosts) {
        HBox recipeContainer = new HBox();
        recipeContainer.setSpacing(20);
        
        for (Post post : userPosts) {
            GridPane recipeNode = createRecipeNode(post);
            if (recipeNode != null) {
                recipeContainer.getChildren().add(recipeNode);
            }
        }
        
        return recipeContainer;
    }

    /**
     * Creates a single recipe node for a post
     */
    private GridPane createRecipeNode(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/recipeItem.fxml"));
            GridPane recipeNode = loader.load();
            RecipeItemController controller = loader.getController();

            configureRecipeController(controller, post);
            loadRecipeImage(controller, post.getImage());

            return recipeNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Configures the recipe item controller with post data
     */
    private void configureRecipeController(RecipeItemController controller, Post post) {
        String title = post.getTitle();
        String likes = post.getLikecount();
        String rating = post.getRating();

        controller.setRecipeData(title, rating != null ? rating : "0.0", likes != null ? likes : "0");
        controller.setPost(post);
        controller.setOnPostUpdated(() -> loadUserRecipes());

        if (main != null) {
            controller.setMainController(main);
        } else {
            logger.warning("MainController not available for RecipeItemController - navigation will not work");
        }
    }

    /**
     * Loads the recipe image for the controller
     */
    private void loadRecipeImage(RecipeItemController controller, String imagePath) {
        ImageView recipeImageView = controller.getImageView();
        if (recipeImageView != null) {
            ImageUtils.loadImage(recipeImageView, imagePath, ImageUtils.DEFAULT_MISSING_IMAGE);
            ImageUtils.scaleToFit(recipeImageView, 280, 174, 20);
        }
    }

    /**
     * Updates the recipe count display
     */
    private void updateRecipeCount(int count) {
        if (recipes != null) {
            recipes.setText(String.valueOf(count));
        }
    }

    /**
     * Initializes the controller by ensuring dummy data and loading profile and recipes.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        repository.ensureDummyData();
        loadUserProfile();
        loadUserRecipes();
        
        // Connect addRecipe button to handleCreatePost method
        if (addRecipe != null) {
            addRecipe.setOnAction(e -> handleCreatePost());
        }
    }
    
    @FXML
    private void handleCreatePost() {
        showCreatePostPopup();
    }

    /**
     * Displays a modal dialog that allows the user to create a new post.
     */
    private void showCreatePostPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/createPost.fxml"));
            Parent popupRoot = loader.load();
            CreatePostController controller = loader.getController();

            popupRoot.setScaleX(0.7);
            popupRoot.setScaleY(0.7);

            Scene popupScene = new Scene(popupRoot);
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(popupScene);
            popupStage.setTitle("Create Post");
            popupStage.setResizable(false);

            popupStage.setOnShown(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(220), popupRoot);
                st.setFromX(0.7);
                st.setFromY(0.7);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            popupStage.showAndWait();
            if (controller.isSuccess()) {
                loadUserRecipes(); // Reload user recipes after creating new post
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
