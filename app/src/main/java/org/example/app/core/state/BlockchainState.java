package core.state;

import java.util.HashMap;
import java.util.Map;

public class BlockchainState {

    private Map<String, Block> blocks;

    public BlockchainState() {
        this.blocks = new HashMap<>();
    }

    // Add a new block to the blockchain state
    public void addBlock(String blockHash, Block block) {
        blocks.put(blockHash, block);
    }

    // Get a block by its hash
    public Block getBlock(String blockHash) {
        return blocks.get(blockHash);
    }

    // Get the total number of blocks in the blockchain
    public int getBlockCount() {
        return blocks.size();
    }
}
