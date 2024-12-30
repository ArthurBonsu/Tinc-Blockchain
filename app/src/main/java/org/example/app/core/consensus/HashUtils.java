package org.example.app.core.consensus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    // Generate a SHA-256 hash for the given input
    public static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    // Check if a given hash is below the target difficulty
    public static boolean isHashBelowTarget(String hash, long difficulty) {
        // Simulate checking if the hash is below a target based on difficulty
        return Long.parseLong(hash.substring(0, 16), 16) < difficulty;
    }
}
