package org.example.app.core.wallet;

import java.security.*;
import java.util.HashMap;

public class Wallet {
    private final KeyPair keyPair;
    private final HashMap<String, Transaction> transactions;
    private long nonce;
    private long balance;

    public Wallet() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstanceStrong();
        keyGen.initialize(256, random);
        this.keyPair = keyGen.generateKeyPair();
        this.transactions = new HashMap<>();
        this.nonce = 0;
        this.balance = 0;
    }

    public Transaction createTransaction(String to, long amount, long gasPrice) {
        if (amount + gasPrice > balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        Transaction tx = new Transaction(
            getAddress(),
            to,
            amount,
            nonce++,
            gasPrice
        );
        
        // Sign transaction
        tx.sign(keyPair.getPrivate());
        transactions.put(tx.getHash(), tx);
        return tx;
    }

    public String getAddress() {
        // Generate address from public key
        return KeyUtils.getAddress(keyPair.getPublic());
    }
}