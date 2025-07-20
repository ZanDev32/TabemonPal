package com.starlight.models;

import org.junit.jupiter.api.Test;

import com.starlight.model.Post;
import com.starlight.repository.PostDataRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class to verify that liked state is properly saved and loaded from XML.
 */
public class PostLikedStateTest {
    
    private Path tempFile;
    private PostDataRepository repository;
    
    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("testPosts", ".xml");
        repository = new PostDataRepository(tempFile.toString());
    }
    
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }
    
    @Test
    void testLikedStateSavedAndLoaded() {
        // Create a post with liked state
        Post post = new Post();
        post.uuid = "test-uuid-123";
        post.title = "Test Post";
        post.description = "Test Description";
        post.likecount = "5";
        post.commentcount = "3";
        post.isLiked = "true";
        
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        
        // Save the post
        repository.savePosts(posts);
        
        // Load the posts back
        List<Post> loadedPosts = repository.loadPosts();
        
        // Verify that the liked state is preserved
        assertNotNull(loadedPosts);
        assertEquals(1, loadedPosts.size());
        
        Post loadedPost = loadedPosts.get(0);
        assertEquals("test-uuid-123", loadedPost.uuid);
        assertEquals("Test Post", loadedPost.title);
        assertEquals("Test Description", loadedPost.description);
        assertEquals("5", loadedPost.likecount);
        assertEquals("3", loadedPost.commentcount);
        assertEquals("true", loadedPost.isLiked);
    }
    
    @Test
    void testMissingFieldsInitialized() {
        // Create a post without commentcount and isLiked fields
        Post post = new Post();
        post.uuid = "test-uuid-456";
        post.title = "Test Post Without New Fields";
        post.likecount = "10";
        // Note: commentcount and isLiked are null
        
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        
        // Save the post
        repository.savePosts(posts);
        
        // Load the posts back
        List<Post> loadedPosts = repository.loadPosts();
        
        // Verify that missing fields are initialized
        assertNotNull(loadedPosts);
        assertEquals(1, loadedPosts.size());
        
        Post loadedPost = loadedPosts.get(0);
        assertEquals("test-uuid-456", loadedPost.uuid);
        assertEquals("Test Post Without New Fields", loadedPost.title);
        assertEquals("10", loadedPost.likecount);
        assertEquals("0", loadedPost.commentcount); // Should be initialized to "0"
        assertEquals("false", loadedPost.isLiked);  // Should be initialized to "false"
    }
    
    @Test
    void testLikeStateToggle() {
        // Create a post with false liked state
        Post post = new Post();
        post.uuid = "test-uuid-789";
        post.title = "Toggle Test Post";
        post.likecount = "0";
        post.commentcount = "0";
        post.isLiked = "false";
        
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        
        // Save the post
        repository.savePosts(posts);
        
        // Load, modify, and save again
        List<Post> loadedPosts = repository.loadPosts();
        Post loadedPost = loadedPosts.get(0);
        
        // Toggle the liked state
        loadedPost.isLiked = "true";
        loadedPost.likecount = "1";
        
        repository.savePosts(loadedPosts);
        
        // Load again and verify the change persisted
        List<Post> reloadedPosts = repository.loadPosts();
        Post reloadedPost = reloadedPosts.get(0);
        
        assertEquals("true", reloadedPost.isLiked);
        assertEquals("1", reloadedPost.likecount);
    }
}
