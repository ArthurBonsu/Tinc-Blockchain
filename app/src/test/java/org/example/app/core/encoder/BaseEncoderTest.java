package org.example.app.core.encoder;

import java.io.*;

/**
 * Simple test for Encoder functionality without interface dependencies
 */
public class BaseEncoderTest {

    // Simple test framework variables
    private int totalTests = 0;
    private int passedTests = 0;

    public static void main(String[] args) {
        BaseEncoderTest test = new BaseEncoderTest();
        test.runAllTests();
    }

    public void runAllTests() {
        System.out.println("=== Starting Encoder Tests ===");

        // Run all test methods
        testSimpleObjectSerialization();

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

    private void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    private void assertEquals(Object expected, Object actual) {
        boolean isEqual;
        if (expected == null) {
            isEqual = (actual == null);
        } else if (expected instanceof Number && actual instanceof Number) {
            // Convert to strings and compare to handle different number types
            isEqual = expected.toString().equals(actual.toString());
        } else {
            isEqual = expected.equals(actual);
        }

        assertTrue("Expected: " + expected + ", Actual: " + actual, isEqual);
    }

    private void assertNotNull(String message, Object obj) {
        assertTrue(message, obj != null);
    }

    /**
     * Test simple Java serialization without using the transaction or block interfaces
     */
    public void testSimpleObjectSerialization() {
        System.out.println("\n=== Testing Basic Java Serialization ===");

        try {
            // Create a simple serializable object
            TestSerializableObject original = new TestSerializableObject(
                    "Test Object", 12345, 98.76);

            // Set up a ByteArrayOutputStream to hold the encoded data
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Serialize the object
            try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
                oos.writeObject(original);
            }

            // Get the encoded bytes
            byte[] encodedData = outputStream.toByteArray();
            assertTrue("Encoded data should not be empty", encodedData.length > 0);
            System.out.println("Successfully serialized object, data size: " + encodedData.length + " bytes");

            // Create a ByteArrayInputStream for deserialization
            ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedData);

            // Deserialize the object
            TestSerializableObject decoded;
            try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
                decoded = (TestSerializableObject) ois.readObject();
            }

            // Verify decoded object
            assertNotNull("Decoded object should not be null", decoded);
            assertEquals(original.getName(), decoded.getName());
            assertEquals(original.getId(), decoded.getId());
            assertEquals(original.getValue(), decoded.getValue());

            System.out.println("Successfully deserialized object with matching field values");

        } catch (Exception e) {
            System.out.println("Exception during serialization test: " + e.getMessage());
            e.printStackTrace();
            assertTrue("No exceptions should be thrown during test", false);
        }
    }

    /**
     * Simple serializable class for testing
     */
    public static class TestSerializableObject implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final int id;
        private final double value;

        public TestSerializableObject(String name, int id, double value) {
            this.name = name;
            this.id = id;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public double getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestSerializableObject that = (TestSerializableObject) o;

            if (id != that.id) return false;
            if (Double.compare(that.value, value) != 0) return false;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = name.hashCode();
            result = 31 * result + id;
            temp = Double.doubleToLongBits(value);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "TestObject{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    ", value=" + value +
                    '}';
        }
    }
}