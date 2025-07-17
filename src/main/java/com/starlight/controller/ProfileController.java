package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.starlight.models.Post;
import com.starlight.models.PostDataRepository;
import com.starlight.models.User;
import com.starlight.util.Session;

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
    
    @SuppressWarnings("unused")
    private MainController main;
    
    private final PostDataRepository repository = new PostDataRepository();
    
    // Use CommunityController for shared utility methods to reduce code redundancy
    private final CommunityController communityController = new CommunityController();

    public void setMainController(MainController main) {
        this.main = main;
        // Also set the main controller for the community controller if needed
        communityController.setMainController(main);
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
                String displayName = communityController.getDisplayUsernameForUser(currentUser.username);
                username.setText(displayName != null ? displayName : currentUser.username);
            }
            
            // Load profile picture using CommunityController's image loading methods
            if (profile != null) {
                communityController.loadImage(profile, currentUser.profilepicture, "/com/starlight/images/missing.png");
                communityController.scaleToFit(profile, 170, 170, 200); // Adjust size as needed
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
        
        List<Post> allPosts = repository.loadPosts();
        if (allPosts == null || allPosts.isEmpty()) {
            // Clear the container if no posts
            myrepiceList.setContent(new HBox());
            if (recipes != null) {
                recipes.setText("0");
            }
            return;
        }
        
        // Filter posts by current user
        List<Post> userPosts = allPosts.stream()
            .filter(post -> currentUser.username.equals(post.username))
            .toList();
            
        if (userPosts.isEmpty()) {
            // Clear the container if no posts
            myrepiceList.setContent(new HBox());
            if (recipes != null) {
                recipes.setText("0");
            }
            return;
        }
        
        // Create horizontal layout for recipes
        HBox recipeContainer = new HBox();
        recipeContainer.setSpacing(20);
        
        for (Post post : userPosts) {
            String title = post.title;
            String image = post.image;
            String likes = post.likecount;
            String rating = post.rating;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/recipeItem.fxml"));
                GridPane recipeNode = loader.load();
                RecipeItemController controller = loader.getController();

                // Set the recipe data using the new setter method
                controller.setRecipeData(title, rating != null ? rating : "0.0", likes != null ? likes : "0");

                // Load recipe image using CommunityController's image loading methods
                ImageView recipeImageView = controller.getImageView();
                if (recipeImageView != null) {
                    communityController.loadImage(recipeImageView, image, "/com/starlight/images/missing.png");
                    communityController.scaleToFit(recipeImageView, 280, 200, 20); // Adjust size for recipe item
                }

                recipeContainer.getChildren().add(recipeNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        myrepiceList.setContent(recipeContainer);
        
        // Update recipe count
        if (recipes != null) {
            recipes.setText(String.valueOf(userPosts.size()));
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
