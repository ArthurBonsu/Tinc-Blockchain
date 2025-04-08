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







package org.example.app.core.pbftconsensus;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.crypto.PublicKeyManager;
import org.example.app.core.p2p.RobustP2PManager;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PBFT class implements the Practical Byzantine Fault Tolerance protocol.
 * - Handles all PBFT phases and manages state synchronization and view changes.
 */
public class PBFT {
    private static final Logger logger = Logger.getLogger(PBFT.class.getName());

    private final RobustP2PManager p2pManager;         // P2P manager for communication
    private final int replicaId;                      // Unique ID of this replica
    private final int totalReplicas;                  // Total replicas in the network
    private final int maxFaulty;                      // Maximum faulty replicas tolerated
    private final PBFTMessageHandler messageHandler;  // Handles encoding/decoding PBFT messages
    private final ViewChangeHandler viewChangeHandler; // Manages view change processes
    private final StateSynchronization stateSynchronization; // Handles state synchronization
    private final ReadWriteLock lock;                 // Thread-safe access to internal state

    private final Set<String> preparedDigests;        // Tracks prepared message digests
    private final Set<String> committedDigests;       // Tracks committed message digests
    private final Map<String, Integer> prepareVotes;  // Tracks votes for prepare phase
    private final Map<String, Integer> commitVotes;   // Tracks votes for commit phase

    private boolean isPrimary;                        // Flag indicating if this replica is the primary
    private final PublicKeyManager keyManager;        // Manages public keys for verification

