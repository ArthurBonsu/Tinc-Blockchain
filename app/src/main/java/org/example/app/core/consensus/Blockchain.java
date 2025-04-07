package org.example.app.core.consensus;

import org.example.app.core.block.Block;
import java.util.List;

public class Blockchain {
    private List<Block> blocks;
    private Consensus consensus;

    public Blockchain() {
        this.blocks = new java.util.ArrayList<>();
        this.consensus = new Consensus();
    }

    // Add a new block to the blockchain after consensus validation
    public boolean addBlock(Block block) {
        if (consensus.validateBlock(block)) {
            blocks.add(block);
            return true;
        }
        return false;
    }

    // Get block by its hash
    public Block getBlockByHash(String blockHash) {
        for (Block block : blocks) {
            if (block.getHash().equals(blockHash)) {
                return block;
            }
        }
        return null;
    }

    // Retrieve the most recent block
    public Block getLatestBlock() {
        if (blocks.isEmpty()) {
            return null;
        }
        return blocks.get(blocks.size() - 1);
    }

    /**
     * Returns the current length/height of the blockchain
     * @return int representing the number of blocks in the chain
     */
    public int getChainLength() {
        // Simply return the size of the blocks collection
        return blocks.size();
    }

    /**
     * Returns the total number of blocks in the blockchain.
     * This is an alias for getChainLength() to maintain compatibility with tests.
     * @return the number of blocks
     */
    public int getBlockCount() {
        return getChainLength();
    }
}