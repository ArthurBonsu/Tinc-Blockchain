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
