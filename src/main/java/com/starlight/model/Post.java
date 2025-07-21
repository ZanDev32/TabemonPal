package com.starlight.model;

/**
 * Simple data model representing a community post.
 */
public class Post {
    /** Unique identifier. */
    public String uuid;
    /** Username of the creator. */
    public String username;
    /** Path to the profile picture. */
    public String profilepicture;
    /** Title of the post. */
    public String title;
    /** Description of the recipe/post. */
    public String description;
    /** Ingredients text. */
    public String ingredients;
    /** Preparation directions. */
    public String directions;
    /** Path to the image of the dish. */
    public String image;
    /** Average rating. */
    public String rating;
    /** Upload timestamp. */
    public String uploadtime;
    /** Like count. */
    public String likecount;
    /** Comment count. */
    public String commentcount;
    /** Whether the current user has liked this post. */
    public String isLiked;
    /** Nutrition facts data from AI analysis. */
    public Nutrition nutrition;
}
