package org.example.app.core.block;

import org.example.app.core.types.Hash;

public interface Validator {
    /**
     * Validates a block.
     *
     * @param block The block to validate.
     * @return True if the block is valid, false otherwise.
     * @throws Exception if an unrecoverable error occurs during validation.
     */
    boolean validateBlock(Block block) throws Exception;
}

