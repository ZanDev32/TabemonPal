package com.starlight.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.api.ApiClient.ApiError;
import com.starlight.api.ApiClient.ApiException;
import com.starlight.models.User;

/**
 * Unit tests for {@link ApiClient}.
 */
public class ApiClientTest {
    
    private ApiClient apiClient;
    
    @BeforeEach
    void setUp() {
        apiClient = new ApiClient();
    }
    
    @Test
    void testApiError_DefaultConstructor() {
        ApiError error = new ApiError();
        assertEquals("Unknown error", error.getMessage());
    }
    
    @Test
    void testApiError_WithMessage() {
        String message = "Test error message";
        ApiError error = new ApiError(message);
        assertEquals(message, error.getMessage());
    }
    
    @Test
    void testApiError_SetMessage() {
        ApiError error = new ApiError();
        String message = "New error message";
        error.setMessage(message);
        assertEquals(message, error.getMessage());
    }
    
    @Test
    void testApiError_NullMessage() {
        ApiError error = new ApiError();
        error.setMessage(null);
        assertEquals("Unknown error", error.getMessage());
    }
    
    @Test
    void testApiException_Constructor() {
        int statusCode = 400;
        String message = "Bad Request";
        ApiException exception = new ApiException(statusCode, message);
        
        assertEquals(statusCode, exception.getStatusCode());
        assertEquals(message, exception.getMessage());
    }
    
    @Test
    void testApiException_InheritanceFromException() {
        ApiException exception = new ApiException(500, "Internal Server Error");
        assertTrue(exception instanceof Exception);
    }
    
    // Note: Testing the actual HTTP operations would require either:
    // 1. A mock HTTP server for integration tests
    // 2. More sophisticated mocking of URL/HttpURLConnection
    // 3. Refactoring ApiClient to use dependency injection for HTTP client
    
    // For now, we'll test the basic structure and exception handling
    @Test
    void testLoginMethodExists() {
        // Verify that the method exists and can be called
        // This will throw an exception due to network connectivity, but that's expected
        assertThrows(ApiException.class, () -> {
            apiClient.login("test@example.com", "password");
        });
    }
    
    @Test
    void testRegisterMethodExists() {
        // Verify that the method exists and can be called
        // This will throw an exception due to network connectivity, but that's expected
        assertThrows(ApiException.class, () -> {
            apiClient.register("testuser", "test@example.com", "password");
        });
    }
    
    @Test
    void testApiClientConstructor() {
        // Test that ApiClient can be instantiated without issues
        assertDoesNotThrow(() -> {
            ApiClient client = new ApiClient();
            assertNotNull(client);
        });
    }
    
    // Test XML handling preparation
    @Test
    void testUserObjectForSerialization() {
        // Test that User objects can be prepared for XML serialization
        User user = new User();
        user.username = "testuser";
        user.email = "test@example.com";
        user.password = "password";
        
        // This tests that the object creation works without serialization errors
        assertDoesNotThrow(() -> {
            assertNotNull(user.username);
            assertNotNull(user.email);
            assertNotNull(user.password);
        });
    }
    
    @Test
    void testApiExceptionWithVariousStatusCodes() {
        int[] statusCodes = {400, 401, 403, 404, 409, 500, 502, 503};
        String[] messages = {
            "Bad Request", "Unauthorized", "Forbidden", "Not Found",
            "Conflict", "Internal Server Error", "Bad Gateway", "Service Unavailable"
        };
        
        for (int i = 0; i < statusCodes.length; i++) {
            ApiException exception = new ApiException(statusCodes[i], messages[i]);
            assertEquals(statusCodes[i], exception.getStatusCode());
            assertEquals(messages[i], exception.getMessage());
        }
    }
    
    @Test
    void testApiErrorEdgeCases() {
        ApiError error = new ApiError("");
        assertEquals("", error.getMessage());
        
        error = new ApiError("   ");
        assertEquals("   ", error.getMessage());
        
        error = new ApiError("Multi\nline\nerror");
        assertEquals("Multi\nline\nerror", error.getMessage());
    }
    
    // Test that the static inner classes can be instantiated independently
    @Test
    void testStaticInnerClassInstantiation() {
        assertDoesNotThrow(() -> {
            ApiError error = new ApiError("Test");
            assertNotNull(error);
            
            ApiException exception = new ApiException(200, "Success");
            assertNotNull(exception);
        });
    }
    
    @Test
    void testLoginWithNullParameters() {
        // Test behavior with null parameters
        assertThrows(ApiException.class, () -> {
            apiClient.login(null, "password");
        });
        
        assertThrows(ApiException.class, () -> {
            apiClient.login("test@example.com", null);
        });
    }
    
    @Test
    void testRegisterWithNullParameters() {
        // Test behavior with null parameters
        assertThrows(ApiException.class, () -> {
            apiClient.register(null, "test@example.com", "password");
        });
        
        assertThrows(ApiException.class, () -> {
            apiClient.register("testuser", null, "password");
        });
        
        assertThrows(ApiException.class, () -> {
            apiClient.register("testuser", "test@example.com", null);
        });
    }
    
    @Test
    void testApiExceptionStatusCodeZero() {
        // Test the case where network error results in status code 0
        ApiException exception = new ApiException(0, "Network error: Connection refused");
        assertEquals(0, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Network error"));
    }
}
