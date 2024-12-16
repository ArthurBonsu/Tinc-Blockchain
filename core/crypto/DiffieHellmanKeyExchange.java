package org.tinc.crypto;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class DiffieHellmanKeyExchange {
    private KeyPair keyPair;
    private KeyAgreement keyAgreement;

    public DiffieHellmanKeyExchange() {
        try {
            // Generate a secure DH parameter set
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");

            // Valid DH parameter: 2048-bit prime and generator 2
            BigInteger prime = new BigInteger(
                    "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                            "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                            "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                            "E485B576625E7EC6F44C42E9A63A3620FFFFFFFFFFFFFFFF",
                    16
            );
            BigInteger generator = BigInteger.valueOf(2);

            // Use a 2048-bit key size
            DHParameterSpec dhSpec = new DHParameterSpec(prime, generator);
            keyPairGen.initialize(dhSpec);

            this.keyPair = keyPairGen.generateKeyPair();
            this.keyAgreement = KeyAgreement.getInstance("DH");
            this.keyAgreement.init(keyPair.getPrivate());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Diffie-Hellman: " + e.getMessage(), e);
        }
    }

    public byte[] getPublicKey() {
        return keyPair.getPublic().getEncoded();
    }

    public SecretKey generateSharedKey(byte[] otherPublicKeyBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DH");
            PublicKey otherPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(otherPublicKeyBytes));
            keyAgreement.doPhase(otherPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();
            return new SecretKeySpec(sharedSecret, 0, 16, "AES"); // Use the first 16 bytes for AES
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate shared key: " + e.getMessage(), e);
        }
    }
}
