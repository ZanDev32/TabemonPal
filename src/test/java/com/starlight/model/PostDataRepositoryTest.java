package com.starlight.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.repository.PostDataRepository;

/**
 * Unit tests for {@link PostDataRepository}.
 */
class PostDataRepositoryTest {
    private Path tempFile;
    private PostDataRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("posts", ".xml");
        repository = new PostDataRepository(tempFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testSaveAndLoadPosts() {
        List<Post> posts = new ArrayList<>();
        Post p = new Post();
        p.setTitle("Sample");
        p.setDescription("Desc");
        p.setIngredients("ing");
        p.setDirections("dir");
        p.setImage("img");
        p.setRating("5");
        p.setUploadtime("now");
        p.setLikecount("0");
        p.setCommentcount("0");
        p.setIsLiked("false");
        posts.add(p);

        repository.savePosts(posts);
        assertTrue(Files.exists(tempFile) && tempFile.toFile().length() > 0);

        List<Post> loaded = repository.loadPosts();
        assertEquals(1, loaded.size());
        Post lp = loaded.get(0);
        assertEquals(p.getTitle(), lp.getTitle());
        assertEquals(p.getDescription(), lp.getDescription());
        assertEquals(p.getIngredients(), lp.getIngredients());
        assertEquals(p.getDirections(), lp.getDirections());
        assertEquals(p.getImage(), lp.getImage());
        assertEquals(p.getRating(), lp.getRating());
        assertEquals(p.getUploadtime(), lp.getUploadtime());
        assertEquals(p.getLikecount(), lp.getLikecount());
        assertEquals(p.getCommentcount(), lp.getCommentcount());
    assertEquals(p.getIsLiked(), lp.getIsLiked());
    }
}
