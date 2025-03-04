package org.example.app.core.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.example.app.core.block.Block; // Add import for Block

public class ConsensusMetrics {
    private final AtomicLong blockHeight;
    private final AtomicLong totalTransactions;
    private final AtomicLong averageBlockTime;
    private final Map<String, AtomicLong> validatorMetrics;
    private Instant lastBlockTime;

    public ConsensusMetrics() {
        this.blockHeight = new AtomicLong(0);
        this.totalTransactions = new AtomicLong(0);
        this.averageBlockTime = new AtomicLong(0);
        this.validatorMetrics = new ConcurrentHashMap<>();
        this.lastBlockTime = Instant.now();
    }

    public void recordNewBlock(Block block, String validator) {
        blockHeight.incrementAndGet();
        totalTransactions.addAndGet(block.getTransactions().size());
        updateBlockTime();

        // Update validator metrics
        validatorMetrics.computeIfAbsent(validator, k -> new AtomicLong(0))
                .incrementAndGet();
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

    public void recordConsensusFailure(String reason) {
        // Placeholder for consensus failure tracking
        // You might want to implement a more sophisticated tracking mechanism
        System.err.println("Consensus failure: " + reason);
    }

    // Getters
    public long getBlockHeight() { return blockHeight.get(); }
    public long getTotalTransactions() { return totalTransactions.get(); }
    public long getAverageBlockTime() { return averageBlockTime.get(); }

    public Map<String, Long> getValidatorMetrics() {
        Map<String, Long> metrics = new ConcurrentHashMap<>();
        validatorMetrics.forEach((k, v) -> metrics.put(k, v.get()));
        return metrics;
    }

    // Optional: Reset method
    public void reset() {
        blockHeight.set(0);
        totalTransactions.set(0);
        averageBlockTime.set(0);
        validatorMetrics.clear();
        lastBlockTime = Instant.now();
    }
}