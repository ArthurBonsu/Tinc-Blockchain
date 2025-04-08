//package org.tinc.consensus.pbft;
//
//import javax.crypto.Cipher;
//import javax.crypto.KeyGenerator;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.ByteArrayInputStream;
//import java.security.*;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * SecureChannelManager manages secure communication between replicas.
// * - Establishes encrypted peer-to-peer communication channels.
// * - Verifies replica identities using certificates.
// * - Handles key exchange for secure message transmission.
// */
//public class SecureChannelManager {
//
//    private final Map<Integer, SecretKey> replicaKeys; // Stores shared keys for secure communication
//    private final KeyPair localKeyPair;               // Local replica's key pair for identity verification
//    private final X509Certificate localCertificate;   // Local replica's certificate
//
//    /**
//     * Constructor to initialize the SecureChannelManager.
//     *
//     * @param keySize          The size of the RSA key pair.
//     * @param certificateBytes The certificate bytes for the local replica.
//     */
//    public SecureChannelManager(int keySize, byte[] certificateBytes) {
//        try {
//            this.localKeyPair = generateKeyPair(keySize);
//            this.localCertificate = loadCertificate(certificateBytes);
//            this.replicaKeys = new ConcurrentHashMap<>();
//        } catch (Exception e) {
//            throw new RuntimeException("Error initializing SecureChannelManager", e);
//        }
//    }
//
//    /**
//     * Establishes a secure channel with a peer replica.
//     *
//     * @param replicaId          The ID of the peer replica.
//     * @param peerCertificate    The X.509 certificate of the peer replica.
//     * @param peerPublicKey      The public key of the peer replica.
//     * @return The established shared secret key.
//     */
//    public SecretKey establishSecureChannel(int replicaId, X509Certificate peerCertificate, PublicKey peerPublicKey) {
//        try {
//            // Verify the peer's certificate
//            verifyCertificate(peerCertificate);
//
//            // Perform key exchange to establish a shared secret
//            SecretKey sharedKey = performKeyExchange(peerPublicKey);
//
//            // Store the shared key for future communication
//            replicaKeys.put(replicaId, sharedKey);
//            System.out.println("Secure channel established with replica " + replicaId);
//            return sharedKey;
//        } catch (Exception e) {
//            throw new RuntimeException("Error establishing secure channel with replica " + replicaId, e);
//        }
//    }
//
//    /**
//     * Encrypts a message using the shared key for the specified replica.
//     *
//     * @param replicaId The ID of the target replica.
//     * @param message   The message to encrypt.
//     * @return The encrypted message as a Base64 string.
//     */
//    public String encryptMessage(int replicaId, String message) {
//        try {
//            SecretKey sharedKey = replicaKeys.get(replicaId);
//            if (sharedKey == null) {
//                throw new IllegalStateException("No secure channel established with replica " + replicaId);
//            }
//
//            Cipher cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
//            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
//            return Base64.getEncoder().encodeToString(encryptedBytes);
//        } catch (Exception e) {
//            throw new RuntimeException("Error encrypting message for replica " + replicaId, e);
//        }
//    }
//
//    /**
//     * Decrypts a message received from a replica using the shared key.
//     *
//     * @param replicaId The ID of the source replica.
//     * @param encryptedMessage The encrypted message as a Base64 string.
//     * @return The decrypted message.
//     */
//    public String decryptMessage(int replicaId, String encryptedMessage) {
//        try {
//            SecretKey sharedKey = replicaKeys.get(replicaId);
//            if (sharedKey == null) {
//                throw new IllegalStateException("No secure channel established with replica " + replicaId);
//            }
//
//            Cipher cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.DECRYPT_MODE, sharedKey);
//            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
//            return new String(decryptedBytes);
//        } catch (Exception e) {
//            throw new RuntimeException("Error decrypting message from replica " + replicaId, e);
//        }
//    }
//
//    /**
//     * Verifies the X.509 certificate of a peer replica.
//     *
//     * @param peerCertificate The X.509 certificate to verify.
//     * @throws Exception If the certificate is invalid.
//     */
//    private void verifyCertificate(X509Certificate peerCertificate) throws Exception {
//        // In a real implementation, verify against a trusted Certificate Authority (CA).
//        peerCertificate.checkValidity();
//        System.out.println("Certificate verified for peer: " + peerCertificate.getSubjectDN());
//    }
//
//    /**
//     * Performs key exchange to establish a shared secret key using RSA.
//     *
//     * @param peerPublicKey The public key of the peer replica.
//     * @return The established shared secret key.
//     * @throws Exception If key exchange fails.
//     */
//    private SecretKey performKeyExchange(PublicKey peerPublicKey) throws Exception {
//        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//        keyGen.init(256); // Use AES-256 for the shared key
//        SecretKey sharedKey = keyGen.generateKey();
//
//        // Encrypt the shared key using the peer's public key
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, peerPublicKey);
//        byte[] encryptedSharedKey = cipher.doFinal(sharedKey.getEncoded());
//
//        // Decrypt the shared key with the local private key (for testing/verification)
//        cipher.init(Cipher.DECRYPT_MODE, localKeyPair.getPrivate());
//        byte[] decryptedSharedKey = cipher.doFinal(encryptedSharedKey);
//
//        if (!Arrays.equals(sharedKey.getEncoded(), decryptedSharedKey)) {
//            throw new IllegalStateException("Key exchange verification failed");
//        }
//
//        return sharedKey;
//    }
//
//    /**
//     * Loads an X.509 certificate from bytes.
//     *
//     * @param certificateBytes The certificate bytes.
//     * @return The loaded X.509 certificate.
//     * @throws Exception If the certificate cannot be loaded.
//     */
//    private X509Certificate loadCertificate(byte[] certificateBytes) throws Exception {
//        CertificateFactory factory = CertificateFactory.getInstance("X.509");
//        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certificateBytes));
//    }
//
//    /**
//     * Generates an RSA key pair.
//     *
//     * @param keySize The size of the key pair.
//     * @return The generated RSA key pair.
//     * @throws Exception If key generation fails.
//     */
//    private KeyPair generateKeyPair(int keySize) throws Exception {
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//        keyGen.initialize(keySize);
//        return keyGen.generateKeyPair();
//    }
//}




