package com.starlight.models;

/**
 * Data model representing a user account.
 */
public class User {
    /** Chosen username. */
    public String username;
    /** Email address. */
    public String email;
    /** Full display name. */
    public String fullname;
    /** Password (plain text for simplicity). */
    public String password;
    /** Birthday in ISO format. */
    public String birthDay;
    /** Path to the profile picture. */
    public String profilepicture;
}
