package com.starlight.api;

import com.starlight.api.ApiClient.ApiException;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the ApiClient.
 * These tests are designed to run without a live server.
 */
class ApiClientTest {

    private ApiClient apiClient;
    private static final String INVALID_URL = "http://localhost:9999";

    @BeforeEach
    void setUp() {
        apiClient = new ApiClient(INVALID_URL); // Point to a non-existent server
    }

    /**
     * Verifies that the ApiClient can be instantiated.
     */
    @Test
    void testApiClientInstantiation() {
        assertNotNull(apiClient, "ApiClient should be instantiated");
    }

    /**
     * Tests that the login method throws an ApiException when the server is unreachable.
     * This confirms that the network error handling is working correctly.
     */
    @Test
    void testLoginThrowsExceptionOnConnectionError() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiClient.login("test@example.com", "password");
        }, "Login should throw ApiException when server is down");

        assertTrue(exception.getMessage().contains("Network error"), "Exception message should indicate a network error");
    }

    /**
     * Tests that the register method throws an ApiException when the server is unreachable.
     */
    @Test
    void testRegisterThrowsExceptionOnConnectionError() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiClient.register("testuser", "test@example.com", "password");
        }, "Register should throw ApiException when server is down");

        assertTrue(exception.getMessage().contains("Network error"), "Exception message should indicate a network error");
    }

    /**
     * Verifies that the ApiException class behaves as expected.
     */
    @Test
    void testApiException() {
        ApiException ex = new ApiException(404, "Not Found");
        assertEquals(404, ex.getStatusCode());
        assertEquals("Not Found", ex.getMessage());
    }

    /**
     * Verifies that the ApiError class behaves as expected.
     */
    @Test
    void testApiError() {
        ApiClient.ApiError error = new ApiClient.ApiError("Something went wrong");
        assertEquals("Something went wrong", error.getMessage());

        ApiClient.ApiError nullError = new ApiClient.ApiError(null);
        assertEquals("Unknown error", nullError.getMessage());
    }

    /**
     * Checks if the UserApiServer can be started on an available port.
     * This is a basic integration test to ensure the server component is not broken.
     */
    @Test
    void testUserApiServerCanBeStarted() {
        UserApiServer server = null;
        try {
            // Find an available port
            int port = findAvailablePort();
            server = new UserApiServer(port);
            server.start();
            assertTrue(server.isServerRunning(), "Server should be running after start()");
        } catch (IOException e) {
            fail("Failed to find an available port for the server test.", e);
        } finally {
            if (server != null) {
                server.stop();
                assertFalse(server.isServerRunning(), "Server should be stopped after stop()");
            }
        }
    }

    /**
     * Helper method to find a free TCP port.
     */
    private int findAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    // The following original tests are commented out as they are not true unit tests
    // and depend on a running server, which is not ideal for a typical build pipeline.
    // The tests above provide better coverage for the intended scenarios (error handling).

    /*
    @Test
    void testLoginMethodExists() {
        // This test is replaced by testLoginThrowsExceptionOnConnectionError
        assertThrows(ApiException.class, () -> {
            apiClient.login("test@example.com", "password");
        });
    }
    
    @Test
    void testRegisterMethodExists() {
        // This test is replaced by testRegisterThrowsExceptionOnConnectionError
        assertThrows(ApiException.class, () -> {
            apiClient.register("testuser", "test@example.com", "password");
        });
    }
    */
}