package org.example.app.core.pbftconsensus;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SecureChannelManager manages secure communication between replicas.
 * - Establishes encrypted peer-to-peer communication channels.
 * - Verifies replica identities using certificates.
 * - Handles key exchange for secure message transmission.
 */
public class SecureChannelManager {
    private static final Logger logger = Logger.getLogger(SecureChannelManager.class.getName());

    private final Map<Integer, SecretKey> replicaKeys; // Stores shared keys for secure communication
    private final KeyPair localKeyPair;               // Local replica's key pair for identity verification
    private final X509Certificate localCertificate;   // Local replica's certificate

    /**
     * Constructor to initialize the SecureChannelManager.
     *
     * @param keySize          The size of the RSA key pair.
     * @param certificateBytes The certificate bytes for the local replica.
     * @throws RuntimeException if initialization fails
     */
    public SecureChannelManager(int keySize, byte[] certificateBytes) {
        if (keySize < 2048) {
            throw new IllegalArgumentException("Key size must be at least 2048 bits for security");
        }
        if (certificateBytes == null || certificateBytes.length == 0) {
            throw new IllegalArgumentException("Certificate bytes cannot be null or empty");
        }

        try {
            this.localKeyPair = generateKeyPair(keySize);
            this.localCertificate = loadCertificate(certificateBytes);
            this.replicaKeys = new ConcurrentHashMap<>();
            logger.info("SecureChannelManager initialized with RSA key size: " + keySize);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing SecureChannelManager", e);
            throw new RuntimeException("Error initializing SecureChannelManager", e);
        }
    }

