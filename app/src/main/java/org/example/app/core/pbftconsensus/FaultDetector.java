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




package org.example.app.core.pbftconsensus;
import org.example.app.core.p2p.RobustP2PManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * FaultDetector monitors the activity of replicas and detects faults,
 * such as non-responsiveness or delays in communication.
 */
public class FaultDetector {
    private static final Logger logger = Logger.getLogger(FaultDetector.class.getName());

    private final Map<Integer, Long> lastMessageTimestamps; // Tracks last message timestamps for replicas
    private final long faultDetectionInterval; // Maximum allowed interval before considering a replica faulty
    private final RobustP2PManager p2pManager; // Peer-to-peer network manager
    private final PBFTNetwork pbftNetwork; // PBFT network instance
    private final PBFTMessageHandler messageHandler; // Message handler
    private final ScheduledExecutorService scheduler; // Scheduler for periodic fault detection

    /**
     * Constructor to initialize the FaultDetector.
     *
     * @param faultDetectionInterval Time in milliseconds after which a replica is considered faulty.
     * @param p2pManager             The RobustP2PManager instance for communication.
     * @param pbftNetwork            The PBFTNetwork instance for node operations.
     * @param messageHandler         The PBFTMessageHandler for message encoding.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public FaultDetector(long faultDetectionInterval, RobustP2PManager p2pManager,
                         PBFTNetwork pbftNetwork, PBFTMessageHandler messageHandler) {
        if (faultDetectionInterval <= 0) {
            throw new IllegalArgumentException("Fault detection interval must be positive");
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

        this.pbftNetwork = pbftNetwork;
        this.lastMessageTimestamps = new ConcurrentHashMap<>();
        this.faultDetectionInterval = faultDetectionInterval;
        this.p2pManager = p2pManager;
        this.messageHandler = messageHandler;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Starts periodic fault detection.
     *
     * @param intervalSeconds The interval in seconds between fault detection runs.
     */
    public void startPeriodicFaultDetection(int intervalSeconds) {
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Interval must be positive");
        }

        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Integer> faultyReplicas = detectFaults();
                if (!faultyReplicas.isEmpty()) {
                    notifyFaults(faultyReplicas);
                }
            } catch (Exception e) {
                logger.severe("Error during periodic fault detection: " + e.getMessage());
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        logger.info("Started periodic fault detection with interval: " + intervalSeconds + " seconds");
    }

    /**
     * Updates the last communication timestamp for a specific replica.
     *
     * @param replicaId ID of the replica that sent a message.
     * @throws IllegalArgumentException if replicaId is negative
     */
    public void updateReplicaTimestamp(int replicaId) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }
        lastMessageTimestamps.put(replicaId, System.currentTimeMillis());
    }

    /**
     * Detects faults by checking if any replica has exceeded the allowed time interval since last communication.
     *
     * @return A list of IDs of replicas detected as faulty.
     */
    public List<Integer> detectFaults() {
        List<Integer> faultyReplicas = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Integer, Long> entry : lastMessageTimestamps.entrySet()) {
            int replicaId = entry.getKey();
            Long lastTimestamp = entry.getValue();
            if (lastTimestamp == null || (currentTime - lastTimestamp > faultDetectionInterval)) {
                faultyReplicas.add(replicaId);
                logger.warning("Fault detected for replica: " + replicaId);
            }
        }
        return faultyReplicas;
    }

    /**
     * Notifies all replicas about the detected faulty replicas.
     *
     * @param faultyReplicas List of IDs of faulty replicas.
     * @throws IllegalArgumentException if faultyReplicas is null
     */
    public void notifyFaults(List<Integer> faultyReplicas) {
        if (faultyReplicas == null) {
            throw new IllegalArgumentException("Faulty replicas list cannot be null");
        }

        if (faultyReplicas.isEmpty()) {
            logger.info("No faults detected to notify.");
            return;
        }

        try {
            String content = "Faulty replicas detected: " + faultyReplicas;
            PBFTMessage faultNotification = new PBFTMessage(
                    "FAULT-DETECTION",
                    PBFTNetwork.getNodeId(),
                    "fault-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            faultNotification.setContent(content);

            p2pManager.sendBroadcast(messageHandler.encodePBFTMessage(faultNotification));
            logger.info("Broadcasted fault notification for replicas: " + faultyReplicas);
        } catch (Exception e) {
            logger.severe("Failed to broadcast fault notification: " + e.getMessage());
        }
    }

    /**
     * Retrieves the list of replicas that have been identified as faulty.
     *
     * @return A map of faulty replica IDs and their last known timestamps.
     */
    public Map<Integer, Long> getFaultyReplicas() {
        Map<Integer, Long> faultyReplicas = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Integer, Long> entry : lastMessageTimestamps.entrySet()) {
            int replicaId = entry.getKey();
            Long lastTimestamp = entry.getValue();
            if (lastTimestamp == null || (currentTime - lastTimestamp > faultDetectionInterval)) {
                faultyReplicas.put(replicaId, lastTimestamp);
            }
        }

        return faultyReplicas;
    }

    /**
     * Clears the fault record for a specific replica, marking it as active again.
     *
     * @param replicaId The ID of the replica to clear the fault for.
     * @throws IllegalArgumentException if replicaId is negative
     */
    public void clearFault(int replicaId) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }

        lastMessageTimestamps.remove(replicaId);
        logger.info("Fault cleared for replica: " + replicaId);
    }

    /**
     * Shuts down the fault detector's scheduled tasks.
     */
    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("Fault detector shutdown completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
            logger.warning("Fault detector shutdown interrupted");
        }
    }
}



