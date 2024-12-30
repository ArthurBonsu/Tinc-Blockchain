// TransactionBuilder.java
package org.example.app.core.wallet;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.time.Instant;

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

        Transaction tx = new Transaction();
        tx.setFrom(from);
        tx.setTo(to);
        tx.setValue(value);
        tx.setGasPrice(gasPrice);
        tx.setGasLimit(gasLimit);
        tx.setData(data);
        tx.setNonce(nonce);
        tx.setTimestamp(Instant.now().getEpochSecond());

        // Sign the transaction if private key is provided
        if (privateKey != null) {
            byte[] messageToSign = tx.getMessageToSign();
            byte[] signature = KeyUtils.sign(messageToSign, privateKey);
            tx.setSignature(signature);
        }

        return tx;
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
}