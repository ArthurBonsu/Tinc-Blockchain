package org.example.app.core.wallet;

import java.security.*;
import java.util.HashMap;
import java.math.BigInteger;
import org.example.app.core.block.Transaction;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;

public class Wallet {
    private final KeyPair keyPair;
    private final HashMap<String, Transaction> transactions;
    private long nonce;
    private long balance;

    public Wallet() throws NoSuchAlgorithmException {
        // Generate key pair using standard Java cryptography
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC"); // Elliptic Curve
        keyGen.initialize(256); // 256-bit key
        this.keyPair = keyGen.generateKeyPair();

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

        // Generate a unique hash for the transaction
        String txHash = generateTransactionHash(tx);

        // Store transaction with generated hash
        transactions.put(txHash, tx);
        return tx;
    }

    // Method to generate a unique hash for the transaction
    private String generateTransactionHash(Transaction tx) {
        try {
            // Use transaction's byte representation to generate a hash
            byte[] txBytes = tx.toBytes();

            // Use SHA-256 for hash generation
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(txBytes);

            return bytesToHex(hashBytes);
        } catch (Exception e) {
            // Fallback to a unique identifier
            return String.valueOf(System.currentTimeMillis());
        }
    }

    // Helper method to convert byte array to hex string
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getAddress() {
        // Generate address from public key
        // This is a simplified address generation
        PublicKey publicKey = keyPair.getPublic();
        byte[] publicKeyBytes = publicKey.getEncoded();
        return bytesToHex(publicKeyBytes).substring(0, 40); // First 40 characters
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