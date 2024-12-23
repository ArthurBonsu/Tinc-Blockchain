package org.tinc.core;

public class MemoryStore implements Storage {

    /**
     * Creates a new instance of MemoryStore.
     */
    public MemoryStore() {
        // Initialize if needed
    }

    /**
     * Stores a block in memory.
     * Currently, this method does not implement any storage logic.
     *
     * @param block The block to store.
     * @throws Exception if an error occurs during storage.
     */
    @Override
    public void put(Block block) throws Exception {
        // No-op for now
    }
}
