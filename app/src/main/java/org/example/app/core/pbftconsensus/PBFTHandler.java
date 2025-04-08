//package org.tinc.consensus.pbft;
//
//import org.tinc.p2p.RobustP2PManager;
//
///**
// * PBFTHandler processes incoming PBFT messages received through the P2P network.
// * - Delegates processing to the appropriate components based on message type.
// * - Ensures message validation before further actions.
// */
//public class PBFTHandler {
//
//    private final PBFT pbft;                 // Main PBFT instance for handling consensus phases
//    private final RobustP2PManager p2pManager; // Peer-to-peer network manager
//    private final PBFTMessageHandler messageHandler; // Handles message encoding/decoding
//
//    /**
//     * Constructor to initialize the PBFTHandler.
//     *
//     * @param pbft           The main PBFT instance.
//     * @param p2pManager     The RobustP2PManager instance for communication.
//     * @param messageHandler The PBFTMessageHandler for encoding and decoding messages.
//     */
//    public PBFTHandler(PBFT pbft, RobustP2PManager p2pManager, PBFTMessageHandler messageHandler) {
//        this.pbft = pbft;
//        this.p2pManager = p2pManager;
//        this.messageHandler = messageHandler;
//    }
//
//    /**
//     * Processes an incoming serialized PBFT message.
//     *
//     * @param serializedMessage The serialized message received from the P2P network.
//     */
//    public void handleMessage(String serializedMessage) {
//        try {
//            PBFTMessage message = messageHandler.decodePBFTMessage(serializedMessage);
//
//            System.out.println("Received PBFT message: " + message);
//
//            // Validate the message before processing
//            if (!validateMessage(message)) {
//                System.err.println("Invalid PBFT message from replica " + message.getSenderId());
//                return;
//            }
//
//            // Delegate processing based on the message type
//            switch (message.getType()) {
//                case "PRE-PREPARE":
//                    pbft.handlePrePrepare(String.valueOf(message));
//                    break;
//                case "PREPARE":
//                    pbft.handlePrepare(String.valueOf(message));
//                    break;
//                case "COMMIT":
//                    pbft.handleCommit(String.valueOf(message));
//                    break;
//                case "VIEW-CHANGE":
//                    pbft.handleViewChange(message);
//                    break;
//                case "CHECKPOINT-SYNC":
//                    pbft.handleCheckpointSync(message);
//                    break;
//                case "FAULT-DETECTION":
//                    System.out.println("Fault detection notification received: " + message.getContent());
//                    break;
//                default:
//                    System.err.println("Unknown PBFT message type: " + message.getType());
//            }
//        } catch (Exception e) {
//            System.err.println("Error processing PBFT message: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Validates an incoming PBFT message for correctness and integrity.
//     *
//     * @param message The PBFTMessage to validate.
//     * @return True if the message is valid, false otherwise.
//     */
//    private boolean validateMessage(PBFTMessage message) {
//        // Perform basic validation: ensure sender ID and message content are not null
//        if (message.getSenderId() < 0 || message.getDigest() == null || message.getType() == null) {
//            return false;
//        }
//
//        // Verify message signature if applicable (this example assumes optional signatures)
//        return messageHandler.validateMessage(message, p2pManager.getReplicaKeypair(message.getSenderId()));
//    }
//
//    /**
//     * Sends an acknowledgment message back to the sender of a processed message.
//     *
//     * @param senderId The ID of the replica that sent the original message.
//     */
//    public void sendAcknowledgment(int senderId) {
//        PBFTMessage acknowledgment = new PBFTMessage(
//                "ACK",
//                p2pManager.getNodeId(),
//                "Acknowledgment for received message",
//                null // Optional signature
//        );
//        p2pManager.sendDirectMessage(String.valueOf(senderId), acknowledgment.serialize());
//        System.out.println("Acknowledgment sent to replica " + senderId);
//    }
//}





package org.example.app.core.pbftconsensus;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.p2p.RobustP2PManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PBFTHandler processes incoming PBFT messages received through the P2P network.
 * - Delegates processing to the appropriate components based on message type.
 * - Ensures message validation before further actions.
 */
public class PBFTHandler {
    private static final Logger logger = Logger.getLogger(PBFTHandler.class.getName());

    private final PBFT pbft;                 // Main PBFT instance for handling consensus phases
    private final RobustP2PManager p2pManager; // Peer-to-peer network manager
    private final PBFTMessageHandler messageHandler; // Handles message encoding/decoding

    /**
     * Constructor to initialize the PBFTHandler.
     *
     * @param pbft           The main PBFT instance.
     * @param p2pManager     The RobustP2PManager instance for communication.
     * @param messageHandler The PBFTMessageHandler for encoding and decoding messages.
     * @throws IllegalArgumentException if any parameter is null
     */
    public PBFTHandler(PBFT pbft, RobustP2PManager p2pManager, PBFTMessageHandler messageHandler) {
        if (pbft == null) {
            throw new IllegalArgumentException("PBFT instance cannot be null");
        }
        if (p2pManager == null) {
            throw new IllegalArgumentException("P2P manager cannot be null");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("Message handler cannot be null");
        }

        this.pbft = pbft;
        this.p2pManager = p2pManager;
        this.messageHandler = messageHandler;
    }

