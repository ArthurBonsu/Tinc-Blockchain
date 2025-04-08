package org.example.app.core.mempool;

import java.math.BigInteger;

/**
 * Simple test for MempoolConfig
 */
public class BaseMempoolTest {

    // Simple test framework variables
    private int totalTests = 0;
    private int passedTests = 0;

    public static void main(String[] args) {
        BaseMempoolTest test = new BaseMempoolTest();
        test.runAllTests();
    }

    public void runAllTests() {
        // Run all test methods
        testMempoolConfig();

        // Print test results
        System.out.println("\n=== Test Results ===");
        System.out.println("Total tests: " + totalTests);
        System.out.println("Passed tests: " + passedTests);
        System.out.println("Failed tests: " + (totalTests - passedTests));
        if (totalTests > 0) {
            System.out.println("Success rate: " + (passedTests * 100 / totalTests) + "%");
        }
    }

    // Simple assertion methods
    private void assertTrue(String message, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("[PASS] " + message);
        } else {
            System.out.println("[FAIL] " + message);
        }
    }

    private void assertEquals(Object expected, Object actual) {
        boolean isEqual;
        if (expected == null) {
            isEqual = (actual == null);
        } else if (expected instanceof Number && actual instanceof Number) {
            // Special handling for numeric values to avoid autoboxing issues
            if (expected instanceof Long && actual instanceof Long) {
                isEqual = ((Long) expected).longValue() == ((Long) actual).longValue();
            } else if (expected instanceof Integer && actual instanceof Integer) {
                isEqual = ((Integer) expected).intValue() == ((Integer) actual).intValue();
            } else {
                // Convert to strings and compare to handle different number types
                isEqual = expected.toString().equals(actual.toString());
            }
        } else {
            isEqual = expected.equals(actual);
        }

        assertTrue("Expected: " + expected + ", Actual: " + actual, isEqual);
    }

    /**
     * Test MempoolConfig builder and getters
     */
    public void testMempoolConfig() {
        System.out.println("\n=== Testing MempoolConfig ===");

        // Test default configuration
        MempoolConfig defaultConfig = new MempoolConfig.Builder().build();
        assertEquals(10000, defaultConfig.getMaxSize());
        assertEquals(BigInteger.valueOf(1000000000), defaultConfig.getMinGasPrice());
        assertEquals(100, defaultConfig.getMaxTransactionsPerAccount());
        assertEquals(3600000, defaultConfig.getTxTimeout());

        // Test custom configuration
        MempoolConfig customConfig = new MempoolConfig.Builder()
                .maxSize(500)
                .minGasPrice(BigInteger.valueOf(2_000_000_000)) // 2 Gwei
                .maxTransactionsPerAccount(50)
                .txTimeout(1800000) // 30 minutes
                .build();

        assertEquals(500, customConfig.getMaxSize());
        assertEquals(BigInteger.valueOf(2_000_000_000), customConfig.getMinGasPrice());
        assertEquals(50, customConfig.getMaxTransactionsPerAccount());
        assertEquals(1800000, customConfig.getTxTimeout());

        System.out.println("All MempoolConfig tests completed");
    }
}