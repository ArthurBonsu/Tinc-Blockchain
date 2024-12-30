// WalletStorage.java
package org.example.app.core.wallet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class WalletStorage {
    private final Path walletPath;
    private final Gson gson;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public WalletStorage(String walletDirectory) {
        this.walletPath = Paths.get(walletDirectory);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        if (!Files.exists(walletPath)) {
            try {
                Files.createDirectories(walletPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create wallet directory", e);
            }
        }
    }

    public void saveWallet(String walletId, WalletData wallet, String password) throws Exception {
        // Generate salt for password-based encryption
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        // Derive encryption key from password
        SecretKey key = deriveKey(password, salt);

        // Convert wallet data to JSON
        String walletJson = gson.toJson(wallet);

        // Encrypt wallet data
        byte[] encrypted = encrypt(walletJson.getBytes(), key);

        // Create wallet file content with salt and encrypted data
        WalletFile walletFile = new WalletFile(salt, encrypted);
        String fileContent = gson.toJson(walletFile);

        // Save to file
        Path filePath = walletPath.resolve(walletId + ".json");
        Files.write(filePath, fileContent.getBytes());
    }

    public WalletData loadWallet(String walletId, String password) throws Exception {
        Path filePath = walletPath.resolve(walletId + ".json");
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Wallet not found: " + walletId);
        }

        // Read wallet file
        String fileContent = new String(Files.readAllBytes(filePath));
        WalletFile walletFile = gson.fromJson(fileContent, WalletFile.class);

        // Derive key from password and salt
        SecretKey key = deriveKey(password, walletFile.salt);

        // Decrypt wallet data
        byte[] decrypted = decrypt(walletFile.encryptedData, key);
        String walletJson = new String(decrypted);

        // Parse wallet data
        return gson.fromJson(walletJson, WalletData.class);
    }

    private SecretKey deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), 
            salt, 
            ITERATIONS, 
            KEY_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    // Inner classes for wallet data structure
    private static class WalletFile {
        public byte[] salt;
        public byte[] encryptedData;

        public WalletFile(byte[] salt, byte[] encryptedData) {
            this.salt = salt;
            this.encryptedData = encryptedData;
        }
    }

    public static class WalletData {
        public String address;
        public String privateKeyEncrypted;
        public Map<String, BigInteger> balances;
        public long lastNonce;

        public WalletData() {
            this.balances = new HashMap<>();
        }
    }

    // Utility methods for managing multiple wallets
    public boolean walletExists(String walletId) {
        return Files.exists(walletPath.resolve(walletId + ".json"));
    }

    public void deleteWallet(String walletId) throws IOException {
        Path filePath = walletPath.resolve(walletId + ".json");
        Files.deleteIfExists(filePath);
    }

    public String[] listWallets() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(walletPath, "*.json")) {
            return StreamSupport.stream(stream.spliterator(), false)
                .map(path -> path.getFileName().toString().replace(".json", ""))
                .toArray(String[]::new);
        }
    }
}