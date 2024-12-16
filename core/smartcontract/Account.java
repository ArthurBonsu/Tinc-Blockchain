package org.tinc.smartcontract;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Account {
    private BigInteger balance = BigInteger.ZERO;
    private long nonce = 0;
    private byte[] code; // Smart contract bytecode
    private Map<String, String> storage = new HashMap<>();

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public long getNonce() {
        return nonce;
    }

    public void incrementNonce() {
        nonce++;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    public Map<String, String> getStorage() {
        return storage;
    }
}
