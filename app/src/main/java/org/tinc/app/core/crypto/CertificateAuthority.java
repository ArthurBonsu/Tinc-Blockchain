package org.example.app.core.crypto;

import java.security.*;

/**
 * Represents a Certificate Authority (CA) that issues and verifies certificates.
 */
public class CertificateAuthority {

    private final Keypair caKeypair; // The CA's keypair

    public CertificateAuthority() {
        this.caKeypair = Keypair.generate(); // Generate CA's keypair
    }

    /**
     * Issues a certificate for a node.
     *
     * @param nodeId    The node's unique identifier.
     * @param publicKey The node's public key.
     * @return A signed certificate.
     * @throws GeneralSecurityException If signing fails.
     */
    public Certificate issueCertificate(String nodeId, PublicKey publicKey) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(caKeypair.getPrivateKey());
        signature.update(publicKey.getEncoded());

        byte[] signatureBytes = signature.sign();
        return new Certificate(nodeId, publicKey, signatureBytes);
    }

    /**
     * Verifies a certificate.
     *
     * @param certificate The certificate to verify.
     * @return True if the certificate is valid; false otherwise.
     */
    public boolean verifyCertificate(Certificate certificate) {
        try {
            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(caKeypair.getPublicKey());
            verifier.update(certificate.getPublicKey().getEncoded());
            return verifier.verify(certificate.getSignature());
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    /**
     * Gets the CA's public key.
     *
     * @return The public key.
     */
    public PublicKey getPublicKey() {
        return caKeypair.getPublicKey();
    }
}
