package core.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    // Utility method to calculate the SHA-256 hash of a string
    public static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
