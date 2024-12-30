package org.example.app.core.block;

import org.example.app.core.types.Address;
import org.example.app.core.types.Hash;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.crypto.Keypair.SignatureResult;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

public class Transaction implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    
    // [Previous enum and nested classes remain the same]

    private Object txInner;
    private byte[] data;
    private Address to;
    private long value;
    private Address from;
    private SignatureResult signature;
    private long nonce;
    private Hash hash;
    private int gasLimit;
    private BigInteger gasPrice; // Added gas price
    private TransactionStatus status; // Added transaction status

    public enum TransactionStatus {
        PENDING,
        CONFIRMED,
        FAILED,
        INVALID
    }

    public Transaction(byte[] data) {
        this.data = data != null ? data.clone() : new byte[0];
        this.nonce = new Random().nextLong();
        this.status = TransactionStatus.PENDING;
        this.gasPrice = BigInteger.ZERO;
        this.gasLimit = 21000; // Default gas limit
    }

    @Override
    public byte[] toBytes() {
        int estimatedSize = calculateBufferSize();
        ByteBuffer buffer = ByteBuffer.allocate(estimatedSize);
        
        // Write all transaction fields
        buffer.put(data != null ? data : new byte[0]);
        writeAddress(buffer, to);
        buffer.putLong(value);
        writeAddress(buffer, from);
        buffer.putLong(nonce);
        buffer.putInt(gasLimit);
        writeGasPrice(buffer);
        
        return Arrays.copyOf(buffer.array(), buffer.position());
    }

    private int calculateBufferSize() {
        return (data != null ? data.length : 0) +
               (to != null ? Address.ADDRESS_LENGTH : 0) +
               (from != null ? Address.ADDRESS_LENGTH : 0) +
               Long.BYTES * 2 + // nonce and value
               Integer.BYTES + // gasLimit
               (gasPrice != null ? gasPrice.toByteArray().length + Integer.BYTES : 0) +
               DEFAULT_BUFFER_SIZE; // Additional buffer for safety
    }

    private void writeAddress(ByteBuffer buffer, Address address) {
        if (address != null) {
            buffer.put(address.toSlice());
        } else {
            buffer.put(new byte[Address.ADDRESS_LENGTH]);
        }
    }

    private void writeGasPrice(ByteBuffer buffer) {
        byte[] gasPriceBytes = gasPrice != null ? gasPrice.toByteArray() : BigInteger.ZERO.toByteArray();
        buffer.putInt(gasPriceBytes.length);
        buffer.put(gasPriceBytes);
    }

    public boolean isValid() {
        if (from == null || to == null) {
            return false;
        }
        if (value < 0 || gasLimit < 0) {
            return false;
        }
        if (gasPrice == null || gasPrice.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        if (signature == null) {
            return false;
        }
        return true;
    }

    // Getters and setters with validation
    public void setGasPrice(BigInteger gasPrice) {
        if (gasPrice == null || gasPrice.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Gas price must be non-negative");
        }
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    // [Previous getters and setters remain the same]

    // Calculate total transaction cost
    public BigInteger getTotalCost() {
        return gasPrice.multiply(BigInteger.valueOf(gasLimit)).add(BigInteger.valueOf(value));
    }

    @Override
    public String toString() {
        return String.format("Transaction{hash=%s, from=%s, to=%s, value=%d, nonce=%d, gasLimit=%d, gasPrice=%s, status=%s}",
                hash, from, to, value, nonce, gasLimit, gasPrice, status);
    }
}