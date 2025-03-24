// NetworkMetrics.java
package org.example.app.core.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkMetrics {
    private final AtomicLong totalPeers;
    private final AtomicLong messagesReceived;
    private final AtomicLong messagesSent;
    private final AtomicLong bytesReceived;
    private final AtomicLong bytesSent;
    private final Map<String, AtomicLong> peerLatencies;

    public NetworkMetrics() {
        this.totalPeers = new AtomicLong(0);
        this.messagesReceived = new AtomicLong(0);
        this.messagesSent = new AtomicLong(0);
        this.bytesReceived = new AtomicLong(0);
        this.bytesSent = new AtomicLong(0);
        this.peerLatencies = new ConcurrentHashMap<>();
    }

    public void recordPeerConnection(String peerId) {
        totalPeers.incrementAndGet();
        peerLatencies.putIfAbsent(peerId, new AtomicLong(0));
    }

    public void recordPeerDisconnection(String peerId) {
        totalPeers.decrementAndGet();
        peerLatencies.remove(peerId);
    }

    public void recordMessageReceived(int bytes) {
        messagesReceived.incrementAndGet();
        bytesReceived.addAndGet(bytes);
    }

    public void recordMessageSent(int bytes) {
        messagesSent.incrementAndGet();
        bytesSent.addAndGet(bytes);
    }

    public void recordPeerLatency(String peerId, long latencyMs) {
        AtomicLong current = peerLatencies.get(peerId);
        if (current != null) {
            current.set(latencyMs);
        }
    }

    // Getters
    public long getTotalPeers() { return totalPeers.get(); }
    public long getMessagesReceived() { return messagesReceived.get(); }
    public long getMessagesSent() { return messagesSent.get(); }
    public long getBytesReceived() { return bytesReceived.get(); }
    public long getBytesSent() { return bytesSent.get(); }
    public Map<String, Long> getPeerLatencies() {
        Map<String, Long> latencies = new ConcurrentHashMap<>();
        peerLatencies.forEach((k, v) -> latencies.put(k, v.get()));
        return latencies;
    }
}