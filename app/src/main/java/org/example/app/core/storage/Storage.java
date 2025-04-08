package org.example.app.core.storage;

import org.example.app.core.block.Block; // Add import for Block

public interface Storage {
    /**
     * Stores a block in the storage.
     *
     * @param block The block to store.
     * @throws Exception if an error occurs during storage.
     */
    void put(Block block) throws Exception;

    /**
     * Retrieves a block by its hash.
     *
     * @param hash The hash of the block to retrieve.
     * @return The block with the given hash, or null if not found.
     * @throws Exception if an error occurs during retrieval.
     */
    Block get(String hash) throws Exception;

    /**
     * Removes a block from the storage.
     *
     * @param hash The hash of the block to remove.
     * @throws Exception if an error occurs during removal.
     */
    void remove(String hash) throws Exception;

    /**
     * Checks if a block exists in the storage.
     *
     * @param hash The hash of the block to check.
     * @return true if the block exists, false otherwise.
     * @throws Exception if an error occurs during the check.
     */
    boolean contains(String hash) throws Exception;

    /**
     * Clears all blocks from the storage.
     *
     * @throws Exception if an error occurs during clearing.
     */
    void clear() throws Exception;

    /**
     * Gets the number of blocks in the storage.
     *
     * @return The number of blocks.
     * @throws Exception if an error occurs while counting blocks.
     */
    int size() throws Exception;
}