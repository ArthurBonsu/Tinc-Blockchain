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
