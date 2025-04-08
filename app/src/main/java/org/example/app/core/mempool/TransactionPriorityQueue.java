package org.example.app.core.mempool;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.math.BigInteger;
import org.example.app.core.block.Transaction; // Add import for Transaction

public class TransactionPriorityQueue {
    private final PriorityQueue<Transaction> queue;
    private final ReentrantLock lock;
    private final int maxSize;

    public TransactionPriorityQueue(int maxSize) {
        this.maxSize = maxSize;
        this.lock = new ReentrantLock();
        this.queue = new PriorityQueue<>(Comparator
                .comparing(Transaction::getGasPrice)
                .reversed()
                .thenComparing(Transaction::getNonce));
    }

    public boolean add(Transaction tx) {
        lock.lock();
        try {
            if (queue.size() >= maxSize) {
                Transaction lowestPriority = queue.peek();
                if (lowestPriority != null &&
                        lowestPriority.getGasPrice().compareTo(tx.getGasPrice()) >= 0) {
                    return false;
                }
                queue.poll();
            }
            return queue.offer(tx);
        } finally {
            lock.unlock();
        }
    }

    public Transaction poll() {
        lock.lock();
        try {
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        lock.lock();
        try {
            queue.clear();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}