//package org.tinc.consensus.pbft;
//
//import org.tinc.p2p.RobustP2PManager;
//
//import java.util.List;
//
///**
// * FaultRecovery implements mechanisms to recover from detected faults,
// * ensuring system continuity and liveness in the PBFT protocol.
// */
//public class FaultRecovery {
//
//    private final PrimaryElection primaryElection;       // Handles primary election during faults
//    private final CheckpointManager checkpointManager;   // Manages stable states for recovery
//    private final TimeoutManager timeoutManager;         // Handles timeouts during recovery
//    private final RobustP2PManager p2pManager;
//    private final PBFTNetwork pbftNetwork;  // Peer-to-peer network for communication
//
//    /**
//     * Constructor to initialize FaultRecovery.
//     *
//     * @param primaryElection   The PrimaryElection instance to manage primary selection.
//     * @param checkpointManager The CheckpointManager instance to manage stable state recovery.
//     * @param timeoutManager    The TimeoutManager instance to handle timeouts.
//     * @param p2pManager        The RobustP2PManager instance for communication.
//     */
//    public FaultRecovery(PrimaryElection primaryElection, CheckpointManager checkpointManager,
//                         TimeoutManager timeoutManager, RobustP2PManager p2pManager, PBFTNetwork pbftNetwork) {
//        this.primaryElection = primaryElection;
//        this.checkpointManager = checkpointManager;
//        this.timeoutManager = timeoutManager;
//        this.p2pManager = p2pManager;
//
//        this.pbftNetwork = pbftNetwork;
//    }
//
//    /**
//     * Triggers recovery actions for a detected fault in the primary.
//     *
//     * @param failedPrimaryId The ID of the failed primary replica.
//     * @param totalReplicas   The total number of replicas in the system.
//     */
//    public void recoverFromPrimaryFailure(int failedPrimaryId, int totalReplicas) {
//        System.out.println("Recovering from primary failure. Failed Primary ID: " + failedPrimaryId);
//
//        // Elect a new primary
//        int newPrimary = primaryElection.electNewPrimary(totalReplicas);
//
//        // Broadcast view change to notify all replicas
//        PBFTMessage viewChangeMessage = new PBFTMessage(
//                "VIEW-CHANGE", pbftNetwork.getNodeId(),
//                "New primary elected: " + newPrimary, null
//        );
//        p2pManager.sendBroadcast(viewChangeMessage.serialize());
//
//        System.out.println("View change broadcasted. New primary: " + newPrimary);
//
//        // Synchronize states to ensure consistency
//        synchronizeToStableState();
//    }
//
//    /**
//     * Recovers the system to the latest stable state.
//     */
//    private void synchronizeToStableState() {
//        int stableSequenceNumber = checkpointManager.getLatestCheckpointSequence();
//        System.out.println("Recovering to stable state at sequence number: " + stableSequenceNumber);
//
//        // Broadcast checkpoint synchronization message to all replicas
//        PBFTMessage checkpointSyncMessage = new PBFTMessage(
//                "CHECKPOINT-SYNC", pbftNetwork.getNodeId(),
//                "Synchronize to sequence " + stableSequenceNumber, null
//        );
//        p2pManager.sendBroadcast(checkpointSyncMessage.serialize());
//
//        System.out.println("Checkpoint synchronization message broadcasted.");
//    }
//
//    /**
//     * Ensures system liveness by resolving deadlocks and enabling progress in consensus.
//     */
//    public void resolveDeadlocks() {
//        System.out.println("Resolving deadlocks to ensure system liveness.");
//
//        // Restart timeouts for consensus phases
//        timeoutManager.cancelAllTimeouts();
//        timeoutManager.startTimeout("CONSENSUS_PHASE", 5000, () -> {
//            System.out.println("Consensus phase timeout reached. Triggering recovery actions.");
//            synchronizeToStableState();
//        });
//
//        System.out.println("Deadlock resolution mechanisms applied.");
//    }
//
//    /**
//     * Performs a complete system recovery, ensuring continuity and progress.
//     *
//     * @param failedReplicaIds List of replica IDs identified as faulty.
//     * @param totalReplicas    The total number of replicas in the system.
//     */
//    public void performSystemRecovery(List<Integer> failedReplicaIds, int totalReplicas) {
//        System.out.println("Performing full system recovery for faulty replicas: " + failedReplicaIds);
//
//        // Recover from primary failure, if applicable
//        if (failedReplicaIds.contains(primaryElection.getCurrentView())) {
//            recoverFromPrimaryFailure(primaryElection.getCurrentView(), totalReplicas);
//        }
//
//        // Re-synchronize states across all replicas
//        synchronizeToStableState();
//
//        // Ensure liveness
//        resolveDeadlocks();
//
//        System.out.println("System recovery completed.");
//    }
//}





