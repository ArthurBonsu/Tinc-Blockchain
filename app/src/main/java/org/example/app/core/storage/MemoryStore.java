package org.example.app.core.storage;

import org.example.app.core.block.Block; // Add import for Block
import java.util.HashMap;
import java.util.Map;

public class MemoryStore implements Storage {
    private final Map<String, Block> blockStore;

    /**
     * Creates a new instance of MemoryStore.
     */
    public MemoryStore() {
        this.blockStore = new HashMap<>();
    }

    /**
     * Stores a block in memory.
     *
     * @param block The block to store.
     * @throws Exception if an error occurs during storage.
     */
    @Override
    public void put(Block block) throws Exception {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }

        // Assuming Block has a method to get its hash
        String blockHash = block.getHash();
        blockStore.put(blockHash, block);
    }

    /**
     * Retrieves a block by its hash.
     *
     * @param hash The hash of the block to retrieve.
     * @return The block with the given hash, or null if not found.
     */
    public Block get(String hash) {
        return blockStore.get(hash);
    }

    /**
     * Removes a block from the store.
     *
     * @param hash The hash of the block to remove.
     */
    public void remove(String hash) {
        blockStore.remove(hash);
    }

    /**
     * Checks if a block exists in the store.
     *
     * @param hash The hash of the block to check.
     * @return true if the block exists, false otherwise.
     */
    public boolean contains(String hash) {
        return blockStore.containsKey(hash);
    }

    /**
     * Clears all blocks from the store.
     */
    public void clear() {
        blockStore.clear();
    }

    /**
     * Gets the number of blocks in the store.
     *
     * @return The number of blocks.
     */
    public int size() {
        return blockStore.size();
    }
}