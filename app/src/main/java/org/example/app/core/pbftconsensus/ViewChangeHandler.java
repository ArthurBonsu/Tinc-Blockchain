//package org.tinc.consensus.pbft;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * ViewChangeHandler manages the view change process, ensuring that all replicas
// * agree on the new primary and maintain consistent state.
// */
//public class ViewChangeHandler {
//
//    private final PrimaryElection primaryElection;      // Handles primary selection logic
//    private final PBFTNetwork pbftNetwork;              // Handles communication between replicas
//    private final Map<Integer, Integer> viewChangeVotes; // Tracks votes for new views
//    private final int quorumSize;                       // Quorum size for view changes
//    private int currentView;                            // Current view (or primary)
//
//    /**
//     * Constructor to initialize the ViewChangeHandler.
//     *
//     * @param primaryElection The PrimaryElection instance to handle primary selection.
//     * @param pbftNetwork     The PBFTNetwork instance for communication.
//     * @param initialView     The initial view ID.
//     * @param totalReplicas   The total number of replicas in the system.
//     */
//    public ViewChangeHandler(PrimaryElection primaryElection, PBFTNetwork pbftNetwork,
//                             int initialView, int totalReplicas) {
//        this.primaryElection = primaryElection;
//        this.pbftNetwork = pbftNetwork;
//        this.viewChangeVotes = new ConcurrentHashMap<>();
//        this.currentView = initialView;
//        this.quorumSize = (2 * (totalReplicas - 1)) / 3 + 1; // 2f + 1 quorum for consistency
//    }
//
//    /**
//     * Initiates a view change process when the current primary fails or misbehaves.
//     *
//     * @param failedPrimary The ID of the failed primary replica.
//     */
//    public void initiateViewChange(int failedPrimary) {
//        System.out.println("Initiating view change due to failure of primary: " + failedPrimary);
//
//        // Elect a new primary
//        int newView = primaryElection.electNewPrimary(quorumSize);
//        broadcastViewChange(newView);
//
//        // Update the current view
//        updateView(newView);
//    }
//
//    /**
//     * Broadcasts a VIEW-CHANGE message to all replicas, indicating the new view.
//     *
//     * @param newView The new view ID to propose.
//     */
//    private void broadcastViewChange(int newView) {
//        PBFTMessage viewChangeMessage = new PBFTMessage(
//                "VIEW-CHANGE",
//                PBFTNetwork.getNodeId(),
//                "View change to " + newView,
//                null
//        );
//        pbftNetwork.broadcastPBFTMessage(String.valueOf(viewChangeMessage));
//        System.out.println("Broadcasting VIEW-CHANGE message for new view: " + newView);
//    }
//
//    /**
//     * Updates the current view and synchronizes all replicas with the new primary.
//     *
//     * @param newView The new view ID to set.
//     */
//    public void updateView(int newView) {
//        this.currentView = newView;
//        primaryElection.updateView(newView);
//        System.out.println("View updated to: " + newView);
//    }
//
//    /**
//     * Handles incoming VIEW-CHANGE messages and ensures quorum agreement on the new view.
//     *
//     * @param senderId    The ID of the replica sending the message.
//     * @param proposedView The view ID proposed by the sender.
//     */
//    public void handleViewChangeMessage(int senderId, int proposedView) {
//        viewChangeVotes.merge(proposedView, 1, Integer::sum);
//        System.out.println("Replica " + senderId + " voted for view: " + proposedView);
//
//        if (viewChangeVotes.get(proposedView) >= quorumSize) {
//            System.out.println("Quorum reached for view: " + proposedView);
//            updateView(proposedView);
//        }
//    }
//
//    /**
//     * Synchronizes replica states after a view change using checkpoints.
//     *
//     * @param checkpointManager The CheckpointManager instance for retrieving stable checkpoints.
//     */
//    public void synchronizeState(CheckpointManager checkpointManager) {
//        int stableSequenceNumber = checkpointManager.getLatestCheckpointSequence();
//        System.out.println("Synchronizing state to sequence number: " + stableSequenceNumber);
//
//        // Broadcast stable state to all replicas
//        PBFTMessage checkpointSyncMessage = new PBFTMessage(
//                "CHECKPOINT-SYNC",
//                currentView,
//                "Synchronize to sequence " + stableSequenceNumber,
//                null
//        );
//        pbftNetwork.broadcastPBFTMessage(String.valueOf(checkpointSyncMessage));
//    }
//
//    /**
//     * Gets the current view (or primary ID).
//     *
//     * @return The current primary ID.
//     */
//    public int getCurrentView() {
//        return currentView;
//    }
//}





