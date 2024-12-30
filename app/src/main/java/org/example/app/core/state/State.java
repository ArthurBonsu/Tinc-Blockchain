package org.example.app.core.state;


import java.util.HashMap;
import java.util.Map;

public class State {

    private final Map<String, byte[]> data;

    public State() {
        this.data = new HashMap<>();
    }

    /**
     * Inserts or updates a key-value pair in the state.
     *
     * @param key The key as a byte array.
     * @param value The value as a byte array.
     */
    public void put(byte[] key, byte[] value) {
        data.put(new String(key), value);
    }

    /**
     * Deletes a key-value pair from the state.
     *
     * @param key The key as a byte array.
     */
    public void delete(byte[] key) {
        data.remove(new String(key));
    }

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key The key as a byte array.
     * @return The value as a byte array, or null if the key does not exist.
     * @throws IllegalStateException if the key is not found.
     */
    public byte[] get(byte[] key) {
        String keyStr = new String(key);

        if (!data.containsKey(keyStr)) {
            throw new IllegalStateException("Key not found: " + keyStr);
        }

        return data.get(keyStr);
    }
}
