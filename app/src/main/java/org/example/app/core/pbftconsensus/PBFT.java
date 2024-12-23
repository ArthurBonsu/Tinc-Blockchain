//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//import org.tinc.crypto.PublicKeyManager;
//import org.tinc.p2p.RobustP2PManager;
//
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * PBFT class implements the Practical Byzantine Fault Tolerance protocol.
// * - Handles all PBFT phases and manages state synchronization and view changes.
// */
//public class PBFT {
//
//    private final RobustP2PManager p2pManager;         // P2P manager for communication
//    private final int replicaId;                      // Unique ID of this replica
//    private final int totalReplicas;                  // Total replicas in the network
//    private final int maxFaulty;                      // Maximum faulty replicas tolerated
//    private final PBFTMessageHandler messageHandler;  // Handles encoding/decoding PBFT messages
//    private final ViewChangeHandler viewChangeHandler; // Manages view change processes
//    private final StateSynchronization stateSynchronization; // Handles state synchronization
//
//    private final Set<String> committedDigests;       // Tracks committed message digests
//
//    /**
//     * Constructor to initialize PBFT.
//     *
//     * @param p2pManager            The RobustP2PManager instance.
//     * @param replicaId             The ID of this replica.
//     * @param totalReplicas         Total replicas in the network.
//     * @param viewChangeHandler     The ViewChangeHandler instance.
//     * @param stateSynchronization  The StateSynchronization instance.
//     */
//    public PBFT(RobustP2PManager p2pManager, int replicaId, int totalReplicas,
//                ViewChangeHandler viewChangeHandler, StateSynchronization stateSynchronization) {
//        this.p2pManager = p2pManager;
//        this.replicaId = replicaId;
//        this.totalReplicas = totalReplicas;
//        this.maxFaulty = (totalReplicas - 1) / 3;
//        this.messageHandler = new PBFTMessageHandler();
//        this.viewChangeHandler = viewChangeHandler;
//        this.stateSynchronization = stateSynchronization;
//        this.committedDigests = ConcurrentHashMap.newKeySet();
//    }
//
//    /**
//     * Processes an incoming PBFT message and routes it to the appropriate handler.
//     *
//     * @param serializedMessage The serialized message received from the P2P network.
//     */
//    public void processMessage(String serializedMessage) {
//        try {
//            PBFTMessage message = messageHandler.decodePBFTMessage(serializedMessage);
//            if (!validateMessage(message)) {
//                System.err.println("Invalid PBFT message: " + message);
//                return;
//            }
//            handleMessage(message);
//        } catch (Exception e) {
//            System.err.println("Error processing PBFT message: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Validates the integrity of a PBFT message.
//     *
//     * @param message The message to validate.
//     * @return True if valid, false otherwise.
//     */
//    private boolean validateMessage(PBFTMessage message) {
//        if (message.getSenderId() < 0 || message.getDigest() == null || message.getType() == null) {
//            return false;
//        }
//        return messageHandler.validateMessage(message, getReplicaKeypair(message.getSenderId()));
//    }
//
//    /**
//     * Routes a valid PBFT message to the corresponding handler based on its type.
//     *
//     * @param message The PBFTMessage to handle.
//     */
//    private void handleMessage(PBFTMessage message) {
//        switch (message.getType()) {
//            case "PRE-PREPARE":
//                handlePrePrepare(message.getDigest());
//                break;
//            case "PREPARE":
//                handlePrepare(message.getDigest());
//                break;
//            case "COMMIT":
//                handleCommit(message.getDigest());
//                break;
//            case "VIEW-CHANGE":
//                viewChangeHandler.handleViewChangeMessage(message.getSenderId(), Integer.parseInt(message.getContent()));
//                break;
//            case "STATE-UPDATE":
//                stateSynchronization.handleStateUpdate(message);
//                break;
//            default:
//                System.err.println("Unknown PBFT message type: " + message.getType());
//        }
//    }
//
//    /**
//     * Initiates the Pre-Prepare phase of PBFT.
//     *
//     * @param clientRequest The client request to process.
//     */
//    public void handlePrePrepare(String clientRequest) {
//        String digest = computeDigest(clientRequest);
//        PBFTMessage prePrepareMessage = new PBFTMessage("PRE-PREPARE", replicaId, digest, null);
//        broadcastMessage(prePrepareMessage);
//        System.out.println("Pre-Prepare initiated with digest: " + digest);
//    }
//
//    /**
//     * Handles the Prepare phase of PBFT.
//     *
//     * @param digest The digest received in the Pre-Prepare phase.
//     */
//    public void handlePrepare(String digest) {
//        PBFTMessage prepareMessage = new PBFTMessage("PREPARE", replicaId, digest, null);
//        broadcastMessage(prepareMessage);
//        System.out.println("Prepare phase completed for digest: " + digest);
//    }
//
//    /**
//     * Handles the Commit phase of PBFT.
//     *
//     * @param digest The digest received in the Prepare phase.
//     */
//    public void handleCommit(String digest) {
//        if (committedDigests.contains(digest)) {
//            System.out.println("Commit already processed for digest: " + digest);
//            return;
//        }
//        PBFTMessage commitMessage = new PBFTMessage("COMMIT", replicaId, digest, null);
//        broadcastMessage(commitMessage);
//        committedDigests.add(digest);
//        System.out.println("Commit phase completed for digest: " + digest);
//    }
//
//    /**
//     * Computes a digest for a given input.
//     *
//     * @param data The input data.
//     * @return The computed digest.
//     */
//    private String computeDigest(String data) {
//        return Integer.toHexString(data.hashCode());
//    }
//
//    /**
//     * Broadcasts a PBFT message to all replicas.
//     *
//     * @param message The PBFTMessage to broadcast.
//     */
//    private void broadcastMessage(PBFTMessage message) {
//        try {
//            String serializedMessage = messageHandler.encodePBFTMessage(message);
//            p2pManager.sendBroadcast(serializedMessage);
//            System.out.println("Broadcasted message: " + message);
//        } catch (Exception e) {
//            System.err.println("Error broadcasting message: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Retrieves the replica's keypair for signing or verification.
//     *
//     * @param senderId The sender's ID.
//     * @return The corresponding keypair.
//     */
//    private Keypair getReplicaKeypair(int senderId) {
//        String nodeId = String.valueOf(senderId);
//        System.out.println("Fetching keypair for replica: " + nodeId);
//
//        try {
//            // Attempt to load the keypair from the database
//            return PublicKeyManager.loadKeypair(nodeId);
//        } catch (RuntimeException e) {
//            // Handle case where keypair is not found
//            if (e.getMessage().contains("Keypair not found")) {
//                System.out.println("Keypair not found for replica: " + nodeId + ". Generating new keypair.");
//                Keypair keypair = PublicKeyManager.generateKeypair();
//                PublicKeyManager.storeKeypair(keypair, nodeId);
//                return keypair;
//            }
//            // Re-throw unexpected exceptions
//            throw e;
//        }
//    }
//
//
//    /**
//     * Handles View Change requests by delegating to the ViewChangeHandler.
//     *
//     * @param viewChangeMessage The message containing view change information.
//     */
//    public void handleViewChange(PBFTMessage viewChangeMessage) {
//        int proposedView = Integer.parseInt(viewChangeMessage.getContent());
//        viewChangeHandler.handleViewChangeMessage(viewChangeMessage.getSenderId(), proposedView);
//    }
//
//    /**
//     * Handles State Synchronization requests by delegating to the StateSynchronization module.
//     *
//     * @param checkpointMessage The checkpoint synchronization message.
//     */
//    public void handleCheckpointSync(PBFTMessage checkpointMessage) {
//        boolean success = stateSynchronization.handleStateUpdate(checkpointMessage);
//        if (success) {
//            System.out.println("Checkpoint synchronized successfully.");
//        } else {
//            System.err.println("Checkpoint synchronization failed.");
//        }
//    }
//}
