package org.example.app.core.wallet;

import java.security.*;
import java.util.HashMap;
import java.math.BigInteger;
import org.example.app.core.block.Transaction; // Add import for Transaction

public class Wallet {
    private final KeyPair keyPair;
    private final HashMap<String, Transaction> transactions;
    private long nonce;
    private long balance;

    public Wallet() throws Exception {
        // Use KeyUtils for key generation
        this.keyPair = KeyUtils.generateKeyPair();
        this.transactions = new HashMap<>();
        this.nonce = 0;
        this.balance = 0;
    }

    public Transaction createTransaction(String to, long amount, long gasPrice) {
        if (amount + gasPrice > balance) {
            throw new IllegalStateException("Insufficient funds");
        }

        // Use TransactionBuilder for creating transactions
        TransactionBuilder builder = new TransactionBuilder();
        Transaction tx = builder
                .from(getAddress())
                .to(to)
                .value(BigInteger.valueOf(amount))
                .gasPrice(BigInteger.valueOf(gasPrice))
                .nonce(nonce++)
                .privateKey(keyPair.getPrivate())
                .build();

        transactions.put(tx.getHash(), tx);
        return tx;
    }

    public String getAddress() {
        // Generate address from public key
        return KeyUtils.getAddress(keyPair.getPublic());
    }

    // Additional wallet methods
    public void updateBalance(long newBalance) {
        this.balance = newBalance;
    }

    public long getBalance() {
        return balance;
    }

    public HashMap<String, Transaction> getTransactions() {
        return new HashMap<>(transactions);
    }

    public Transaction getTransaction(String hash) {
        return transactions.get(hash);
    }
}