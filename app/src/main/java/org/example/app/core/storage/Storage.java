package org.example.app.core.storage;


public interface Storage {
    /**
     * Stores a block in the storage.
     *
     * @param block The block to store.
     * @throws Exception if an error occurs during storage.
     */
    void put(Block block) throws Exception;
}

