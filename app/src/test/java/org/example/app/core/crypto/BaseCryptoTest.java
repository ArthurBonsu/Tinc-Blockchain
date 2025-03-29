package org.example.app.core.crypto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.SecretKey;
import org.example.app.core.types.Address;

/**
 * Test class for blockchain cryptography components
 */
public class BaseCryptoTest {
    
    @TempDir
    File tempDir;
    
    private Keypair keypair;
    private CertificateAuthority ca;
    private PKIManager pkiManager;
    private DiffieHellmanKeyExchange dhExchange;
    private File dbFile;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize cryptography components for testing
        keypair = Keypair.generate();
        ca = new CertificateAuthority();
        pkiManager = new PKIManager();
        dhExchange = new DiffieHellmanKeyExchange();
        
        // Create temporary database file for PublicKeyManager tests
        dbFile = new File(tempDir, "test-keys.db");
        PublicKeyManager.setDatabaseFile(dbFile.getAbsolutePath());
    }
    
    @AfterEach
    public void tearDown() {
        // Ensure the database file is deleted after test
        if (dbFile != null && dbFile.exists()) {
            dbFile.delete();
        }
    }
    
    @Test
    public void testKeypairGeneration() {
        // Test keypair generation
        Keypair generatedKeypair = Keypair.generate();
        
        assertNotNull(generatedKeypair, "Generated keypair should not be null");
        assertNotNull(generatedKeypair.getPrivateKey(), "Private key should not be null");
        assertNotNull(generatedKeypair.getPublicKey(), "Public key should not be null");
    }
    
    @Test
    public void testKeypairSignAndVerify() {
        // Test signature creation and verification
        byte[] data = "Test data for signing".getBytes();
        Keypair.SignatureResult signature = keypair.sign(data);
        
        assertNotNull(signature, "Signature should not be null");
        assertNotNull(signature.getR(), "R value should not be null");
        assertNotNull(signature.getS(), "S value should not be null");
        
        boolean verified = keypair.verify(signature, data);
        assertTrue(verified, "Signature should be valid for original data");
        
        // Test with different data
        byte[] differentData = "Different data".getBytes();
        boolean invalidVerification = keypair.verify(signature, differentData);
        assertFalse(invalidVerification, "Signature should not be valid for different data");
    }
    
    @Test
    public void testKeypairAddress() {
        // Test address derivation from keypair
        Address address = keypair.getAddress();
        
        assertNotNull(address, "Derived address should not be null");
    }
    
    @Test
    public void testCertificateCreation() throws Exception {
        // Test certificate creation and verification
        PublicKey publicKey = keypair.getPublicKey();
        Certificate cert = ca.issueCertificate("node1", publicKey);
        
        assertNotNull(cert, "Certificate should not be null");
        assertEquals("node1", cert.getNodeId(), "Certificate nodeId should match");
        assertEquals(publicKey, cert.getPublicKey(), "Certificate public key should match");
        assertNotNull(cert.getSignature(), "Certificate signature should not be null");
        
        boolean isValid = ca.verifyCertificate(cert);
        assertTrue(isValid, "Certificate should be valid");
    }
    
    @Test
    public void testPKIManagerCertificateRequest() throws Exception {
        // Test PKI manager certificate request process
        pkiManager.requestCertificate(ca);
        
        assertNotNull(pkiManager.certificate, "Certificate should be issued");
        boolean isValid = ca.verifyCertificate(pkiManager.certificate);
        assertTrue(isValid, "Certificate should be valid");
    }
    
    @Test
    public void testPKIManagerCertificateVerification() throws Exception {
        // Test PKI manager certificate verification
        pkiManager.requestCertificate(ca);
        
        boolean isValid = pkiManager.verifyCertificate(pkiManager.certificate, ca);
        assertTrue(isValid, "PKI Manager should verify the certificate");
    }
    
    @Test
    public void testDiffieHellmanKeyExchange() {
        // Test Diffie-Hellman key exchange
        DiffieHellmanKeyExchange dhA = new DiffieHellmanKeyExchange();
        DiffieHellmanKeyExchange dhB = new DiffieHellmanKeyExchange();
        
        byte[] publicKeyA = dhA.getPublicKey();
        byte[] publicKeyB = dhB.getPublicKey();
        
        assertNotNull(publicKeyA, "DH public key A should not be null");
        assertNotNull(publicKeyB, "DH public key B should not be null");
        
        SecretKey sharedKeyA = dhA.generateSharedKey(publicKeyB);
        SecretKey sharedKeyB = dhB.generateSharedKey(publicKeyA);
        
        assertNotNull(sharedKeyA, "Shared key A should not be null");
        assertNotNull(sharedKeyB, "Shared key B should not be null");
        
        // Test that shared secrets are equal
        assertArrayEquals(sharedKeyA.getEncoded(), sharedKeyB.getEncoded(), "Shared keys should be equal");
    }
    
    @Test
    public void testSymmetricKeyEncryptionDecryption() {
        // Test symmetric key generation, encryption, and decryption
        SecretKey key = SymmetricKeyManager.generateSymmetricKey();
        assertNotNull(key, "Generated symmetric key should not be null");
        
        String plaintext = "This is a test message for encryption";
        String encrypted = SymmetricKeyManager.encrypt(plaintext, key);
        
        assertNotNull(encrypted, "Encrypted text should not be null");
        assertNotEquals(plaintext, encrypted, "Encrypted text should be different from plaintext");
        
        String decrypted = SymmetricKeyManager.decrypt(encrypted, key);
        assertEquals(plaintext, decrypted, "Decrypted text should match the original plaintext");
    }
    
    @Test
    public void testPublicKeyManagerKeypairStorage() {
        // Test storing and retrieving a keypair
        String nodeId = "test-node-" + System.currentTimeMillis();
        Keypair originalKeypair = Keypair.generate();
        
        // Store the keypair
        PublicKeyManager.storeKeypair(originalKeypair, nodeId);
        
        // Retrieve the keypair
        Keypair retrievedKeypair = PublicKeyManager.loadKeypair(nodeId);
        
        assertNotNull(retrievedKeypair, "Retrieved keypair should not be null");
        
        // Compare public keys (note: PrivateKey doesn't typically implement equals)
        byte[] originalPubKeyEncoded = originalKeypair.getPublicKey().getEncoded();
        byte[] retrievedPubKeyEncoded = retrievedKeypair.getPublicKey().getEncoded();
        assertArrayEquals(originalPubKeyEncoded, retrievedPubKeyEncoded, "Public keys should match");
        
        // Test the retrieved keypair functionality
        byte[] testData = "Test verification after storage".getBytes();
        Keypair.SignatureResult signature = originalKeypair.sign(testData);
        boolean verified = retrievedKeypair.verify(signature, testData);
        
        assertTrue(verified, "Retrieved keypair should verify signatures from original keypair");
    }
    
    @Test
    public void testEndToEndCryptoFlow() throws Exception {
        // Test full crypto flow: certificate issuance, key exchange, encryption/decryption
        
        // 1. Set up two nodes with certificates
        PKIManager nodeA = new PKIManager();
        PKIManager nodeB = new PKIManager();
        
        nodeA.requestCertificate(ca);
        nodeB.requestCertificate(ca);
        
        assertTrue(ca.verifyCertificate(nodeA.certificate), "Node A certificate should be valid");
        assertTrue(ca.verifyCertificate(nodeB.certificate), "Node B certificate should be valid");
        
        // 2. Exchange keys using Diffie-Hellman
        DiffieHellmanKeyExchange dhA = new DiffieHellmanKeyExchange();
        DiffieHellmanKeyExchange dhB = new DiffieHellmanKeyExchange();
        
        byte[] publicKeyA = dhA.getPublicKey();
        byte[] publicKeyB = dhB.getPublicKey();
        
        SecretKey sharedKeyA = dhA.generateSharedKey(publicKeyB);
        SecretKey sharedKeyB = dhB.generateSharedKey(publicKeyA);
        
        assertArrayEquals(sharedKeyA.getEncoded(), sharedKeyB.getEncoded(), "Shared keys should be equal");
        
        // 3. Node A encrypts a message for Node B
        String message = "Confidential blockchain data";
        String encrypted = SymmetricKeyManager.encrypt(message, sharedKeyA);
        
        // 4. Node B decrypts the message
        String decrypted = SymmetricKeyManager.decrypt(encrypted, sharedKeyB);
        
        assertEquals(message, decrypted, "Decrypted message should match original");
        
        // 5. Node A signs a message for Node B to verify
        byte[] data = "Important blockchain state".getBytes();
        Keypair.SignatureResult signature = nodeA.keypair.sign(data);
        
        // 6. Node B verifies the signature using Node A's public key
        boolean verified = nodeB.keypair.verify(signature, data);
        
        assertTrue(verified, "Node B should verify Node A's signature");
    }
}