package org.example.app.core.pbftconsensus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewChangeHandler manages the view change process, ensuring that all replicas
 * agree on the new primary and maintain consistent state.
 */
public class ViewChangeHandler {
    private static final Logger logger = Logger.getLogger(ViewChangeHandler.class.getName());

    private final PrimaryElection primaryElection;      // Handles primary selection logic
    private final PBFTNetwork pbftNetwork;              // Handles communication between replicas
    private final Map<Integer, Integer> viewChangeVotes; // Tracks votes for new views
    private final int quorumSize;                       // Quorum size for view changes
    private final ReadWriteLock viewLock;               // Thread-safe view access
    private final PBFTMessageHandler messageHandler;    // For message encoding/decoding

    /**
     * Constructor to initialize the ViewChangeHandler.
     *
     * @param primaryElection The PrimaryElection instance to handle primary selection.
     * @param pbftNetwork     The PBFTNetwork instance for communication.
     * @param initialView     The initial view ID.
     * @param totalReplicas   The total number of replicas in the system.
     * @param messageHandler  The PBFTMessageHandler for message encoding/decoding.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public ViewChangeHandler(PrimaryElection primaryElection, PBFTNetwork pbftNetwork,
                             int initialView, int totalReplicas, PBFTMessageHandler messageHandler) {
        if (primaryElection == null) {
            throw new IllegalArgumentException("Primary election cannot be null");
        }
        if (pbftNetwork == null) {
            throw new IllegalArgumentException("PBFT network cannot be null");
        }
        if (initialView < 0) {
            throw new IllegalArgumentException("Initial view cannot be negative");
        }
        if (totalReplicas <= 0) {
            throw new IllegalArgumentException("Total replicas must be positive");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("Message handler cannot be null");
        }

        this.primaryElection = primaryElection;
        this.pbftNetwork = pbftNetwork;
        this.messageHandler = messageHandler;
        this.viewChangeVotes = new ConcurrentHashMap<>();
        this.viewLock = new ReentrantReadWriteLock();

        // Calculate quorum size based on the Byzantine consensus requirement: 2f + 1
        // where f is the maximum number of faulty replicas (f = (n-1)/3)
        this.quorumSize = (2 * (totalReplicas - 1)) / 3 + 1;

        logger.info("ViewChangeHandler initialized with quorum size: " + quorumSize);
    }

    /**
     * Initiates a view change process when the current primary fails or misbehaves.
     *
     * @param failedPrimary The ID of the failed primary replica.
     * @throws IllegalArgumentException if failedPrimary is negative
     */
    public void initiateViewChange(int failedPrimary) {
        if (failedPrimary < 0) {
            throw new IllegalArgumentException("Failed primary ID cannot be negative");
        }

        logger.info("Initiating view change due to failure of primary: " + failedPrimary);

        try {
            viewLock.writeLock().lock();

            // Elect a new primary
            int newView = primaryElection.electNewPrimary(quorumSize);
            broadcastViewChange(newView);

            // Update the current view locally
            updateView(newView);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during view change initiation", e);
            throw new RuntimeException("Failed to initiate view change", e);
        } finally {
            viewLock.writeLock().unlock();
        }
    }

