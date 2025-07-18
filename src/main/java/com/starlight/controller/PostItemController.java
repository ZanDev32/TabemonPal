package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import com.starlight.models.Post;
import com.starlight.models.PostDataRepository;
import java.util.List;

/**
 * Simple controller used for dynamically loaded post items.
 */
public class PostItemController {
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

    @FXML
    private void initialize() {
        // FXML initialize method - minimal setup only
    }

    /**
     * FXML binding method for like button click
     */
    @FXML
    private void handleLikeClick() {
        if (currentPost == null) return;
        
        try {
            int currentLikes = Integer.parseInt(currentPost.likecount != null ? currentPost.likecount : "0");
            boolean wasLiked = "true".equals(currentPost.isLiked);
            
            if (wasLiked) {
                // Unlike the post
                currentLikes = Math.max(0, currentLikes - 1);
                currentPost.likecount = String.valueOf(currentLikes);
                currentPost.isLiked = "false";
                
                // Change image back to normal like icon
                if (likebutton != null) {
                    likebutton.setImage(new Image(getClass().getResourceAsStream("/com/starlight/icon/like.png")));
                }
            } else {
                // Like the post
                currentLikes++;
                currentPost.likecount = String.valueOf(currentLikes);
                currentPost.isLiked = "true";
                
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
            System.err.println("Invalid like count for post: " + (currentPost != null ? currentPost.uuid : "unknown"));
            e.printStackTrace();
            currentPost.likecount = "0";
            currentPost.isLiked = "false";
            likecounter.setText("0");
        }
    }

    /**
     * FXML binding method for comment button click
     */
    @FXML
    private void handleCommentClick() {
        if (currentPost == null) return;
        
        try {
            int currentComments = Integer.parseInt(currentPost.commentcount != null ? currentPost.commentcount : "0");
            currentComments++;
            currentPost.commentcount = String.valueOf(currentComments);
            
            // Update the button text
            commentcounter.setText(String.valueOf(currentComments));
            
            // Save the updated post data
            savePostData();
            
        } catch (NumberFormatException e) {
            // Handle invalid comment count
            currentPost.commentcount = "0";
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
            likecounter.setText(currentPost.likecount != null ? currentPost.likecount : "0");
        }
        
        // Update comment counter
        if (commentcounter != null) {
            commentcounter.setText(currentPost.commentcount != null ? currentPost.commentcount : "0");
        }
        
        // Update like button image based on liked state
        if (likebutton != null) {
            boolean isLiked = "true".equals(currentPost.isLiked);
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
                if (post.uuid != null && post.uuid.equals(currentPost.uuid)) {
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
}
