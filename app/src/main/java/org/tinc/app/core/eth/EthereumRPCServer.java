package org.example.app.core.eth;

import org.example.app.core.block.Block;
import org.example.app.core.consensus.Blockchain;

import java.util.List;

public class EthereumRPCServer {

    private EthereumProtocol protocol;

    public EthereumRPCServer() {
        this.protocol = new EthereumProtocol();
    }

    // Handle a request to get the latest block in the Ethereum blockchain
    public Block getLatestBlock() {
        return protocol.getLatestBlock();
    }

    // Handle a request to get the block by hash
    public Block getBlockByHash(String blockHash) {
        return protocol.getBlockByHash(blockHash);
    }

    // Handle a request to add a block to the blockchain
    public boolean addBlock(Block block) {
        return protocol.addBlock(block);
    }

    // Handle a request to mine a new block
    public Block mineBlock(String miner, long difficulty) {
        return protocol.mineBlock(miner, difficulty);
    }
}
