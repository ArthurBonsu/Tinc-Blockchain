// ConsensusConfig.java
package org.example.app.core.config;

import java.math.BigInteger;

public class ConsensusConfig {
    private final String consensusType; // "POW", "PBFT", etc.
    private final long blockTime;
    private final BigInteger difficulty;
    private final int minValidators;
    private final double requiredMajority;

    public static class Builder {
        private String consensusType = "POW";
        private long blockTime = 15000; // 15 seconds
        private BigInteger difficulty = BigInteger.valueOf(100000);
        private int minValidators = 4;
        private double requiredMajority = 0.67; // 67%

        public Builder consensusType(String type) {
            this.consensusType = type;
            return this;
        }

        public ConsensusConfig build() {
            return new ConsensusConfig(this);
        }
    }

    private ConsensusConfig(Builder builder) {
        this.consensusType = builder.consensusType;
        this.blockTime = builder.blockTime;
        this.difficulty = builder.difficulty;
        this.minValidators = builder.minValidators;
        this.requiredMajority = builder.requiredMajority;
    }

    // Getters
    public String getConsensusType() { return consensusType; }
    public long getBlockTime() { return blockTime; }
    public BigInteger getDifficulty() { return difficulty; }
    public int getMinValidators() { return minValidators; }
    public double getRequiredMajority() { return requiredMajority; }
}