package org.example.app.core.pbftconsensus;
import org.example.app.core.p2p.RobustP2PManager;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * FaultRecovery implements mechanisms to recover from detected faults,
 * ensuring system continuity and liveness in the PBFT protocol.
 */
public class FaultRecovery {
    private static final Logger logger = Logger.getLogger(FaultRecovery.class.getName());

    private final PrimaryElection primaryElection;       // Handles primary election during faults
    private final CheckpointManager checkpointManager;   // Manages stable states for recovery
    private final TimeoutManager timeoutManager;         // Handles timeouts during recovery
    private final RobustP2PManager p2pManager;           // P2P communication manager
    private final PBFTNetwork pbftNetwork;               // PBFT network interface
    private final PBFTMessageHandler messageHandler;     // Message encoding/decoding handler

    /**
     * Constructor to initialize FaultRecovery.
     *
     * @param primaryElection   The PrimaryElection instance to manage primary selection.
     * @param checkpointManager The CheckpointManager instance to manage stable state recovery.
     * @param timeoutManager    The TimeoutManager instance to handle timeouts.
     * @param p2pManager        The RobustP2PManager instance for communication.
     * @param pbftNetwork       The PBFTNetwork instance for network operations.
     * @param messageHandler    The PBFTMessageHandler for message encoding.
     * @throws IllegalArgumentException if any parameter is null
     */
    public FaultRecovery(PrimaryElection primaryElection, CheckpointManager checkpointManager,
                         TimeoutManager timeoutManager, RobustP2PManager p2pManager,
                         PBFTNetwork pbftNetwork, PBFTMessageHandler messageHandler) {
        if (primaryElection == null) {
            throw new IllegalArgumentException("Primary election cannot be null");
        }
        if (checkpointManager == null) {
            throw new IllegalArgumentException("Checkpoint manager cannot be null");
        }
        if (timeoutManager == null) {
            throw new IllegalArgumentException("Timeout manager cannot be null");
        }
        if (p2pManager == null) {
            throw new IllegalArgumentException("P2P manager cannot be null");
        }
        if (pbftNetwork == null) {
            throw new IllegalArgumentException("PBFT network cannot be null");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("Message handler cannot be null");
        }

        this.primaryElection = primaryElection;
        this.checkpointManager = checkpointManager;
        this.timeoutManager = timeoutManager;
        this.p2pManager = p2pManager;
        this.pbftNetwork = pbftNetwork;
        this.messageHandler = messageHandler;
    }

