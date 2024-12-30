package org.example.app.core.eth;

import java.util.LinkedList;
import java.util.Queue;

public class TransactionPool {

    private Queue<String> txPool;

    public TransactionPool() {
        this.txPool = new LinkedList<>();
    }

    // Add a transaction to the pool
    public void addTransaction(String transaction) {
        txPool.add(transaction);
    }

    // Get the next transaction in the pool
    public String getNextTransaction() {
        return txPool.poll();
    }

    // Check if there are transactions in the pool
    public boolean hasTransactions() {
        return !txPool.isEmpty();
    }
}