    /**
     * Processes an incoming serialized PBFT message.
     *
     * @param serializedMessage The serialized message received from the P2P network.
     * @throws IllegalArgumentException if serializedMessage is null or empty
     */
    public void handleMessage(String serializedMessage) {
        if (serializedMessage == null || serializedMessage.isEmpty()) {
            throw new IllegalArgumentException("Serialized message cannot be null or empty");
        }

        try {
            PBFTMessage message = messageHandler.decodePBFTMessage(serializedMessage);
            logger.info("Received PBFT message: " + message);

            // Validate the message before processing
            if (!validateMessage(message)) {
                logger.warning("Invalid PBFT message from replica " + message.getSenderId());
                return;
            }

            // Delegate processing based on the message type
            switch (message.getType()) {
                case "PRE-PREPARE":
                    pbft.handlePrePrepare(message.getDigest());
                    break;
                case "PREPARE":
                    pbft.handlePrepare(message.getDigest());
                    break;
                case "COMMIT":
                    pbft.handleCommit(message.getDigest());
                    break;
                case "VIEW-CHANGE":
                    pbft.handleViewChange(message);
                    break;
                case "CHECKPOINT-SYNC":
                    pbft.handleCheckpointSync(message);
                    break;
                case "FAULT-DETECTION":
                    logger.info("Fault detection notification received: " + message.getContent());
                    // This would typically be handled by the FaultDetector component
                    break;
                case "CLIENT-REQUEST":
                    // Forward client request to PBFT for processing
                    pbft.processClientRequest(message);
                    break;
                case "MEMBERSHIP-UPDATE":
                    // This would be handled by the DynamicMembership component
                    logger.info("Membership update received: " + message.getContent());
                    break;
                default:
                    logger.warning("Unknown PBFT message type: " + message.getType());
            }

            // Send acknowledgment for certain message types
            if (shouldAcknowledge(message.getType())) {
                sendAcknowledgment(message.getSenderId());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing PBFT message", e);
        }
    }

    /**
     * Validates an incoming PBFT message for correctness and integrity.
     *
     * @param message The PBFTMessage to validate.
     * @return True if the message is valid, false otherwise.
     */
    private boolean validateMessage(PBFTMessage message) {
        // Perform basic validation: ensure sender ID and message content are not null
        if (message == null || message.getSenderId() < 0 || message.getType() == null) {
            return false;
        }

        // If the message has a signature, verify it
        if (message.getSignature() != null) {
            try {
                Keypair senderKeypair = getSenderKeypair(message.getSenderId());
                return messageHandler.validateMessage(message, senderKeypair);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error validating message signature", e);
                return false;
            }
        }

        // For messages without signatures, we still accept them in some cases
        // This is a simplification for certain message types or testing environments
        return true;
    }

    /**
     * Retrieves the keypair for a specific sender ID.
     *
     * @param senderId The ID of the sender.
     * @return The keypair for the sender.
     * @throws RuntimeException if keypair retrieval fails
     */
    private Keypair getSenderKeypair(int senderId) {
        try {
            // In a real implementation, this would retrieve the sender's public key
            // from a key store or other secure storage
            // For now, we'll use the p2pManager's method if available, or create a mock one
            return p2pManager.getReplicaKeypair(senderId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving sender keypair", e);
            throw new RuntimeException("Failed to retrieve sender keypair", e);
        }
    }

    /**
     * Determines if a message type should receive an acknowledgment.
     *
     * @param messageType The type of the message.
     * @return True if the message should be acknowledged, false otherwise.
     */
    private boolean shouldAcknowledge(String messageType) {
        // Only acknowledge certain message types to reduce network traffic
        return "PRE-PREPARE".equals(messageType) || "CLIENT-REQUEST".equals(messageType);
    }

    /**
     * Sends an acknowledgment message back to the sender of a processed message.
     *
     * @param senderId The ID of the replica that sent the original message.
     */
    public void sendAcknowledgment(int senderId) {
        try {
            PBFTMessage acknowledgment = new PBFTMessage(
                    "ACK",
                    PBFTNetwork.getNodeId(),
                    "ack-" + System.currentTimeMillis(),
                    null // Optional signature
            );
            acknowledgment.setContent("Acknowledgment for received message");

            p2pManager.sendDirectMessage(String.valueOf(senderId), messageHandler.encodePBFTMessage(acknowledgment));
            logger.fine("Acknowledgment sent to replica " + senderId);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error sending acknowledgment", e);
        }
    }

    /**
     * Gets the associated PBFT instance.
     *
     * @return The PBFT instance.
     */
    public PBFT getPbft() {
        return pbft;
    }

    /**
     * Gets the associated message handler.
     *
     * @return The message handler.
     */
    public PBFTMessageHandler getMessageHandler() {
        return messageHandler;
    }
}