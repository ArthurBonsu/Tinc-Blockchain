package org.tinc.smartcontract;

import java.util.Deque;
import java.util.LinkedList;

public class Stack<T> {
    private final Deque<T> stack = new LinkedList<>();

    public void push(T value) {
        stack.push(value);
    }

    public T pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack underflow: No elements to pop.");
        }
        return stack.pop();
    }

    public T peek() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack is empty: No elements to peek.");
        }
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}