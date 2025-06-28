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
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            User creds = (User) xstream.fromXML(body);
            List<User> users = repository.loadUsers();
            Optional<User> match = users.stream()
                    .filter(u -> u.email != null && u.email.equals(creds.email) && u.password.equals(creds.password))
                    .findFirst();
            if (match.isPresent()) {
                sendXml(exchange, 200, xstream.toXML(match.get()));
            } else {
                sendXml(exchange, 401, "<error/>");
            }
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
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 3) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String username = parts[2];
            if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                User updated = (User) xstream.fromXML(body);
                List<User> users = repository.loadUsers();
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).username != null && users.get(i).username.equals(username)) {
                        User u = users.get(i);
                        if (updated.email != null) u.email = updated.email;
                        if (updated.password != null) u.password = updated.password;
                        if (updated.birthDay != null) u.birthDay = updated.birthDay;
                        repository.saveUsers(users);
                        sendXml(exchange, 200, xstream.toXML(u));
                        return;
                    }
                }
                exchange.sendResponseHeaders(404, -1);
            } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<User> users = repository.loadUsers();
                Optional<User> user = users.stream()
                        .filter(u -> u.username != null && u.username.equals(username))
                        .findFirst();
                if (user.isPresent()) {
                    sendXml(exchange, 200, xstream.toXML(user.get()));
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
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
