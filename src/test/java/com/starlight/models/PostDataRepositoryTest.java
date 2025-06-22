package com.starlight.models;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PostDataRepositoryTest {
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
        p.title = "Sample";
        p.description = "Desc";
        p.ingredients = "ing";
        p.directions = "dir";
        p.image = "img";
        p.rating = "5";
        p.uploadtime = "now";
        p.likecount = "0";
        posts.add(p);

        repository.savePosts(posts);
        assertTrue(Files.exists(tempFile) && tempFile.toFile().length() > 0);

        List<Post> loaded = repository.loadPosts();
        assertEquals(1, loaded.size());
        Post lp = loaded.get(0);
        assertEquals(p.title, lp.title);
        assertEquals(p.description, lp.description);
        assertEquals(p.ingredients, lp.ingredients);
        assertEquals(p.directions, lp.directions);
        assertEquals(p.image, lp.image);
        assertEquals(p.rating, lp.rating);
        assertEquals(p.uploadtime, lp.uploadtime);
        assertEquals(p.likecount, lp.likecount);
    }
}
