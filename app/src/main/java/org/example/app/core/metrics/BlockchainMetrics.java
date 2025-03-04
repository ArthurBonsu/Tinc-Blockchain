package org.example.app.core.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;
import org.example.app.core.block.Block; // Add import for Block

public class BlockchainMetrics {
    private final AtomicLong totalTransactions;
    private final AtomicLong totalBlocks;
    private final AtomicLong averageBlockTime;
    private Instant lastBlockTime;

    public BlockchainMetrics() {
        this.totalTransactions = new AtomicLong(0);
        this.totalBlocks = new AtomicLong(0);
        this.averageBlockTime = new AtomicLong(0);
        this.lastBlockTime = Instant.now();
    }

    public void recordNewBlock(Block block) {
        totalBlocks.incrementAndGet();
        totalTransactions.addAndGet(block.getTransactions().size());
        updateBlockTime();
    }

    private void updateBlockTime() {
        Instant now = Instant.now();
        long newBlockTime = now.toEpochMilli() - lastBlockTime.toEpochMilli();
        lastBlockTime = now;
        long currentAvg = averageBlockTime.get();
        if (currentAvg == 0) {
            averageBlockTime.set(newBlockTime);
        } else {
            averageBlockTime.set((currentAvg + newBlockTime) / 2);
        }
    }

    // Getter methods for metrics
    public long getTotalBlocks() {
        return totalBlocks.get();
    }

    public long getTotalTransactions() {
        return totalTransactions.get();
    }

    public long getAverageBlockTime() {
        return averageBlockTime.get();
    }

    // Reset metrics if needed
    public void reset() {
        totalTransactions.set(0);
        totalBlocks.set(0);
        averageBlockTime.set(0);
        lastBlockTime = Instant.now();
    }
}