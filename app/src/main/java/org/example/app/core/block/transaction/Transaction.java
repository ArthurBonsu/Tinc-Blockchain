package org.example.app.core.transaction;

import java.math.BigInteger;

public class Transaction {
    private String sender;
    private String recipient;
    private BigInteger value;
    private BigInteger fee;
    private long nonce;
    private byte[] data;
    private boolean contractCreation;
    private boolean contractCall;

    // Getters
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public BigInteger getValue() { return value; }
    public BigInteger getFee() { return fee; }
    public long getNonce() { return nonce; }
    public byte[] getData() { return data; }
    public boolean isContractCreation() { return contractCreation; }
    public boolean isContractCall() { return contractCall; }
}