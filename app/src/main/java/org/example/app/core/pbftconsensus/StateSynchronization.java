//package org.tinc.consensus.pbft;
//
//import java.security.MessageDigest;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
///**
// * StateSynchronization ensures replicas synchronize their states correctly after faults or view changes.
// * - Uses Merkle Trees to validate and verify state updates efficiently.
// */
//public class StateSynchronization {
//
//    private final CheckpointManager checkpointManager;   // Manages stable state checkpoints
//    private final PBFTNetwork pbftNetwork;              // Manages communication with other replicas
//    private final Map<Integer, String> localState;       // Local state storage (key-value pairs)
//
//    /**
//     * Constructor to initialize StateSynchronization.
//     *
//     * @param checkpointManager The CheckpointManager to manage stable states.
//     * @param pbftNetwork       The PBFTNetwork for communication.
//     */
//    public StateSynchronization(CheckpointManager checkpointManager, PBFTNetwork pbftNetwork) {
//        this.checkpointManager = checkpointManager;
//        this.pbftNetwork = pbftNetwork;
//        this.localState = new ConcurrentHashMap<>();
//    }
//
//    /**
//     * Synchronizes the replica's state using Merkle Trees to validate state updates.
//     *
//     * @param sequenceNumber The latest checkpoint sequence number.
//     */
//    public void synchronizeState(int sequenceNumber) {
//        System.out.println("Starting state synchronization using Merkle Trees for sequence: " + sequenceNumber);
//
//        // Generate Merkle Root for local state
//        String localMerkleRoot = generateMerkleRoot(new ArrayList<>(localState.values()));
//        System.out.println("Local Merkle Root: " + localMerkleRoot);
//
//        // Broadcast state request with Merkle Root
//        PBFTMessage stateRequest = new PBFTMessage(
//                "STATE-REQUEST", pbftNetwork.getNodeId(),
//                "Requesting state for sequence " + sequenceNumber, null
//        );
//        stateRequest.setContent(localMerkleRoot);
//        pbftNetwork.broadcastPBFTMessage(String.valueOf(stateRequest));
//
//        System.out.println("State request with Merkle Root broadcasted.");
//    }
//
//    /**
//     * Handles an incoming state update and verifies it using Merkle Trees.
//     *
//     * @param stateUpdateMessage The received state update message.
//     * @return True if the state update is valid and successfully integrated, false otherwise.
//     */
//    public boolean handleStateUpdate(PBFTMessage stateUpdateMessage) {
//        System.out.println("Handling state update message: " + stateUpdateMessage);
//
//        // Extract Merkle Root and updated state data
//        String receivedMerkleRoot = stateUpdateMessage.getDigest();
//        List<String> receivedState = Arrays.asList(stateUpdateMessage.getContent().split(","));
//
//        // Verify the integrity of the received state
//        String computedMerkleRoot = generateMerkleRoot(receivedState);
//        if (!computedMerkleRoot.equals(receivedMerkleRoot)) {
//            System.err.println("Merkle Root mismatch. State update is invalid.");
//            return false;
//        }
//
//        // Merge the received state with the local state
//        mergeState(receivedState);
//
//        // Update the checkpoint manager with the valid state
//        checkpointManager.createCheckpoint(
//                Integer.parseInt(stateUpdateMessage.getDigest()), String.join(",", receivedState)
//        );
//        System.out.println("State successfully synchronized.");
//        return true;
//    }
//
//    /**
//     * Generates the Merkle Root for a given list of data items.
//     *
//     * @param dataItems The list of data items to hash.
//     * @return The Merkle Root as a string.
//     */
//    private String generateMerkleRoot(List<String> dataItems) {
//        if (dataItems.isEmpty()) {
//            return "";
//        }
//
//        List<String> hashes = dataItems.stream()
//                .map(this::computeHash)
//                .collect(Collectors.toList());
//
//        while (hashes.size() > 1) {
//            List<String> newLevel = new ArrayList<>();
//            for (int i = 0; i < hashes.size(); i += 2) {
//                if (i + 1 < hashes.size()) {
//                    newLevel.add(computeHash(hashes.get(i) + hashes.get(i + 1)));
//                } else {
//                    newLevel.add(hashes.get(i)); // Handle odd number of nodes
//                }
//            }
//            hashes = newLevel;
//        }
//
//        return hashes.get(0);
//    }
//
//    /**
//     * Merges the received state with the local state.
//     *
//     * @param receivedState The received state as a list of strings.
//     */
//    private void mergeState(List<String> receivedState) {
//        receivedState.forEach(data -> localState.putIfAbsent(data.hashCode(), data));
//        System.out.println("Local state merged with received state.");
//    }
//
//    /**
//     * Computes a cryptographic hash for a given input using SHA-256.
//     *
//     * @param input The input string to hash.
//     * @return The hash as a string.
//     */
//    private String computeHash(String input) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hashBytes = digest.digest(input.getBytes());
//            StringBuilder hashBuilder = new StringBuilder();
//            for (byte b : hashBytes) {
//                hashBuilder.append(String.format("%02x", b));
//            }
//            return hashBuilder.toString();
//        } catch (Exception e) {
//            throw new RuntimeException("Error computing hash", e);
//        }
//    }
//
//    /**
//     * Retrieves the current local state as a map.
//     *
//     * @return The local state map.
//     */
//    public Map<Integer, String> getLocalState() {
//        return new HashMap<>(localState);
//    }
//}



