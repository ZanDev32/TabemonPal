package com.starlight.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import com.starlight.models.Post;
import com.starlight.repository.PostDataRepository;

import java.util.List;

public class DeleteDialogController {
    @FXML
    private MFXButton delete;

    @FXML
    private MFXButton cancel;

    private Post postToDelete;
    private boolean confirmed = false;
    private final PostDataRepository repository = new PostDataRepository();

    /**
     * Sets the post to be deleted
     */
    public void setPost(Post post) {
        this.postToDelete = post;
    }

    /**
     * Returns whether the user confirmed the deletion
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void initialize() {
        // Delete button logic
        delete.setOnAction(event -> {
            if (postToDelete == null) {
                System.err.println("No post to delete");
                return;
            }

            // Remove the post from the repository
            List<Post> posts = repository.loadPosts();
            posts.removeIf(post -> post.uuid != null && post.uuid.equals(postToDelete.uuid));
            repository.savePosts(posts);

            confirmed = true;

            // Close the dialog
            Stage stage = (Stage) delete.getScene().getWindow();
            stage.close();
        });

        // Cancel button logic
        cancel.setOnAction(event -> {
            confirmed = false;
            // Close the dialog without deleting
            Stage stage = (Stage) cancel.getScene().getWindow();
            stage.close();
        });
    }
}
