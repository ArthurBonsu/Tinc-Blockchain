//package org.tinc.consensus.pbft;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * CheckpointManager handles periodic state checkpoints to ensure data persistence,
// * reduce memory overhead, and support recovery in case of failures.
// */
//public class CheckpointManager {
//
//    private final Map<Integer, String> checkpoints; // Maps sequence numbers to state snapshots
//    private int latestCheckpointSequence; // Tracks the latest stable checkpoint sequence number
//
//    /**
//     * Constructor to initialize the CheckpointManager.
//     */
//    public CheckpointManager() {
//        this.checkpoints = new HashMap<>();
//        this.latestCheckpointSequence = 0;
//    }
//
//    /**
//     * Saves a snapshot of the system state at a specific sequence number.
//     *
//     * @param sequenceNumber The sequence number for the checkpoint.
//     * @param state The state snapshot to save.
//     */
//    public void createCheckpoint(int sequenceNumber, String state) {
//        checkpoints.put(sequenceNumber, state);
//        latestCheckpointSequence = sequenceNumber;
//        System.out.println("Checkpoint created at sequence: " + sequenceNumber);
//    }
//
//    /**
//     * Retrieves a saved checkpoint for a given sequence number.
//     *
//     * @param sequenceNumber The sequence number of the checkpoint to retrieve.
//     * @return The state snapshot associated with the sequence number, or null if not found.
//     */
//    public String getCheckpoint(int sequenceNumber) {
//        return checkpoints.get(sequenceNumber);
//    }
//
//    /**
//     * Verifies that a checkpoint is consistent across replicas by comparing state hashes.
//     *
//     * @param sequenceNumber The sequence number of the checkpoint to verify.
//     * @param replicaStateHash The hash of the state reported by another replica.
//     * @return True if the checkpoint is consistent, false otherwise.
//     */
//    public boolean verifyCheckpoint(int sequenceNumber, String replicaStateHash) {
//        String localState = checkpoints.get(sequenceNumber);
//        if (localState == null) {
//            System.out.println("No checkpoint found at sequence: " + sequenceNumber);
//            return false;
//        }
//
//        boolean isConsistent = localState.hashCode() == replicaStateHash.hashCode();
//        System.out.println("Checkpoint consistency for sequence " + sequenceNumber + ": " + isConsistent);
//        return isConsistent;
//    }
//
//    /**
//     * Discards old checkpoints to reduce memory usage.
//     * Retains only checkpoints starting from the latest stable checkpoint.
//     */
//    public void discardOldCheckpoints() {
//        checkpoints.entrySet().removeIf(entry -> entry.getKey() < latestCheckpointSequence);
//        System.out.println("Old checkpoints discarded. Latest stable checkpoint: " + latestCheckpointSequence);
//    }
//
//    /**
//     * Retrieves the latest stable checkpoint sequence number.
//     *
//     * @return The sequence number of the latest stable checkpoint.
//     */
//    public int getLatestCheckpointSequence() {
//        return latestCheckpointSequence;
//    }
//}




package org.example.app.core.pbftconsensus;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * CheckpointManager handles periodic state checkpoints to ensure data persistence,
 * reduce memory overhead, and support recovery in case of failures.
 */
public class CheckpointManager {
    private static final Logger logger = Logger.getLogger(CheckpointManager.class.getName());

    private final NavigableMap<Integer, String> checkpoints; // Maps sequence numbers to state snapshots
    private int latestCheckpointSequence; // Tracks the latest stable checkpoint sequence number
    private final ReadWriteLock lock; // Ensures thread-safe operations

    /**
     * Constructor to initialize the CheckpointManager.
     */
    public CheckpointManager() {
        this.checkpoints = new TreeMap<>();
        this.latestCheckpointSequence = 0;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Saves a snapshot of the system state at a specific sequence number.
     *
     * @param sequenceNumber The sequence number for the checkpoint.
     * @param state The state snapshot to save.
     * @throws IllegalArgumentException if the sequence number is less than or equal to zero or if state is null
     */
    public void createCheckpoint(int sequenceNumber, String state) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("Sequence number must be greater than zero");
        }
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        lock.writeLock().lock();
        try {
            checkpoints.put(sequenceNumber, state);
            if (sequenceNumber > latestCheckpointSequence) {
                latestCheckpointSequence = sequenceNumber;
            }
            logger.info("Checkpoint created at sequence: " + sequenceNumber);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a saved checkpoint for a given sequence number.
     *
     * @param sequenceNumber The sequence number of the checkpoint to retrieve.
     * @return The state snapshot associated with the sequence number, or null if not found.
     * @throws IllegalArgumentException if the sequence number is less than or equal to zero
     */
    public String getCheckpoint(int sequenceNumber) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("Sequence number must be greater than zero");
        }

        lock.readLock().lock();
        try {
            return checkpoints.get(sequenceNumber);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Verifies that a checkpoint is consistent across replicas by comparing state hashes.
     *
     * @param sequenceNumber The sequence number of the checkpoint to verify.
     * @param replicaStateHash The hash of the state reported by another replica.
     * @return True if the checkpoint is consistent, false otherwise.
     * @throws IllegalArgumentException if sequenceNumber is less than or equal to zero or if replicaStateHash is null
     */
    public boolean verifyCheckpoint(int sequenceNumber, String replicaStateHash) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("Sequence number must be greater than zero");
        }
        if (replicaStateHash == null) {
            throw new IllegalArgumentException("Replica state hash cannot be null");
        }

        lock.readLock().lock();
        try {
            String localState = checkpoints.get(sequenceNumber);
            if (localState == null) {
                logger.warning("No checkpoint found at sequence: " + sequenceNumber);
                return false;
            }

            // Compare state hash with replica's reported hash
            boolean isConsistent = localState.hashCode() == replicaStateHash.hashCode();
            logger.info("Checkpoint consistency for sequence " + sequenceNumber + ": " + isConsistent);
            return isConsistent;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Discards old checkpoints to reduce memory usage.
     * Retains only checkpoints starting from the latest stable checkpoint.
     */
    public void discardOldCheckpoints() {
        lock.writeLock().lock();
        try {
            // Keep a map of entries to remove to avoid concurrent modification
            Map<Integer, String> toRemove = new HashMap<>();

            for (Map.Entry<Integer, String> entry : checkpoints.entrySet()) {
                if (entry.getKey() < latestCheckpointSequence) {
                    toRemove.put(entry.getKey(), entry.getValue());
                }
            }

            // Remove the collected entries
            for (Integer key : toRemove.keySet()) {
                checkpoints.remove(key);
            }

            logger.info("Old checkpoints discarded. Latest stable checkpoint: " + latestCheckpointSequence);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves the latest stable checkpoint sequence number.
     *
     * @return The sequence number of the latest stable checkpoint.
     */
    public int getLatestCheckpointSequence() {
        lock.readLock().lock();
        try {
            return latestCheckpointSequence;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets all checkpoints currently stored by the manager.
     *
     * @return A copy of the checkpoints map
     */
    public Map<Integer, String> getAllCheckpoints() {
        lock.readLock().lock();
        try {
            return new HashMap<>(checkpoints);
        } finally {
            lock.readLock().unlock();
        }
    }
}