package org.example.app.core.pbftconsensus;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * StateSynchronization ensures replicas synchronize their states correctly after faults or view changes.
 * - Uses Merkle Trees to validate and verify state updates efficiently.
 */
public class StateSynchronization {
    private static final Logger logger = Logger.getLogger(StateSynchronization.class.getName());

    private final CheckpointManager checkpointManager;   // Manages stable state checkpoints
    private final PBFTNetwork pbftNetwork;              // Manages communication with other replicas
    private final Map<Integer, String> localState;       // Local state storage (key-value pairs)
    private final ReadWriteLock stateLock;              // For thread-safe state access
    private final PBFTMessageHandler messageHandler;    // For message encoding/decoding

    /**
     * Constructor to initialize StateSynchronization.
     *
     * @param checkpointManager The CheckpointManager to manage stable states.
     * @param pbftNetwork       The PBFTNetwork for communication.
     * @param messageHandler    The PBFTMessageHandler for message encoding/decoding.
     * @throws IllegalArgumentException if any parameter is null
     */
    public StateSynchronization(CheckpointManager checkpointManager, PBFTNetwork pbftNetwork,
                                PBFTMessageHandler messageHandler) {
        if (checkpointManager == null) {
            throw new IllegalArgumentException("Checkpoint manager cannot be null");
        }
        if (pbftNetwork == null) {
            throw new IllegalArgumentException("PBFT network cannot be null");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("Message handler cannot be null");
        }

        this.checkpointManager = checkpointManager;
        this.pbftNetwork = pbftNetwork;
        this.messageHandler = messageHandler;
        this.localState = new ConcurrentHashMap<>();
        this.stateLock = new ReentrantReadWriteLock();

        logger.info("StateSynchronization initialized");
    }

