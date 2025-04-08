//package org.tinc.consensus.pbft;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.stream.Collectors;
//
///**
// * PerformanceMetrics monitors and reports performance metrics for consensus processes.
// * Tracks throughput, latency, identifies bottlenecks, and adjusts parameters dynamically.
// */
//public class PerformanceMetrics {
//
//    private final Map<Integer, Long> consensusStartTimes; // Tracks start times of consensus rounds
//    private final Map<Integer, Long> consensusEndTimes;   // Tracks end times of consensus rounds
//    private final AtomicLong totalRequests;              // Total number of requests processed
//    private final AtomicLong totalLatency;               // Cumulative latency of all consensus rounds
//    private final Map<Integer, Long> replicaResponseTimes; // Tracks response times of replicas
//    private final Map<String, Long> dynamicParameters;   // Stores dynamic parameters like timeouts, batch sizes
//
//    /**
//     * Constructor to initialize PerformanceMetrics.
//     */
//    public PerformanceMetrics() {
//        this.consensusStartTimes = new ConcurrentHashMap<>();
//        this.consensusEndTimes = new ConcurrentHashMap<>();
//        this.totalRequests = new AtomicLong(0);
//        this.totalLatency = new AtomicLong(0);
//        this.replicaResponseTimes = new ConcurrentHashMap<>();
//        this.dynamicParameters = new ConcurrentHashMap<>();
//        initializeDefaultParameters();
//    }
//
//    /**
//     * Initializes default dynamic parameters.
//     */
//    private void initializeDefaultParameters() {
//        dynamicParameters.put("timeout", 5000L); // Default timeout in ms
//        dynamicParameters.put("batchSize", 10L); // Default batch size
//    }
//
//    /**
//     * Records the start time of a consensus round.
//     *
//     * @param roundId The unique ID of the consensus round.
//     */
//    public void recordConsensusStart(int roundId) {
//        consensusStartTimes.put(roundId, System.currentTimeMillis());
//        System.out.println("Consensus round " + roundId + " started.");
//    }
//
//    /**
//     * Records the end time of a consensus round and calculates metrics.
//     *
//     * @param roundId The unique ID of the consensus round.
//     */
//    public void recordConsensusEnd(int roundId) {
//        long endTime = System.currentTimeMillis();
//        consensusEndTimes.put(roundId, endTime);
//
//        Long startTime = consensusStartTimes.get(roundId);
//        if (startTime != null) {
//            long latency = endTime - startTime;
//            totalRequests.incrementAndGet();
//            totalLatency.addAndGet(latency);
//            System.out.println("Consensus round " + roundId + " completed with latency: " + latency + " ms.");
//        } else {
//            System.err.println("Start time not found for consensus round: " + roundId);
//        }
//    }
//
//    /**
//     * Records the response time of a replica.
//     *
//     * @param replicaId   The unique ID of the replica.
//     * @param responseTime The response time of the replica in ms.
//     */
//    public void recordReplicaResponseTime(int replicaId, long responseTime) {
//        replicaResponseTimes.put(replicaId, responseTime);
//        System.out.println("Replica " + replicaId + " responded in: " + responseTime + " ms.");
//    }
//
//    /**
//     * Calculates the average throughput (requests per second).
//     *
//     * @return The average throughput.
//     */
//    public double calculateThroughput() {
//        long elapsedTime = consensusEndTimes.values().stream()
//                .max(Long::compareTo)
//                .orElse(0L) -
//                consensusStartTimes.values().stream()
//                        .min(Long::compareTo)
//                        .orElse(0L);
//
//        if (elapsedTime <= 0) {
//            return 0.0;
//        }
//
//        return (double) totalRequests.get() / (elapsedTime / 1000.0);
//    }
//
//    /**
//     * Calculates the average latency for consensus rounds.
//     *
//     * @return The average latency in milliseconds.
//     */
//    public double calculateAverageLatency() {
//        long count = totalRequests.get();
//        return count == 0 ? 0.0 : (double) totalLatency.get() / count;
//    }
//
//    /**
//     * Identifies slow replicas based on response times.
//     *
//     * @return A list of replica IDs with the highest response times.
//     */
//    public List<Integer> identifySlowReplicas() {
//        long threshold = dynamicParameters.get("timeout");
//        return replicaResponseTimes.entrySet().stream()
//                .filter(entry -> entry.getValue() > threshold)
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Dynamically adjusts parameters like timeouts or batch sizes based on metrics.
//     */
//    public void adjustParameters() {
//        double avgLatency = calculateAverageLatency();
//        double throughput = calculateThroughput();
//
//        // Adjust timeout based on average latency
//        long newTimeout = Math.max((long) (avgLatency * 1.5), 2000L);
//        dynamicParameters.put("timeout", newTimeout);
//        System.out.println("Timeout dynamically adjusted to: " + newTimeout + " ms.");
//
//        // Adjust batch size based on throughput
//        long newBatchSize = throughput > 100 ? 20L : 10L;
//        dynamicParameters.put("batchSize", newBatchSize);
//        System.out.println("Batch size dynamically adjusted to: " + newBatchSize);
//    }
//
//    /**
//     * Reports current performance metrics.
//     */
//    public void reportMetrics() {
//        System.out.println("Performance Metrics Report:");
//        System.out.println("  Throughput (requests/sec): " + calculateThroughput());
//        System.out.println("  Average Latency (ms): " + calculateAverageLatency());
//        System.out.println("  Slow Replicas: " + identifySlowReplicas());
//        System.out.println("  Dynamic Parameters: " + dynamicParameters);
//    }
//}




