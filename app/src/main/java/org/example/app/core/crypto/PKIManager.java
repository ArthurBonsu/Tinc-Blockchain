package org.example.app.core.crypto;

import java.security.*;
import java.util.Base64;

/**
 * PKI Manager for managing keys and certificates for nodes.
 */
public class PKIManager {

    final Keypair keypair; // The local node's keypair
    Certificate certificate; // The signed certificate for the node

    public PKIManager() {
        this.keypair = Keypair.generate(); // Generate a new keypair
    }

    /**
     * Gets the node's public key.
     *
     * @return The public key.
     */
    public PublicKey getPublicKey() {
        return keypair.getPublicKey();
    }

    /**
     * Requests a certificate from the CA.
     *
     * @param ca The certificate authority.
     * @throws GeneralSecurityException If signing fails.
     */
    public void requestCertificate(CertificateAuthority ca) throws GeneralSecurityException {
        String nodeId = Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
        certificate = ca.issueCertificate(nodeId, getPublicKey());
        System.out.println("PKIManager: Certificate received: " + certificate);
    }

    /**
     * Verifies a peer's certificate with the CA.
     *
     * @param certificate The peer's certificate.
     * @param ca          The certificate authority.
     * @return True if the certificate is valid; false otherwise.
     */
    public boolean verifyCertificate(Certificate certificate, CertificateAuthority ca) {
        return ca.verifyCertificate(certificate);
    }
}
