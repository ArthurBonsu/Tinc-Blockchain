package org.example.app.core.consensus;

import org.example.app.core.block.Block;

public class Consensus {

    private EthashConsensus ethashConsensus;

    public Consensus() {
        this.ethashConsensus = new EthashConsensus();
    }

    // Validates the block using the consensus mechanism
    public boolean validateBlock(Block block) {
        return ethashConsensus.validateProofOfWork(block);
    }

    // Mines a block using the current consensus mechanism
    public Block mineBlock(Block previousBlock, String miner, long difficulty) {
        return ethashConsensus.mineBlock(previousBlock, miner, difficulty);
    }
}
