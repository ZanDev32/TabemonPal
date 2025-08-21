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