    /**
     * Constructor to initialize PBFT.
     *
     * @param p2pManager            The RobustP2PManager instance.
     * @param replicaId             The ID of this replica.
     * @param totalReplicas         Total replicas in the network.
     * @param viewChangeHandler     The ViewChangeHandler instance.
     * @param stateSynchronization  The StateSynchronization instance.
     * @param keyManager            The PublicKeyManager for key management.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public PBFT(RobustP2PManager p2pManager, int replicaId, int totalReplicas,
                ViewChangeHandler viewChangeHandler, StateSynchronization stateSynchronization,
                PublicKeyManager keyManager) {
        if (p2pManager == null) {
            throw new IllegalArgumentException("P2P manager cannot be null");
        }
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }
        if (totalReplicas <= 0) {
            throw new IllegalArgumentException("Total replicas must be positive");
        }
        if (viewChangeHandler == null) {
            throw new IllegalArgumentException("View change handler cannot be null");
        }
        if (stateSynchronization == null) {
            throw new IllegalArgumentException("State synchronization cannot be null");
        }
        if (keyManager == null) {
            throw new IllegalArgumentException("Key manager cannot be null");
        }

        this.p2pManager = p2pManager;
        this.replicaId = replicaId;
        this.totalReplicas = totalReplicas;
        // f = (n-1)/3, where f is max faulty replicas and n is total replicas
        this.maxFaulty = (totalReplicas - 1) / 3;
        this.messageHandler = new PBFTMessageHandler();
        this.viewChangeHandler = viewChangeHandler;
        this.stateSynchronization = stateSynchronization;
        this.keyManager = keyManager;
        this.lock = new ReentrantReadWriteLock();

        // Initialize tracking sets for message digests
        this.preparedDigests = ConcurrentHashMap.newKeySet();
        this.committedDigests = ConcurrentHashMap.newKeySet();
        this.prepareVotes = new ConcurrentHashMap<>();
        this.commitVotes = new ConcurrentHashMap<>();

        // Set initial primary status based on view
        this.isPrimary = (replicaId == viewChangeHandler.getCurrentView());

        logger.info("PBFT initialized with replicaId=" + replicaId + ", totalReplicas=" + totalReplicas +
                ", maxFaulty=" + maxFaulty + ", isPrimary=" + isPrimary);
    }

    /**
     * Processes an incoming PBFT message and routes it to the appropriate handler.
     *
     * @param serializedMessage The serialized message received from the P2P network.
     * @throws IllegalArgumentException if serializedMessage is null or empty
     */
    public void processMessage(String serializedMessage) {
        if (serializedMessage == null || serializedMessage.isEmpty()) {
            throw new IllegalArgumentException("Serialized message cannot be null or empty");
        }

        try {
            PBFTMessage message = messageHandler.decodePBFTMessage(serializedMessage);
            if (!validateMessage(message)) {
                logger.warning("Invalid PBFT message: " + message);
                return;
            }
            handleMessage(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing PBFT message", e);
        }
    }

    /**
     * Validates the integrity of a PBFT message.
     *
     * @param message The message to validate.
     * @return True if valid, false otherwise.
     */
    private boolean validateMessage(PBFTMessage message) {
        if (message == null || message.getSenderId() < 0 || message.getType() == null) {
            return false;
        }

        // If signature is present, verify it
        if (message.getSignature() != null) {
            try {
                Keypair senderKeypair = getReplicaKeypair(message.getSenderId());
                return messageHandler.validateMessage(message, senderKeypair);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Signature validation failed", e);
                return false;
            }
        }

        // For messages without signatures, we still accept them for certain types
        return true;
    }

    /**
     * Routes a valid PBFT message to the corresponding handler based on its type.
     *
     * @param message The PBFTMessage to handle.
     */
    private void handleMessage(PBFTMessage message) {
        switch (message.getType()) {
            case "PRE-PREPARE":
                handlePrePrepare(message.getDigest());
                break;
            case "PREPARE":
                handlePrepare(message.getDigest());
                break;
            case "COMMIT":
                handleCommit(message.getDigest());
                break;
            case "VIEW-CHANGE":
                handleViewChange(message);
                break;
            case "STATE-UPDATE":
                handleStateUpdate(message);
                break;
            case "CLIENT-REQUEST":
                processClientRequest(message);
                break;
            default:
                logger.warning("Unknown PBFT message type: " + message.getType());
        }
    }

    /**
     * Processes a client request and starts the Pre-Prepare phase if this replica is the primary.
     *
     * @param requestMessage The client request message.
     */
    public void processClientRequest(PBFTMessage requestMessage) {
        if (requestMessage == null) {
            throw new IllegalArgumentException("Request message cannot be null");
        }

        logger.info("Processing client request: " + requestMessage);

        // Only the primary should initiate the Pre-Prepare phase
        if (isPrimary) {
            try {
                // Create a unique digest for this request
                String requestDigest = computeDigest(requestMessage.getContent());

                // Check if this request has already been processed
                if (committedDigests.contains(requestDigest)) {
                    logger.info("Request already processed: " + requestDigest);
                    return;
                }

                // Initiate the Pre-Prepare phase
                handlePrePrepare(requestDigest);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error processing client request", e);
            }
        } else {
            // Non-primary replicas should forward client requests to the primary
            logger.info("Forwarding client request to primary");
            try {
                int primaryId = viewChangeHandler.getCurrentView();
                p2pManager.sendDirectMessage(String.valueOf(primaryId),
                        messageHandler.encodePBFTMessage(requestMessage));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error forwarding client request to primary", e);
            }
        }
    }

    /**
     * Initiates the Pre-Prepare phase of PBFT.
     *
     * @param clientRequest The client request to process.
     * @throws IllegalArgumentException if clientRequest is null or empty
     */
    public void handlePrePrepare(String clientRequest) {
        if (clientRequest == null || clientRequest.isEmpty()) {
            throw new IllegalArgumentException("Client request cannot be null or empty");
        }

        String digest = computeDigest(clientRequest);

        lock.writeLock().lock();
        try {
            // Check if we have already prepared this request
            if (preparedDigests.contains(digest)) {
                logger.info("Already prepared digest: " + digest);
                return;
            }

            // As primary, broadcast pre-prepare to all replicas
            if (isPrimary) {
                PBFTMessage prePrepareMessage = new PBFTMessage("PRE-PREPARE", replicaId, digest, null);
                prePrepareMessage.setContent(clientRequest);
                broadcastMessage(prePrepareMessage);
                logger.info("Pre-Prepare initiated with digest: " + digest);
            }

            // Move to the Prepare phase
            handlePrepare(digest);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Handles the Prepare phase of PBFT.
     *
     * @param digest The digest received in the Pre-Prepare phase.
     * @throws IllegalArgumentException if digest is null or empty
     */
    public void handlePrepare(String digest) {
        if (digest == null || digest.isEmpty()) {
            throw new IllegalArgumentException("Digest cannot be null or empty");
        }

        lock.writeLock().lock();
        try {
            // Check if we have already prepared this request
            if (preparedDigests.contains(digest)) {
                logger.info("Already prepared digest: " + digest);
                return;
            }

            // Broadcast prepare message to all replicas
            PBFTMessage prepareMessage = new PBFTMessage("PREPARE", replicaId, digest, null);
            broadcastMessage(prepareMessage);

            // Update prepare votes for this digest
            int votes = prepareVotes.getOrDefault(digest, 0) + 1;
            prepareVotes.put(digest, votes);

            logger.info("Prepare phase: " + votes + " votes for digest: " + digest);

            // Check if we have enough prepare votes to move to commit phase
            // Need 2f + 1 votes (including this replica's vote)
            if (votes >= 2 * maxFaulty + 1) {
                preparedDigests.add(digest);
                prepareVotes.remove(digest); // Clean up votes once prepared
                logger.info("Prepare phase completed for digest: " + digest);

                // Move to commit phase
                handleCommit(digest);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Handles the Commit phase of PBFT.
     *
     * @param digest The digest received in the Prepare phase.
     * @throws IllegalArgumentException if digest is null or empty
     */
    public void handleCommit(String digest) {
        if (digest == null || digest.isEmpty()) {
            throw new IllegalArgumentException("Digest cannot be null or empty");
        }

        lock.writeLock().lock();
        try {
            // Check if this digest is already committed
            if (committedDigests.contains(digest)) {
                logger.info("Commit already processed for digest: " + digest);
                return;
            }

            // Check if we have prepared this digest (necessary precondition)
            if (!preparedDigests.contains(digest)) {
                logger.warning("Cannot commit a digest that hasn't been prepared: " + digest);
                return;
            }

            // Broadcast commit message to all replicas
            PBFTMessage commitMessage = new PBFTMessage("COMMIT", replicaId, digest, null);
            broadcastMessage(commitMessage);

            // Update commit votes for this digest
            int votes = commitVotes.getOrDefault(digest, 0) + 1;
            commitVotes.put(digest, votes);

            logger.info("Commit phase: " + votes + " votes for digest: " + digest);

            // Check if we have enough commit votes to finalize
            // Need 2f + 1 votes (including this replica's vote)
            if (votes >= 2 * maxFaulty + 1) {
                committedDigests.add(digest);
                commitVotes.remove(digest); // Clean up votes once committed

                // Execute the request and update state
                executeRequest(digest);

                logger.info("Commit phase completed for digest: " + digest);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Executes the committed request and updates the replica state.
     *
     * @param digest The digest of the committed request.
     */
    private void executeRequest(String digest) {
        try {
            // In a real implementation, this would execute the client's request
            // and update the replica's state

            // For now, we'll just update our state synchronization with a dummy value
            stateSynchronization.updateState(digest.hashCode(), "Executed: " + digest);

            // Create a checkpoint periodically to allow recovery
            if (committedDigests.size() % 10 == 0) { // Every 10 commits
                int sequenceNumber = committedDigests.size();
                stateSynchronization.createStateCheckpoint(sequenceNumber);
            }

            logger.info("Request executed for digest: " + digest);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing request", e);
        }
    }

    /**
     * Computes a digest for a given input.
     *
     * @param data The input data.
     * @return The computed digest.
     */
    private String computeDigest(String data) {
        if (data == null) {
            return "";
        }
        return Integer.toHexString(data.hashCode());
    }

    /**
     * Broadcasts a PBFT message to all replicas.
     *
     * @param message The PBFTMessage to broadcast.
     */
    private void broadcastMessage(PBFTMessage message) {
        try {
            String serializedMessage = messageHandler.encodePBFTMessage(message);
            p2pManager.sendBroadcast(serializedMessage);
            logger.fine("Broadcasted message: " + message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error broadcasting message", e);
        }
    }

    /**
     * Retrieves the replica's keypair for signing or verification.
     *
     * @param senderId The sender's ID.
     * @return The corresponding keypair.
     * @throws RuntimeException if keypair retrieval fails
     */
    private Keypair getReplicaKeypair(int senderId) {
        String nodeId = String.valueOf(senderId);
        logger.fine("Fetching keypair for replica: " + nodeId);

        try {
            // Attempt to load the keypair from the key manager
            return keyManager.loadKeypair(nodeId);
        } catch (RuntimeException e) {
            // Handle case where keypair is not found
            if (e.getMessage() != null && e.getMessage().contains("Keypair not found")) {
                logger.info("Keypair not found for replica: " + nodeId + ". Generating new keypair.");
                Keypair keypair = keyManager.generateKeypair();
                keyManager.storeKeypair(keypair, nodeId);
                return keypair;
            }
            // Re-throw unexpected exceptions
            logger.log(Level.SEVERE, "Error retrieving keypair", e);
            throw e;
        }
    }

    /**
     * Handles View Change requests by delegating to the ViewChangeHandler.
     *
     * @param viewChangeMessage The message containing view change information.
     */
    public void handleViewChange(PBFTMessage viewChangeMessage) {
        if (viewChangeMessage == null) {
            throw new IllegalArgumentException("View change message cannot be null");
        }

        try {
            int proposedView = Integer.parseInt(viewChangeMessage.getContent());
            viewChangeHandler.handleViewChangeMessage(viewChangeMessage.getSenderId(), proposedView);

            // Update our primary status based on the new view
            lock.writeLock().lock();
            try {
                int currentView = viewChangeHandler.getCurrentView();
                isPrimary = (replicaId == currentView);
                logger.info("View changed to " + currentView + ". This replica is " +
                        (isPrimary ? "now primary" : "not primary"));
            } finally {
                lock.writeLock().unlock();
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid view number in view change message", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling view change", e);
        }
    }

    /**
     * Handles State Synchronization requests by delegating to the StateSynchronization module.
     *
     * @param checkpointMessage The checkpoint synchronization message.
     */
    public void handleCheckpointSync(PBFTMessage checkpointMessage) {
        if (checkpointMessage == null) {
            throw new IllegalArgumentException("Checkpoint message cannot be null");
        }

        try {
            boolean success = stateSynchronization.handleStateUpdate(checkpointMessage);
            if (success) {
                logger.info("Checkpoint synchronized successfully.");
            } else {
                logger.warning("Checkpoint synchronization failed.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling checkpoint synchronization", e);
        }
    }

    /**
     * Handles state update messages from other replicas.
     *
     * @param stateUpdateMessage The state update message.
     */
    public void handleStateUpdate(PBFTMessage stateUpdateMessage) {
        if (stateUpdateMessage == null) {
            throw new IllegalArgumentException("State update message cannot be null");
        }

        try {
            boolean success = stateSynchronization.handleStateUpdate(stateUpdateMessage);
            if (success) {
                logger.info("State update handled successfully.");
            } else {
                logger.warning("State update handling failed.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling state update", e);
        }
    }

    /**
     * Gets the current replica ID.
     *
     * @return The replica ID.
     */
    public int getReplicaId() {
        return replicaId;
    }

    /**
     * Checks if this replica is currently the primary.
     *
     * @return true if this replica is the primary, false otherwise.
     */
    public boolean isPrimary() {
        lock.readLock().lock();
        try {
            return isPrimary;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the total number of replicas in the network.
     *
     * @return The total replica count.
     */
    public int getTotalReplicas() {
        return totalReplicas;
    }

    /**
     * Gets the maximum number of faulty replicas that can be tolerated.
     *
     * @return The maximum faulty replica count.
     */
    public int getMaxFaulty() {
        return maxFaulty;
    }

    /**
     * Gets the message handler used by this PBFT instance.
     *
     * @return The message handler.
     */
    public PBFTMessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * Gets the set of digests that have been committed.
     *
     * @return An unmodifiable view of the committed digests.
     */
    public Set<String> getCommittedDigests() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableSet(new HashSet<>(committedDigests));
        } finally {
            lock.readLock().unlock();
        }
    }
}