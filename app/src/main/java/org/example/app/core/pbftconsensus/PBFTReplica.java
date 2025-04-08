//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//import org.tinc.crypto.Keypair.SignatureResult;
//
//import java.util.Base64;
//import java.util.HashSet;
//import java.util.Set;
//
//public class PBFTReplica {
//
//    private final int replicaId;
//    private final Keypair keypair;
//    private final Set<String> processedMessages;
//
//    public PBFTReplica(int replicaId) {
//        this.replicaId = replicaId;
//        this.keypair = Keypair.generate();
//        this.processedMessages = new HashSet<>();
//    }
//
//    public PBFTMessage createMessage(String type, byte[] data) {
//        String digest = generateDigest(data);
//        if (isDuplicate(digest)) return null;
//
//        SignatureResult signature = signMessage(data);
//        markAsProcessed(digest);
//        return new PBFTMessage(type, replicaId, digest, signature.toString());
//    }
//
//    public boolean verifyMessage(PBFTMessage message, Keypair senderKeypair) {
//        byte[] dataToVerify = (message.getType() + message.getSenderId() + message.getDigest()).getBytes();
//        return senderKeypair.verify(message.getSignature(), dataToVerify);
//    }
//
//    public void processMessage(PBFTMessage message) {
//        if (isDuplicate(message.getDigest())) {
//            System.out.println("Duplicate message detected: " + message);
//            return;
//        }
//        markAsProcessed(message.getDigest());
//        System.out.println("Replica " + replicaId + " processed: " + message);
//    }
//
//    private String generateDigest(byte[] data) {
//        return Base64.getEncoder().encodeToString(data);
//    }
//
//    private boolean isDuplicate(String digest) {
//        return processedMessages.contains(digest);
//    }
//
//    private void markAsProcessed(String digest) {
//        processedMessages.add(digest);
//    }
//
//    public SignatureResult signMessage(byte[] data) {
//        return keypair.sign(data);
//    }
//}




package org.example.app.core.pbftconsensus;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.crypto.Keypair.SignatureResult;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * PBFTReplica represents a single node in the PBFT network.
 * Handles message creation, verification, and processing.
 */
public class PBFTReplica {
    private static final Logger logger = Logger.getLogger(PBFTReplica.class.getName());

    private final int replicaId;
    private final Keypair keypair;
    private final Set<String> processedMessages;

    /**
     * Constructor to initialize a PBFTReplica.
     *
     * @param replicaId The unique ID of this replica.
     * @throws IllegalArgumentException if replicaId is negative
     */
    public PBFTReplica(int replicaId) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }

        this.replicaId = replicaId;
        this.keypair = Keypair.generate();
        this.processedMessages = new HashSet<>();
        logger.info("Replica " + replicaId + " initialized with new keypair");
    }

    /**
     * Constructor to initialize a PBFTReplica with an existing keypair.
     *
     * @param replicaId The unique ID of this replica.
     * @param keypair The keypair to use for this replica.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public PBFTReplica(int replicaId, Keypair keypair) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }
        if (keypair == null) {
            throw new IllegalArgumentException("Keypair cannot be null");
        }

        this.replicaId = replicaId;
        this.keypair = keypair;
        this.processedMessages = new HashSet<>();
        logger.info("Replica " + replicaId + " initialized with provided keypair");
    }

    /**
     * Creates a new PBFT message of the specified type with the given data.
     *
     * @param type The message type.
     * @param data The data to include in the message.
     * @return The created PBFTMessage, or null if the message is a duplicate.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public PBFTMessage createMessage(String type, byte[] data) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Message type cannot be null or empty");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Message data cannot be null or empty");
        }

        String digest = generateDigest(data);
        if (isDuplicate(digest)) {
            logger.info("Duplicate message detected with digest: " + digest);
            return null;
        }

        SignatureResult signature = signMessage(data);
        markAsProcessed(digest);

        PBFTMessage message = new PBFTMessage(type, replicaId, digest, signature);
        logger.info("Created new " + type + " message with digest: " + digest);
        return message;
    }

    /**
     * Verifies a received message using the sender's keypair.
     *
     * @param message The message to verify.
     * @param senderKeypair The keypair of the sender for verification.
     * @return True if the message is valid, false otherwise.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public boolean verifyMessage(PBFTMessage message, Keypair senderKeypair) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (senderKeypair == null) {
            throw new IllegalArgumentException("Sender keypair cannot be null");
        }

        byte[] dataToVerify = (message.getType() + message.getSenderId() + message.getDigest()).getBytes();
        if (message.getContent() != null) {
            dataToVerify = (message.getType() + message.getSenderId() + message.getDigest() + message.getContent()).getBytes();
        }

        boolean isValid = senderKeypair.verify(message.getSignature(), dataToVerify);
        logger.info("Message verification from replica " + message.getSenderId() +
                ": " + (isValid ? "success" : "failed"));
        return isValid;
    }

    /**
     * Processes a received message, checking for duplicates.
     *
     * @param message The message to process.
     * @throws IllegalArgumentException if message is null
     */
    public void processMessage(PBFTMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (isDuplicate(message.getDigest())) {
            logger.warning("Duplicate message detected: " + message);
            return;
        }
        markAsProcessed(message.getDigest());
        logger.info("Replica " + replicaId + " processed: " + message);
    }

    /**
     * Generates a digest for the given data.
     *
     * @param data The data to hash.
     * @return A Base64-encoded digest string.
     */
    private String generateDigest(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Checks if a message with the given digest has already been processed.
     *
     * @param digest The digest to check.
     * @return True if the message has been processed before, false otherwise.
     */
    private boolean isDuplicate(String digest) {
        return processedMessages.contains(digest);
    }

    /**
     * Marks a message digest as processed.
     *
     * @param digest The digest to mark as processed.
     */
    private void markAsProcessed(String digest) {
        processedMessages.add(digest);
    }

    /**
     * Signs a message using this replica's private key.
     *
     * @param data The data to sign.
     * @return The signature.
     */
    public SignatureResult signMessage(byte[] data) {
        return keypair.sign(data);
    }

    /**
     * Gets the replica's ID.
     *
     * @return The replica ID.
     */
    public int getReplicaId() {
        return replicaId;
    }

    /**
     * Gets the replica's public key.
     *
     * @return The public key.
     */
    public java.security.PublicKey getPublicKey() {
        return keypair.getPublicKey();
    }

    /**
     * Clears the processed messages history.
     */
    public void clearProcessedMessages() {
        processedMessages.clear();
        logger.info("Processed messages history cleared for replica " + replicaId);
    }

    /**
     * Gets the number of processed messages.
     *
     * @return The count of processed messages.
     */
    public int getProcessedMessageCount() {
        return processedMessages.size();
    }
}




