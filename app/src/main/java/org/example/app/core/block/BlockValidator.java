package org.example.app.core.block;

import org.example.app.core.types.Hash;
import org.example.app.core.consensus.Blockchain; // Correct import for Blockchain

public class BlockValidator implements Validator {
    private final Blockchain blockchain;

    public BlockValidator(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public boolean validateBlock(Block block) throws Exception {
        try {
            // Check if the block's height already exists in the blockchain
            int blockHeight = block.getHeader().getHeight();
            if (blockchain.hasBlock(blockHeight)) {
                throw new IllegalStateException("Block already exists at height: " + blockHeight);
            }

            // Ensure the block's height is exactly one more than the current blockchain height
            if (blockHeight != blockchain.getHeight() + 1) {
                throw new IllegalStateException(
                        String.format(
                                "Invalid block height. Expected: %d, Found: %d",
                                blockchain.getHeight() + 1,
                                blockHeight
                        )
                );
            }

            // Retrieve and validate the previous block hash
            Hash expectedPrevHash = blockchain.getHeader(blockHeight - 1).getPrevBlockHash();
            if (!expectedPrevHash.equals(block.getHeader().getPrevBlockHash())) {
                throw new IllegalStateException(
                        String.format(
                                "Previous block hash mismatch. Expected: %s, Found: %s",
                                expectedPrevHash,
                                block.getHeader().getPrevBlockHash()
                        )
                );
            }

            // Verify the block's content
            block.verify();

            // Block is valid
            return true;
        } catch (Exception e) {
            System.err.println("Block validation failed: " + e.getMessage());
            return false;
        }
    }
}