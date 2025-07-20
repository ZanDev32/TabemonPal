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

/**
 * Controller for the user management panel that displays and manages user data.
 * Provides functionality to view, edit, sort, and delete users from XML data.
 */
public class UserManagerController implements Initializable {

    @FXML
    private MFXTableView<UserTableData> postsTable;

    // Create table columns programmatically instead of FXML injection
    private MFXTableColumn<UserTableData> usernameColumn;
    private MFXTableColumn<UserTableData> fullNameColumn;
    private MFXTableColumn<UserTableData> emailColumn;
    private MFXTableColumn<UserTableData> statusColumn;
    private MFXTableColumn<UserTableData> postsCountColumn;
    private MFXTableColumn<UserTableData> actionColumn;

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
    private ObservableList<UserTableData> userDataList;

    /**
     * Data model for the user table view
     */
    public static class UserTableData {
        private final SimpleStringProperty email; // Using email as unique identifier
        private final SimpleStringProperty username;
        private final SimpleStringProperty fullName;
        private final SimpleStringProperty joinDate;
        private final SimpleStringProperty status;
        private final SimpleStringProperty postsCount;
        private final User originalUser;

        public UserTableData(User user, int userPostsCount) {
            this.originalUser = user;
            this.email = new SimpleStringProperty(user.email != null ? user.email : "");
            this.username = new SimpleStringProperty(user.username != null ? user.username : "");
            this.fullName = new SimpleStringProperty(user.fullname != null ? user.fullname : "");
            this.joinDate = new SimpleStringProperty("N/A"); // User model doesn't have creation date
            this.status = new SimpleStringProperty(determineUserStatus(user));
            this.postsCount = new SimpleStringProperty(String.valueOf(userPostsCount));
        }
        
        private static String determineUserStatus(User user) {
            if (user.username != null && user.username.toLowerCase().equals("admin")) {
                return "Admin";
            }
            // You can add more status logic here based on user properties
            return "Active";
        }

        // Getters for table columns
        public String getEmail() { return email.get(); }
        public String getUsername() { return username.get(); }
        public String getFullName() { return fullName.get(); }
        public String getJoinDate() { return joinDate.get(); }
        public String getStatus() { return status.get(); }
        public String getPostsCount() { return postsCount.get(); }
        public User getOriginalUser() { return originalUser; }

