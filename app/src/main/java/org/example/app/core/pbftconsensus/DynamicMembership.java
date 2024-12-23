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
