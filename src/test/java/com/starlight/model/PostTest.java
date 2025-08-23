package com.starlight.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Post} data model.
 */
class PostTest {
    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post();
    }

    @Test
    void testPostCreation() {
        assertNotNull(post);
    assertNull(post.getUuid());
    assertNull(post.getUsername());
    assertNull(post.getProfilepicture());
    assertNull(post.getTitle());
    assertNull(post.getDescription());
    assertNull(post.getIngredients());
    assertNull(post.getDirections());
    assertNull(post.getImage());
    assertNull(post.getRating());
    assertNull(post.getUploadtime());
    assertNull(post.getLikecount());
    assertNull(post.getCommentcount());
    assertNull(post.getIsLiked());
    }

    @Test
    void testSetAndGetUuid() {
        String testUuid = "123e4567-e89b-12d3-a456-426614174000";
    post.setUuid(testUuid);
    assertEquals(testUuid, post.getUuid());
    }

    @Test
    void testSetAndGetUsername() {
        String testUsername = "chef123";
    post.setUsername(testUsername);
    assertEquals(testUsername, post.getUsername());
    }

    @Test
    void testSetAndGetProfilePicture() {
        String testProfilePicture = "/images/profiles/chef123.jpg";
    post.setProfilepicture(testProfilePicture);
    assertEquals(testProfilePicture, post.getProfilepicture());
    }

    @Test
    void testSetAndGetTitle() {
        String testTitle = "Delicious Chocolate Cake";
    post.setTitle(testTitle);
    assertEquals(testTitle, post.getTitle());
    }

    @Test
    void testSetAndGetDescription() {
        String testDescription = "A moist and rich chocolate cake perfect for any occasion.";
    post.setDescription(testDescription);
    assertEquals(testDescription, post.getDescription());
    }

    @Test
    void testSetAndGetIngredients() {
        String testIngredients = "2 cups flour, 1 cup sugar, 3 eggs, 1/2 cup cocoa powder";
    post.setIngredients(testIngredients);
    assertEquals(testIngredients, post.getIngredients());
    }

    @Test
    void testSetAndGetDirections() {
        String testDirections = "1. Mix dry ingredients. 2. Add wet ingredients. 3. Bake at 350°F for 30 minutes.";
    post.setDirections(testDirections);
    assertEquals(testDirections, post.getDirections());
    }

    @Test
    void testSetAndGetImage() {
        String testImage = "/images/posts/chocolate_cake.jpg";
    post.setImage(testImage);
    assertEquals(testImage, post.getImage());
    }

    @Test
    void testSetAndGetRating() {
        String testRating = "4.5";
    post.setRating(testRating);
    assertEquals(testRating, post.getRating());
    }

    @Test
    void testSetAndGetUploadTime() {
        String testUploadTime = "2025-07-07T10:30:00Z";
    post.setUploadtime(testUploadTime);
    assertEquals(testUploadTime, post.getUploadtime());
    }

    @Test
    void testSetAndGetLikeCount() {
        String testLikeCount = "42";
    post.setLikecount(testLikeCount);
    assertEquals(testLikeCount, post.getLikecount());
    }

    @Test
    void testSetAndGetCommentCount() {
        String testCommentCount = "15";
    post.setCommentcount(testCommentCount);
    assertEquals(testCommentCount, post.getCommentcount());
    }

    @Test
    void testSetAndGetIsLiked() {
        String testIsLiked = "true";
    post.setIsLiked(testIsLiked);
    assertEquals(testIsLiked, post.getIsLiked());
    }

    @Test
    void testPostWithAllFieldsSet() {
    post.setUuid("550e8400-e29b-41d4-a716-446655440000");
    post.setUsername("masterchef");
    post.setProfilepicture("/images/profiles/masterchef.png");
    post.setTitle("Perfect Pasta Carbonara");
    post.setDescription("Traditional Italian carbonara with eggs, cheese, and pancetta.");
    post.setIngredients("400g spaghetti, 200g pancetta, 4 eggs, 100g Pecorino Romano, black pepper");
    post.setDirections("1. Cook pasta. 2. Fry pancetta. 3. Mix eggs and cheese. 4. Combine all together off heat.");
    post.setImage("/images/posts/carbonara.jpg");
    post.setRating("4.8");
    post.setUploadtime("2025-07-07T15:45:30Z");
    post.setLikecount("127");
    post.setCommentcount("23");
    post.setIsLiked("false");

    assertEquals("550e8400-e29b-41d4-a716-446655440000", post.getUuid());
    assertEquals("masterchef", post.getUsername());
    assertEquals("/images/profiles/masterchef.png", post.getProfilepicture());
    assertEquals("Perfect Pasta Carbonara", post.getTitle());
    assertEquals("Traditional Italian carbonara with eggs, cheese, and pancetta.", post.getDescription());
    assertEquals("400g spaghetti, 200g pancetta, 4 eggs, 100g Pecorino Romano, black pepper", post.getIngredients());
    assertEquals("1. Cook pasta. 2. Fry pancetta. 3. Mix eggs and cheese. 4. Combine all together off heat.", post.getDirections());
    assertEquals("/images/posts/carbonara.jpg", post.getImage());
    assertEquals("4.8", post.getRating());
    assertEquals("2025-07-07T15:45:30Z", post.getUploadtime());
    assertEquals("127", post.getLikecount());
    assertEquals("23", post.getCommentcount());
    assertEquals("false", post.getIsLiked());
    }

    @Test
    void testPostFieldsCanBeNull() {
    // Set some values first
    post.setUuid("test-uuid");
    post.setUsername("testuser");
    post.setTitle("Test Title");
        
    // Then set to null
    post.setUuid(null);
    post.setUsername(null);
    post.setProfilepicture(null);
    post.setTitle(null);
    post.setDescription(null);
    post.setIngredients(null);
    post.setDirections(null);
    post.setImage(null);
    post.setRating(null);
    post.setUploadtime(null);
    post.setLikecount(null);
    post.setCommentcount(null);
    post.setIsLiked(null);

    assertNull(post.getUuid());
    assertNull(post.getUsername());
    assertNull(post.getProfilepicture());
    assertNull(post.getTitle());
    assertNull(post.getDescription());
    assertNull(post.getIngredients());
    assertNull(post.getDirections());
    assertNull(post.getImage());
    assertNull(post.getRating());
    assertNull(post.getUploadtime());
    assertNull(post.getLikecount());
    assertNull(post.getCommentcount());
    assertNull(post.getIsLiked());
    }

    @Test
    void testPostFieldsCanBeEmpty() {
    post.setUuid("");
    post.setUsername("");
    post.setProfilepicture("");
    post.setTitle("");
    post.setDescription("");
    post.setIngredients("");
    post.setDirections("");
    post.setImage("");
    post.setRating("");
    post.setUploadtime("");
    post.setLikecount("");
    post.setCommentcount("");
    post.setIsLiked("");

    assertEquals("", post.getUuid());
    assertEquals("", post.getUsername());
    assertEquals("", post.getProfilepicture());
    assertEquals("", post.getTitle());
    assertEquals("", post.getDescription());
    assertEquals("", post.getIngredients());
    assertEquals("", post.getDirections());
    assertEquals("", post.getImage());
    assertEquals("", post.getRating());
    assertEquals("", post.getUploadtime());
    assertEquals("", post.getLikecount());
    assertEquals("", post.getCommentcount());
    assertEquals("", post.getIsLiked());
    }

    @Test
    void testPostWithSpecialCharacters() {
    post.setTitle("Café au Lait & Croissants");
    post.setDescription("A French breakfast with café au lait ☕ and buttery croissants 🥐");
    post.setIngredients("Café beans (organic), milk, butter, flour, eggs");
    post.setDirections("1. Brew coffee ☕ 2. Heat milk 🥛 3. Bake croissants @ 200°C");
    post.setRating("4.9");
    post.setLikecount("1,234");
    post.setCommentcount("87");
    post.setIsLiked("true");

    assertEquals("Café au Lait & Croissants", post.getTitle());
    assertEquals("A French breakfast with café au lait ☕ and buttery croissants 🥐", post.getDescription());
    assertEquals("Café beans (organic), milk, butter, flour, eggs", post.getIngredients());
    assertEquals("1. Brew coffee ☕ 2. Heat milk 🥛 3. Bake croissants @ 200°C", post.getDirections());
    assertEquals("4.9", post.getRating());
    assertEquals("1,234", post.getLikecount());
    assertEquals("87", post.getCommentcount());
    assertEquals("true", post.getIsLiked());
    }
}
