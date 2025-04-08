package org.example.app.core.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comprehensive test file for testing components in the internal package.
 */
public class BaseInternalTest {

    // Test objects
    private CustomLogger logger;
    private EventLog eventLog;
    private Metrics metrics;
    private MetricsClient metricsClient;
    private NodeDiscovery nodeDiscovery;
    private NonceManager nonceManager;

    @BeforeEach
    void setUp() {
        // Initialize test objects
        logger = CustomLogger.getLogger(BaseInternalTest.class);
        eventLog = new EventLog();
        metrics = new Metrics();
        metricsClient = new MetricsClient(metrics);
        nodeDiscovery = new NodeDiscovery();
        nonceManager = new NonceManager();

        // Log test setup
        System.out.println("Setting up test...");
    }

    @Test
    void testCPUProfiler() {
        System.out.println("\n----- Testing CPUProfiler -----");

        // Since these methods rely on system resources, we just verify they don't throw exceptions
        double systemLoad = CPUProfiler.getCurrentCpuLoad();
        double processLoad = CPUProfiler.getProcessCpuLoad();

        System.out.println("System CPU Load: " + systemLoad + "%");
        System.out.println("Process CPU Load: " + processLoad + "%");

        // Note: We can't assert exact values as they depend on the system state
        assertTrue(systemLoad >= 0.0 && systemLoad <= 100.0, "System CPU load should be between 0 and 100");
        assertTrue(processLoad >= 0.0 && processLoad <= 100.0, "Process CPU load should be between 0 and 100");

        System.out.println("CPUProfiler Test Result: PASSED");
    }

    @Test
    void testCustomLogger() {
        System.out.println("\n----- Testing CustomLogger -----");

        // Test different log levels
        logger.info("This is an info message");
        logger.debug("This is a debug message");
        logger.warn("This is a warning message");
        logger.error("This is an error message");

        // Test error with throwable
        Exception testException = new RuntimeException("Test exception");
        logger.error("Error with exception", testException);

        // Since logger outputs to console, we just ensure no exceptions are thrown
        System.out.println("CustomLogger Test Result: PASSED");
    }

    @Test
    void testEventLog() {
        System.out.println("\n----- Testing EventLog -----");

        // Test logging events
        eventLog.logEvent("Event 1");
        eventLog.logEvent("Event 2");
        eventLog.logEvent("Event 3");

        // Verify logs
        List<String> logs = eventLog.getLogs();
        assertEquals(3, logs.size(), "Should have 3 logged events");
        assertEquals("Event 1", logs.get(0), "First log should be 'Event 1'");
        assertEquals("Event 2", logs.get(1), "Second log should be 'Event 2'");
        assertEquals("Event 3", logs.get(2), "Third log should be 'Event 3'");

        System.out.println("Logged events: " + logs);
        System.out.println("EventLog Test Result: PASSED");
    }

    @Test
    void testMetrics() {
        System.out.println("\n----- Testing Metrics -----");

        // Test adding metrics
        metrics.addMetric("requests", 100);
        metrics.addMetric("errors", 5);
        metrics.addMetric("latency", 250);

        // Test retrieving individual metrics
        assertEquals(100, metrics.getMetric("requests"), "Requests metric should be 100");
        assertEquals(5, metrics.getMetric("errors"), "Errors metric should be 5");
        assertEquals(250, metrics.getMetric("latency"), "Latency metric should be 250");

        // Test default value for non-existent metric
        assertEquals(0, metrics.getMetric("nonexistent"), "Non-existent metric should return 0");

        // Test retrieving all metrics
        Map<String, Long> allMetrics = metrics.getAllMetrics();
        assertEquals(3, allMetrics.size(), "Should have 3 metrics");
        assertTrue(allMetrics.containsKey("requests"), "Metrics should contain 'requests'");
        assertTrue(allMetrics.containsKey("errors"), "Metrics should contain 'errors'");
        assertTrue(allMetrics.containsKey("latency"), "Metrics should contain 'latency'");

        System.out.println("All metrics: " + allMetrics);
        System.out.println("Metrics Test Result: PASSED");
    }

    @Test
    void testMetricsClient() {
        System.out.println("\n----- Testing MetricsClient -----");

        // Add some metrics first
        metrics.addMetric("requests", 200);
        metrics.addMetric("errors", 10);

        // Test sending metrics (should print to console)
        System.out.println("Attempting to send metrics...");
        metricsClient.sendMetrics();

        System.out.println("MetricsClient Test Result: PASSED");
    }

    @Test
    void testNodeDiscovery() {
        System.out.println("\n----- Testing NodeDiscovery -----");

        // Test discovering nodes
        nodeDiscovery.discoverNode("node1.example.com");
        nodeDiscovery.discoverNode("node2.example.com");
        nodeDiscovery.discoverNode("node3.example.com");

        // Test duplicate node (should be ignored in a Set)
        nodeDiscovery.discoverNode("node1.example.com");

        // Verify discovered nodes
        Set<String> nodes = nodeDiscovery.getDiscoveredNodes();
        assertEquals(3, nodes.size(), "Should have 3 discovered nodes");
        assertTrue(nodes.contains("node1.example.com"), "Should contain node1");
        assertTrue(nodes.contains("node2.example.com"), "Should contain node2");
        assertTrue(nodes.contains("node3.example.com"), "Should contain node3");

        // Test isNodeDiscovered method
        assertTrue(nodeDiscovery.isNodeDiscovered("node1.example.com"), "Should recognize node1 as discovered");
        assertFalse(nodeDiscovery.isNodeDiscovered("unknown.example.com"), "Should not recognize unknown node");

        System.out.println("Discovered nodes: " + nodes);
        System.out.println("NodeDiscovery Test Result: PASSED");
    }

