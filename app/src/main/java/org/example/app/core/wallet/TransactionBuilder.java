package org.example.app.core.wallet;

import java.math.BigInteger;
import java.security.PrivateKey;
import org.example.app.core.block.Transaction;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.crypto.Keypair.SignatureResult;

public class TransactionBuilder {
    private String from;
    private String to;
    private BigInteger value;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private byte[] data;
    private long nonce;
    private PrivateKey privateKey;

    public TransactionBuilder() {
        this.value = BigInteger.ZERO;
        this.gasPrice = BigInteger.valueOf(1_000_000_000); // 1 Gwei
        this.gasLimit = BigInteger.valueOf(21000); // Standard transfer
        this.data = new byte[0];
    }

    public TransactionBuilder from(String from) {
        this.from = from;
        return this;
    }

    public TransactionBuilder to(String to) {
        this.to = to;
        return this;
    }

    public TransactionBuilder value(BigInteger value) {
        this.value = value;
        return this;
    }

    public TransactionBuilder gasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public TransactionBuilder gasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }

    public TransactionBuilder data(byte[] data) {
        this.data = data;
        return this;
    }

    public TransactionBuilder nonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    public TransactionBuilder privateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public Transaction build() {
        validate();

        // Create transaction with data
        Transaction tx = new Transaction(data);

        // Use reflection to set private fields
        setSenderInTransaction(tx, from);
        setRecipientInTransaction(tx, to);
        setValueInTransaction(tx, value.longValue());
        tx.setGasPrice(gasPrice);
        setGasLimitInTransaction(tx, gasLimit.intValue());

        // Sign the transaction if private key is provided
        if (privateKey != null) {
            SignatureResult signature = createSignature(tx);
            tx.setSignature(signature);
        }

        return tx;
    }

    // Use reflection to set private fields
    private void setSenderInTransaction(Transaction tx, String sender) {
        try {
            java.lang.reflect.Field field = Transaction.class.getDeclaredField("sender");
            field.setAccessible(true);
            field.set(tx, sender);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set sender", e);
        }
    }

    private void setRecipientInTransaction(Transaction tx, String recipient) {
        try {
            java.lang.reflect.Field field = Transaction.class.getDeclaredField("recipient");
            field.setAccessible(true);
            field.set(tx, recipient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set recipient", e);
        }
    }

    private void setValueInTransaction(Transaction tx, Long value) {
        try {
            java.lang.reflect.Field field = Transaction.class.getDeclaredField("value");
            field.setAccessible(true);
            field.set(tx, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set value", e);
        }
    }

    private void setGasLimitInTransaction(Transaction tx, int gasLimit) {
        try {
            java.lang.reflect.Field field = Transaction.class.getDeclaredField("gasLimit");
            field.setAccessible(true);
            field.set(tx, gasLimit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set gasLimit", e);
        }
    }

    private SignatureResult createSignature(Transaction tx) {
        try {
            byte[] messageToSign = tx.toBytes();
            // Placeholder for signature creation
            // If SignatureResult requires two BigInteger parameters
            return new SignatureResult(
                    BigInteger.ONE,  // Placeholder for r value
                    BigInteger.ONE   // Placeholder for s value
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create signature", e);
        }
    }

    private void validate() {
        if (from == null || from.isEmpty()) {
            throw new IllegalStateException("From address is required");
        }
        if (to == null || to.isEmpty()) {
            throw new IllegalStateException("To address is required");
        }
        if (value == null) {
            throw new IllegalStateException("Value cannot be null");
        }
        if (gasPrice == null || gasPrice.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalStateException("Invalid gas price");
        }
        if (gasLimit == null || gasLimit.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalStateException("Invalid gas limit");
        }
    }

    // Optional: Add a method to reset the builder for reuse
    public TransactionBuilder reset() {
        this.from = null;
        this.to = null;
        this.value = BigInteger.ZERO;
        this.gasPrice = BigInteger.valueOf(1_000_000_000);
        this.gasLimit = BigInteger.valueOf(21000);
        this.data = new byte[0];
        this.nonce = 0;
        this.privateKey = null;
        return this;
    }
}