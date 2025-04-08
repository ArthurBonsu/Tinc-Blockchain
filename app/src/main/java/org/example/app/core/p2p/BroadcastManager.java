//package org.example.app.core.p2p;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.time.Instant;
//
//public class BroadcastManager {
//    private final Set<String> peers;
//    private final Map<String, PeerStatus> peerStatuses;
//    private final Peer peer;
//    private final ExecutorService broadcastExecutor;
//    private static final int MAX_RETRIES = 3;
//    private static final long RETRY_DELAY_MS = 1000;
//    private static final long ACKNOWLEDGMENT_TIMEOUT_MS = 10000; // 10 seconds
//
//    public static class PeerStatus {
//        private boolean acknowledged;
//        private Instant lastAttempt;
//        private int retryCount;
//        private BroadcastStatus status;
//
//        public PeerStatus() {
//            this.acknowledged = false;
//            this.lastAttempt = null;
//            this.retryCount = 0;
//            this.status = BroadcastStatus.PENDING;
//        }
//    }
//
//    public enum BroadcastStatus {
//        PENDING,
//        SENT,
//        ACKNOWLEDGED,
//        FAILED,
//        TIMEOUT
//    }
//
//    public BroadcastManager(Peer peer, Set<String> peers) {
//        if (peer == null) {
//            throw new IllegalArgumentException("Peer cannot be null.");
//        }
//        this.peer = peer;
//        this.peers = Collections.synchronizedSet(peers != null ? new HashSet<>(peers) : new HashSet<>());
//        this.peerStatuses = new ConcurrentHashMap<>();
//        this.broadcastExecutor = Executors.newCachedThreadPool();
//
//        // Initialize status tracking for existing peers
//        peers.forEach(peerAddress -> peerStatuses.put(peerAddress, new PeerStatus()));
//    }
//
//    public CompletableFuture<Map<String, BroadcastStatus>> broadcastMessage(String messageContent) {
//        if (messageContent == null || messageContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be null or empty.");
//        }
//
//        Message message = new Message(peer.getPeerId(), messageContent);
//        Map<String, CompletableFuture<BroadcastStatus>> futures = new ConcurrentHashMap<>();
//
//        synchronized (peers) {
//            for (String peerAddress : peers) {
//                PeerStatus status = peerStatuses.computeIfAbsent(peerAddress, k -> new PeerStatus());
//                status.lastAttempt = Instant.now();
//                status.retryCount = 0;
//                status.status = BroadcastStatus.PENDING;
//
//                CompletableFuture<BroadcastStatus> future = CompletableFuture.supplyAsync(
//                    () -> sendWithRetry(peerAddress, message),
//                    broadcastExecutor
//                );
//                futures.put(peerAddress, future);
//            }
//        }
//
//        return CompletableFuture.supplyAsync(() -> {
//            Map<String, BroadcastStatus> results = new ConcurrentHashMap<>();
//            futures.forEach((peerAddress, future) -> {
//                try {
//                    results.put(peerAddress, future.get(ACKNOWLEDGMENT_TIMEOUT_MS, TimeUnit.MILLISECONDS));
//                } catch (TimeoutException e) {
//                    results.put(peerAddress, BroadcastStatus.TIMEOUT);
//                    peerStatuses.get(peerAddress).status = BroadcastStatus.TIMEOUT;
//                } catch (Exception e) {
//                    results.put(peerAddress, BroadcastStatus.FAILED);
//                    peerStatuses.get(peerAddress).status = BroadcastStatus.FAILED;
//                }
//            });
//            return results;
//        });
//    }
//
//    private BroadcastStatus sendWithRetry(String peerAddress, Message message) {
//        PeerStatus status = peerStatuses.get(peerAddress);
//
//        while (status.retryCount < MAX_RETRIES) {
//            try {
//                peer.connectToPeer(peerAddress, message.getContent());
//                status.status = BroadcastStatus.SENT;
//                System.out.println("BroadcastManager: Sent message to " + peerAddress);
//                return BroadcastStatus.SENT;
//            } catch (Exception e) {
//                status.retryCount++;
//                System.err.println("BroadcastManager: Failed to send message to " + peerAddress +
//                                 " (Attempt " + status.retryCount + "/" + MAX_RETRIES + ")");
//
//                if (status.retryCount < MAX_RETRIES) {
//                    try {
//                        Thread.sleep(RETRY_DELAY_MS);
//                    } catch (InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                        break;
//                    }
//                }
//            }
//        }
//
//        status.status = BroadcastStatus.FAILED;
//        return BroadcastStatus.FAILED;
//    }
//
//    public void acknowledgeReceipt(String peerAddress) {
//        PeerStatus status = peerStatuses.get(peerAddress);
//        if (status == null) {
//            System.err.println("BroadcastManager: Acknowledgment from unknown peer: " + peerAddress);
//            return;
//        }
//
//        status.acknowledged = true;
//        status.status = BroadcastStatus.ACKNOWLEDGED;
//        System.out.println("BroadcastManager: Acknowledgment received from " + peerAddress);
//    }
//
//    public Map<String, BroadcastStatus> getBroadcastStatus() {
//        Map<String, BroadcastStatus> statuses = new ConcurrentHashMap<>();
//        peerStatuses.forEach((peer, status) -> statuses.put(peer, status.status));
//        return Collections.unmodifiableMap(statuses);
//    }
//
//    public synchronized void addPeer(String peerAddress) {
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            throw new IllegalArgumentException("Invalid peer address: " + peerAddress);
//        }
//        peers.add(peerAddress);
//        peerStatuses.put(peerAddress, new PeerStatus());
//        System.out.println("BroadcastManager: Added peer " + peerAddress);
//    }
//
//    public synchronized void removePeer(String peerAddress) {
//        if (peers.remove(peerAddress)) {
//            peerStatuses.remove(peerAddress);
//            System.out.println("BroadcastManager: Removed peer " + peerAddress);
//        }
//    }
//
//    public void shutdown() {
//        broadcastExecutor.shutdown();
//        try {
//            if (!broadcastExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
//                broadcastExecutor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            broadcastExecutor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//}




