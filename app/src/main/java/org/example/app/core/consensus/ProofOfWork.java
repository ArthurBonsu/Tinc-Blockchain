package core.consensus;

import core.block.Block;
import core.util.HashUtils;

public class ProofOfWork {

    // Perform proof-of-work on a block
    public boolean validatePoW(Block block, long difficulty) {
        String blockHash = block.getHash();
        // Check if block hash meets the proof-of-work criteria
        return HashUtils.isHashBelowTarget(blockHash, difficulty);
    }

    // Mine the block with proof-of-work
    public String mineBlock(Block block, long difficulty) {
        // Simplified: Return a valid hash (in real life, you'd use a mining algorithm here)
        return HashUtils.generateHash(block.getHash() + difficulty);
    }
}