        // Property getters for table binding
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty fullNameProperty() { return fullName; }
        public SimpleStringProperty joinDateProperty() { return joinDate; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty postsCountProperty() { return postsCount; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check admin access first
        if (!hasAdminAccess()) {
            showAccessDeniedAndClose();
            return;
        }
        
        System.out.println("DEBUG: Initializing UserManagerController...");
        
        // Initialize repositories
        postRepository = new PostDataRepository();
        userRepository = new UserDataRepository();
        userDataList = FXCollections.observableArrayList();

        // Setup table columns first
        setupTableColumns();
        System.out.println("DEBUG: Table columns setup completed");

        // Setup sort combo box
        setupSortComboBox();

        // Load actual data
        loadData();

        // Setup refresh button
        refreshButton.setOnAction(e -> loadData());
        
        System.out.println("DEBUG: UserManagerController initialization completed");
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
        System.out.println("DEBUG: Setting up MFXTableView columns for users...");
        
        // Create table columns programmatically
        usernameColumn = new MFXTableColumn<>("Username");
        fullNameColumn = new MFXTableColumn<>("Full Name");
        emailColumn = new MFXTableColumn<>("Email");
        statusColumn = new MFXTableColumn<>("Status");
        postsCountColumn = new MFXTableColumn<>("Posts");
        actionColumn = new MFXTableColumn<>("Actions");

        // Set preferred widths
        usernameColumn.setPrefWidth(120.0);
        fullNameColumn.setPrefWidth(150.0);
        emailColumn.setPrefWidth(180.0);
        statusColumn.setPrefWidth(80.0);
        postsCountColumn.setPrefWidth(70.0);
        actionColumn.setPrefWidth(180.0);

        // Set minimum widths
        usernameColumn.setMinWidth(100.0);
        fullNameColumn.setMinWidth(120.0);
        emailColumn.setMinWidth(150.0);
        statusColumn.setMinWidth(70.0);
        postsCountColumn.setMinWidth(60.0);
        actionColumn.setMinWidth(160.0);

        // Setup cell factories
        usernameColumn.setRowCellFactory(item -> new MFXTableRowCell<>(UserTableData::getUsername));
        fullNameColumn.setRowCellFactory(item -> new MFXTableRowCell<>(UserTableData::getFullName));
        emailColumn.setRowCellFactory(item -> new MFXTableRowCell<>(UserTableData::getEmail));
        statusColumn.setRowCellFactory(item -> new MFXTableRowCell<>(UserTableData::getStatus));
        postsCountColumn.setRowCellFactory(item -> new MFXTableRowCell<>(UserTableData::getPostsCount));
        
        // Setup action column
        actionColumn.setRowCellFactory(item -> {
            MFXTableRowCell<UserTableData, String> cell = new MFXTableRowCell<>(data -> "");
            
            // Create Edit button
            MFXButton editButton = new MFXButton("Edit");
            editButton.getStyleClass().add("mfx-button");
            editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 60px;");
            editButton.setOnAction(event -> showEditDialog(item));
            
            // Create Delete button (only for non-admin users)
            MFXButton deleteButton = new MFXButton("Delete");
            deleteButton.getStyleClass().add("mfx-button");
            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 60px;");
            deleteButton.setOnAction(event -> showDeleteConfirmation(item));
            
            // Disable delete for admin users
            if (item.getUsername().toLowerCase().equals("admin")) {
                deleteButton.setDisable(true);
                deleteButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: white; -fx-font-size: 12px; -fx-pref-width: 60px;");
            }
            
            // Create HBox to hold both buttons
            HBox buttonBox = new HBox(5); // 5px spacing between buttons
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(editButton, deleteButton);
            
            cell.setGraphic(buttonBox);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        // Add columns to table - using the existing FXML table ID
        postsTable.getTableColumns().clear();
        postsTable.getTableColumns().add(usernameColumn);
        postsTable.getTableColumns().add(fullNameColumn);
        postsTable.getTableColumns().add(emailColumn);
        postsTable.getTableColumns().add(statusColumn);
        postsTable.getTableColumns().add(postsCountColumn);
        postsTable.getTableColumns().add(actionColumn);

        // Set the items
        postsTable.setItems(userDataList);
        
        // Configure table sizing
        postsTable.setPrefHeight(400.0);
        postsTable.setMinHeight(300.0);
        postsTable.setMaxHeight(Double.MAX_VALUE);
        
        System.out.println("DEBUG: MFXTableView columns setup completed, userDataList size: " + userDataList.size());
        
        // Force table update
        Platform.runLater(() -> {
            postsTable.update();
            System.out.println("DEBUG: Platform.runLater update called for users table");
        });
    }

    /**
     * Shows edit dialog for a user
     */
    private void showEditDialog(UserTableData data) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user details for: " + data.getUsername());

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setText(data.getUsername());
        usernameField.setPrefWidth(300);

        TextField fullNameField = new TextField();
        fullNameField.setText(data.getFullName());
        fullNameField.setPrefWidth(300);

        TextField emailField = new TextField();
        emailField.setText(data.getEmail());
        emailField.setPrefWidth(300);

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Active", "Inactive", "Admin");
        statusComboBox.setValue(data.getStatus());
        statusComboBox.setPrefWidth(300);

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable save button depending on whether valid data was entered
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false);

        // Convert the result when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate input
                if (usernameField.getText().trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Username cannot be empty.");
                    return null;
                }
                
                if (emailField.getText().trim().isEmpty()) {
                    showErrorAlert("Validation Error", "Email cannot be empty.");
                    return null;
                }
                
                // Basic email validation
                if (!emailField.getText().contains("@")) {
                    showErrorAlert("Validation Error", "Please enter a valid email address.");
                    return null;
                }

                // Create updated user
                User updatedUser = data.getOriginalUser();
                updatedUser.username = usernameField.getText().trim();
                updatedUser.fullname = fullNameField.getText().trim();
                updatedUser.email = emailField.getText().trim();
                // Note: You might want to add more fields to User model for status management
                
                return updatedUser;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(updatedUser -> {
            // Save changes
            saveUserChanges(updatedUser);
            
            // Update the table data
            data.username.set(updatedUser.username);
            data.fullName.set(updatedUser.fullname);
            data.email.set(updatedUser.email);
            
            // Refresh the table
            postsTable.update();
            
            showSuccessAlert("Success", "User updated successfully!");
        });
    }

    /**
     * Sets up the sort combo box with sorting options
     */
    private void setupSortComboBox() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
            "Username (A-Z)", "Username (Z-A)", 
            "Full Name (A-Z)", "Full Name (Z-A)",
            "Email (A-Z)", "Email (Z-A)",
            "Status", "Posts Count (High-Low)", "Posts Count (Low-High)"
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
                // Load users and posts (to count posts per user)
                List<User> users = userRepository.loadUsers();
                List<Post> posts = postRepository.loadPosts();

                System.out.println("DEBUG: Loaded " + users.size() + " users from repository");

                Platform.runLater(() -> {
                    // Clear existing data
                    userDataList.clear();

                    // Convert users to table data with post counts
                    List<UserTableData> tableData = users.stream()
                        .map(user -> {
                            int userPostsCount = (int) posts.stream()
                                .filter(post -> post.username != null && post.username.equals(user.username))
                                .count();
                            return new UserTableData(user, userPostsCount);
                        })
                        .collect(Collectors.toList());

                    System.out.println("DEBUG: Converted " + tableData.size() + " users to table data");
                    
                    // Debug: Print first few items
                    for (int i = 0; i < Math.min(3, tableData.size()); i++) {
                        UserTableData item = tableData.get(i);
                        System.out.println("DEBUG: User " + i + ": " + item.getUsername() + " (" + item.getEmail() + ")");
                    }

                    userDataList.addAll(tableData);

                    System.out.println("DEBUG: Added data to table, userDataList size: " + userDataList.size());
                    
                    // Force MFXTableView to recognize the new data
                    postsTable.setItems(null);  // Clear first
                    postsTable.setItems(userDataList);  // Reset
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
                    
                    System.out.println("DEBUG: User data loading completed successfully");
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
        Comparator<UserTableData> comparator = null;

        switch (sortCriteria) {
            case "Username (A-Z)":
                comparator = Comparator.comparing(UserTableData::getUsername);
                break;
            case "Username (Z-A)":
                comparator = Comparator.comparing(UserTableData::getUsername).reversed();
                break;
            case "Full Name (A-Z)":
                comparator = Comparator.comparing(UserTableData::getFullName);
                break;
            case "Full Name (Z-A)":
                comparator = Comparator.comparing(UserTableData::getFullName).reversed();
                break;
            case "Email (A-Z)":
                comparator = Comparator.comparing(UserTableData::getEmail);
                break;
            case "Email (Z-A)":
                comparator = Comparator.comparing(UserTableData::getEmail).reversed();
                break;
            case "Status":
                comparator = Comparator.comparing(UserTableData::getStatus);
                break;
            case "Posts Count (High-Low)":
                comparator = Comparator.comparing(data -> {
                    try {
                        return Integer.parseInt(data.getPostsCount());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }, Comparator.reverseOrder());
                break;
            case "Posts Count (Low-High)":
                comparator = Comparator.comparing(data -> {
                    try {
                        return Integer.parseInt(data.getPostsCount());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });
                break;
        }

        if (comparator != null) {
            userDataList.sort(comparator);
        }
    }

    /**
     * Shows confirmation dialog for user deletion
     */
    private void showDeleteConfirmation(UserTableData data) {
        // Prevent deletion of admin user
        if (data.getUsername().toLowerCase().equals("admin")) {
            showErrorAlert("Cannot Delete", "Admin user cannot be deleted.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete the user '" + data.getUsername() + "'? This will also delete all their posts.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUser(data);
            }
        });
    }

    /**
     * Deletes a user from the data and XML file
     */
    private void deleteUser(UserTableData data) {
        try {
            // Use the repository's deleteUser method (using email as identifier)
            boolean deleted = userRepository.deleteUser(data.getEmail());
            
            if (deleted) {
                // Also delete all posts by this user
                List<Post> allPosts = postRepository.loadPosts();
                List<Post> postsToKeep = allPosts.stream()
                    .filter(post -> !post.username.equals(data.getUsername()))
                    .collect(Collectors.toList());
                
                if (postsToKeep.size() < allPosts.size()) {
                    postRepository.savePosts(postsToKeep);
                }
                
                // Remove from UI
                userDataList.remove(data);
                
                // Update statistics
                totalUsersLabel.setText("Total Users: " + userDataList.size());
                
                showSuccessAlert("Success", "User and their posts deleted successfully!");
            } else {
                showErrorAlert("Error", "User not found or could not be deleted.");
            }

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * Saves changes made to a user back to XML
     */
    private void saveUserChanges(User user) {
        try {
            List<User> allUsers = userRepository.loadUsers();
            
            // Find and update the user (using email as identifier)
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).email != null && allUsers.get(i).email.equals(user.email)) {
                    allUsers.set(i, user);
                    break;
                }
            }

            userRepository.saveUsers(allUsers);
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