    /**
     * Broadcasts a VIEW-CHANGE message to all replicas, indicating the new view.
     *
     * @param newView The new view ID to propose.
     */
    private void broadcastViewChange(int newView) {
        try {
            PBFTMessage viewChangeMessage = new PBFTMessage(
                    "VIEW-CHANGE",
                    pbftNetwork.getNodeId(),
                    "view-change-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            viewChangeMessage.setContent(String.valueOf(newView));

            pbftNetwork.broadcastPBFTMessage(messageHandler.encodePBFTMessage(viewChangeMessage));
            logger.info("Broadcasting VIEW-CHANGE message for new view: " + newView);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error broadcasting view change", e);
            throw new RuntimeException("Failed to broadcast view change", e);
        }
    }

    /**
     * Updates the current view and synchronizes all replicas with the new primary.
     *
     * @param newView The new view ID to set.
     * @throws IllegalArgumentException if newView is negative
     */
    public void updateView(int newView) {
        if (newView < 0) {
            throw new IllegalArgumentException("New view cannot be negative");
        }

        try {
            viewLock.writeLock().lock();
            primaryElection.updateView(newView);
            logger.info("View updated to: " + newView);
        } finally {
            viewLock.writeLock().unlock();
        }
    }

    /**
     * Handles incoming VIEW-CHANGE messages and ensures quorum agreement on the new view.
     *
     * @param senderId    The ID of the replica sending the message.
     * @param proposedView The view ID proposed by the sender.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void handleViewChangeMessage(int senderId, int proposedView) {
        if (senderId < 0) {
            throw new IllegalArgumentException("Sender ID cannot be negative");
        }
        if (proposedView < 0) {
            throw new IllegalArgumentException("Proposed view cannot be negative");
        }

        // Update votes for this view
        viewChangeVotes.merge(proposedView, 1, Integer::sum);
        logger.info("Replica " + senderId + " voted for view: " + proposedView);

        // Check if we have a quorum for this view
        Integer votes = viewChangeVotes.get(proposedView);
        if (votes != null && votes >= quorumSize) {
            logger.info("Quorum reached for view: " + proposedView + " with " + votes + " votes");
            updateView(proposedView);

            // Clear all votes for other views once we've reached a decision
            viewLock.writeLock().lock();
            try {
                viewChangeVotes.clear();
                viewChangeVotes.put(proposedView, votes); // Retain the votes for the winning view
            } finally {
                viewLock.writeLock().unlock();
            }
        }
    }

    /**
     * Synchronizes replica states after a view change using checkpoints.
     *
     * @param checkpointManager The CheckpointManager instance for retrieving stable checkpoints.
     * @throws IllegalArgumentException if checkpointManager is null
     */
    public void synchronizeState(CheckpointManager checkpointManager) {
        if (checkpointManager == null) {
            throw new IllegalArgumentException("Checkpoint manager cannot be null");
        }

        try {
            int stableSequenceNumber = checkpointManager.getLatestCheckpointSequence();
            logger.info("Synchronizing state to sequence number: " + stableSequenceNumber);

            // Broadcast stable state to all replicas
            PBFTMessage checkpointSyncMessage = new PBFTMessage(
                    "CHECKPOINT-SYNC",
                    primaryElection.getCurrentView(),
                    "checkpoint-sync-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            checkpointSyncMessage.setContent("Synchronize to sequence " + stableSequenceNumber);

            pbftNetwork.broadcastPBFTMessage(messageHandler.encodePBFTMessage(checkpointSyncMessage));
            logger.info("Checkpoint synchronization message broadcasted");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during state synchronization", e);
            throw new RuntimeException("Failed to synchronize state", e);
        }
    }

    /**
     * Gets the current view (or primary ID).
     *
     * @return The current primary ID.
     */
    public int getCurrentView() {
        return primaryElection.getCurrentView();
    }

    /**
     * Gets the quorum size required for view changes.
     *
     * @return The quorum size.
     */
    public int getQuorumSize() {
        return quorumSize;
    }

    /**
     * Gets the number of votes received for a specific view.
     *
     * @param view The view to check
     * @return The number of votes, or 0 if none
     */
    public int getVotesForView(int view) {
        return viewChangeVotes.getOrDefault(view, 0);
    }

    /**
     * Resets all view change votes.
     */
    public void resetVotes() {
        viewLock.writeLock().lock();
        try {
            viewChangeVotes.clear();
            logger.info("View change votes reset");
        } finally {
            viewLock.writeLock().unlock();
        }
    }

    /**
     * Checks if a quorum has been reached for any view.
     *
     * @return true if a quorum has been reached, false otherwise
     */
    public boolean isQuorumReached() {
        for (Integer votes : viewChangeVotes.values()) {
            if (votes >= quorumSize) {
                return true;
            }
        }
        return false;
    }
}
