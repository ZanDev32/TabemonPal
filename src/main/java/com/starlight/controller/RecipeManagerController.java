package com.starlight.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import com.starlight.model.Post;
import com.starlight.model.User;
import com.starlight.repository.PostDataRepository;
import com.starlight.repository.UserDataRepository;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller for the admin panel that displays and manages posts and users data.
 * Provides functionality to view, edit, sort, and delete posts from XML data.
 */
public class RecipeManagerController implements Initializable {

    @FXML
    private MFXTableView<PostTableData> postsTable;

    // Create table columns programmatically instead of FXML injection
    private MFXTableColumn<PostTableData> titleColumn;
    private MFXTableColumn<PostTableData> usernameColumn;
    private MFXTableColumn<PostTableData> ratingColumn;
    private MFXTableColumn<PostTableData> likeCountColumn;
    private MFXTableColumn<PostTableData> uploadTimeColumn;
    private MFXTableColumn<PostTableData> actionColumn;

    @FXML
    private MFXComboBox<String> sortComboBox;

    @FXML
    private MFXButton refreshButton;

    @FXML
    private Label totalPostsLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    private PostDataRepository postRepository;
    private UserDataRepository userRepository;
    private ObservableList<PostTableData> postDataList;

    /**
     * Data model for the table view
     */
    public static class PostTableData {
        private final SimpleStringProperty uuid;
        private final SimpleStringProperty title;
        private final SimpleStringProperty username;
        private final SimpleStringProperty rating;
        private final SimpleStringProperty likeCount;
        private final SimpleStringProperty uploadTime;
        private final SimpleStringProperty description;
        private final Post originalPost;

        public PostTableData(Post post) {
            this.originalPost = post;
            this.uuid = new SimpleStringProperty(post.uuid != null ? post.uuid : "");
            this.title = new SimpleStringProperty(post.title != null ? post.title : "");
            this.username = new SimpleStringProperty(post.username != null ? post.username : "");
            this.rating = new SimpleStringProperty(post.rating != null ? post.rating : "0.0");
            this.likeCount = new SimpleStringProperty(post.likecount != null ? post.likecount : "0");
            this.uploadTime = new SimpleStringProperty(formatDateTime(post.uploadtime));
            this.description = new SimpleStringProperty(post.description != null ? post.description : "");
        }

        private static String formatDateTime(String uploadTime) {
            if (uploadTime == null || uploadTime.trim().isEmpty()) {
                return "Unknown";
            }
            try {
                LocalDateTime dateTime = LocalDateTime.parse(uploadTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            } catch (DateTimeParseException e) {
                // Try alternative format
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(uploadTime.replace("Z", ""));
                    return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                } catch (Exception ex) {
                    return uploadTime; // Return original if parsing fails
                }
            }
        }

        // Getters for table columns
        public String getUuid() { return uuid.get(); }
        public String getTitle() { return title.get(); }
        public String getUsername() { return username.get(); }
        public String getRating() { return rating.get(); }
        public String getLikeCount() { return likeCount.get(); }
        public String getUploadTime() { return uploadTime.get(); }
        public String getDescription() { return description.get(); }
        public Post getOriginalPost() { return originalPost; }

        // Property getters for table binding
        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty ratingProperty() { return rating; }
        public SimpleStringProperty likeCountProperty() { return likeCount; }
        public SimpleStringProperty uploadTimeProperty() { return uploadTime; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check admin access first
        if (!hasAdminAccess()) {
            showAccessDeniedAndClose();
            return;
        }
        
        System.out.println("DEBUG: Initializing AdminController...");
        
        // Initialize repositories
        postRepository = new PostDataRepository();
        userRepository = new UserDataRepository();
        postDataList = FXCollections.observableArrayList();

        // Setup table columns first
        setupTableColumns();
        System.out.println("DEBUG: Table columns setup completed");

        // Setup sort combo box
        setupSortComboBox();

        // Add test data to verify table works
        addTestData();

        // Load actual data
        loadData();

        // Setup refresh button
        refreshButton.setOnAction(e -> loadData());
        
        System.out.println("DEBUG: AdminController initialization completed");
    }
    
    /**
     * Checks if the current user has admin access
     */
    private boolean hasAdminAccess() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Only allow access for users with exact username "admin"
        return currentUser.username != null && 
               currentUser.username.toLowerCase().equals("admin");
    }
    
    /**
     * Shows access denied message and closes the admin panel
     */
    private void showAccessDeniedAndClose() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Admin Access Required");
            alert.setContentText("You do not have admin privileges to access this panel.");
            alert.showAndWait();
            
