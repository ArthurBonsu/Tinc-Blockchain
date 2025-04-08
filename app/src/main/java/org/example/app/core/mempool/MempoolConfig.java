// MempoolConfig.java
package org.example.app.core.mempool;

import java.math.BigInteger;

public class MempoolConfig {
    private final int maxSize;
    private final BigInteger minGasPrice;
    private final int maxTransactionsPerAccount;
    private final long txTimeout;

    public static class Builder {
        private int maxSize = 10000;
        private BigInteger minGasPrice = BigInteger.valueOf(1000000000); // 1 Gwei
        private int maxTransactionsPerAccount = 100;
        private long txTimeout = 3600000; // 1 hour in milliseconds

        public Builder maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder minGasPrice(BigInteger minGasPrice) {
            this.minGasPrice = minGasPrice;
            return this;
        }

        public Builder maxTransactionsPerAccount(int maxTransactionsPerAccount) {
            this.maxTransactionsPerAccount = maxTransactionsPerAccount;
            return this;
        }

        public Builder txTimeout(long txTimeout) {
            this.txTimeout = txTimeout;
            return this;
        }

        public MempoolConfig build() {
            return new MempoolConfig(this);
        }
    }

    private MempoolConfig(Builder builder) {
        this.maxSize = builder.maxSize;
        this.minGasPrice = builder.minGasPrice;
        this.maxTransactionsPerAccount = builder.maxTransactionsPerAccount;
        this.txTimeout = builder.txTimeout;
    }

    // Getters
    public int getMaxSize() { return maxSize; }
    public BigInteger getMinGasPrice() { return minGasPrice; }
    public int getMaxTransactionsPerAccount() { return maxTransactionsPerAccount; }
    public long getTxTimeout() { return txTimeout; }
}