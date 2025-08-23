package com.starlight.model;

import org.junit.jupiter.api.Test;

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
class PostLikedStateTest {
    
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
    post.setUuid("test-uuid-123");
    post.setTitle("Test Post");
    post.setDescription("Test Description");
    post.setLikecount("5");
    post.setCommentcount("3");
    post.setIsLiked("true");
        
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
    assertEquals("test-uuid-123", loadedPost.getUuid());
    assertEquals("Test Post", loadedPost.getTitle());
    assertEquals("Test Description", loadedPost.getDescription());
    assertEquals("5", loadedPost.getLikecount());
    assertEquals("3", loadedPost.getCommentcount());
    assertEquals("true", loadedPost.getIsLiked());
    }
    
    @Test
    void testMissingFieldsInitialized() {
        // Create a post without commentcount and isLiked fields
    Post post = new Post();
    post.setUuid("test-uuid-456");
    post.setTitle("Test Post Without New Fields");
    post.setLikecount("10");
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
    assertEquals("test-uuid-456", loadedPost.getUuid());
    assertEquals("Test Post Without New Fields", loadedPost.getTitle());
    assertEquals("10", loadedPost.getLikecount());
    assertEquals("0", loadedPost.getCommentcount()); // Should be initialized to "0"
    assertEquals("false", loadedPost.getIsLiked());  // Should be initialized to "false"
    }
    
    @Test
    void testLikeStateToggle() {
        // Create a post with false liked state
    Post post = new Post();
    post.setUuid("test-uuid-789");
    post.setTitle("Toggle Test Post");
    post.setLikecount("0");
    post.setCommentcount("0");
    post.setIsLiked("false");
        
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        
        // Save the post
        repository.savePosts(posts);
        
        // Load, modify, and save again
        List<Post> loadedPosts = repository.loadPosts();
        Post loadedPost = loadedPosts.get(0);
        
    // Toggle the liked state
    loadedPost.setIsLiked("true");
    loadedPost.setLikecount("1");
        
        repository.savePosts(loadedPosts);
        
        // Load again and verify the change persisted
        List<Post> reloadedPosts = repository.loadPosts();
        Post reloadedPost = reloadedPosts.get(0);
        
    assertEquals("true", reloadedPost.getIsLiked());
        assertEquals("1", reloadedPost.getLikecount());
    }
}
