package com.starlight.model;

/**
 * Simple data model representing a community post.
 */
public class Post {
    /** Unique identifier. */
    private String uuid;
    /** Username of the creator. */
    private String username;
    /** Path to the profile picture. */
    private String profilepicture;
    /** Title of the post. */
    private String title;
    /** Description of the recipe/post. */
    private String description;
    /** Ingredients text. */
    private String ingredients;
    /** Preparation directions. */
    private String directions;
    /** Path to the image of the dish. */
    private String image;
    /** Average rating. */
    private String rating;
    /** Upload timestamp. */
    private String uploadtime;
    /** Like count. */
    private String likecount;
    /** Comment count. */
    private String commentcount;
    /** Whether the current user has liked this post. */
    private String isLiked;
    /** Nutrition facts data from AI analysis. */
    private Nutrition nutrition;

    // Accessors - keep these to allow callers to use getters while preserving
    // the existing public fields for backwards compatibility.
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfilepicture() { return profilepicture; }
    public void setProfilepicture(String profilepicture) { this.profilepicture = profilepicture; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getDirections() { return directions; }
    public void setDirections(String directions) { this.directions = directions; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getUploadtime() { return uploadtime; }
    public void setUploadtime(String uploadtime) { this.uploadtime = uploadtime; }

    public String getLikecount() { return likecount; }
    public void setLikecount(String likecount) { this.likecount = likecount; }

    public String getCommentcount() { return commentcount; }
    public void setCommentcount(String commentcount) { this.commentcount = commentcount; }

    public String getIsLiked() { return isLiked; }
    public void setIsLiked(String isLiked) { this.isLiked = isLiked; }

    public Nutrition getNutrition() { return nutrition; }
    public void setNutrition(Nutrition nutrition) { this.nutrition = nutrition; }
}
