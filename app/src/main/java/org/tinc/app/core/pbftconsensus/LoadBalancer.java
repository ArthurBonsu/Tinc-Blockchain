//package org.tinc.consensus.pbft;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * LoadBalancer balances client requests across replicas to prevent overloads.
// * - Monitors replica workloads and distributes client requests evenly.
// * - Redirects requests from overloaded replicas to less busy ones.
// * - Integrates with performance metrics to dynamically adjust request allocation strategies.
// */
//public class LoadBalancer {
//
//    private final Map<Integer, AtomicInteger> replicaWorkloads; // Tracks the number of active requests per replica
//    private final Map<Integer, Boolean> replicaStatus;          // Tracks the health status of replicas
//    private final int maxRequestsPerReplica;                    // Maximum allowed requests per replica
//    private final PerformanceMetrics performanceMetrics;        // Monitors replica performance metrics
//
//    /**
//     * Constructor to initialize the LoadBalancer.
//     *
//     * @param replicaIds          List of replica IDs to manage.
//     * @param maxRequestsPerReplica Maximum allowed requests per replica.
//     * @param performanceMetrics  PerformanceMetrics instance to monitor replica performance.
//     */
//    public LoadBalancer(List<Integer> replicaIds, int maxRequestsPerReplica, PerformanceMetrics performanceMetrics) {
//        this.replicaWorkloads = new ConcurrentHashMap<>();
//        this.replicaStatus = new ConcurrentHashMap<>();
//        this.maxRequestsPerReplica = maxRequestsPerReplica;
//        this.performanceMetrics = performanceMetrics;
//
//        // Initialize workloads and status
//        for (int replicaId : replicaIds) {
//            replicaWorkloads.put(replicaId, new AtomicInteger(0));
//            replicaStatus.put(replicaId, true); // Assume all replicas are healthy initially
//        }
//    }
//
//    /**
//     * Distributes a client request to the least busy replica.
//     *
//     * @return The ID of the selected replica.
//     */
//    public int distributeRequest() {
//        Optional<Map.Entry<Integer, AtomicInteger>> selectedReplica = replicaWorkloads.entrySet().stream()
//                .filter(entry -> replicaStatus.getOrDefault(entry.getKey(), false)) // Only consider healthy replicas
//                .min(Comparator.comparingInt(entry -> entry.getValue().get()));
//
//        if (selectedReplica.isPresent()) {
//            int replicaId = selectedReplica.get().getKey();
//            replicaWorkloads.get(replicaId).incrementAndGet();
//            System.out.println("Request distributed to replica: " + replicaId);
//            return replicaId;
//        }
//
//        throw new IllegalStateException("No healthy replicas available to handle the request.");
//    }
//
//    /**
//     * Marks a request as completed for a replica, freeing up its capacity.
//     *
//     * @param replicaId The ID of the replica that completed the request.
//     */
//    public void completeRequest(int replicaId) {
//        AtomicInteger workload = replicaWorkloads.get(replicaId);
//        if (workload != null && workload.get() > 0) {
//            workload.decrementAndGet();
//            System.out.println("Request completed by replica: " + replicaId);
//        } else {
//            System.err.println("Attempted to complete request for replica " + replicaId + " with no active requests.");
//        }
//    }
//
//    /**
//     * Updates the status of a replica (healthy/unhealthy).
//     *
//     * @param replicaId The ID of the replica to update.
//     * @param isHealthy True if the replica is healthy, false otherwise.
//     */
//    public void updateReplicaStatus(int replicaId, boolean isHealthy) {
//        if (replicaStatus.containsKey(replicaId)) {
//            replicaStatus.put(replicaId, isHealthy);
//            System.out.println("Replica " + replicaId + " status updated to: " + (isHealthy ? "healthy" : "unhealthy"));
//        } else {
//            System.err.println("Replica " + replicaId + " does not exist in the system.");
//        }
//    }
//
//    /**
//     * Dynamically adjusts request allocation strategies based on performance metrics.
//     */
//    public void adjustAllocationStrategy() {
//        // Identify slow replicas from performance metrics
//        List<Integer> slowReplicas = performanceMetrics.identifySlowReplicas();
//
//        // Mark slow replicas as unhealthy
//        for (int replicaId : slowReplicas) {
//            updateReplicaStatus(replicaId, false);
//        }
//
//        // Reactivate replicas if their performance improves
//        replicaStatus.keySet().stream()
//                .filter(replicaId -> !slowReplicas.contains(replicaId) && !replicaStatus.get(replicaId))
//                .forEach(replicaId -> updateReplicaStatus(replicaId, true));
//
//        System.out.println("Allocation strategy adjusted based on performance metrics.");
//    }
//
//    /**
//     * Retrieves the current workload of all replicas.
//     *
//     * @return A map of replica IDs to their current workloads.
//     */
//    public Map<Integer, Integer> getReplicaWorkloads() {
//        Map<Integer, Integer> workloads = new HashMap<>();
//        for (Map.Entry<Integer, AtomicInteger> entry : replicaWorkloads.entrySet()) {
//            workloads.put(entry.getKey(), entry.getValue().get());
//        }
//        return workloads;
//    }
//}
