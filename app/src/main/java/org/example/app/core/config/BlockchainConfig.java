package org.example.app.core.config;

public class BlockchainConfig {
    private final long blockTime;
    private final long maxBlockSize;
    private final long minGasPrice;
    private final String networkId;
    private final boolean isTestnet;

    public static class Builder {
        private long blockTime = 15000; // 15 seconds
        private long maxBlockSize = 8000000; // 8MB
        private long minGasPrice = 1000000000; // 1 Gwei
        private String networkId = "1";
        private boolean isTestnet = false;

        public Builder blockTime(long blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public BlockchainConfig build() {
            return new BlockchainConfig(this);
        }
    }

    private BlockchainConfig(Builder builder) {
        this.blockTime = builder.blockTime;
        this.maxBlockSize = builder.maxBlockSize;
        this.minGasPrice = builder.minGasPrice;
        this.networkId = builder.networkId;
        this.isTestnet = builder.isTestnet;
    }

    // Getters
    public long getBlockTime() { return blockTime; }
}