    /**
     * Triggers recovery actions for a detected fault in the primary.
     *
     * @param failedPrimaryId The ID of the failed primary replica.
     * @param totalReplicas   The total number of replicas in the system.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void recoverFromPrimaryFailure(int failedPrimaryId, int totalReplicas) {
        if (failedPrimaryId < 0) {
            throw new IllegalArgumentException("Failed primary ID cannot be negative");
        }
        if (totalReplicas <= 0) {
            throw new IllegalArgumentException("Total replicas must be positive");
        }

        logger.info("Recovering from primary failure. Failed Primary ID: " + failedPrimaryId);

        try {
            // Elect a new primary
            int newPrimary = primaryElection.electNewPrimary(totalReplicas);

            // Broadcast view change to notify all replicas
            PBFTMessage viewChangeMessage = new PBFTMessage(
                    "VIEW-CHANGE",
                    pbftNetwork.getNodeId(),
                    "view-change-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            viewChangeMessage.setContent("New primary elected: " + newPrimary);

            p2pManager.sendBroadcast(messageHandler.encodePBFTMessage(viewChangeMessage));
            logger.info("View change broadcasted. New primary: " + newPrimary);

            // Synchronize states to ensure consistency
            synchronizeToStableState();
        } catch (Exception e) {
            logger.severe("Error during primary failure recovery: " + e.getMessage());
            throw new RuntimeException("Failed to recover from primary failure", e);
        }
    }

    /**
     * Recovers the system to the latest stable state.
     */
    private void synchronizeToStableState() {
        try {
            int stableSequenceNumber = checkpointManager.getLatestCheckpointSequence();
            logger.info("Recovering to stable state at sequence number: " + stableSequenceNumber);

            // Broadcast checkpoint synchronization message to all replicas
            PBFTMessage checkpointSyncMessage = new PBFTMessage(
                    "CHECKPOINT-SYNC",
                    pbftNetwork.getNodeId(),
                    "checkpoint-sync-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            checkpointSyncMessage.setContent("Synchronize to sequence " + stableSequenceNumber);

            p2pManager.sendBroadcast(messageHandler.encodePBFTMessage(checkpointSyncMessage));
            logger.info("Checkpoint synchronization message broadcasted.");
        } catch (Exception e) {
            logger.severe("Error during state synchronization: " + e.getMessage());
            throw new RuntimeException("Failed to synchronize to stable state", e);
        }
    }

    /**
     * Ensures system liveness by resolving deadlocks and enabling progress in consensus.
     */
    public void resolveDeadlocks() {
        logger.info("Resolving deadlocks to ensure system liveness.");

        try {
            // Restart timeouts for consensus phases
            timeoutManager.cancelAllTimeouts();
            timeoutManager.startTimeout("CONSENSUS_PHASE", 5000, () -> {
                logger.info("Consensus phase timeout reached. Triggering recovery actions.");
                synchronizeToStableState();
            });

            logger.info("Deadlock resolution mechanisms applied.");
        } catch (Exception e) {
            logger.severe("Error during deadlock resolution: " + e.getMessage());
            throw new RuntimeException("Failed to resolve deadlocks", e);
        }
    }

    /**
     * Performs a complete system recovery, ensuring continuity and progress.
     *
     * @param failedReplicaIds List of replica IDs identified as faulty.
     * @param totalReplicas    The total number of replicas in the system.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void performSystemRecovery(List<Integer> failedReplicaIds, int totalReplicas) {
        if (failedReplicaIds == null) {
            throw new IllegalArgumentException("Failed replica IDs list cannot be null");
        }
        if (totalReplicas <= 0) {
            throw new IllegalArgumentException("Total replicas must be positive");
        }

        logger.info("Performing full system recovery for faulty replicas: " + failedReplicaIds);

        try {
            // Recover from primary failure, if applicable
            int currentPrimary = primaryElection.getCurrentView();
            if (failedReplicaIds.contains(currentPrimary)) {
                recoverFromPrimaryFailure(currentPrimary, totalReplicas);
            }

            // Re-synchronize states across all replicas
            synchronizeToStableState();

            // Ensure liveness
            resolveDeadlocks();

            logger.info("System recovery completed.");
        } catch (Exception e) {
            logger.severe("Error during system recovery: " + e.getMessage());
            throw new RuntimeException("Failed to perform system recovery", e);
        }
    }

    /**
     * Initiates a targeted recovery for a specific sequence number.
     *
     * @param sequenceNumber The sequence number to recover
     * @throws IllegalArgumentException if sequenceNumber is not positive
     */
    public void recoverSpecificSequence(int sequenceNumber) {
        if (sequenceNumber <= 0) {
            throw new IllegalArgumentException("Sequence number must be positive");
        }

        logger.info("Initiating targeted recovery for sequence: " + sequenceNumber);

        try {
            String checkpoint = checkpointManager.getCheckpoint(sequenceNumber);
            if (checkpoint == null) {
                logger.warning("No checkpoint found for sequence " + sequenceNumber + ". Using latest available.");
                synchronizeToStableState();
                return;
            }

            // Broadcast specific sequence recovery message
            PBFTMessage sequenceRecoveryMessage = new PBFTMessage(
                    "SEQUENCE-RECOVERY",
                    pbftNetwork.getNodeId(),
                    "sequence-recovery-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            sequenceRecoveryMessage.setContent("Recover sequence " + sequenceNumber);

            p2pManager.sendBroadcast(messageHandler.encodePBFTMessage(sequenceRecoveryMessage));
            logger.info("Sequence recovery message broadcasted for sequence: " + sequenceNumber);
        } catch (Exception e) {
            logger.severe("Error during sequence recovery: " + e.getMessage());
            throw new RuntimeException("Failed to recover specific sequence", e);
        }
    }
}



