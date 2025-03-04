package org.example.app.core.block;

import org.example.app.core.eth.EthereumMiner;
import org.example.app.core.consensus.Blockchain;

public class Miner {
    private EthereumMiner miner;
    private Blockchain blockchain;
    private String minerAddress;
    private long difficulty;

    public Miner(Blockchain blockchain, String minerAddress) {
        this.blockchain = blockchain;
        this.minerAddress = minerAddress;
        this.difficulty = 10; // Default difficulty, can be adjusted

        // Create EthereumMiner with blockchain and miner address
        this.miner = new EthereumMiner(blockchain, minerAddress);
    }

    public Block mine(Block lastBlock) {
        // Mine block using the latest block and current difficulty
        return miner.mineBlock(lastBlock, difficulty);
    }

    // Alternative mining method if needed
    public Block mineBlock() {
        Block lastBlock = blockchain.getLatestBlock();
        return mine(lastBlock);
    }

    // Optional: Get current miner
    public EthereumMiner getMiner() {
        return miner;
    }

    // Setter for difficulty
    public void setDifficulty(long difficulty) {
        this.difficulty = difficulty;
    }
}