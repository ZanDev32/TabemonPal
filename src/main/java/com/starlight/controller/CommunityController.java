package com.starlight.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import com.starlight.model.Post;
import com.starlight.model.User;
import com.starlight.repository.PostDataRepository;
import com.starlight.repository.UserDataRepository;
import com.starlight.util.ImageUtils;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controller backing the community page. It displays recent posts and allows
 * the user to create new posts.
 */
public class CommunityController implements Initializable {
    @FXML
    private MFXButton toProfile;

    @FXML
    private VBox dailypost1;

    @FXML
    private VBox dailypost2;

    @FXML
    private VBox dailypost3;

    @FXML
    private VBox dailypost4;

    @FXML
    private GridPane manu;

    @FXML
    private ImageView dailyphoto1;

    @FXML
    private Label dailytitle1;

    @FXML
    private Label starrating;

    @FXML
    private Label dailylikecounter;

    @FXML
    private ImageView dailyphoto2;

    @FXML
    private Label dailytitle2;

    @FXML
    private Label starrating2;

    @FXML
    private Label dailylikecounter2;

    @FXML
    private ImageView dailyphoto3;

    @FXML
    private Label dailytitle3;

    @FXML
    private Label starrating3;

    @FXML
    private Label dailylikecounter3;

    @FXML
    private ImageView dailyphoto4;

    @FXML
    private Label dailytitle4;

    @FXML
    private Label starrating4;

    @FXML
    private Label dailylikecounter4;

    @FXML
    private VBox postlist;

    private MainController main;

    private final PostDataRepository repository = new PostDataRepository();
    private final UserDataRepository userRepository = new UserDataRepository();

    public void setMainController(MainController main) {
        this.main = main;
        
        // If posts haven't been loaded yet because main was null, load them now
        if (postlist != null && postlist.getChildren().isEmpty()) {
            loadPosts();
            loadDailyPosts();
        }
    }

    @FXML
    public void toProfile(MouseEvent event) {
        main.loadPage("profile");
    }
    
    /**
     * Gets the profile picture path for a given username from UserData.xml
     */
    public String getProfilePictureForUser(String username) {
        if (username == null) return null;
        
        List<User> users = userRepository.loadUsers();
        for (User user : users) {
            if (username.equals(user.getUsername())) {
                return user.getProfilepicture();
            }
        }
        return null;
    }

    /**
     * Gets the display username from UserData.xml for a given username
     */
    public String getDisplayUsernameForUser(String username) {
        if (username == null) return null;
        
        List<User> users = userRepository.loadUsers();
        for (User user : users) {
            if (username.equals(user.getUsername())) {
                // Return fullname if available, otherwise return username
                return user.getFullname() != null && !user.getFullname().trim().isEmpty() 
                    ? user.getFullname() : user.getUsername();
            }
        }
        return username; // fallback to original username if not found
    }

