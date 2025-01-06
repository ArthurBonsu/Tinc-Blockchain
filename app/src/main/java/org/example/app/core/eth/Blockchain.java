package org.example.app.core.eth;



import java.util.ArrayList;
import java.util.List;
import org.example.app.core.block.Block;
import org.example.app.core.block.TransactionManager;
import org.example.app.core.block.Transaction;
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
