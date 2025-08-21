package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import com.starlight.model.Post;
import com.starlight.repository.PostDataRepository;

import java.util.List;

/**
 * Simple controller used for dynamically loaded post items.
 */
public class PostItemController {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PostItemController.class.getName());
    private static final String HAND_CURSOR_STYLE = "-fx-cursor: hand;";

    @FXML
    private VBox postTemplate;

    @FXML
    private VBox post1;

    @FXML
    public ImageView likebutton;

    @FXML
    public MFXButton commentcounter;

    @FXML
    private ImageView likebutton1;

    @FXML
    private MFXButton sharebutton;

    @FXML
    private ImageView likebutton11;

    @FXML 
    public Label username;
    @FXML 
    public Label uploadtime;
    @FXML 
    public Label title;
    @FXML 
    public Label description;
    @FXML 
    public ImageView recentphoto1;
    @FXML 
    public ImageView profile1;
    @FXML 
    public MFXButton likecounter;

    private Post currentPost;
    private PostDataRepository repository = new PostDataRepository();
    private MainController mainController; // Reference to main controller for navigation

    @FXML
    private void initialize() {
        // FXML initialize method - minimal setup only
        // Click handling will be set up when main controller is set
    }

    /**
     * FXML binding method for like button click
     */
    @FXML
    public void handleLikeClick(javafx.event.ActionEvent event) {
        if (currentPost == null) return;
        
        try {
            int currentLikes = Integer.parseInt(currentPost.getLikecount() != null ? currentPost.getLikecount() : "0");
            boolean wasLiked = "true".equals(currentPost.getIsLiked());
            
            if (wasLiked) {
                // Unlike the post
                currentLikes = Math.max(0, currentLikes - 1);
                currentPost.setLikecount(String.valueOf(currentLikes));
                currentPost.setIsLiked("false");
                
                // Change image back to normal like icon
                if (likebutton != null) {
                    likebutton.setImage(new Image(getClass().getResourceAsStream("/com/starlight/icon/like.png")));
                }
            } else {
                // Like the post
                currentLikes++;
                currentPost.setLikecount(String.valueOf(currentLikes));
                currentPost.setIsLiked("true");
                
                // Change image to liked icon
                if (likebutton != null) {
                    likebutton.setImage(new Image(getClass().getResourceAsStream("/com/starlight/icon/like_1.png")));
                }
            }
            
            // Update the button text
            likecounter.setText(String.valueOf(currentLikes));
            
            // Save the updated post data
            savePostData();
            
        } catch (NumberFormatException e) {
            // Handle invalid like count
            logger.warning("Invalid like count for post: " + (currentPost != null ? currentPost.getUuid() : "unknown"));
            e.printStackTrace();
            currentPost.setLikecount("0");
            currentPost.setIsLiked("false");
            likecounter.setText("0");
        }
    }

    /**
     * FXML binding method for comment button click
     */
    @FXML
    public void handleCommentClick(javafx.event.ActionEvent event) {
        if (currentPost == null) return;
        
        try {
            int currentComments = Integer.parseInt(currentPost.getCommentcount() != null ? currentPost.getCommentcount() : "0");
            currentComments++;
            currentPost.setCommentcount(String.valueOf(currentComments));
            
            // Update the button text
            commentcounter.setText(String.valueOf(currentComments));
            
            // Save the updated post data
            savePostData();
            
        } catch (NumberFormatException e) {
            // Handle invalid comment count
            currentPost.setCommentcount("0");
            commentcounter.setText("0");
        }
    }

    /**
     * Sets the current post data for this controller instance
     */
    public void setPost(Post post) {
        this.currentPost = post;
        updateUIFromPost();
    }

    /**
     * Updates the UI elements with the current post data
     */
    private void updateUIFromPost() {
        if (currentPost == null) return;
        
        // Update like counter
        if (likecounter != null) {
            likecounter.setText(currentPost.getLikecount() != null ? currentPost.getLikecount() : "0");
        }
        
        // Update comment counter
        if (commentcounter != null) {
            commentcounter.setText(currentPost.getCommentcount() != null ? currentPost.getCommentcount() : "0");
        }
        
        // Update like button image based on liked state
        if (likebutton != null) {
            boolean isLiked = "true".equals(currentPost.getIsLiked());
            if (isLiked) {
                likebutton.setImage(new Image(getClass().getResourceAsStream("/com/starlight/icon/like_1.png")));
            } else {
                likebutton.setImage(new Image(getClass().getResourceAsStream("/com/starlight/icon/like.png")));
            }
        }
    }

    /**
     * Saves the updated post data to the repository
     */
    private void savePostData() {
        if (currentPost == null) return;
        
        try {
            List<Post> posts = repository.loadPosts();
            
            // Find and update the current post in the list
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                if (post.getUuid() != null && post.getUuid().equals(currentPost.getUuid())) {
                    posts.set(i, currentPost);
                    break;
                }
            }
            
            // Save the updated posts
            repository.savePosts(posts);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the main controller reference for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        setupPostClickHandling();
    }
    
    /**
     * Sets up click handling for the post area (excluding buttons)
     */
    private void setupPostClickHandling() {
        // Make the post clickable (but not the buttons)
        if (postTemplate != null) {
            postTemplate.setOnMouseClicked(event -> {
                // Check if click target is a button or interactive element
                javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
                
                // Prevent clicks on buttons from triggering navigation
                if (isClickableButton(target)) {
                    return;
                }
                
                handlePostClick();
            });
            
            // Set cursor to indicate clickable area
            postTemplate.setStyle(HAND_CURSOR_STYLE);
        }
        
        // Also make the image and text content explicitly clickable
        if (recentphoto1 != null) {
            recentphoto1.setOnMouseClicked(event -> handlePostClick());
            recentphoto1.setStyle(HAND_CURSOR_STYLE);
        }
        
        if (title != null) {
            title.setOnMouseClicked(event -> handlePostClick());
            title.setStyle(HAND_CURSOR_STYLE);
        }
        
        if (description != null) {
            description.setOnMouseClicked(event -> handlePostClick());
            description.setStyle(HAND_CURSOR_STYLE);
        }
    }
    
    /**
     * Checks if the clicked element is a button or interactive element
     */
    private boolean isClickableButton(javafx.scene.Node target) {
        // Check if the target itself or any of its parents is a button
        javafx.scene.Node current = target;
        while (current != null) {
            if (current == likecounter || current == commentcounter || current == sharebutton ||
                current == likebutton || current == likebutton1 || current == likebutton11) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
    
    /**
     * Handles clicks on the post content to navigate to full post view
     */
    private void handlePostClick() {
        if (currentPost != null && mainController != null) {
            mainController.setCurrentPost(currentPost);
            mainController.loadPage("Post");
        } else {
            if (currentPost == null) {
                logger.warning("Cannot navigate: currentPost is null");
            }
            if (mainController == null) {
                logger.warning("Cannot navigate: mainController is null");
            }
        }
    }
}
