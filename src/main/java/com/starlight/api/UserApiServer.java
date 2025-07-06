package com.starlight.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.models.User;
import com.starlight.models.UserDataRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Very small HTTP server exposing basic user related endpoints. It uses
 * {@link XStream} for XML serialization of {@link User} objects.
 */
public class UserApiServer {
    /** Underlying lightweight HTTP server. */
    private final HttpServer server;
    /** Repository used to read and write user data. */
    private final UserDataRepository repository = new UserDataRepository();
    /** XStream instance configured for the user model. */
    private final XStream xstream = new XStream(new DomDriver());

    /**
     * Creates the API server bound to the given port.
     *
     * @param port the port to bind the server to
     */
    public UserApiServer(int port) throws IOException {
        xstream.allowTypesByWildcard(new String[]{"com.starlight.models.*", "java.util.*"});
        xstream.alias("user", User.class);
        xstream.alias("users", List.class);
        repository.ensureDummyData();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/users", new UsersHandler());
    }

    /**
     * Starts the HTTP server.
     */
    public void start() {
        server.start();
    }

    /**
     * Stops the HTTP server.
     */
    public void stop() {
        server.stop(0);
    }

    /** Handler for user login requests. */
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method not allowed. Use POST for login requests.");
                return;
            }
            
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            User creds = (User) xstream.fromXML(body);
            List<User> users = repository.loadUsers();
            
            // Check for either email or username match
            Optional<User> match = users.stream()
                    .filter(u -> ((u.email != null && u.email.equals(creds.email)) || 
                                 (u.username != null && u.username.equals(creds.email))) && 
                                 u.password.equals(creds.password))
                    .findFirst();
                    
            if (match.isPresent()) {
                sendXml(exchange, 200, xstream.toXML(match.get()));
            } else {
                // More descriptive error message for authentication failure
                sendErrorResponse(exchange, 401, "Invalid username/email or password");
            }
        }
        
        /**
         * Sends an error response with the specified status code and message
         */
        private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            String errorXml = "<error><message>" + message + "</message></error>";
            sendXml(exchange, statusCode, errorXml);
        }
    }

    /** Handler for user registration requests. */
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            User newUser = (User) xstream.fromXML(body);
            List<User> users = repository.loadUsers();
            boolean exists = users.stream().anyMatch(u -> newUser.email.equals(u.email));
            if (exists) {
                sendXml(exchange, 409, "<error/>");
                return;
            }
            users.add(newUser);
            repository.saveUsers(users);
            sendXml(exchange, 201, xstream.toXML(newUser));
        }
    }

    /** Handler for user profile retrieval and update. */
    private class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Parse path to extract username
                String path = exchange.getRequestURI().getPath();
                String username = extractUsername(path);
                
                if (username == null) {
                    sendErrorResponse(exchange, 404, "Invalid user path. Expected format: /users/{username}");
                    return;
                }
                
                // Route to appropriate method based on HTTP method
                String method = exchange.getRequestMethod();
                switch (method) {
                    case "GET":
                        handleGetUser(exchange, username);
                        break;
                    case "PUT":
                        handleUpdateUser(exchange, username);
                        break;
                    default:
                        sendErrorResponse(exchange, 405, "Method not allowed. Supported methods: GET, PUT");
                        break;
                }
            } catch (Exception e) {
                // Log the exception
                System.err.println("Error handling user request: " + e.getMessage());
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
        
        /**
         * Extracts username from the path.
         * Valid path format: /users/{username}
         */
        private String extractUsername(String path) {
            if (path == null) return null;
            
            String[] parts = path.split("/");
            // Path should be: "", "users", "{username}"
            if (parts.length != 3) return null;
            
            return parts[2];
        }
        
        /**
         * Handles GET request to retrieve user profile
         */
        private void handleGetUser(HttpExchange exchange, String username) throws IOException {
            List<User> users = repository.loadUsers();
            Optional<User> user = users.stream()
                    .filter(u -> u.username != null && u.username.equals(username))
                    .findFirst();
                    
            if (user.isPresent()) {
                sendXml(exchange, 200, xstream.toXML(user.get()));
            } else {
                sendErrorResponse(exchange, 404, "User not found: " + username);
            }
        }
        
        /**
         * Handles PUT request to update user profile
         */
        private void handleUpdateUser(HttpExchange exchange, String username) throws IOException {
            // Read and parse request body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (body == null || body.isEmpty()) {
                sendErrorResponse(exchange, 400, "Request body is empty");
                return;
            }
            
            // Parse user object
            User updated;
            try {
                updated = (User) xstream.fromXML(body);
            } catch (Exception e) {
                sendErrorResponse(exchange, 400, "Invalid user data format: " + e.getMessage());
                return;
            }
            
            // Find and update user
            List<User> users = repository.loadUsers();
            boolean userFound = false;
            
            for (int i = 0; i < users.size(); i++) {
                User existingUser = users.get(i);
                if (existingUser.username != null && existingUser.username.equals(username)) {
                    // Update user fields if provided
                    if (updated.email != null) existingUser.email = updated.email;
                    if (updated.password != null) existingUser.password = updated.password;
                    if (updated.birthDay != null) existingUser.birthDay = updated.birthDay;
                    
                    // Save updates and send response
                    repository.saveUsers(users);
                    sendXml(exchange, 200, xstream.toXML(existingUser));
                    userFound = true;
                    break;
                }
            }
            
            if (!userFound) {
                sendErrorResponse(exchange, 404, "User not found: " + username);
            }
        }
        
        /**
         * Sends an error response with the specified status code and message
         */
        private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            String errorXml = "<error><message>" + message + "</message></error>";
            sendXml(exchange, statusCode, errorXml);
        }
    }

    /**
     * Utility to send an XML response.
     *
     * @param exchange HTTP exchange
     * @param code HTTP status code
     * @param xml XML payload to send
     */
    private void sendXml(HttpExchange exchange, int code, String xml) throws IOException {
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/xml");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Convenience main method to run the server outside of JavaFX.
     */
    public static void main(String[] args) throws Exception {
        UserApiServer server = new UserApiServer(8000);
        System.out.println("User API server running on port 8000");
        server.start();
    }
}