    /**
     * Synchronizes the replica's state using Merkle Trees to validate state updates.
     *
     * @param sequenceNumber The latest checkpoint sequence number.
     * @throws IllegalArgumentException if sequenceNumber is not positive
     */
    public void synchronizeState(int sequenceNumber) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("Sequence number must be positive");
        }

        logger.info("Starting state synchronization using Merkle Trees for sequence: " + sequenceNumber);

        try {
            stateLock.readLock().lock();
            List<String> stateValues = new ArrayList<>(localState.values());

            // Generate Merkle Root for local state
            String localMerkleRoot = generateMerkleRoot(stateValues);
            logger.info("Local Merkle Root: " + localMerkleRoot);

            // Broadcast state request with Merkle Root
            PBFTMessage stateRequest = new PBFTMessage(
                    "STATE-REQUEST",
                    pbftNetwork.getNodeId(),
                    "state-req-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            stateRequest.setContent(sequenceNumber + ":" + localMerkleRoot);

            pbftNetwork.broadcastPBFTMessage(messageHandler.encodePBFTMessage(stateRequest));
            logger.info("State request with Merkle Root broadcasted for sequence: " + sequenceNumber);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during state synchronization", e);
            throw new RuntimeException("State synchronization failed", e);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Handles an incoming state update and verifies it using Merkle Trees.
     *
     * @param stateUpdateMessage The received state update message.
     * @return True if the state update is valid and successfully integrated, false otherwise.
     * @throws IllegalArgumentException if the message is null
     */
    public boolean handleStateUpdate(PBFTMessage stateUpdateMessage) {
        if (stateUpdateMessage == null) {
            throw new IllegalArgumentException("State update message cannot be null");
        }

        logger.info("Handling state update message: " + stateUpdateMessage);

        try {
            // Extract Merkle Root and updated state data
            String content = stateUpdateMessage.getContent();
            if (content == null || content.isEmpty()) {
                logger.warning("State update message has no content");
                return false;
            }

            String[] parts = content.split(":");
            if (parts.length < 2) {
                logger.warning("Invalid state update message format");
                return false;
            }

            String receivedMerkleRoot = parts[0];
            List<String> receivedState = Arrays.asList(parts[1].split(","));

            // Verify the integrity of the received state
            String computedMerkleRoot = generateMerkleRoot(receivedState);
            if (!computedMerkleRoot.equals(receivedMerkleRoot)) {
                logger.warning("Merkle Root mismatch. State update is invalid.");
                return false;
            }

            // Merge the received state with the local state
            mergeState(receivedState);

            // Extract the sequence number from the digest
            int sequenceNumber;
            try {
                sequenceNumber = Integer.parseInt(stateUpdateMessage.getDigest().split("-")[1]);
            } catch (Exception e) {
                // If we can't extract a sequence number, use the current time as a fallback
                sequenceNumber = (int)(System.currentTimeMillis() / 1000);
                logger.warning("Could not extract sequence number from message, using fallback: " + sequenceNumber);
            }

            // Update the checkpoint manager with the valid state
            checkpointManager.createCheckpoint(sequenceNumber, String.join(",", receivedState));
            logger.info("State successfully synchronized at sequence: " + sequenceNumber);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling state update", e);
            return false;
        }
    }

    /**
     * Generates the Merkle Root for a given list of data items.
     *
     * @param dataItems The list of data items to hash.
     * @return The Merkle Root as a string.
     */
    private String generateMerkleRoot(List<String> dataItems) {
        if (dataItems == null || dataItems.isEmpty()) {
            return "";
        }

        List<String> hashes = dataItems.stream()
                .map(this::computeHash)
                .collect(Collectors.toList());

        while (hashes.size() > 1) {
            List<String> newLevel = new ArrayList<>();
            for (int i = 0; i < hashes.size(); i += 2) {
                if (i + 1 < hashes.size()) {
                    // Concatenate and hash pairs of nodes
                    newLevel.add(computeHash(hashes.get(i) + hashes.get(i + 1)));
                } else {
                    // Handle odd number of nodes by promoting the last one
                    newLevel.add(hashes.get(i));
                }
            }
            hashes = newLevel;
        }

        return hashes.get(0);
    }

    /**
     * Merges the received state with the local state.
     *
     * @param receivedState The received state as a list of strings.
     */
    private void mergeState(List<String> receivedState) {
        if (receivedState == null || receivedState.isEmpty()) {
            logger.warning("Received state is null or empty, nothing to merge");
            return;
        }

        stateLock.writeLock().lock();
        try {
            for (String data : receivedState) {
                // We use the hash of the data as the key to ensure uniqueness
                localState.putIfAbsent(data.hashCode(), data);
            }
            logger.info("Local state merged with received state. New state size: " + localState.size());
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Computes a cryptographic hash for a given input using SHA-256.
     *
     * @param input The input string to hash.
     * @return The hash as a string.
     */
    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            // Convert the byte array to a hex string
            StringBuilder hashBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                hashBuilder.append(String.format("%02x", b));
            }
            return hashBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Error computing hash", e);
            throw new RuntimeException("Error computing hash", e);
        }
    }

    /**
     * Retrieves the current local state as a map.
     *
     * @return The local state map.
     */
    public Map<Integer, String> getLocalState() {
        stateLock.readLock().lock();
        try {
            return new HashMap<>(localState);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Updates a specific state item.
     *
     * @param key The key for the state item
     * @param value The value to set
     */
    public void updateState(int key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("State value cannot be null");
        }

        stateLock.writeLock().lock();
        try {
            localState.put(key, value);
            logger.fine("State updated for key: " + key);
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Removes a specific state item.
     *
     * @param key The key for the state item to remove
     * @return true if the item was removed, false if it wasn't found
     */
    public boolean removeState(int key) {
        stateLock.writeLock().lock();
        try {
            String removed = localState.remove(key);
            if (removed != null) {
                logger.fine("State removed for key: " + key);
                return true;
            }
            return false;
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Clears all local state.
     */
    public void clearState() {
        stateLock.writeLock().lock();
        try {
            localState.clear();
            logger.info("All local state cleared");
        } finally {
            stateLock.writeLock().unlock();
        }
    }

    /**
     * Gets the state item for a specific key.
     *
     * @param key The key to look up
     * @return The state value, or null if not found
     */
    public String getState(int key) {
        stateLock.readLock().lock();
        try {
            return localState.get(key);
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Gets the size of the local state.
     *
     * @return The number of state items
     */
    public int getStateSize() {
        stateLock.readLock().lock();
        try {
            return localState.size();
        } finally {
            stateLock.readLock().unlock();
        }
    }

    /**
     * Creates a full state checkpoint at the current sequence.
     *
     * @param sequenceNumber The sequence number for the checkpoint
     * @return true if checkpoint was created successfully, false otherwise
     */
    public boolean createStateCheckpoint(int sequenceNumber) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("Sequence number must be positive");
        }

        stateLock.readLock().lock();
        try {
            if (localState.isEmpty()) {
                logger.warning("Cannot create checkpoint for empty state");
                return false;
            }

            // Convert the state to a string representation
            String stateSnapshot = localState.values().stream()
                    .collect(Collectors.joining(","));

            // Create a checkpoint with the state snapshot
            checkpointManager.createCheckpoint(sequenceNumber, stateSnapshot);
            logger.info("State checkpoint created at sequence: " + sequenceNumber);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating state checkpoint", e);
            return false;
        } finally {
            stateLock.readLock().unlock();
        }
    }
}