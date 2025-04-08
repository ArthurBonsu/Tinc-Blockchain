package org.example.app.core.consensus;

import org.example.app.core.block.Block;
import org.example.app.core.block.Transaction;
import org.example.app.core.types.Hash;
import java.util.ArrayList;
import java.util.List;

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
        List<Transaction> transactions = new ArrayList<>(); // Empty list of transactions

        String blockHash = generateHash(parentHash, miner, timestamp, difficulty);

        // Create a new block using the full constructor
        return new Block(
            blockHash,                     // hash
            parentHash,                    // parent hash
            miner,                         // miner address
            timestamp,                     // timestamp
            difficulty,                    // difficulty
            previousBlock.getNumber() + 1, // block number
            transactions                   // transactions
        );
    }

    // Generate a hash for the block (simplified version)
    private String generateHash(String parentHash, String miner, long timestamp, long difficulty) {
        return HashUtils.generateHash(parentHash + miner + timestamp + difficulty);
    }
}