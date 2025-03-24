// KeyUtils.java
package org.example.app.core.wallet;

import java.security.*;
import java.security.spec.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class KeyUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", 
                                                             new BouncyCastleProvider());
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        
        try {
            keyGen.initialize(ecSpec, random);
            return keyGen.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

    public static String getAddress(PublicKey publicKey) {
        try {
            // Get the public key bytes
            byte[] publicKeyBytes = publicKey.getEncoded();
            
            // Hash the public key using Keccak-256
            byte[] hashBytes = keccak256(publicKeyBytes);
            
            // Take the last 20 bytes
            byte[] addressBytes = new byte[20];
            System.arraycopy(hashBytes, hashBytes.length - 20, addressBytes, 0, 20);
            
            // Convert to hex string
            return "0x" + Hex.toHexString(addressBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate address", e);
        }
    }

    private static byte[] keccak256(byte[] input) {
        // Implement Keccak-256 hashing
        // Note: You'll need to add a dependency for a Keccak implementation
        return new byte[32]; // Placeholder
    }

    public static boolean verifySignature(byte[] message, byte[] signature, 
                                        PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(message);
            return sig.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }

    public static byte[] sign(byte[] message, PrivateKey privateKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initSign(privateKey);
            sig.update(message);
            return sig.sign();
        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }
}