    /**
     * Establishes a secure channel with a peer replica.
     *
     * @param replicaId          The ID of the peer replica.
     * @param peerCertificate    The X.509 certificate of the peer replica.
     * @param peerPublicKey      The public key of the peer replica.
     * @return The established shared secret key.
     * @throws RuntimeException if channel establishment fails
     */
    public SecretKey establishSecureChannel(int replicaId, X509Certificate peerCertificate, PublicKey peerPublicKey) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }
        if (peerCertificate == null) {
            throw new IllegalArgumentException("Peer certificate cannot be null");
        }
        if (peerPublicKey == null) {
            throw new IllegalArgumentException("Peer public key cannot be null");
        }

        try {
            // Verify the peer's certificate
            verifyCertificate(peerCertificate);

            // Perform key exchange to establish a shared secret
            SecretKey sharedKey = performKeyExchange(peerPublicKey);

            // Store the shared key for future communication
            replicaKeys.put(replicaId, sharedKey);
            logger.info("Secure channel established with replica " + replicaId);
            return sharedKey;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error establishing secure channel with replica " + replicaId, e);
            throw new RuntimeException("Error establishing secure channel with replica " + replicaId, e);
        }
    }

    /**
     * Encrypts a message using the shared key for the specified replica.
     *
     * @param replicaId The ID of the target replica.
     * @param message   The message to encrypt.
     * @return The encrypted message as a Base64 string.
     * @throws RuntimeException if encryption fails
     * @throws IllegalStateException if no secure channel exists with the replica
     */
    public String encryptMessage(int replicaId, String message) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }

        try {
            SecretKey sharedKey = replicaKeys.get(replicaId);
            if (sharedKey == null) {
                throw new IllegalStateException("No secure channel established with replica " + replicaId);
            }

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);
            logger.fine("Message encrypted for replica " + replicaId);
            return encryptedMessage;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error encrypting message for replica " + replicaId, e);
            throw new RuntimeException("Error encrypting message for replica " + replicaId, e);
        }
    }

    /**
     * Decrypts a message received from a replica using the shared key.
     *
     * @param replicaId The ID of the source replica.
     * @param encryptedMessage The encrypted message as a Base64 string.
     * @return The decrypted message.
     * @throws RuntimeException if decryption fails
     * @throws IllegalStateException if no secure channel exists with the replica
     */
    public String decryptMessage(int replicaId, String encryptedMessage) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }
        if (encryptedMessage == null || encryptedMessage.isEmpty()) {
            throw new IllegalArgumentException("Encrypted message cannot be null or empty");
        }

        try {
            SecretKey sharedKey = replicaKeys.get(replicaId);
            if (sharedKey == null) {
                throw new IllegalStateException("No secure channel established with replica " + replicaId);
            }

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sharedKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
            String decryptedMessage = new String(decryptedBytes);
            logger.fine("Message decrypted from replica " + replicaId);
            return decryptedMessage;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error decrypting message from replica " + replicaId, e);
            throw new RuntimeException("Error decrypting message from replica " + replicaId, e);
        }
    }

    /**
     * Verifies the X.509 certificate of a peer replica.
     *
     * @param peerCertificate The X.509 certificate to verify.
     * @throws Exception If the certificate is invalid.
     */
    private void verifyCertificate(X509Certificate peerCertificate) throws Exception {
        // Verify certificate validity period
        try {
            peerCertificate.checkValidity();
            logger.info("Certificate verified for peer: " + peerCertificate.getSubjectDN());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Certificate validation failed", e);
            throw e;
        }

        // In a production environment, you would also verify the certificate against a trusted CA
        // This would typically involve signature verification using the CA's public key
    }

    /**
     * Performs key exchange to establish a shared secret key using RSA.
     *
     * @param peerPublicKey The public key of the peer replica.
     * @return The established shared secret key.
     * @throws Exception If key exchange fails.
     */
    private SecretKey performKeyExchange(PublicKey peerPublicKey) throws Exception {
        try {
            // Generate an AES key to use as the shared key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // Use AES-256 for the shared key
            SecretKey sharedKey = keyGen.generateKey();

            // In a real system, you would encrypt this shared key with the peer's public key
            // and send it to the peer
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, peerPublicKey);
            byte[] encryptedSharedKey = cipher.doFinal(sharedKey.getEncoded());

            // For testing/verification, we decrypt the shared key locally
            // In a real system, the peer would do this
            cipher.init(Cipher.DECRYPT_MODE, localKeyPair.getPrivate());
            byte[] decryptedSharedKey = cipher.doFinal(encryptedSharedKey);

            // Verify that the key exchange worked correctly
            if (!Arrays.equals(sharedKey.getEncoded(), decryptedSharedKey)) {
                throw new IllegalStateException("Key exchange verification failed");
            }

            logger.info("Key exchange completed successfully");
            return sharedKey;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Key exchange failed", e);
            throw e;
        }
    }

    /**
     * Loads an X.509 certificate from bytes.
     *
     * @param certificateBytes The certificate bytes.
     * @return The loaded X.509 certificate.
     * @throws CertificateException If the certificate cannot be loaded.
     */
    private X509Certificate loadCertificate(byte[] certificateBytes) throws CertificateException {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        } catch (CertificateException e) {
            logger.log(Level.SEVERE, "Failed to load certificate", e);
            throw e;
        }
    }

    /**
     * Generates an RSA key pair.
     *
     * @param keySize The size of the key pair.
     * @return The generated RSA key pair.
     * @throws NoSuchAlgorithmException If key generation fails.
     */
    private KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Failed to generate key pair", e);
            throw e;
        }
    }

    /**
     * Gets the local X.509 certificate.
     *
     * @return The local certificate.
     */
    public X509Certificate getLocalCertificate() {
        return localCertificate;
    }

    /**
     * Gets the local public key.
     *
     * @return The local public key.
     */
    public PublicKey getLocalPublicKey() {
        return localKeyPair.getPublic();
    }

    /**
     * Removes a secure channel with a replica.
     *
     * @param replicaId The ID of the replica.
     * @return true if a channel was removed, false otherwise.
     */
    public boolean removeSecureChannel(int replicaId) {
        SecretKey removedKey = replicaKeys.remove(replicaId);
        if (removedKey != null) {
            logger.info("Secure channel with replica " + replicaId + " removed");
            return true;
        }
        return false;
    }

    /**
     * Checks if a secure channel exists with a replica.
     *
     * @param replicaId The ID of the replica.
     * @return true if a secure channel exists, false otherwise.
     */
    public boolean hasSecureChannel(int replicaId) {
        return replicaKeys.containsKey(replicaId);
    }

    /**
     * Gets the number of established secure channels.
     *
     * @return The count of secure channels.
     */
    public int getSecureChannelCount() {
        return replicaKeys.size();
    }
}



