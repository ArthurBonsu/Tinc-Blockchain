//package org.tinc.consensus.pbft;
//
//import org.tinc.p2p.RobustP2PManager;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * FaultDetector monitors the activity of replicas and detects faults,
// * such as non-responsiveness or delays in communication.
// */
//public class FaultDetector {
//
//    private final Map<Integer, Long> lastMessageTimestamps; // Tracks last message timestamps for replicas
//    private final long faultDetectionInterval; // Maximum allowed interval before considering a replica faulty
//    private final RobustP2PManager p2pManager; // Peer-to-peer network manager
//    private final PBFTNetwork pbftNetwork;
//    /**
//     * Constructor to initialize the FaultDetector.
//     *
//     * @param faultDetectionInterval Time in milliseconds after which a replica is considered faulty.
//     * @param p2pManager             The RobustP2PManager instance for communication.
//     */
//    public FaultDetector(long faultDetectionInterval, RobustP2PManager p2pManager, PBFTNetwork pbftNetwork) {
//        this.pbftNetwork = pbftNetwork;
//        this.lastMessageTimestamps = new ConcurrentHashMap<>();
//        this.faultDetectionInterval = faultDetectionInterval;
//        this.p2pManager = p2pManager;
//    }
//
//    /**
//     * Updates the last communication timestamp for a specific replica.
//     *
//     * @param replicaId ID of the replica that sent a message.
//     */
//    public void updateReplicaTimestamp(int replicaId) {
//        lastMessageTimestamps.put(replicaId, System.currentTimeMillis());
//    }
//
//    /**
//     * Detects faults by checking if any replica has exceeded the allowed time interval since last communication.
//     *
//     * @return A list of IDs of replicas detected as faulty.
//     */
//    public List<Integer> detectFaults() {
//        List<Integer> faultyReplicas = new ArrayList<>();
//        long currentTime = System.currentTimeMillis();
//
//        for (Map.Entry<Integer, Long> entry : lastMessageTimestamps.entrySet()) {
//            int replicaId = entry.getKey();
//            Long lastTimestamp = entry.getValue();
//            if (lastTimestamp == null || (currentTime - lastTimestamp > faultDetectionInterval)) {
//                faultyReplicas.add(replicaId);
//                System.out.println("Fault detected for replica: " + replicaId);
//            }
//        }
//        return faultyReplicas;
//    }
//
//    /**
//     * Notifies all replicas about the detected faulty replicas.
//     *
//     * @param faultyReplicas List of IDs of faulty replicas.
//     */
//    public void notifyFaults(List<Integer> faultyReplicas) {
//        if (faultyReplicas.isEmpty()) {
//            System.out.println("No faults detected to notify.");
//            return;
//        }
//
//        String content = "Faulty replicas detected: " + faultyReplicas;
//        PBFTMessage faultNotification = new PBFTMessage(
//                "FAULT-DETECTION",
//                PBFTNetwork.getNodeId(),
//                content,
//                null // Optional signature can be added here
//        );
//
//        p2pManager.sendBroadcast(faultNotification.serialize());
//        System.out.println("Broadcasted fault notification for replicas: " + faultyReplicas);
//    }
//
//    /**
//     * Retrieves the list of replicas that have been identified as faulty.
//     *
//     * @return A map of faulty replica IDs and their last known timestamps.
//     */
//    public Map<Integer, Long> getFaultyReplicas() {
//        Map<Integer, Long> faultyReplicas = new HashMap<>();
//        long currentTime = System.currentTimeMillis();
//
//        for (Map.Entry<Integer, Long> entry : lastMessageTimestamps.entrySet()) {
//            int replicaId = entry.getKey();
//            Long lastTimestamp = entry.getValue();
//            if (lastTimestamp == null || (currentTime - lastTimestamp > faultDetectionInterval)) {
//                faultyReplicas.put(replicaId, lastTimestamp);
//            }
//        }
//
//        return faultyReplicas;
//    }
//
//    /**
//     * Clears the fault record for a specific replica, marking it as active again.
//     *
//     * @param replicaId The ID of the replica to clear the fault for.
//     */
//    public void clearFault(int replicaId) {
//        lastMessageTimestamps.remove(replicaId);
//        System.out.println("Fault cleared for replica: " + replicaId);
//    }
//}
