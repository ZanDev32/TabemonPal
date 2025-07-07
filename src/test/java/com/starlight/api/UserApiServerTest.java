package com.starlight.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserApiServer}.
 */
public class UserApiServerTest {
    
    private UserApiServer server;
    private static final int TEST_PORT = 9000; // Use a different port range to avoid conflicts
    
    @BeforeEach
    void setUp() throws IOException {
        // Create server on a test port with some randomization to avoid conflicts
        int basePort = TEST_PORT + (int)(Math.random() * 1000);
        server = new UserApiServer(basePort);
    }
    
    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }
    
    @Test
    void testServerConstruction() throws IOException {
        // Test that server can be constructed without throwing
        assertDoesNotThrow(() -> {
            int testPort = 9100 + (int)(Math.random() * 500);
            UserApiServer testServer = new UserApiServer(testPort);
            assertNotNull(testServer);
            testServer.stop(); // Clean up
        });
    }
    
    @Test
    void testServerConstructionWithPortInUse() throws IOException {
        // Start first server
        int basePort = 9200 + (int)(Math.random() * 100);
        UserApiServer firstServer = new UserApiServer(basePort);
        firstServer.start();
        
        try {
            // Creating second server on same port should work (it will find next available port)
            assertDoesNotThrow(() -> {
                UserApiServer secondServer = new UserApiServer(basePort);
                assertNotNull(secondServer);
                secondServer.stop();
            });
        } finally {
            firstServer.stop();
        }
    }
    
    @Test
    void testStartServer() {
        assertDoesNotThrow(() -> {
            server.start();
        });
    }
    
    @Test
    void testStopServer() {
        server.start();
        assertDoesNotThrow(() -> {
            server.stop();
        });
    }
    
    @Test
    void testStartStopCycle() {
        // Test start/stop cycle (Note: HttpServer cannot be restarted once stopped)
        assertDoesNotThrow(() -> {
            server.start();
            Thread.sleep(10); // Give server time to start
            server.stop();
            Thread.sleep(10); // Give server time to stop
            // Cannot restart the same HttpServer instance - this is a Java HttpServer limitation
        });
    }
    
    @Test
    void testDoubleStart() {
        // Starting an already started server should not cause issues
        assertDoesNotThrow(() -> {
            server.start();
            server.start(); // Second start should be ignored
        });
    }
    
    @Test
    void testDoubleStop() {
        // Stopping an already stopped server should not cause issues
        server.start();
        assertDoesNotThrow(() -> {
            server.stop();
            server.stop(); // Second stop should be ignored
        });
    }
    
    @Test
    void testStopWithoutStart() {
        // Stopping a server that was never started should not cause issues
        assertDoesNotThrow(() -> {
            server.stop();
        });
    }
    
    @Test
    void testServerState() {
        // Test that server can handle state changes properly
        assertDoesNotThrow(() -> {
            // Initial state - not started
            server.stop(); // Should not throw
            
            // Start server
            server.start();
            Thread.sleep(10); // Give server time to start
            
            // Stop server
            server.stop();
            Thread.sleep(10); // Give server time to stop
            
            // Note: HttpServer cannot be restarted once stopped - this is a Java limitation
            // So we don't test restart here
        });
    }
    
    @Test
    void testRestartWithNewInstance() throws IOException {
        // Test restart functionality by creating new server instances
        // (since HttpServer cannot be restarted once stopped)
        int testPort = 9800 + (int)(Math.random() * 100);
        
        assertDoesNotThrow(() -> {
            // First instance
            UserApiServer server1 = new UserApiServer(testPort);
            server1.start();
            server1.stop();
            
            // Second instance on same port (should work after first is stopped)
            UserApiServer server2 = new UserApiServer(testPort);
            server2.start();
            server2.stop();
        });
    }
    
    @Test
    void testMultipleServerInstances() throws IOException {
        // Test that multiple server instances can coexist (on different ports)
        int port1 = 9300 + (int)(Math.random() * 50);
        int port2 = 9350 + (int)(Math.random() * 50);
        UserApiServer server1 = new UserApiServer(port1);
        UserApiServer server2 = new UserApiServer(port2);
        
        try {
            assertDoesNotThrow(() -> {
                server1.start();
                server2.start();
            });
            
            assertDoesNotThrow(() -> {
                server1.stop();
                server2.stop();
            });
        } finally {
            server1.stop();
            server2.stop();
        }
    }
    
    @Test
    void testServerConstructionPortRange() {
        // Test that server handles port binding failures gracefully
        // by trying multiple ports (this tests the port binding retry logic)
        assertDoesNotThrow(() -> {
            // This should work even if some ports are in use
            int testPort = 9400 + (int)(Math.random() * 100);
            UserApiServer testServer = new UserApiServer(testPort);
            testServer.stop();
        });
    }
    
    @Test
    void testServerResourceCleanup() throws IOException {
        // Test that server properly cleans up resources
        int testPort = 9500 + (int)(Math.random() * 100);
        UserApiServer testServer = new UserApiServer(testPort);
        
        testServer.start();
        testServer.stop();
        
        // Should be able to create another server on same port after proper cleanup
        assertDoesNotThrow(() -> {
            UserApiServer secondServer = new UserApiServer(testPort);
            secondServer.stop();
        });
    }
    
    @Test
    void testServerStartAfterException() throws IOException {
        // Test that server can be started after handling construction exceptions
        try {
            // This should succeed since we handle port conflicts
            int testPort = 9600 + (int)(Math.random() * 100);
            UserApiServer testServer = new UserApiServer(testPort);
            testServer.start();
            testServer.stop();
        } catch (Exception e) {
            fail("Server construction should handle port conflicts gracefully");
        }
    }
    
    // Note: Testing the actual HTTP endpoints would require integration tests
    // with actual HTTP requests. For unit tests, we focus on the lifecycle
    // and basic functionality of the server.
    
    @Test
    void testServerLifecycle() {
        // Test complete server lifecycle
        assertDoesNotThrow(() -> {
            // Construction phase
            int testPort = 9700 + (int)(Math.random() * 100);
            UserApiServer lifecycleServer = new UserApiServer(testPort);
            
            // Startup phase
            lifecycleServer.start();
            
            // Runtime phase (server is running)
            // In a real scenario, this is where HTTP requests would be handled
            
            // Shutdown phase
            lifecycleServer.stop();
        });
    }
}
