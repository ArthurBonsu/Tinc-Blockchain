package core.eth;

import core.block.Block;
import core.consensus.Blockchain;

public class EthereumMiner {

    private Blockchain blockchain;
    private String minerAddress;

    public EthereumMiner(String minerAddress) {
        this.blockchain = new Blockchain();
        this.minerAddress = minerAddress;
    }

    // Start the mining process
    public Block mine(long difficulty) {
        Block latestBlock = blockchain.getLatestBlock();
        if (latestBlock == null) {
            return null;
        }
        return blockchain.mineBlock(latestBlock, minerAddress, difficulty);
    }

    // Get the miner's address
    public String getMinerAddress() {
        return minerAddress;
    }
}