    /**
     * Loads posts from the repository and populates the UI. All posts are
     * added to the postlist container.
     */
    private void loadPosts() {
        // Safety check - don't load posts if main controller isn't available
        if (main == null) {
            return;
        }
        
        postlist.getChildren().clear();

        List<Post> posts = repository.loadPosts();
        if (posts == null || posts.isEmpty()) {
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            String tits = p.getTitle();
            String usr = p.getUsername();
            String desc = p.getDescription();
            String image = p.getImage();
            String time = p.getUploadtime();
            String likes = p.getLikecount();
            
            // Get profile picture and display username from UserData.xml instead of Post data
            String pp = getProfilePictureForUser(usr);
            String displayUsername = getDisplayUsernameForUser(usr);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/starlight/view/postItem.fxml"));
                VBox node = loader.load();
                PostItemController c = loader.getController();

                c.username.setText(displayUsername != null ? displayUsername : usr);
                c.title.setText(tits);
                c.uploadtime.setText(formatRelativeTime(time));
                c.description.setText(desc);
                c.likecounter.setText(likes);
                
                // Set comment count (default to 0 if not set)
                String commentCount = p.getCommentcount() != null ? p.getCommentcount() : "0";
                c.commentcounter.setText(commentCount);
                
                // Set the post data for the controller
                c.setPost(p);
                
                // Set main controller reference for navigation
                c.setMainController(main);
                
                // Initialize isLiked field if not set
                if (p.getIsLiked() == null) {
                    p.setIsLiked("false");
                }

                // Load profile picture
                ImageUtils.loadImage(c.profile1, pp, ImageUtils.DEFAULT_MISSING_IMAGE);
                ImageUtils.scaleToFit(c.profile1, 40, 40, 40);

                // Load post image
                ImageUtils.loadImage(c.recentphoto1, image, ImageUtils.DEFAULT_MISSING_IMAGE);
                ImageUtils.scaleToFit(c.recentphoto1, 674, 485, 20);

                postlist.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Public method to refresh the posts and daily posts - used when posts are updated externally
     */
    public void refreshPosts() {
        if (postlist != null) {
            loadPosts();
        }
        loadDailyPosts();
    }

    /**
     * Loads the top posts into the daily post slots with random selection
     */
    private void loadDailyPosts() {
        List<Post> posts = repository.loadPosts();
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // Create a copy of the posts list and shuffle it randomly
        List<Post> shuffledPosts = new java.util.ArrayList<>(posts);
        Collections.shuffle(shuffledPosts);

        // Take up to 4 posts for daily posts from the shuffled list
        for (int i = 0; i < Math.min(4, shuffledPosts.size()); i++) {
            Post post = shuffledPosts.get(i);
            switch (i) {
                case 0:
                    dailytitle1.setText(post.getTitle());
                    starrating.setText(post.getRating());
                    dailylikecounter.setText(post.getLikecount());
                    ImageUtils.loadImage(dailyphoto1, post.getImage(), ImageUtils.DEFAULT_MISSING_IMAGE);
                    ImageUtils.scaleToFit(dailyphoto1, 280, 174, 30);
                    break;
                case 1:
                    dailytitle2.setText(post.getTitle());
                    starrating2.setText(post.getRating());
                    dailylikecounter2.setText(post.getLikecount());
                    ImageUtils.loadImage(dailyphoto2, post.getImage(), ImageUtils.DEFAULT_MISSING_IMAGE);
                    ImageUtils.scaleToFit(dailyphoto2, 280, 174, 30);
                    break;
                case 2:
                    dailytitle3.setText(post.getTitle());
                    starrating3.setText(post.getRating());
                    dailylikecounter3.setText(post.getLikecount());
                    ImageUtils.loadImage(dailyphoto3, post.getImage(), ImageUtils.DEFAULT_MISSING_IMAGE);
                    ImageUtils.scaleToFit(dailyphoto3, 280, 174, 30);
                    break;
                case 3:
                    dailytitle4.setText(post.getTitle());
                    starrating4.setText(post.getRating());
                    dailylikecounter4.setText(post.getLikecount());
                    ImageUtils.loadImage(dailyphoto4, post.getImage(), ImageUtils.DEFAULT_MISSING_IMAGE);
                    ImageUtils.scaleToFit(dailyphoto4, 280, 174, 30);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Initializes the controller by loading dummy data and wiring up actions.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        repository.ensureDummyData();
        
        // Only load posts if main controller is available
        // Otherwise, they'll be loaded when setMainController is called
        if (main != null) {
            loadPosts();
            loadDailyPosts();
        }
    }

    /**
     * Formats a timestamp string into a relative time format like "2h ago", "1d ago"
     */
    public String formatRelativeTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return "unknown";
        }
        
        try {
            LocalDateTime postTime = parseDateTime(timeString.trim());
            if (postTime == null) {
                return timeString;
            }
            
            long hours = ChronoUnit.HOURS.between(postTime, LocalDateTime.now());
            long minutes = ChronoUnit.MINUTES.between(postTime, LocalDateTime.now());
            
            if (minutes < 1) return "just now";
            if (minutes < 60) return minutes + "m ago";
            if (hours < 24) return hours + "h ago";
            
            long days = hours / 24;
            if (days < 30) return days + "d ago";
            
            long months = days / 30;
            if (months < 12) return months + "mo ago";
            
            return (months / 12) + "y ago";
        } catch (Exception e) {
            return timeString;
        }
    }
    
    /**
     * Parses a date time string using multiple formats.
     * 
     * @param trimmedTime The trimmed time string to parse
     * @return LocalDateTime if parsing is successful, null otherwise
     */
    private LocalDateTime parseDateTime(String trimmedTime) {
        // Try different formats to handle various timestamp formats
        try {
            // First try format with seconds: "2025-07-11 17:12:55"
            DateTimeFormatter formatterWithSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(trimmedTime, formatterWithSeconds);
        } catch (Exception e1) {
            try {
                // Then try format without seconds: "2025-07-11 17:12"
                DateTimeFormatter formatterWithoutSeconds = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(trimmedTime, formatterWithoutSeconds);
            } catch (Exception e2) {
                // If both fail, return null
                return null;
            }
        }
    }
}
