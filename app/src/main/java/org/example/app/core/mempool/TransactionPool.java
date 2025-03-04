package org.example.app.core.mempool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.PriorityQueue;
import java.util.Comparator;
import org.example.app.core.types.*; // Keep existing types import
import org.example.app.core.block.Transaction; // Add Transaction import

public class TransactionPool {
    private final ConcurrentHashMap<String, Transaction> txPool;
    private final PriorityQueue<Transaction> pendingTxs;
    private final long maxSize;

    public TransactionPool(long maxSize) {
        this.maxSize = maxSize;
        this.txPool = new ConcurrentHashMap<>();
        this.pendingTxs = new PriorityQueue<>(Comparator.comparingLong(Transaction::getGasPrice).reversed());
    }

    public boolean addTransaction(Transaction tx) {
        if (txPool.size() >= maxSize) {
            return false;
        }
        String txHash = tx.getHash();
        if (txPool.containsKey(txHash)) {
            return false;
        }
        txPool.put(txHash, tx);
        pendingTxs.offer(tx);
        return true;
    }

    public Transaction getPendingTransaction() {
        return pendingTxs.poll();
    }

    public int size() {
        return txPool.size();
    }

    public void clear() {
        txPool.clear();
        pendingTxs.clear();
    }
}