package org.example.app.core.p2p;

import java.util.*;
import java.util.concurrent.*;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The BroadcastManager handles broadcasting messages to multiple peers,
 * with support for retries, acknowledgments, and status tracking.
 */
public class BroadcastManager {
    private static final Logger logger = Logger.getLogger(BroadcastManager.class.getName());

    private final Set<String> peers;
    private final Map<String, PeerStatus> peerStatuses;
    private final Peer peer;
    private final ExecutorService broadcastExecutor;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long ACKNOWLEDGMENT_TIMEOUT_MS = 10000; // 10 seconds

    /**
     * Class to track the status of a message sent to a peer.
     */
    public static class PeerStatus {
        private volatile boolean acknowledged;
        private volatile Instant lastAttempt;
        private volatile int retryCount;
        private volatile BroadcastStatus status;

        /**
         * Constructs a new PeerStatus object.
         */
        public PeerStatus() {
            this.acknowledged = false;
            this.lastAttempt = null;
            this.retryCount = 0;
            this.status = BroadcastStatus.PENDING;
        }

        /**
         * Gets whether this message has been acknowledged.
         *
         * @return true if acknowledged, false otherwise
         */
        public boolean isAcknowledged() {
            return acknowledged;
        }

        /**
         * Gets the timestamp of the last attempt.
         *
         * @return the last attempt timestamp, or null if no attempts yet
         */
        public Instant getLastAttempt() {
            return lastAttempt;
        }

        /**
         * Gets the current retry count.
         *
         * @return the current retry count
         */
        public int getRetryCount() {
            return retryCount;
        }

        /**
         * Gets the current status.
         *
         * @return the current status
         */
        public BroadcastStatus getStatus() {
            return status;
        }
    }

    /**
     * Enum representing the status of a broadcast message.
     */
    public enum BroadcastStatus {
        PENDING,
        SENT,
        ACKNOWLEDGED,
        FAILED,
        TIMEOUT
    }

