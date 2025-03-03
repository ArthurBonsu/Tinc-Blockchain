package org.example.app.core.block;

import org.example.app.core.eth.EthereumMiner; // Add this import

public class Miner {
    private EthereumMiner miner;

    public Miner() {
        miner = new EthereumMiner();
    }

    public Block mine(Block lastBlock) {
        return miner.mineBlock(lastBlock);
    }
}