    @Test
    void testNonceManager() {
        System.out.println("\n----- Testing NonceManager -----");

        String account1 = "0xabc123";
        String account2 = "0xdef456";

        // Test initial nonce values
        assertEquals(0, nonceManager.getNonce(account1), "Initial nonce should be 0");
        assertEquals(0, nonceManager.getNonce(account2), "Initial nonce should be 0");

        System.out.println("Initial nonce for " + account1 + ": " + nonceManager.getNonce(account1));

        // Test incrementing nonces
        nonceManager.incrementNonce(account1);
        nonceManager.incrementNonce(account1);
        nonceManager.incrementNonce(account2);

        assertEquals(2, nonceManager.getNonce(account1), "Nonce should be 2 after two increments");
        assertEquals(1, nonceManager.getNonce(account2), "Nonce should be 1 after one increment");

        System.out.println("Nonce for " + account1 + " after 2 increments: " + nonceManager.getNonce(account1));
        System.out.println("Nonce for " + account2 + " after 1 increment: " + nonceManager.getNonce(account2));

        // Test setting nonce directly
        nonceManager.setNonce(account1, 10);
        assertEquals(10, nonceManager.getNonce(account1), "Nonce should be 10 after setting");

        System.out.println("Nonce for " + account1 + " after setting to 10: " + nonceManager.getNonce(account1));
        System.out.println("NonceManager Test Result: PASSED");
    }

    @Test
    void testUtils() {
        System.out.println("\n----- Testing Utils -----");

        try {
            // Test SHA-256 hash function
            String input = "test string";
            String expectedHash = "d5579c46dfcc7f18207013e65b44e4cb4e2c2298f4ac457ba8f82743f31e930b";

            String actualHash = Utils.sha256(input);
            assertEquals(expectedHash, actualHash, "SHA-256 hash should match expected value");

            System.out.println("Input: '" + input + "'");
            System.out.println("SHA-256 hash: " + actualHash);

            // Test different input
            String input2 = "another test";
            String expectedHash2 = "64320dd12e5c2caeac673b91454dac750c08ba333639d129671c2f58cb5d0ad1";

            String actualHash2 = Utils.sha256(input2);
            assertEquals(expectedHash2, actualHash2, "SHA-256 hash should match expected value");

            System.out.println("Input: '" + input2 + "'");
            System.out.println("SHA-256 hash: " + actualHash2);
            System.out.println("Utils Test Result: PASSED");
        } catch (NoSuchAlgorithmException e) {
            fail("Exception thrown during SHA-256 calculation: " + e.getMessage());
        }
    }

    @Test
    void testIntegration() {
        System.out.println("\n----- Testing Integration of Components -----");

        // Log events and metrics
        eventLog.logEvent("Application started");
        metrics.addMetric("startup_time", 1500);

        System.out.println("Logged event: Application started");
        System.out.println("Added metric: startup_time = 1500ms");

        // Discover some nodes
        nodeDiscovery.discoverNode("node1.example.com");
        nodeDiscovery.discoverNode("node2.example.com");
        metrics.addMetric("discovered_nodes", nodeDiscovery.getDiscoveredNodes().size());

        System.out.println("Discovered nodes: " + nodeDiscovery.getDiscoveredNodes());
        System.out.println("Added metric: discovered_nodes = " + nodeDiscovery.getDiscoveredNodes().size());

        // Log the discovery
        eventLog.logEvent("Discovered " + nodeDiscovery.getDiscoveredNodes().size() + " nodes");
        System.out.println("Logged event: Discovered " + nodeDiscovery.getDiscoveredNodes().size() + " nodes");

        // Manage nonces for accounts
        String account = "0xabc123";
        nonceManager.incrementNonce(account);
        metrics.addMetric("current_nonce", nonceManager.getNonce(account));

        System.out.println("Incremented nonce for " + account + ": " + nonceManager.getNonce(account));
        System.out.println("Added metric: current_nonce = " + nonceManager.getNonce(account));

        // Send metrics
        System.out.println("Sending metrics...");
        metricsClient.sendMetrics();

        // Log completion
        eventLog.logEvent("Test completed");
        System.out.println("Logged event: Test completed");

        // Verify logs
        List<String> logs = eventLog.getLogs();
        assertEquals(3, logs.size(), "Should have 3 logged events");
        assertEquals("Application started", logs.get(0));
        assertEquals("Discovered 2 nodes", logs.get(1));
        assertEquals("Test completed", logs.get(2));

        // Verify metrics
        Map<String, Long> allMetrics = metrics.getAllMetrics();
        assertEquals(3, allMetrics.size(), "Should have 3 metrics");
        assertEquals(1500, metrics.getMetric("startup_time"));
        assertEquals(2, metrics.getMetric("discovered_nodes"));
        assertEquals(1, metrics.getMetric("current_nonce"));

        System.out.println("All logs: " + logs);
        System.out.println("All metrics: " + allMetrics);
        System.out.println("Integration Test Result: PASSED");
    }

}