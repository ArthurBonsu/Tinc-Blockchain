package org.example.app.core.mempool;

import java.math.BigInteger;
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
        // Use BigInteger comparison instead of long
        this.pendingTxs = new PriorityQueue<>(
                Comparator.comparing(Transaction::getGasPrice).reversed()
        );
    }

    public boolean addTransaction(Transaction tx) {
        if (txPool.size() >= maxSize) {
            return false;
        }
        // If no getHash(), use a unique identifier like transaction data or nonce
        String txHash = generateTransactionHash(tx);

        if (txPool.containsKey(txHash)) {
            return false;
        }
        txPool.put(txHash, tx);
        pendingTxs.offer(tx);
        return true;
    }

    // Fallback method to generate a unique hash if getHash() doesn't exist
    private String generateTransactionHash(Transaction tx) {
        // This is a simplistic approach and should be replaced with a proper hashing mechanism
        return String.valueOf(tx.getNonce()) +
                tx.getSender() +
                tx.getRecipient() +
                tx.getValue();
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