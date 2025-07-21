package com.starlight.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.starlight.model.User;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Client for interacting with the User API Server.
 * Handles XML serialization/deserialization and error handling.
 */
public class ApiClient {
    private static final String BASE_URL = "http://localhost:8000";
    private final XStream xstream;
    
    public ApiClient() {
        this.xstream = new XStream(new DomDriver());
        this.xstream.allowTypesByWildcard(new String[]{"com.starlight.model.*"});
        this.xstream.alias("user", User.class);
        this.xstream.alias("error", ApiError.class);
    }
    
    /**
     * Attempts to login a user
     * @return User object if successful
     * @throws ApiException if login fails
     */
    public User login(String emailOrUsername, String password) throws ApiException {
        try {
            URL url = new URL(BASE_URL + "/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setDoOutput(true);

            User creds = new User();
            creds.email = emailOrUsername;
            creds.password = password;

            String xml = xstream.toXML(creds);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(xml.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream()) {
                    return (User) xstream.fromXML(is);
                }
            } else {
                // Parse error response
                ApiError error = parseErrorResponse(conn);
                throw new ApiException(responseCode, error.getMessage());
            }
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        }
    }
    
    /**
     * Registers a new user
     * @return User object if successful
     * @throws ApiException if registration fails
     */
    public User register(String username, String email, String password) throws ApiException {
        try {
            URL url = new URL(BASE_URL + "/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setDoOutput(true);
            
            User newUser = new User();
            newUser.username = username;
            newUser.email = email;
            newUser.password = password;
            
            String xml = xstream.toXML(newUser);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(xml.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                try (InputStream is = conn.getInputStream()) {
                    return (User) xstream.fromXML(is);
                }
            } else {
                // Parse error response
                ApiError error = parseErrorResponse(conn);
                if (responseCode == 409) {
                    throw new ApiException(responseCode, "Account already exists with this email");
                } else {
                    throw new ApiException(responseCode, error.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        }
    }
    
    /**
     * Parses an error response from the server
     */
    private ApiError parseErrorResponse(HttpURLConnection conn) {
        try (InputStream errorStream = conn.getErrorStream()) {
            if (errorStream != null) {
                try {
                    // Try to parse the XML error response
                    return (ApiError) xstream.fromXML(errorStream);
                } catch (Exception e) {
                    // If XML parsing fails, return a more descriptive message based on status code
                    return createContextualErrorMessage(conn.getResponseCode());
                }
            } else {
                return createContextualErrorMessage(conn.getResponseCode());
            }
        } catch (IOException e) {
            return new ApiError("Failed to read error response: " + e.getMessage());
        }
    }
    
    /**
     * Creates a contextual error message based on HTTP status code
     */
    private ApiError createContextualErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return new ApiError("Bad request: The server could not understand your request");
            case 401:
                return new ApiError("Authentication failed: Invalid username/email or password");
            case 403:
                return new ApiError("Access denied: You don't have permission to access this resource");
            case 404:
                return new ApiError("Resource not found: The requested item doesn't exist");
            case 409:
                return new ApiError("Conflict: An account with this email already exists");
            case 500:
                return new ApiError("Server error: Something went wrong on our end");
            default:
                return new ApiError("Error code: " + statusCode);
        }
    }
    
    /**
     * Represents an API error response
     */
    public static class ApiError {
        private String message;
        
        public ApiError() {}
        
        public ApiError(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message != null ? message : "Unknown error";
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    /**
     * Exception thrown when an API request fails
     */
    public static class ApiException extends Exception {
        private final int statusCode;
        
        public ApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
}