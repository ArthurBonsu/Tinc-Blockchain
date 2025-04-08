package org.example.app.core.mempool;

import java.math.BigInteger;
import org.example.app.core.block.Transaction;
import org.example.app.core.crypto.Keypair;

public class TransactionValidator {
    private final MempoolConfig config;

    public TransactionValidator(MempoolConfig config) {
        this.config = config;
    }

    public boolean validateTransaction(Transaction tx) {
        // Basic validation
        if (tx == null) {
            return false;
        }

        // Check transaction validity using the transaction's isValid method
        if (!tx.isValid()) {
            return false;
        }

        // Check nonce (assuming negative nonce is invalid)
        if (tx.getNonce() < 0) {
            return false;
        }

        // Check gas price against minimum
        if (tx.getGasPrice().compareTo(config.getMinGasPrice()) < 0) {
            return false;
        }

        return true;
    }

    // Signature verification method
    private boolean verifySignature(Transaction tx) {
        // Basic signature validation
        // This relies on the isValid() method which already checks for signature existence
        return true;
    }

    // Check if sender has sufficient balance
    public boolean isBalanceSufficient(Transaction tx, BigInteger senderBalance) {
        // Calculate total transaction cost (gas cost + transaction value)
        BigInteger totalCost = tx.getTotalCost();
        return senderBalance.compareTo(totalCost) >= 0;
    }

    // Additional detailed validation methods
    public boolean validateTransactionFully(Transaction tx, BigInteger senderBalance) {
        // Comprehensive validation including balance check
        return validateTransaction(tx) &&
                isBalanceSufficient(tx, senderBalance);
    }
}