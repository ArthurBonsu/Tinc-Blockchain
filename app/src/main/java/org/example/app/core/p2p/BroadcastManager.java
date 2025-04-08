package org.example.app.core.p2p;

import java.util.*;
import java.util.concurrent.*;
import java.time.Instant;

public class BroadcastManager {
    private final Set<String> peers;
    private final Map<String, PeerStatus> peerStatuses;
    private final Peer peer;
    private final ExecutorService broadcastExecutor;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long ACKNOWLEDGMENT_TIMEOUT_MS = 10000; // 10 seconds

    public static class PeerStatus {
        private boolean acknowledged;
        private Instant lastAttempt;
        private int retryCount;
        private BroadcastStatus status;

        public PeerStatus() {
            this.acknowledged = false;
            this.lastAttempt = null;
            this.retryCount = 0;
            this.status = BroadcastStatus.PENDING;
        }
    }

    public enum BroadcastStatus {
        PENDING,
        SENT,
        ACKNOWLEDGED,
        FAILED,
        TIMEOUT
    }

    public BroadcastManager(Peer peer, Set<String> peers) {
        if (peer == null) {
            throw new IllegalArgumentException("Peer cannot be null.");
        }
        this.peer = peer;
        this.peers = Collections.synchronizedSet(peers != null ? new HashSet<>(peers) : new HashSet<>());
        this.peerStatuses = new ConcurrentHashMap<>();
        this.broadcastExecutor = Executors.newCachedThreadPool();

        // Initialize status tracking for existing peers
        peers.forEach(peerAddress -> peerStatuses.put(peerAddress, new PeerStatus()));
    }

    public CompletableFuture<Map<String, BroadcastStatus>> broadcastMessage(String messageContent) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty.");
        }

        Message message = new Message(peer.getPeerId(), messageContent);
        Map<String, CompletableFuture<BroadcastStatus>> futures = new ConcurrentHashMap<>();

        synchronized (peers) {
            for (String peerAddress : peers) {
                PeerStatus status = peerStatuses.computeIfAbsent(peerAddress, k -> new PeerStatus());
                status.lastAttempt = Instant.now();
                status.retryCount = 0;
                status.status = BroadcastStatus.PENDING;

                CompletableFuture<BroadcastStatus> future = CompletableFuture.supplyAsync(
                    () -> sendWithRetry(peerAddress, message),
                    broadcastExecutor
                );
                futures.put(peerAddress, future);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            Map<String, BroadcastStatus> results = new ConcurrentHashMap<>();
            futures.forEach((peerAddress, future) -> {
                try {
                    results.put(peerAddress, future.get(ACKNOWLEDGMENT_TIMEOUT_MS, TimeUnit.MILLISECONDS));
                } catch (TimeoutException e) {
                    results.put(peerAddress, BroadcastStatus.TIMEOUT);
                    peerStatuses.get(peerAddress).status = BroadcastStatus.TIMEOUT;
                } catch (Exception e) {
                    results.put(peerAddress, BroadcastStatus.FAILED);
                    peerStatuses.get(peerAddress).status = BroadcastStatus.FAILED;
                }
            });
            return results;
        });
    }

    private BroadcastStatus sendWithRetry(String peerAddress, Message message) {
        PeerStatus status = peerStatuses.get(peerAddress);
        
        while (status.retryCount < MAX_RETRIES) {
            try {
                peer.connectToPeer(peerAddress, message.getContent());
                status.status = BroadcastStatus.SENT;
                System.out.println("BroadcastManager: Sent message to " + peerAddress);
                return BroadcastStatus.SENT;
            } catch (Exception e) {
                status.retryCount++;
                System.err.println("BroadcastManager: Failed to send message to " + peerAddress + 
                                 " (Attempt " + status.retryCount + "/" + MAX_RETRIES + ")");
                
                if (status.retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        status.status = BroadcastStatus.FAILED;
        return BroadcastStatus.FAILED;
    }

    public void acknowledgeReceipt(String peerAddress) {
        PeerStatus status = peerStatuses.get(peerAddress);
        if (status == null) {
            System.err.println("BroadcastManager: Acknowledgment from unknown peer: " + peerAddress);
            return;
        }

        status.acknowledged = true;
        status.status = BroadcastStatus.ACKNOWLEDGED;
        System.out.println("BroadcastManager: Acknowledgment received from " + peerAddress);
    }

    public Map<String, BroadcastStatus> getBroadcastStatus() {
        Map<String, BroadcastStatus> statuses = new ConcurrentHashMap<>();
        peerStatuses.forEach((peer, status) -> statuses.put(peer, status.status));
        return Collections.unmodifiableMap(statuses);
    }

    public synchronized void addPeer(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address: " + peerAddress);
        }
        peers.add(peerAddress);
        peerStatuses.put(peerAddress, new PeerStatus());
        System.out.println("BroadcastManager: Added peer " + peerAddress);
    }

    public synchronized void removePeer(String peerAddress) {
        if (peers.remove(peerAddress)) {
            peerStatuses.remove(peerAddress);
            System.out.println("BroadcastManager: Removed peer " + peerAddress);
        }
    }

    public void shutdown() {
        broadcastExecutor.shutdown();
        try {
            if (!broadcastExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                broadcastExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            broadcastExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}