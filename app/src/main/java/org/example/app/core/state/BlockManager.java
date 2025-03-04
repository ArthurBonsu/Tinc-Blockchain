package org.example.app.core.state;

import java.util.LinkedList;
import java.util.List;
import org.example.app.core.block.Block; // Add import for Block

public class BlockManager {

    private List<Block> blocks;

    public BlockManager() {
        this.blocks = new LinkedList<>();
    }

    // Add a new block to the blockchain
    public void addBlock(Block block) {
        blocks.add(block);
    }

    // Get the latest block
    public Block getLatestBlock() {
        return blocks.isEmpty() ? null : blocks.get(blocks.size() - 1);
    }

    // Get the block at a specific index
    public Block getBlock(int index) {
        return blocks.get(index);
    }

    // Get the total number of blocks
    public int getBlockCount() {
        return blocks.size();
    }

    // Remove the last block
    public Block removeLastBlock() {
        return blocks.isEmpty() ? null : blocks.remove(blocks.size() - 1);
    }

    // Check if a block exists at a specific index
    public boolean hasBlock(int index) {
        return index >= 0 && index < blocks.size();
    }

    // Clear all blocks
    public void clear() {
        blocks.clear();
    }

    // Get a copy of all blocks
    public List<Block> getAllBlocks() {
        return new LinkedList<>(blocks);
    }
}