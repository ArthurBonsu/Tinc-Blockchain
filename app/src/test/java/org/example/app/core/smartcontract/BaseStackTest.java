package org.example.app.core.smartcontract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Logger;

public class BaseStackTest {
    private static final Logger LOGGER = Logger.getLogger(BaseStackTest.class.getName());
    
    private Stack<Integer> stack;

    @BeforeEach
    public void setUp() {
        stack = new Stack<>();
        LOGGER.info("Initialized new stack for testing");
    }

    @Test
    public void testPushAndPop() {
        LOGGER.info("Starting testPushAndPop");
        
        // Push multiple elements
        stack.push(10);
        stack.push(20);
        stack.push(30);
        
        // Verify top elements are popped in correct order (LIFO)
        assertEquals(30, stack.pop(), "Top element should be 30");
        assertEquals(20, stack.pop(), "Next element should be 20");
        assertEquals(10, stack.pop(), "Bottom element should be 10");
        
        assertTrue(stack.isEmpty(), "Stack should be empty after all elements are popped");
        
        LOGGER.info("Push and pop test completed successfully");
    }

    @Test
    public void testPeek() {
        LOGGER.info("Starting testPeek");
        
        stack.push(42);
        
        // Peek should return the top element without removing it
        assertEquals(42, stack.peek(), "Peek should return the top element");
        assertFalse(stack.isEmpty(), "Stack should not be empty after peek");
        
        // Peek again should return the same element
        assertEquals(42, stack.peek(), "Peek should return the same element multiple times");
        
        LOGGER.info("Peek test completed successfully");
    }

    @Test
    public void testStackUnderflow() {
        LOGGER.info("Starting testStackUnderflow");
        
        // Attempting to pop from an empty stack should throw exception
        assertTrue(stack.isEmpty(), "Initial stack should be empty");
        
        assertThrows(IllegalStateException.class, () -> stack.pop(), 
            "Popping from an empty stack should throw IllegalStateException");
        
        assertThrows(IllegalStateException.class, () -> stack.peek(), 
            "Peeking an empty stack should throw IllegalStateException");
        
        LOGGER.info("Stack underflow test completed successfully");
    }

    @Test
    public void testMultipleDataTypes() {
        LOGGER.info("Starting testMultipleDataTypes");
        
        // Test stack with different data types
        Stack<String> stringStack = new Stack<>();
        stringStack.push("Hello");
        stringStack.push("World");
        
        assertEquals("World", stringStack.pop(), "String stack should work correctly");
        assertEquals("Hello", stringStack.pop(), "String stack should work correctly");
        
        Stack<Double> doubleStack = new Stack<>();
        doubleStack.push(3.14);
        doubleStack.push(2.718);
        
        assertEquals(2.718, doubleStack.pop(), 0.001, "Double stack should work correctly");
        assertEquals(3.14, doubleStack.pop(), 0.001, "Double stack should work correctly");
        
        LOGGER.info("Multiple data types test completed successfully");
    }

    @Test
    public void testStackLimit() {
        LOGGER.info("Starting testStackLimit");
        
        // Simulate a large number of push operations
        for (int i = 0; i < 1000; i++) {
            stack.push(i);
        }
        
        // Verify the last pushed element
        assertEquals(999, stack.peek(), "Last pushed element should be 999");
        
        // Pop all elements
        for (int i = 999; i >= 0; i--) {
            assertEquals(i, stack.pop(), "Elements should be popped in reverse order");
        }
        
        assertTrue(stack.isEmpty(), "Stack should be empty after popping all elements");
        
        LOGGER.info("Stack limit and order test completed successfully");
    }
}