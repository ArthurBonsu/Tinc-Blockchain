//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//import org.tinc.p2p.RobustP2PManager;
//
//import java.security.PublicKey;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * DynamicMembership manages dynamic membership changes in the PBFT network.
// * - Allows nodes to join or leave the network dynamically.
// * - Updates membership lists and associated public keys.
// * - Ensures consistent membership across replicas using quorum-based updates.
// */
//public class DynamicMembership {
//
//    private final Map<Integer, PublicKey> membershipList; // Maps node IDs to public keys
//    private final ReentrantLock lock;                    // Ensures thread-safe updates
//    private final int quorumSize;                        // Quorum size for consistent updates
//    private final RobustP2PManager p2pManager;           // Peer-to-peer network manager
//
//    /**
//     * Constructor to initialize DynamicMembership.
//     *
//     * @param initialMembership Map of initial node IDs and their public keys.
//     * @param totalReplicas     Total number of replicas in the network.
//     * @param p2pManager        The RobustP2PManager instance for broadcasting updates.
//     */
//    public DynamicMembership(Map<Integer, Keypair> initialMembership, int totalReplicas, RobustP2PManager p2pManager) {
//        this.membershipList = new ConcurrentHashMap<>();
//        this.lock = new ReentrantLock();
//        this.quorumSize = (2 * totalReplicas) / 3 + 1; // 2f + 1 for consistency
//        this.p2pManager = p2pManager;
//
//        // Initialize membership list using public keys from Keypair
//        for (Map.Entry<Integer, Keypair> entry : initialMembership.entrySet()) {
//            this.membershipList.put(entry.getKey(), entry.getValue().getPublicKey());
//        }
//    }
//
//    /**
//     * Adds a new node to the network using Keypair.
//     *
//     * @param nodeId  The ID of the new node.
//     * @param keypair The Keypair instance of the new node.
//     */
//    public void addNode(int nodeId, Keypair keypair) {
//        lock.lock();
//        try {
//            if (membershipList.containsKey(nodeId)) {
//                System.out.println("Node " + nodeId + " is already a member.");
//                return;
//            }
//
//            // Add the node to the membership list
//            PublicKey publicKey = keypair.getPublicKey();
//            membershipList.put(nodeId, publicKey);
//            broadcastMembershipUpdate("ADD", nodeId, publicKey);
//            System.out.println("Node " + nodeId + " added to the network.");
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Removes a node from the network.
//     *
//     * @param nodeId The ID of the node to remove.
//     */
//    public void removeNode(int nodeId) {
//        lock.lock();
//        try {
//            if (!membershipList.containsKey(nodeId)) {
//                System.out.println("Node " + nodeId + " is not a member.");
//                return;
//            }
//
//            // Remove the node from the membership list
//            membershipList.remove(nodeId);
//            broadcastMembershipUpdate("REMOVE", nodeId, null);
//            System.out.println("Node " + nodeId + " removed from the network.");
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Handles membership update messages from other replicas.
//     *
//     * @param updateType The type of update ("ADD" or "REMOVE").
//     * @param nodeId     The ID of the node being updated.
//     * @param publicKey  The public key of the node (for "ADD" updates).
//     */
//    public void handleMembershipUpdate(String updateType, int nodeId, PublicKey publicKey) {
//        lock.lock();
//        try {
//            if ("ADD".equals(updateType)) {
//                if (!membershipList.containsKey(nodeId)) {
//                    membershipList.put(nodeId, publicKey);
//                    System.out.println("Node " + nodeId + " added via quorum update.");
//                }
//            } else if ("REMOVE".equals(updateType)) {
//                if (membershipList.containsKey(nodeId)) {
//                    membershipList.remove(nodeId);
//                    System.out.println("Node " + nodeId + " removed via quorum update.");
//                }
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    /**
//     * Broadcasts membership updates to other replicas.
//     *
//     * @param updateType The type of update ("ADD" or "REMOVE").
//     * @param nodeId     The ID of the node being updated.
//     * @param publicKey  The public key of the node (for "ADD" updates).
//     */
//    private void broadcastMembershipUpdate(String updateType, int nodeId, PublicKey publicKey) {
//        String messageContent = updateType + ":" + nodeId;
//        if (publicKey != null) {
//            messageContent += ":" + Base64.getEncoder().encodeToString(publicKey.getEncoded());
//        }
//
//        PBFTMessage message = new PBFTMessage(
//                "MEMBERSHIP-UPDATE",
//                PBFTNetwork.getNodeId(),
//                messageContent,
//                null // Optional signature can be added here
//        );
//
//        p2pManager.sendBroadcast(message.serialize());
//        System.out.println("Broadcasted " + updateType + " update for node " + nodeId);
//    }
//
//    /**
//     * Retrieves the current membership list.
//     *
//     * @return A map of node IDs to their public keys.
//     */
//    public Map<Integer, PublicKey> getMembershipList() {
//        return Collections.unmodifiableMap(membershipList);
//    }
//}





