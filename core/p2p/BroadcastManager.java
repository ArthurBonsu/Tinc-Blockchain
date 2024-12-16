package org.tinc.p2p;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastManager {
    private final Set<String> peers; // List of known peers
    private final Map<String, Boolean> acknowledgments; // Tracks acknowledgment from each peer
    private final Peer peer; // The local peer instance for broadcasting messages

    /**
     * Initializes the BroadcastManager with a list of known peers.
     *
     * @param peer  The local peer instance.
     * @param peers The list of known peers' addresses.
     */
    public BroadcastManager(Peer peer, Set<String> peers) {
        if (peer == null) {
            throw new IllegalArgumentException("Peer cannot be null.");
        }
        this.peer = peer;
        this.peers = peers != null ? new HashSet<>(peers) : new HashSet<>();
        this.acknowledgments = new ConcurrentHashMap<>();
    }

    /**
     * Broadcasts a message to all known peers in the network.
     *
     * @param messageContent The content of the message to broadcast.
     */
//    public void broadcastMessage(String messageContent) {
//        if (messageContent == null || messageContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be null or empty.");
//        }
//
//        Message message = new Message(peer.getPeerId(), messageContent);
//
//        // Iterate over all known peers and send the message
//        for (String peerAddress : peers) {
//            try {
//                peer.connectToPeer(peerAddress, message.getContent());
//                System.out.println("BroadcastManager: Sent message to " + peerAddress);
//                acknowledgments.put(peerAddress, false); // Track acknowledgment for each peer
//            } catch (Exception e) {
//                System.err.println("BroadcastManager: Failed to send message to " + peerAddress + ". Error: " + e.getMessage());
//            }
//        }
//    }
    public void broadcastMessage(String messageContent) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty.");
        }

        Message message = new Message(peer.getPeerId(), messageContent);
        for (String peerAddress : peers) {
            int retries = 3;
            while (retries-- > 0) {
                try {
                    peer.connectToPeer(peerAddress, message.getContent());
                    System.out.println("BroadcastManager: Sent message to " + peerAddress);
                    acknowledgments.put(peerAddress, false);
                    break;
                } catch (Exception e) {
                    System.err.println("BroadcastManager: Failed to send message to " + peerAddress + ". Retrying...");
                    if (retries == 0) {
                        System.err.println("BroadcastManager: Permanently failed to send message to " + peerAddress);
                    }
                }
            }
        }
    }

    /**
     * Handles acknowledgment messages from peers confirming receipt of broadcasted messages.
     *
     * @param peerAddress The address of the peer sending the acknowledgment.
     */
    public void acknowledgeReceipt(String peerAddress) {
        if (peerAddress == null || !acknowledgments.containsKey(peerAddress)) {
            System.err.println("BroadcastManager: Acknowledgment from unknown or invalid peer: " + peerAddress);
            return;
        }
        acknowledgments.put(peerAddress, true); // Mark acknowledgment as received
        System.out.println("BroadcastManager: Acknowledgment received from " + peerAddress);
    }

    /**
     * Checks the status of all acknowledgments.
     *
     * @return A map of peers and their acknowledgment statuses.
     */
    public Map<String, Boolean> getAcknowledgmentStatus() {
        return Collections.unmodifiableMap(acknowledgments);
    }

    /**
     * Adds a new peer to the broadcast list.
     *
     * @param peerAddress The address of the new peer to add.
     */
    public void addPeer(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            System.err.println("BroadcastManager: Invalid peer address: " + peerAddress);
            return;
        }
        peers.add(peerAddress);
        System.out.println("BroadcastManager: Added peer " + peerAddress + " to broadcast list.");
    }

    /**
     * Removes a peer from the broadcast list.
     *
     * @param peerAddress The address of the peer to remove.
     */
    public void removePeer(String peerAddress) {
        if (peers.remove(peerAddress)) {
            System.out.println("BroadcastManager: Removed peer " + peerAddress + " from broadcast list.");
        } else {
            System.err.println("BroadcastManager: Peer " + peerAddress + " not found in broadcast list.");
        }
    }
}