            // Close the window or navigate back
            // This would ideally navigate back to the previous page
        });
    }

    /**
     * Sets up table columns with appropriate cell factories and value factories
     */
    private void setupTableColumns() {
        System.out.println("DEBUG: Setting up MFXTableView columns...");
        
        // Create table columns programmatically with simplified approach
        titleColumn = new MFXTableColumn<>("Title");
        usernameColumn = new MFXTableColumn<>("Author");
        ratingColumn = new MFXTableColumn<>("Rating");
        likeCountColumn = new MFXTableColumn<>("Likes");
        uploadTimeColumn = new MFXTableColumn<>("Upload Date");
        actionColumn = new MFXTableColumn<>("Actions");

        // Set preferred widths - make them larger for better visibility
        titleColumn.setPrefWidth(250.0);
        usernameColumn.setPrefWidth(150.0);
        ratingColumn.setPrefWidth(100.0);
        likeCountColumn.setPrefWidth(100.0);
        uploadTimeColumn.setPrefWidth(180.0);
        actionColumn.setPrefWidth(180.0);

        // Set minimum widths to prevent columns from being too small
        titleColumn.setMinWidth(200.0);
        usernameColumn.setMinWidth(120.0);
        ratingColumn.setMinWidth(80.0);
        likeCountColumn.setMinWidth(80.0);
        uploadTimeColumn.setMinWidth(150.0);
        actionColumn.setMinWidth(160.0);

        // Setup cell factories using simple string extraction
        titleColumn.setRowCellFactory(item -> new MFXTableRowCell<>(PostTableData::getTitle));
        usernameColumn.setRowCellFactory(item -> new MFXTableRowCell<>(PostTableData::getUsername));
        ratingColumn.setRowCellFactory(item -> new MFXTableRowCell<>(PostTableData::getRating));
        likeCountColumn.setRowCellFactory(item -> new MFXTableRowCell<>(PostTableData::getLikeCount));
        uploadTimeColumn.setRowCellFactory(item -> new MFXTableRowCell<>(PostTableData::getUploadTime));
        
        // Setup action column
        actionColumn.setRowCellFactory(item -> {
            MFXTableRowCell<PostTableData, String> cell = new MFXTableRowCell<>(data -> "");
            
            // Create Edit button
            MFXButton editButton = new MFXButton("Edit");
            editButton.getStyleClass().add("mfx-button");
            editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 60px;");
            editButton.setOnAction(event -> showEditDialog(item));
            
            // Create Delete button
            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.getStyleClass().add("mfx-button");
            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 60px;");
            deleteButton.setOnAction(event -> showDeleteConfirmation(item));
            
            // Create HBox to hold both buttons
            HBox buttonBox = new HBox(5); // 5px spacing between buttons
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(editButton, deleteButton);
            
            cell.setGraphic(buttonBox);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Add columns to table
        postsTable.getTableColumns().clear();
        postsTable.getTableColumns().add(titleColumn);
        postsTable.getTableColumns().add(usernameColumn);
        postsTable.getTableColumns().add(ratingColumn);
        postsTable.getTableColumns().add(likeCountColumn);
        postsTable.getTableColumns().add(uploadTimeColumn);
        postsTable.getTableColumns().add(actionColumn);

        // Set the items
        postsTable.setItems(postDataList);
        
        // Configure table sizing
        postsTable.setPrefHeight(400.0);
        postsTable.setMinHeight(300.0);
        postsTable.setMaxHeight(Double.MAX_VALUE);
        
        System.out.println("DEBUG: MFXTableView columns setup completed, postDataList size: " + postDataList.size());
        System.out.println("DEBUG: Table columns count: " + postsTable.getTableColumns().size());
        System.out.println("DEBUG: Table items count: " + postsTable.getItems().size());
        
        // Force table update
        Platform.runLater(() -> {
            postsTable.update();
            System.out.println("DEBUG: Platform.runLater update called");
        });
    }

    /**
     * Shows edit dialog for a post
     */
    private void showEditDialog(PostTableData data) {
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Edit Post");
        dialog.setHeaderText("Edit post details for: " + data.getTitle());

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setText(data.getTitle());
        titleField.setPrefWidth(300);

        TextField ratingField = new TextField();
        ratingField.setText(data.getRating());
        ratingField.setPrefWidth(100);

        TextField likeCountField = new TextField();
        likeCountField.setText(data.getLikeCount());
        likeCountField.setPrefWidth(100);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setText(data.getDescription());
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Rating:"), 0, 1);
        grid.add(ratingField, 1, 1);
        grid.add(new Label("Like Count:"), 0, 2);
        grid.add(likeCountField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable save button depending on whether valid data was entered
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false);

        // Convert the result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate input
                if (titleField.getText().trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Title cannot be empty.");
                    return null;
                }
                
                try {
                    double rating = Double.parseDouble(ratingField.getText());
                    if (rating < 0 || rating > 5) {
                        showErrorAlert("Validation Error", "Rating must be between 0 and 5.");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showErrorAlert("Validation Error", "Rating must be a valid number.");
                    return null;
                }
                
                try {
                    int likeCount = Integer.parseInt(likeCountField.getText());
                    if (likeCount < 0) {
                        showErrorAlert("Validation Error", "Like count cannot be negative.");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showErrorAlert("Validation Error", "Like count must be a valid number.");
                    return null;
                }

                // Create updated post
                Post updatedPost = data.getOriginalPost();
                updatedPost.title = titleField.getText().trim();
                updatedPost.rating = ratingField.getText().trim();
                updatedPost.likecount = likeCountField.getText().trim();
                updatedPost.description = descriptionArea.getText().trim();
                
                return updatedPost;
            }
            return null;
        });

        Optional<Post> result = dialog.showAndWait();
        result.ifPresent(updatedPost -> {
            // Save changes
            savePostChanges(updatedPost);
            
            // Update the table data
            data.title.set(updatedPost.title);
            data.rating.set(updatedPost.rating);
            data.likeCount.set(updatedPost.likecount);
            data.description.set(updatedPost.description);
            
            // Refresh the table
            postsTable.update();
            
            showSuccessAlert("Success", "Post updated successfully!");
        });
    }

    /**
     * Sets up the sort combo box with sorting options
     */
    private void setupSortComboBox() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
            "Name (A-Z)", "Name (Z-A)", 
            "Rating (High-Low)", "Rating (Low-High)",
            "Likes (High-Low)", "Likes (Low-High)",
            "Date (Newest)", "Date (Oldest)"
        );

        sortComboBox.setItems(sortOptions);
        sortComboBox.setPromptText("Sort by...");
        
        sortComboBox.setOnAction(e -> {
            String selectedSort = sortComboBox.getSelectionModel().getSelectedItem();
            if (selectedSort != null) {
                sortTable(selectedSort);
            }
        });
    }

    /**
     * Loads data from XML files and populates the table
     */
    private void loadData() {
        loadingIndicator.setVisible(true);

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load posts and users
                List<Post> posts = postRepository.loadPosts();
                List<User> users = userRepository.loadUsers();

                System.out.println("DEBUG: Loaded " + posts.size() + " posts from repository");

                Platform.runLater(() -> {
                    // Clear existing data
                    postDataList.clear();

                    // Convert posts to table data
                    List<PostTableData> tableData = posts.stream()
                        .map(PostTableData::new)
                        .collect(Collectors.toList());

                    System.out.println("DEBUG: Converted " + tableData.size() + " posts to table data");
                    
                    // Debug: Print first few items
                    for (int i = 0; i < Math.min(3, tableData.size()); i++) {
                        PostTableData item = tableData.get(i);
                        System.out.println("DEBUG: Item " + i + ": " + item.getTitle() + " by " + item.getUsername());
                    }

                    postDataList.addAll(tableData);

                    System.out.println("DEBUG: Added data to table, postDataList size: " + postDataList.size());
                    
                    // Force MFXTableView to recognize the new data - multiple approaches
                    postsTable.setItems(null);  // Clear first
                    postsTable.setItems(postDataList);  // Reset
                    postsTable.update();  // Update
                    
                    // Try to manually trigger table update with delay
                    Platform.runLater(() -> {
                        postsTable.update();
                        System.out.println("DEBUG: Manual table update after data load");
                        
                        // Additional force update
                        Platform.runLater(() -> {
                            postsTable.update();
                            System.out.println("DEBUG: Second manual table update");
                        });
                    });

                    // Update statistics
                    totalPostsLabel.setText("Total Posts: " + posts.size());
                    totalUsersLabel.setText("Total Users: " + users.size());

                    // Force table refresh for MFXTableView
                    postsTable.update();
                    
                    // Additional debug
                    System.out.println("DEBUG: After refresh - Table items: " + postsTable.getItems().size());
                    System.out.println("DEBUG: After refresh - Table columns: " + postsTable.getTableColumns().size());

                    loadingIndicator.setVisible(false);
                    
                    System.out.println("DEBUG: Data loading completed successfully");
                });

                return null;
            }
        };

        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                System.err.println("DEBUG: Failed to load data: " + e.getSource().getException());
                e.getSource().getException().printStackTrace();
                showErrorAlert("Error", "Failed to load data from XML files: " + e.getSource().getException().getMessage());
            });
        });

        new Thread(loadTask).start();
    }

    /**
     * Sorts the table based on the selected criteria
     */
    private void sortTable(String sortCriteria) {
        Comparator<PostTableData> comparator = null;

        switch (sortCriteria) {
            case "Name (A-Z)":
                comparator = Comparator.comparing(PostTableData::getTitle);
                break;
            case "Name (Z-A)":
                comparator = Comparator.comparing(PostTableData::getTitle).reversed();
                break;
            case "Rating (High-Low)":
                comparator = Comparator.comparing(data -> {
                    try {
                        return Double.parseDouble(data.getRating());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                }, Comparator.reverseOrder());
                break;
            case "Rating (Low-High)":
                comparator = Comparator.comparing(data -> {
                    try {
                        return Double.parseDouble(data.getRating());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                });
                break;
            case "Likes (High-Low)":
                comparator = Comparator.comparing(data -> {
                    try {
                        return Integer.parseInt(data.getLikeCount());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }, Comparator.reverseOrder());
                break;
            case "Likes (Low-High)":
                comparator = Comparator.comparing(data -> {
                    try {
                        return Integer.parseInt(data.getLikeCount());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });
                break;
            case "Date (Newest)":
                comparator = Comparator.comparing((PostTableData data) -> parseDateTime(data.getOriginalPost().uploadtime))
                    .reversed();
                break;
            case "Date (Oldest)":
                comparator = Comparator.comparing((PostTableData data) -> parseDateTime(data.getOriginalPost().uploadtime));
                break;
        }

        if (comparator != null) {
            postDataList.sort(comparator);
        }
    }

    /**
     * Parses datetime string for sorting purposes
     */
    private LocalDateTime parseDateTime(String uploadTime) {
        if (uploadTime == null || uploadTime.trim().isEmpty()) {
            return LocalDateTime.MIN;
        }
        try {
            return LocalDateTime.parse(uploadTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(uploadTime.replace("Z", ""));
            } catch (Exception ex) {
                return LocalDateTime.MIN;
            }
        }
    }

    /**
     * Shows confirmation dialog for post deletion
     */
    private void showDeleteConfirmation(PostTableData data) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Post");
        alert.setContentText("Are you sure you want to delete the post '" + data.getTitle() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deletePost(data);
            }
        });
    }

    /**
     * Deletes a post from the data and XML file
     */
    private void deletePost(PostTableData data) {
        try {
            // Use the repository's deletePost method
            boolean deleted = postRepository.deletePost(data.getUuid());
            
            if (deleted) {
                // Remove from UI
                postDataList.remove(data);
                
                // Update statistics
                totalPostsLabel.setText("Total Posts: " + postDataList.size());
                
                showSuccessAlert("Success", "Post deleted successfully!");
            } else {
                showErrorAlert("Error", "Post not found or could not be deleted.");
            }

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to delete post: " + e.getMessage());
        }
    }

    /**
     * Saves changes made to a post back to XML
     */
    private void savePostChanges(Post post) {
        try {
            List<Post> allPosts = postRepository.loadPosts();
            
            // Find and update the post
            for (int i = 0; i < allPosts.size(); i++) {
                if (allPosts.get(i).uuid != null && allPosts.get(i).uuid.equals(post.uuid)) {
                    allPosts.set(i, post);
                    break;
                }
            }

            postRepository.savePosts(allPosts);
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to save changes: " + e.getMessage());
        }
    }

    /**
     * Shows success alert
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Adds test data to verify the table is working
     */
    private void addTestData() {
        System.out.println("DEBUG: Adding test data to verify table functionality...");
        
        // Create dummy posts for testing
        Post testPost1 = new Post();
        testPost1.uuid = "test-1";
        testPost1.title = "Test Post 1";
        testPost1.username = "TestUser1";
        testPost1.rating = "4.5";
        testPost1.likecount = "10";
        testPost1.uploadtime = "2025-07-20T19:00:00";
        testPost1.description = "This is a test post to verify table functionality";
        
        Post testPost2 = new Post();
        testPost2.uuid = "test-2";
        testPost2.title = "Test Post 2";
        testPost2.username = "TestUser2";
        testPost2.rating = "3.8";
        testPost2.likecount = "5";
        testPost2.uploadtime = "2025-07-20T18:30:00";
        testPost2.description = "Another test post";
        
        // Convert to table data
        PostTableData tableData1 = new PostTableData(testPost1);
        PostTableData tableData2 = new PostTableData(testPost2);
        
        // Add to the list
        postDataList.clear();
        postDataList.add(tableData1);
        postDataList.add(tableData2);
        
        System.out.println("DEBUG: Added " + postDataList.size() + " test items to table");
        System.out.println("DEBUG: Test item 1: " + tableData1.getTitle() + " by " + tableData1.getUsername());
        System.out.println("DEBUG: Test item 2: " + tableData2.getTitle() + " by " + tableData2.getUsername());
        
        // Force table update
        postsTable.update();
        
        Platform.runLater(() -> {
            postsTable.update();
            System.out.println("DEBUG: Test data added and table updated");
        });
    }
}
