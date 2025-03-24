package org.example.app.core.eth;

import org.example.app.core.block.Block;
import org.example.app.core.consensus.Blockchain;
import org.example.app.core.consensus.EthashConsensus;

public class EthereumMiner {
    private Blockchain blockchain;
    private String minerAddress;
    private EthashConsensus consensus;

    public EthereumMiner(Blockchain blockchain, String minerAddress) {
        this.blockchain = blockchain;
        this.minerAddress = minerAddress;
        this.consensus = new EthashConsensus();
    }

    // Mine a block with given last block and difficulty
    public Block mineBlock(Block lastBlock, long difficulty) {
        if (lastBlock == null) {
            return null;
        }

        // Use EthashConsensus to mine the block
        return consensus.mineBlock(lastBlock, minerAddress, difficulty);
    }

    // Get the miner's address
    public String getMinerAddress() {
        return minerAddress;
    }
}