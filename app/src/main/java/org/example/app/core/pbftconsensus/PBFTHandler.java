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
