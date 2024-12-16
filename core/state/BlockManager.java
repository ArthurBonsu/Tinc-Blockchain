package core.state;

import java.util.LinkedList;
import java.util.List;

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
}
