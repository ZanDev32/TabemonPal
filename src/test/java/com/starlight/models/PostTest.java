package com.starlight.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Post} data model.
 */
public class PostTest {
    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post();
    }

    @Test
    void testPostCreation() {
        assertNotNull(post);
        assertNull(post.uuid);
        assertNull(post.username);
        assertNull(post.profilepicture);
        assertNull(post.title);
        assertNull(post.description);
        assertNull(post.ingredients);
        assertNull(post.directions);
        assertNull(post.image);
        assertNull(post.rating);
        assertNull(post.uploadtime);
        assertNull(post.likecount);
        assertNull(post.commentcount);
        assertNull(post.isLiked);
    }

    @Test
    void testSetAndGetUuid() {
        String testUuid = "123e4567-e89b-12d3-a456-426614174000";
        post.uuid = testUuid;
        assertEquals(testUuid, post.uuid);
    }

    @Test
    void testSetAndGetUsername() {
        String testUsername = "chef123";
        post.username = testUsername;
        assertEquals(testUsername, post.username);
    }

    @Test
    void testSetAndGetProfilePicture() {
        String testProfilePicture = "/images/profiles/chef123.jpg";
        post.profilepicture = testProfilePicture;
        assertEquals(testProfilePicture, post.profilepicture);
    }

    @Test
    void testSetAndGetTitle() {
        String testTitle = "Delicious Chocolate Cake";
        post.title = testTitle;
        assertEquals(testTitle, post.title);
    }

    @Test
    void testSetAndGetDescription() {
        String testDescription = "A moist and rich chocolate cake perfect for any occasion.";
        post.description = testDescription;
        assertEquals(testDescription, post.description);
    }

    @Test
    void testSetAndGetIngredients() {
        String testIngredients = "2 cups flour, 1 cup sugar, 3 eggs, 1/2 cup cocoa powder";
        post.ingredients = testIngredients;
        assertEquals(testIngredients, post.ingredients);
    }

    @Test
    void testSetAndGetDirections() {
        String testDirections = "1. Mix dry ingredients. 2. Add wet ingredients. 3. Bake at 350°F for 30 minutes.";
        post.directions = testDirections;
        assertEquals(testDirections, post.directions);
    }

    @Test
    void testSetAndGetImage() {
        String testImage = "/images/posts/chocolate_cake.jpg";
        post.image = testImage;
        assertEquals(testImage, post.image);
    }

    @Test
    void testSetAndGetRating() {
        String testRating = "4.5";
        post.rating = testRating;
        assertEquals(testRating, post.rating);
    }

    @Test
    void testSetAndGetUploadTime() {
        String testUploadTime = "2025-07-07T10:30:00Z";
        post.uploadtime = testUploadTime;
        assertEquals(testUploadTime, post.uploadtime);
    }

    @Test
    void testSetAndGetLikeCount() {
        String testLikeCount = "42";
        post.likecount = testLikeCount;
        assertEquals(testLikeCount, post.likecount);
    }

    @Test
    void testSetAndGetCommentCount() {
        String testCommentCount = "15";
        post.commentcount = testCommentCount;
        assertEquals(testCommentCount, post.commentcount);
    }

    @Test
    void testSetAndGetIsLiked() {
        String testIsLiked = "true";
        post.isLiked = testIsLiked;
        assertEquals(testIsLiked, post.isLiked);
    }

    @Test
    void testPostWithAllFieldsSet() {
        post.uuid = "550e8400-e29b-41d4-a716-446655440000";
        post.username = "masterchef";
        post.profilepicture = "/images/profiles/masterchef.png";
        post.title = "Perfect Pasta Carbonara";
        post.description = "Traditional Italian carbonara with eggs, cheese, and pancetta.";
        post.ingredients = "400g spaghetti, 200g pancetta, 4 eggs, 100g Pecorino Romano, black pepper";
        post.directions = "1. Cook pasta. 2. Fry pancetta. 3. Mix eggs and cheese. 4. Combine all together off heat.";
        post.image = "/images/posts/carbonara.jpg";
        post.rating = "4.8";
        post.uploadtime = "2025-07-07T15:45:30Z";
        post.likecount = "127";
        post.commentcount = "23";
        post.isLiked = "false";

        assertEquals("550e8400-e29b-41d4-a716-446655440000", post.uuid);
        assertEquals("masterchef", post.username);
        assertEquals("/images/profiles/masterchef.png", post.profilepicture);
        assertEquals("Perfect Pasta Carbonara", post.title);
        assertEquals("Traditional Italian carbonara with eggs, cheese, and pancetta.", post.description);
        assertEquals("400g spaghetti, 200g pancetta, 4 eggs, 100g Pecorino Romano, black pepper", post.ingredients);
        assertEquals("1. Cook pasta. 2. Fry pancetta. 3. Mix eggs and cheese. 4. Combine all together off heat.", post.directions);
        assertEquals("/images/posts/carbonara.jpg", post.image);
        assertEquals("4.8", post.rating);
        assertEquals("2025-07-07T15:45:30Z", post.uploadtime);
        assertEquals("127", post.likecount);
        assertEquals("23", post.commentcount);
        assertEquals("false", post.isLiked);
    }

    @Test
    void testPostFieldsCanBeNull() {
        // Set some values first
        post.uuid = "test-uuid";
        post.username = "testuser";
        post.title = "Test Title";
        
        // Then set to null
        post.uuid = null;
        post.username = null;
        post.profilepicture = null;
        post.title = null;
        post.description = null;
        post.ingredients = null;
        post.directions = null;
        post.image = null;
        post.rating = null;
        post.uploadtime = null;
        post.likecount = null;
        post.commentcount = null;
        post.isLiked = null;

        assertNull(post.uuid);
        assertNull(post.username);
        assertNull(post.profilepicture);
        assertNull(post.title);
        assertNull(post.description);
        assertNull(post.ingredients);
        assertNull(post.directions);
        assertNull(post.image);
        assertNull(post.rating);
        assertNull(post.uploadtime);
        assertNull(post.likecount);
        assertNull(post.commentcount);
        assertNull(post.isLiked);
    }

    @Test
    void testPostFieldsCanBeEmpty() {
        post.uuid = "";
        post.username = "";
        post.profilepicture = "";
        post.title = "";
        post.description = "";
        post.ingredients = "";
        post.directions = "";
        post.image = "";
        post.rating = "";
        post.uploadtime = "";
        post.likecount = "";
        post.commentcount = "";
        post.isLiked = "";

        assertEquals("", post.uuid);
        assertEquals("", post.username);
        assertEquals("", post.profilepicture);
        assertEquals("", post.title);
        assertEquals("", post.description);
        assertEquals("", post.ingredients);
        assertEquals("", post.directions);
        assertEquals("", post.image);
        assertEquals("", post.rating);
        assertEquals("", post.uploadtime);
        assertEquals("", post.likecount);
        assertEquals("", post.commentcount);
        assertEquals("", post.isLiked);
    }

    @Test
    void testPostWithSpecialCharacters() {
        post.title = "Café au Lait & Croissants";
        post.description = "A French breakfast with café au lait ☕ and buttery croissants 🥐";
        post.ingredients = "Café beans (organic), milk, butter, flour, eggs";
        post.directions = "1. Brew coffee ☕ 2. Heat milk 🥛 3. Bake croissants @ 200°C";
        post.rating = "4.9";
        post.likecount = "1,234";
        post.commentcount = "87";
        post.isLiked = "true";

        assertEquals("Café au Lait & Croissants", post.title);
        assertEquals("A French breakfast with café au lait ☕ and buttery croissants 🥐", post.description);
        assertEquals("Café beans (organic), milk, butter, flour, eggs", post.ingredients);
        assertEquals("1. Brew coffee ☕ 2. Heat milk 🥛 3. Bake croissants @ 200°C", post.directions);
        assertEquals("4.9", post.rating);
        assertEquals("1,234", post.likecount);
        assertEquals("87", post.commentcount);
        assertEquals("true", post.isLiked);
    }
}
