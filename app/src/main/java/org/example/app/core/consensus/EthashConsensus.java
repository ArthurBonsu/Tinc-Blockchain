package core.consensus;

import core.block.Block;
import core.util.HashUtils;

public class EthashConsensus {

    // Check if the block's proof of work is valid
    public boolean validateProofOfWork(Block block) {
        // Simplified: Proof of work validation checks the block's hash against difficulty
        String blockHash = block.getHash();
        long difficulty = block.getDifficulty();
        
        // Simulate checking if the block's hash is less than the target difficulty
        return HashUtils.isHashBelowTarget(blockHash, difficulty);
    }

    // Mine a new block (simplified)
    public Block mineBlock(Block previousBlock, String miner, long difficulty) {
        long timestamp = System.currentTimeMillis();
        String parentHash = previousBlock.getHash();
        
        // Start mining (this would involve hashing, nonce, etc. in a real system)
        String[] transactions = new String[] {}; // Assuming empty transactions for now
        
        String blockHash = generateHash(parentHash, miner, timestamp, difficulty);
        
        return new Block(blockHash, parentHash, miner, timestamp, difficulty, previousBlock.getNumber() + 1, transactions);
    }

    // Generate a hash for the block (simplified version)
    private String generateHash(String parentHash, String miner, long timestamp, long difficulty) {
        return HashUtils.generateHash(parentHash + miner + timestamp + difficulty);
    }
}
