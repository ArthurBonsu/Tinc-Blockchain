package org.tinc.crypto;

import org.tinc.types.Address;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

public class Keypair {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Keypair(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public static Keypair generate() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
            java.security.KeyPair keyPair = keyGen.generateKeyPair();
            return new Keypair(keyPair.getPrivate(), keyPair.getPublic());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Keypair: " + e.getMessage(), e);
        }
    }

    public SignatureResult sign(byte[] data) {
        try {
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(data);
            byte[] signature = ecdsaSign.sign();

            return decodeSignature(signature);
        } catch (Exception e) {
            throw new RuntimeException("Signing failed: " + e.getMessage(), e);
        }
    }

    public boolean verify(SignatureResult signature, byte[] data) {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data);

            return ecdsaVerify.verify(encodeSignature(signature));
        } catch (Exception e) {
            throw new RuntimeException("Verification failed: " + e.getMessage(), e);
        }
    }

    public Address getAddress() {
        try {
            byte[] pubKeyBytes = publicKey.getEncoded();
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(pubKeyBytes);

            return Address.fromBytes(Arrays.copyOfRange(hash, hash.length - 20, hash.length));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found: " + e.getMessage(), e);
        }
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    private SignatureResult decodeSignature(byte[] signature) {
        try {
            int rLength = signature[3];
            BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature, 4, 4 + rLength));
            int sOffset = 4 + rLength + 2; // Skip past the R's length field and marker
            BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, sOffset, sOffset + signature[sOffset - 1]));

            return new SignatureResult(r, s);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode signature: " + e.getMessage(), e);
        }
    }

    private byte[] encodeSignature(SignatureResult signature) {
        try {
            byte[] rBytes = signature.getR().toByteArray();
            byte[] sBytes = signature.getS().toByteArray();

            int totalLength = 6 + rBytes.length + sBytes.length;
            byte[] derSignature = new byte[totalLength];
            derSignature[0] = 0x30; // SEQUENCE
            derSignature[1] = (byte) (totalLength - 2);
            derSignature[2] = 0x02; // INTEGER
            derSignature[3] = (byte) rBytes.length;
            System.arraycopy(rBytes, 0, derSignature, 4, rBytes.length);
            int sOffset = 4 + rBytes.length + 2;
            derSignature[4 + rBytes.length] = 0x02; // INTEGER
            derSignature[4 + rBytes.length + 1] = (byte) sBytes.length;
            System.arraycopy(sBytes, 0, derSignature, sOffset, sBytes.length);

            return derSignature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode signature: " + e.getMessage(), e);
        }
    }

    public PrivateKey getPrivateKey() {
        return (PrivateKey) this.privateKey;
    }

    public static class SignatureResult {
        private final BigInteger r;
        private final BigInteger s;

        public SignatureResult(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        public BigInteger getR() {
            return r;
        }

        public BigInteger getS() {
            return s;
        }
    }
}
