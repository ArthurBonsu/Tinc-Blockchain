package org.example.app.core.mempool;

import java.math.BigInteger;
import org.example.app.core.block.Transaction; // Add import for Transaction

public class TransactionValidator {
    private final MempoolConfig config;

    public TransactionValidator(MempoolConfig config) {
        this.config = config;
    }

    public boolean validateTransaction(Transaction tx) throws Exception {
        // Basic validation
        if (tx == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        // Check nonce
        if (tx.getNonce() < 0) {
            return false;
        }

        // Check amounts
        if (tx.getValue().compareTo(BigInteger.ZERO) < 0) {
            return false;
        }

        // Check gas price against minimum
        if (tx.getGasPrice().compareTo(config.getMinGasPrice()) < 0) {
            return false;
        }

        // Verify signature
        if (!verifySignature(tx)) {
            return false;
        }

        return true;
    }

    private boolean verifySignature(Transaction tx) {
        try {
            return tx.verifySignature();
        } catch (Exception e) {
            return false;
        }
    }

    // Additional validation methods can be added here
    public boolean isBalanceSufficient(Transaction tx, BigInteger senderBalance) {
        BigInteger totalCost = tx.getValue().add(tx.getGasPrice().multiply(BigInteger.valueOf(tx.getGasLimit())));
        return senderBalance.compareTo(totalCost) >= 0;
    }
}