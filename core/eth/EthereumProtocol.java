package core.eth;

import core.block.Block;
import core.consensus.Blockchain;
import core.util.HashUtils;

public class EthereumProtocol {

    private Blockchain blockchain;

    public EthereumProtocol() {
        this.blockchain = new Blockchain();
    }

    // Add a new block to the Ethereum blockchain after validating it
    public boolean addBlock(Block block) {
        return blockchain.addBlock(block);
    }

    // Retrieve the latest block in the Ethereum blockchain
    public Block getLatestBlock() {
        return blockchain.getLatestBlock();
    }

    // Validate the block using Ethereum's consensus rules
    public boolean validateBlock(Block block) {
        // Assuming validation is done through Ethereum's consensus protocol (PoW, etc.)
        return blockchain.getLatestBlock() != null && blockchain.getLatestBlock().getNumber() + 1 == block.getNumber();
    }

    // Mine a new block for the Ethereum blockchain
    public Block mineBlock(String miner, long difficulty) {
        Block latestBlock = getLatestBlock();
        if (latestBlock == null) {
            return null;
        }
        return blockchain.mineBlock(latestBlock, miner, difficulty);
    }
}
