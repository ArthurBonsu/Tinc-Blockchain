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