package org.example.app.core.pbftconsensus;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.p2p.RobustP2PManager;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * DynamicMembership manages dynamic membership changes in the PBFT network.
 * - Allows nodes to join or leave the network dynamically.
 * - Updates membership lists and associated public keys.
 * - Ensures consistent membership across replicas using quorum-based updates.
 */
public class DynamicMembership {
    private static final Logger logger = Logger.getLogger(DynamicMembership.class.getName());

    private final Map<Integer, PublicKey> membershipList; // Maps node IDs to public keys
    private final ReentrantReadWriteLock lock;           // Ensures thread-safe updates
    private final int quorumSize;                        // Quorum size for consistent updates
    private final RobustP2PManager p2pManager;           // Peer-to-peer network manager
    private final PBFTMessageHandler messageHandler;     // For message handling

    /**
     * Constructor to initialize DynamicMembership.
     *
     * @param initialMembership Map of initial node IDs and their public keys.
     * @param totalReplicas     Total number of replicas in the network.
     * @param p2pManager        The RobustP2PManager instance for broadcasting updates.
     * @param messageHandler    The PBFTMessageHandler for encoding messages.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public DynamicMembership(Map<Integer, Keypair> initialMembership, int totalReplicas,
                             RobustP2PManager p2pManager, PBFTMessageHandler messageHandler) {
        if (initialMembership == null) {
            throw new IllegalArgumentException("Initial membership cannot be null");
        }
        if (totalReplicas < 1) {
            throw new IllegalArgumentException("Total replicas must be at least 1");
        }
        if (p2pManager == null) {
            throw new IllegalArgumentException("P2P manager cannot be null");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("Message handler cannot be null");
        }

        this.membershipList = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.quorumSize = Math.max((2 * totalReplicas) / 3 + 1, 1); // 2f + 1 for consistency, min 1
        this.p2pManager = p2pManager;
        this.messageHandler = messageHandler;

        // Initialize membership list using public keys from Keypair
        for (Map.Entry<Integer, Keypair> entry : initialMembership.entrySet()) {
            if (entry.getValue() != null) {
                this.membershipList.put(entry.getKey(), entry.getValue().getPublicKey());
            }
        }
    }

    /**
     * Adds a new node to the network using Keypair.
     *
     * @param nodeId  The ID of the new node.
     * @param keypair The Keypair instance of the new node.
     * @throws IllegalArgumentException if nodeId is negative or keypair is null
     */
    public void addNode(int nodeId, Keypair keypair) {
        if (nodeId < 0) {
            throw new IllegalArgumentException("Node ID cannot be negative");
        }
        if (keypair == null) {
            throw new IllegalArgumentException("Keypair cannot be null");
        }

        lock.writeLock().lock();
        try {
            if (membershipList.containsKey(nodeId)) {
                logger.info("Node " + nodeId + " is already a member.");
                return;
            }

            // Add the node to the membership list
            PublicKey publicKey = keypair.getPublicKey();
            membershipList.put(nodeId, publicKey);
            broadcastMembershipUpdate("ADD", nodeId, publicKey);
            logger.info("Node " + nodeId + " added to the network.");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a node from the network.
     *
     * @param nodeId The ID of the node to remove.
     * @throws IllegalArgumentException if nodeId is negative
     */
    public void removeNode(int nodeId) {
        if (nodeId < 0) {
            throw new IllegalArgumentException("Node ID cannot be negative");
        }

        lock.writeLock().lock();
        try {
            if (!membershipList.containsKey(nodeId)) {
                logger.info("Node " + nodeId + " is not a member.");
                return;
            }

            // Remove the node from the membership list
            membershipList.remove(nodeId);
            broadcastMembershipUpdate("REMOVE", nodeId, null);
            logger.info("Node " + nodeId + " removed from the network.");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Handles membership update messages from other replicas.
     *
     * @param updateType The type of update ("ADD" or "REMOVE").
     * @param nodeId     The ID of the node being updated.
     * @param publicKey  The public key of the node (for "ADD" updates).
     * @throws IllegalArgumentException if the updateType is invalid
     */
    public void handleMembershipUpdate(String updateType, int nodeId, PublicKey publicKey) {
        if (updateType == null || !(updateType.equals("ADD") || updateType.equals("REMOVE"))) {
            throw new IllegalArgumentException("Invalid update type: " + updateType);
        }

        lock.writeLock().lock();
        try {
            if ("ADD".equals(updateType)) {
                if (publicKey == null) {
                    logger.warning("Cannot add node " + nodeId + " with null public key");
                    return;
                }

                if (!membershipList.containsKey(nodeId)) {
                    membershipList.put(nodeId, publicKey);
                    logger.info("Node " + nodeId + " added via quorum update.");
                }
            } else if ("REMOVE".equals(updateType)) {
                if (membershipList.containsKey(nodeId)) {
                    membershipList.remove(nodeId);
                    logger.info("Node " + nodeId + " removed via quorum update.");
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Broadcasts membership updates to other replicas.
     *
     * @param updateType The type of update ("ADD" or "REMOVE").
     * @param nodeId     The ID of the node being updated.
     * @param publicKey  The public key of the node (for "ADD" updates).
     */
    private void broadcastMembershipUpdate(String updateType, int nodeId, PublicKey publicKey) {
        try {
            String messageContent = updateType + ":" + nodeId;
            if (publicKey != null) {
                messageContent += ":" + Base64.getEncoder().encodeToString(publicKey.getEncoded());
            }

            PBFTMessage message = new PBFTMessage(
                    "MEMBERSHIP-UPDATE",
                    PBFTNetwork.getNodeId(),
                    "membership-" + UUID.randomUUID().toString().substring(0, 8),
                    null
            );
            message.setContent(messageContent);

            p2pManager.sendBroadcast(messageHandler.encodePBFTMessage(message));
            logger.info("Broadcasted " + updateType + " update for node " + nodeId);
        } catch (Exception e) {
            logger.severe("Failed to broadcast membership update: " + e.getMessage());
        }
    }

    /**
     * Retrieves the current membership list.
     *
     * @return A map of node IDs to their public keys.
     */
    public Map<Integer, PublicKey> getMembershipList() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new HashMap<>(membershipList));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if a node ID is part of the current membership.
     *
     * @param nodeId The node ID to check
     * @return true if the node is a member, false otherwise
     */
    public boolean isMember(int nodeId) {
        lock.readLock().lock();
        try {
            return membershipList.containsKey(nodeId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the current number of nodes in the membership.
     *
     * @return The number of nodes
     */
    public int getMembershipSize() {
        lock.readLock().lock();
        try {
            return membershipList.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the quorum size required for membership changes.
     *
     * @return The quorum size
     */
    public int getQuorumSize() {
        return quorumSize;
    }
}


