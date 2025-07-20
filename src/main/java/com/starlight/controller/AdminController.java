package com.starlight.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.application.Platform;
import javafx.concurrent.Task;

import com.starlight.models.Post;
import com.starlight.models.User;
import com.starlight.repository.PostDataRepository;
import com.starlight.repository.UserDataRepository;
import com.starlight.util.Session;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;

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
public class AdminController implements Initializable {

    @FXML
    private TableView<PostTableData> postsTable;

    @FXML
    private TableColumn<PostTableData, String> titleColumn;

    @FXML
    private TableColumn<PostTableData, String> usernameColumn;

    @FXML
    private TableColumn<PostTableData, String> ratingColumn;

    @FXML
    private TableColumn<PostTableData, String> likeCountColumn;

    @FXML
    private TableColumn<PostTableData, String> uploadTimeColumn;

    @FXML
    private TableColumn<PostTableData, Void> actionColumn;

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
        
        // Initialize repositories
        postRepository = new PostDataRepository();
        userRepository = new UserDataRepository();
        postDataList = FXCollections.observableArrayList();

        // Setup table columns
        setupTableColumns();

        // Setup sort combo box
        setupSortComboBox();

        // Load initial data
        loadData();

        // Setup refresh button
        refreshButton.setOnAction(e -> loadData());
    }
    
    /**
     * Checks if the current user has admin access
     */
    private boolean hasAdminAccess() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Allow admin access for users with "admin" in their username or email
        // This is a simple implementation - in a real app, you'd have proper role management
        return currentUser.username != null && 
               (currentUser.username.toLowerCase().contains("admin") || 
                currentUser.email != null && currentUser.email.toLowerCase().contains("admin"));
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
        // Basic columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        likeCountColumn.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        uploadTimeColumn.setCellValueFactory(new PropertyValueFactory<>("uploadTime"));

        // Make rating column editable with ComboBox
        setupEditableRatingColumn();

        // Setup action column with delete button
        setupActionColumn();

        // Bind data to table
        postsTable.setItems(postDataList);

        // Enable row selection
        postsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Sets up the rating column to be editable with a ComboBox
     */
    private void setupEditableRatingColumn() {
        ObservableList<String> ratingOptions = FXCollections.observableArrayList(
            "0.0", "0.5", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"
        );

        ratingColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
            new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    return object != null ? object : "0.0";
                }

                @Override
                public String fromString(String string) {
                    return string;
                }
            },
            ratingOptions
        ));

        ratingColumn.setOnEditCommit(event -> {
            PostTableData data = event.getRowValue();
            data.rating.set(event.getNewValue());
            // Update the original post object
            data.getOriginalPost().rating = event.getNewValue();
            // Save changes to XML
            savePostChanges(data.getOriginalPost());
        });

        // Make the table editable
        postsTable.setEditable(true);
    }

    /**
     * Sets up the action column with delete button
     */
    private void setupActionColumn() {
        Callback<TableColumn<PostTableData, Void>, TableCell<PostTableData, Void>> cellFactory = 
            new Callback<TableColumn<PostTableData, Void>, TableCell<PostTableData, Void>>() {
                @Override
                public TableCell<PostTableData, Void> call(TableColumn<PostTableData, Void> param) {
                    return new TableCell<PostTableData, Void>() {
                        private final MFXButton deleteButton = new MFXButton("Delete");

                        {
                            deleteButton.getStyleClass().add("mfx-button");
                            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                            deleteButton.setOnAction(event -> {
                                PostTableData data = getTableView().getItems().get(getIndex());
                                showDeleteConfirmation(data);
                            });
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(deleteButton);
                            }
                        }
                    };
                }
            };
        
        actionColumn.setCellFactory(cellFactory);
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

                Platform.runLater(() -> {
                    // Clear existing data
                    postDataList.clear();

                    // Convert posts to table data
                    List<PostTableData> tableData = posts.stream()
                        .map(PostTableData::new)
                        .collect(Collectors.toList());

                    postDataList.addAll(tableData);

                    // Update statistics
                    totalPostsLabel.setText("Total Posts: " + posts.size());
                    totalUsersLabel.setText("Total Users: " + users.size());

                    loadingIndicator.setVisible(false);
                });

                return null;
            }
        };

        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                showErrorAlert("Error", "Failed to load data from XML files.");
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
}
