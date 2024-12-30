package org.example.app.core.block;

import sun.jvm.hotspot.opto.Block;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<Block> chain;
    private TransactionManager transactionManager;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.transactionManager = new TransactionManager();
    }

    public void addTransaction(Transaction transaction) {
        transactionManager.addTransaction(transaction);
    }

    public Block getBlockByIndex(int index) {
        return chain.get(index);
    }

    public int getHeight() {
        return chain.size();
    }

    // Method to add a block to the blockchain
    public void addBlock(Block block) {
        this.chain.add(block);
    }
}