package org.example.app.core.pbftconsensus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PerformanceMetrics monitors and reports performance metrics for consensus processes.
 * Tracks throughput, latency, identifies bottlenecks, and adjusts parameters dynamically.
 */
public class PerformanceMetrics {
    private static final Logger logger = Logger.getLogger(PerformanceMetrics.class.getName());

    private final Map<Integer, Long> consensusStartTimes; // Tracks start times of consensus rounds
    private final Map<Integer, Long> consensusEndTimes;   // Tracks end times of consensus rounds
    private final AtomicLong totalRequests;              // Total number of requests processed
    private final AtomicLong totalLatency;               // Cumulative latency of all consensus rounds
    private final Map<Integer, Long> replicaResponseTimes; // Tracks response times of replicas
    private final Map<String, Long> dynamicParameters;   // Stores dynamic parameters like timeouts, batch sizes

    /**
     * Constructor to initialize PerformanceMetrics.
     */
    public PerformanceMetrics() {
        this.consensusStartTimes = new ConcurrentHashMap<>();
        this.consensusEndTimes = new ConcurrentHashMap<>();
        this.totalRequests = new AtomicLong(0);
        this.totalLatency = new AtomicLong(0);
        this.replicaResponseTimes = new ConcurrentHashMap<>();
        this.dynamicParameters = new ConcurrentHashMap<>();
        initializeDefaultParameters();
    }

    /**
     * Initializes default dynamic parameters.
     */
    private void initializeDefaultParameters() {
        dynamicParameters.put("timeout", 5000L); // Default timeout in ms
        dynamicParameters.put("batchSize", 10L); // Default batch size
        logger.info("Performance metrics initialized with default parameters");
    }

    /**
     * Records the start time of a consensus round.
     *
     * @param roundId The unique ID of the consensus round.
     * @throws IllegalArgumentException if roundId is negative
     */
    public void recordConsensusStart(int roundId) {
        if (roundId < 0) {
            throw new IllegalArgumentException("Round ID cannot be negative");
        }

        consensusStartTimes.put(roundId, System.currentTimeMillis());
        logger.info("Consensus round " + roundId + " started.");
    }

    /**
     * Records the end time of a consensus round and calculates metrics.
     *
     * @param roundId The unique ID of the consensus round.
     * @throws IllegalArgumentException if roundId is negative
     */
    public void recordConsensusEnd(int roundId) {
        if (roundId < 0) {
            throw new IllegalArgumentException("Round ID cannot be negative");
        }

        long endTime = System.currentTimeMillis();
        consensusEndTimes.put(roundId, endTime);

        Long startTime = consensusStartTimes.get(roundId);
        if (startTime != null) {
            long latency = endTime - startTime;
            totalRequests.incrementAndGet();
            totalLatency.addAndGet(latency);
            logger.info("Consensus round " + roundId + " completed with latency: " + latency + " ms.");
        } else {
            logger.warning("Start time not found for consensus round: " + roundId);
        }
    }