    /**
     * Constructor to initialize BroadcastManager.
     *
     * @param peer The peer instance for sending messages
     * @param peers The initial set of peers to broadcast to, or null for an empty set
     * @throws IllegalArgumentException if peer is null
     */
    public BroadcastManager(Peer peer, Set<String> peers) {
        if (peer == null) {
            throw new IllegalArgumentException("Peer cannot be null");
        }

        this.peer = peer;
        this.peers = Collections.synchronizedSet(peers != null ? new HashSet<>(peers) : new HashSet<>());
        this.peerStatuses = new ConcurrentHashMap<>();
        this.broadcastExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "BroadcastThread-" + UUID.randomUUID().toString().substring(0, 8));
            t.setDaemon(true);
            return t;
        });

        // Initialize status tracking for existing peers
        if (peers != null) {
            for (String peerAddress : peers) {
                peerStatuses.put(peerAddress, new PeerStatus());
            }
        }

        logger.info("BroadcastManager initialized with " + (peers != null ? peers.size() : 0) + " peers");
    }

    /**
     * Broadcasts a message to all known peers.
     *
     * @param messageContent The message content to broadcast
     * @return A CompletableFuture that resolves to a map of peer addresses and their broadcast statuses
     * @throws IllegalArgumentException if message content is invalid
     */
    public CompletableFuture<Map<String, BroadcastStatus>> broadcastMessage(String messageContent) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }

        final Message message = new Message(peer.getPeerId(), messageContent);
        final Map<String, CompletableFuture<BroadcastStatus>> futures = new ConcurrentHashMap<>();

        synchronized (peers) {
            // Reset status for each peer
            for (String peerAddress : peers) {
                PeerStatus status = peerStatuses.computeIfAbsent(peerAddress, k -> new PeerStatus());

                // Update status fields
                ((PeerStatus)status).lastAttempt = Instant.now();
                ((PeerStatus)status).retryCount = 0;
                ((PeerStatus)status).status = BroadcastStatus.PENDING;

                // Create and submit future for this peer
                CompletableFuture<BroadcastStatus> future = CompletableFuture.supplyAsync(
                        () -> sendWithRetry(peerAddress, message),
                        broadcastExecutor
                );
                futures.put(peerAddress, future);
            }
        }

        logger.info("Broadcasting message to " + peers.size() + " peers");

        // Return a future that collects all results
        return CompletableFuture.supplyAsync(() -> {
            Map<String, BroadcastStatus> results = new ConcurrentHashMap<>();

            futures.forEach((peerAddress, future) -> {
                try {
                    results.put(peerAddress, future.get(ACKNOWLEDGMENT_TIMEOUT_MS, TimeUnit.MILLISECONDS));
                } catch (TimeoutException e) {
                    results.put(peerAddress, BroadcastStatus.TIMEOUT);
                    PeerStatus status = peerStatuses.get(peerAddress);
                    if (status != null) {
                        ((PeerStatus)status).status = BroadcastStatus.TIMEOUT;
                    }
                    logger.warning("Timeout waiting for broadcast to peer: " + peerAddress);
                } catch (Exception e) {
                    results.put(peerAddress, BroadcastStatus.FAILED);
                    PeerStatus status = peerStatuses.get(peerAddress);
                    if (status != null) {
                        ((PeerStatus)status).status = BroadcastStatus.FAILED;
                    }
                    logger.log(Level.WARNING, "Error broadcasting to peer: " + peerAddress, e);
                }
            });

            logger.info("Broadcast completed with results: " + summarizeResults(results));
            return results;
        });
    }

    /**
     * Summarizes the broadcast results for logging.
     *
     * @param results The map of results
     * @return A summary string
     */
    private String summarizeResults(Map<String, BroadcastStatus> results) {
        Map<BroadcastStatus, Integer> counts = new EnumMap<>(BroadcastStatus.class);
        for (BroadcastStatus status : results.values()) {
            counts.put(status, counts.getOrDefault(status, 0) + 1);
        }
        return counts.toString();
    }

    /**
     * Sends a message to a peer with retry capability.
     *
     * @param peerAddress The address of the peer
     * @param message The message to send
     * @return The final broadcast status
     */
    private BroadcastStatus sendWithRetry(String peerAddress, Message message) {
        PeerStatus status = peerStatuses.get(peerAddress);
        if (status == null) {
            // Should not happen, but just in case
            status = new PeerStatus();
            peerStatuses.put(peerAddress, status);
        }

        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                ((PeerStatus)status).lastAttempt = Instant.now();
                ((PeerStatus)status).retryCount = retryCount;

                // Send the message
                peer.sendMessage(message.getContent(), peerAddress);

                // Update status
                ((PeerStatus)status).status = BroadcastStatus.SENT;
                logger.fine("Sent message to " + peerAddress +
                        (retryCount > 0 ? " (Attempt " + (retryCount + 1) + ")" : ""));

                return BroadcastStatus.SENT;
            } catch (Exception e) {
                retryCount++;
                logger.log(Level.WARNING, "Failed to send message to " + peerAddress +
                        " (Attempt " + retryCount + "/" + MAX_RETRIES + ")", e);

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Update status after all retries failed
        ((PeerStatus)status).status = BroadcastStatus.FAILED;
        logger.warning("Failed to send message to " + peerAddress + " after " + MAX_RETRIES + " attempts");

        return BroadcastStatus.FAILED;
    }

    /**
     * Records an acknowledgment from a peer.
     *
     * @param peerAddress The address of the acknowledging peer
     * @throws IllegalArgumentException if peerAddress is invalid
     */
    public void acknowledgeReceipt(String peerAddress) {
        if (peerAddress == null || peerAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }

        PeerStatus status = peerStatuses.get(peerAddress);
        if (status == null) {
            logger.warning("Acknowledgment from unknown peer: " + peerAddress);

            // Create status for future use
            status = new PeerStatus();
            peerStatuses.put(peerAddress, status);
        }

        // Update status
        ((PeerStatus)status).acknowledged = true;
        ((PeerStatus)status).status = BroadcastStatus.ACKNOWLEDGED;

        logger.fine("Acknowledgment received from peer: " + peerAddress);
    }

    /**
     * Gets the current broadcast status for all peers.
     *
     * @return An unmodifiable map of peer addresses to broadcast statuses
     */
    public Map<String, BroadcastStatus> getBroadcastStatus() {
        Map<String, BroadcastStatus> statuses = new ConcurrentHashMap<>();

        peerStatuses.forEach((peerAddress, status) ->
                statuses.put(peerAddress, status.status)
        );

        return Collections.unmodifiableMap(statuses);
    }

    /**
     * Gets the detailed status for a specific peer.
     *
     * @param peerAddress The address of the peer
     * @return The peer status, or null if not found
     */
    public PeerStatus getPeerStatus(String peerAddress) {
        return peerStatuses.get(peerAddress);
    }

    /**
     * Adds a peer to the broadcast list.
     *
     * @param peerAddress The address of the peer to add
     * @throws IllegalArgumentException if peerAddress is invalid
     */
    public synchronized void addPeer(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address: " + peerAddress);
        }

        peers.add(peerAddress);
        peerStatuses.putIfAbsent(peerAddress, new PeerStatus());

        logger.info("Added peer to broadcast list: " + peerAddress);
    }

    /**
     * Removes a peer from the broadcast list.
     *
     * @param peerAddress The address of the peer to remove
     * @throws IllegalArgumentException if peerAddress is invalid
     */
    public synchronized void removePeer(String peerAddress) {
        if (peerAddress == null || peerAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }

        if (peers.remove(peerAddress)) {
            peerStatuses.remove(peerAddress);
            logger.info("Removed peer from broadcast list: " + peerAddress);
        } else {
            logger.fine("Peer not found in broadcast list: " + peerAddress);
        }
    }

    /**
     * Gets the number of peers in the broadcast list.
     *
     * @return The number of peers
     */
    public int getPeerCount() {
        return peers.size();
    }

    /**
     * Gets the list of peer addresses.
     *
     * @return A copy of the peer addresses set
     */
    public Set<String> getPeers() {
        synchronized (peers) {
            return new HashSet<>(peers);
        }
    }

    /**
     * Checks if a peer is in the broadcast list.
     *
     * @param peerAddress The address of the peer to check
     * @return true if the peer is in the list, false otherwise
     */
    public boolean containsPeer(String peerAddress) {
        if (peerAddress == null || peerAddress.trim().isEmpty()) {
            return false;
        }

        synchronized (peers) {
            return peers.contains(peerAddress);
        }
    }

    /**
     * Clears all peers from the broadcast list.
     */
    public synchronized void clearPeers() {
        peers.clear();
        peerStatuses.clear();
        logger.info("Cleared all peers from broadcast list");
    }

    /**
     * Shuts down the broadcast manager and its resources.
     */
    public void shutdown() {
        broadcastExecutor.shutdown();
        try {
            if (!broadcastExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                broadcastExecutor.shutdownNow();
                if (!broadcastExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.severe("Broadcast executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            broadcastExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Broadcast manager shut down");
    }
}