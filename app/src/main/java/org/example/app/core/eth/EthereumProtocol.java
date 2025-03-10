package org.example.app.core.eth;

import org.example.app.core.block.Block;
import org.example.app.core.block.Transaction;
import org.example.app.core.consensus.Blockchain;
import org.example.app.core.consensus.ProofOfWork;
import java.util.ArrayList;
import java.util.List;

public class EthereumProtocol {
    private Blockchain blockchain;
    private ProofOfWork proofOfWork;

    public EthereumProtocol() {
        this.blockchain = new Blockchain();
        this.proofOfWork = new ProofOfWork();
    }

    // Add a new block to the Ethereum blockchain after validating it
    public boolean addBlock(Block block) {
        return blockchain.addBlock(block);
    }

    // Retrieve the latest block in the Ethereum blockchain
    public Block getLatestBlock() {
        return blockchain.getLatestBlock();
    }

    // Retrieve a block by its hash
    public Block getBlockByHash(String blockHash) {
        return blockchain.getBlockByHash(blockHash);
    }

    // Validate the block using Ethereum's consensus rules
    public boolean validateBlock(Block block) {
        // Validate block number sequence and proof of work
        return blockchain.getLatestBlock() != null &&
                blockchain.getLatestBlock().getNumber() + 1 == block.getNumber() &&
                proofOfWork.validatePoW(block, block.getDifficulty());
    }

    // Mine a new block for the Ethereum blockchain
    public Block mineBlock(String miner, long difficulty) {
        Block latestBlock = getLatestBlock();
        if (latestBlock == null) {
            return null;
        }

        // Prepare block parameters
        String parentHash = latestBlock.getHash();
        long blockNumber = latestBlock.getNumber() + 1;
        long timestamp = System.currentTimeMillis();

        // Create an empty list of transactions for now
        List<Transaction> transactions = new ArrayList<>();

        // Create a new block with all required parameters
        Block newBlock = new Block(
                null,           // Initial hash (will be set after mining)
                parentHash,     // Parent hash
                miner,          // Miner address
                timestamp,      // Timestamp
                difficulty,     // Difficulty
                blockNumber,    // Block number
                transactions    // Transactions list
        );

        // Mine the block (generate a valid hash)
        String minedBlockHash = proofOfWork.mineBlock(newBlock, difficulty);

        // Set the mined hash to the block
        newBlock.setHash(minedBlockHash);

        // Add the block to the blockchain
        if (addBlock(newBlock)) {
            return newBlock;
        }

        return null;
    }
}