    /**
     * Records the response time of a replica.
     *
     * @param replicaId    The unique ID of the replica.
     * @param responseTime The response time of the replica in ms.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void recordReplicaResponseTime(int replicaId, long responseTime) {
        if (replicaId < 0) {
            throw new IllegalArgumentException("Replica ID cannot be negative");
        }
        if (responseTime < 0) {
            throw new IllegalArgumentException("Response time cannot be negative");
        }

        replicaResponseTimes.put(replicaId, responseTime);
        logger.info("Replica " + replicaId + " responded in: " + responseTime + " ms.");
    }

    /**
     * Calculates the average throughput (requests per second).
     *
     * @return The average throughput.
     */
    public double calculateThroughput() {
        if (consensusStartTimes.isEmpty() || consensusEndTimes.isEmpty()) {
            return 0.0;
        }

        // Find the earliest start time and latest end time
        long earliestStart = consensusStartTimes.values().stream()
                .min(Long::compareTo)
                .orElse(System.currentTimeMillis());

        long latestEnd = consensusEndTimes.values().stream()
                .max(Long::compareTo)
                .orElse(System.currentTimeMillis());

        long elapsedTime = latestEnd - earliestStart;

        if (elapsedTime <= 0) {
            return 0.0;
        }

        return (double) totalRequests.get() / (elapsedTime / 1000.0);
    }

    /**
     * Calculates the average latency for consensus rounds.
     *
     * @return The average latency in milliseconds.
     */
    public double calculateAverageLatency() {
        long count = totalRequests.get();
        return count == 0 ? 0.0 : (double) totalLatency.get() / count;
    }

    /**
     * Identifies slow replicas based on response times.
     *
     * @return A list of replica IDs with the highest response times.
     */
    public List<Integer> identifySlowReplicas() {
        if (replicaResponseTimes.isEmpty()) {
            return Collections.emptyList();
        }

        long threshold = dynamicParameters.getOrDefault("timeout", 5000L);

        return replicaResponseTimes.entrySet().stream()
                .filter(entry -> entry.getValue() > threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Dynamically adjusts parameters like timeouts or batch sizes based on metrics.
     */
    public void adjustParameters() {
        try {
            double avgLatency = calculateAverageLatency();
            double throughput = calculateThroughput();

            // Adjust timeout based on average latency
            // Use a minimum of 2000ms, and 1.5x the average latency
            long newTimeout = Math.max((long) (avgLatency * 1.5), 2000L);
            dynamicParameters.put("timeout", newTimeout);

            // Adjust batch size based on throughput
            // If throughput is high, increase batch size
            long newBatchSize = throughput > 100 ? 20L : 10L;
            dynamicParameters.put("batchSize", newBatchSize);

            logger.info("Parameters dynamically adjusted - Timeout: " + newTimeout +
                    " ms, Batch size: " + newBatchSize);
        } catch (Exception e) {
            logger.warning("Error adjusting parameters: " + e.getMessage());
        }
    }

    /**
     * Reports current performance metrics.
     */
    public void reportMetrics() {
        StringBuilder report = new StringBuilder("Performance Metrics Report:\n");
        report.append("  Throughput (requests/sec): ").append(String.format("%.2f", calculateThroughput())).append("\n");
        report.append("  Average Latency (ms): ").append(String.format("%.2f", calculateAverageLatency())).append("\n");
        report.append("  Slow Replicas: ").append(identifySlowReplicas()).append("\n");
        report.append("  Dynamic Parameters: ").append(dynamicParameters);

        logger.info(report.toString());
    }

    /**
     * Gets the current value of a dynamic parameter.
     *
     * @param paramName The name of the parameter
     * @return The parameter value, or null if not found
     */
    public Long getParameter(String paramName) {
        return dynamicParameters.get(paramName);
    }

    /**
     * Sets a dynamic parameter value.
     *
     * @param paramName The name of the parameter
     * @param value The value to set
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void setParameter(String paramName, Long value) {
        if (paramName == null || paramName.isEmpty()) {
            throw new IllegalArgumentException("Parameter name cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }

        dynamicParameters.put(paramName, value);
        logger.info("Parameter '" + paramName + "' set to: " + value);
    }

    /**
     * Resets all performance metrics.
     */
    public void resetMetrics() {
        consensusStartTimes.clear();
        consensusEndTimes.clear();
        totalRequests.set(0);
        totalLatency.set(0);
        replicaResponseTimes.clear();
        logger.info("Performance metrics reset");
    }

    /**
     * Gets the number of completed consensus rounds.
     *
     * @return The count of completed rounds
     */
    public int getCompletedRoundCount() {
        return consensusEndTimes.size();
    }
}





