package org.example.app.core.block;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager {
    private List<Transaction> transactionPool;

    public TransactionManager() {
        transactionPool = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        transactionPool.add(transaction);
    }

    public List<Transaction> getTransactionsForBlock() {
        return new ArrayList<>(transactionPool); // Get transactions for the next block
    }
}
