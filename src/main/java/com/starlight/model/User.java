package com.starlight.model;

/**
 * Data model representing a user account.
 */
public class User {
    /** Chosen username. */
    private String username;
    /** Email address. */
    private String email;
    /** Full display name. */
    private String fullname;
    /** Password (plain text for simplicity). */
    private String password;
    /** Birthday in ISO format. */
    private String birthDay;
    /** Path to the profile picture. */
    private String profilepicture;

    // Accessors
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getBirthDay() { return birthDay; }
    public void setBirthDay(String birthDay) { this.birthDay = birthDay; }
    public String getProfilepicture() { return profilepicture; }
    public void setProfilepicture(String profilepicture) { this.profilepicture = profilepicture; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
    return java.util.Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
    return java.util.Objects.hash(email);
    }
}
