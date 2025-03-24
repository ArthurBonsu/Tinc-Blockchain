// MetricsReporter.java
package org.example.app.core.metrics;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;
import java.io.FileWriter;
import java.time.Instant;

public class MetricsReporter {
    private final NetworkMetrics networkMetrics;
    private final ConsensusMetrics consensusMetrics;
    private final ScheduledExecutorService scheduler;
    private final String metricsPath;
    private final Gson gson;

    public MetricsReporter(NetworkMetrics networkMetrics, 
                          ConsensusMetrics consensusMetrics,
                          String metricsPath) {
        this.networkMetrics = networkMetrics;
        this.consensusMetrics = consensusMetrics;
        this.metricsPath = metricsPath;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.gson = new Gson();
    }

    public void start(long period, TimeUnit unit) {
        scheduler.scheduleAtFixedRate(this::reportMetrics, 0, period, unit);
    }

    private void reportMetrics() {
        try {
            MetricsSnapshot snapshot = new MetricsSnapshot(
                Instant.now(),
                networkMetrics,
                consensusMetrics
            );

            String json = gson.toJson(snapshot);
            
            // Write to file
            try (FileWriter writer = new FileWriter(metricsPath, true)) {
                writer.write(json + "\n");
            }

            // Optionally send to monitoring service
            sendToMonitoringService(snapshot);
        } catch (Exception e) {
            System.err.println("Failed to report metrics: " + e.getMessage());
        }
    }

    private void sendToMonitoringService(MetricsSnapshot snapshot) {
        // Implement sending metrics to external monitoring service
        // (e.g., Prometheus, Grafana, etc.)
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private static class MetricsSnapshot {
        private final Instant timestamp;
        private final long totalPeers;
        private final long blockHeight;
        private final long totalTransactions;
        private final long averageBlockTime;
        private final long messagesReceived;
        private final long messagesSent;

        public MetricsSnapshot(Instant timestamp, 
                             NetworkMetrics networkMetrics,
                             ConsensusMetrics consensusMetrics) {
            this.timestamp = timestamp;
            this.totalPeers = networkMetrics.getTotalPeers();
            this.blockHeight = consensusMetrics.getBlockHeight();
            this.totalTransactions = consensusMetrics.getTotalTransactions();
            this.averageBlockTime = consensusMetrics.getAverageBlockTime();
            this.messagesReceived = networkMetrics.getMessagesReceived();
            this.messagesSent = networkMetrics.getMessagesSent();
        }
    }
}