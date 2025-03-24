package org.example.app.core.types;

import java.util.ArrayList;
import java.util.Objects;

public class List<T> {
    private final ArrayList<T> data;

    public List() {
        this.data = new ArrayList<>();
    }

    public T get(int index) {
        if (index >= data.size()) {
            throw new IndexOutOfBoundsException(
                    "The given index (" + index + ") is higher than the length (" + data.size() + ")"
            );
        }
        return data.get(index);
    }

    public void insert(T value) {
        data.add(value);
    }

    public void clear() {
        data.clear();
    }

    // Returns the index of the given value. If not found, returns -1.
    public int getIndex(T value) {
        for (int i = 0; i < data.size(); i++) {
            if (Objects.equals(data.get(i), value)) {
                return i;
            }
        }
        return -1;
    }

    public void remove(T value) {
        int index = getIndex(value);
        if (index != -1) {
            pop(index);
        }
    }

    public void pop(int index) {
        if (index < 0 || index >= data.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        data.remove(index);
    }

    public boolean contains(T value) {
        for (T item : data) {
            if (Objects.equals(item, value)) {
                return true;
            }
        }
        return false;
    }

    public T last() {
        if (data.isEmpty()) {
            throw new IllegalStateException("List is empty.");
        }
        return data.get(data.size() - 1);
    }

    public int len() {
        return data.size();
    }

    // Optional getter for internal data (useful for debugging or external purposes)
    public ArrayList<T> getData() {
        return new ArrayList<>(data);
    }
}
