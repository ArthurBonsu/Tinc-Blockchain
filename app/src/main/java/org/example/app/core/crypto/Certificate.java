package org.example.app.core.crypto;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Represents a certificate issued by the Certificate Authority.
 */
public class Certificate implements Serializable {

    private final String nodeId;      // Node's unique identifier
    private final PublicKey publicKey; // Node's public key
    private final byte[] signature;   // CA's signature

    public Certificate(String nodeId, PublicKey publicKey, byte[] signature) {
        this.nodeId = nodeId;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public String getNodeId() {
        return nodeId;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "nodeId='" + nodeId + '\'' +
                ", publicKey=" + Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                ", signature=" + Base64.getEncoder().encodeToString(signature) +
                '}';
    }
}
