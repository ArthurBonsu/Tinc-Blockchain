//package org.tinc.consensus.pbft;
//
//import java.util.List;
//
///**
// * PrimaryElection handles the election of a new primary replica in the event of faults or view changes.
// */
//public class PrimaryElection {
//
//    private int currentView; // Tracks the current view (or primary)
//
//    /**
//     * Constructor to initialize the primary election process.
//     *
//     * @param initialView The initial primary replica ID.
//     */
//    public PrimaryElection(int initialView) {
//        this.currentView = initialView;
//    }
//
//    /**
//     * Elects a new primary replica based on the current view and total number of replicas.
//     *
//     * @param totalReplicas The total number of replicas in the system.
//     * @return The ID of the newly elected primary.
//     */
//    public int electNewPrimary(int totalReplicas) {
//        currentView = (currentView + 1) % totalReplicas; // Round-robin election
//        System.out.println("New primary elected: Replica " + currentView);
//        return currentView;
//    }
//
//    /**
//     * Gets the current view (or primary replica ID).
//     *
//     * @return The current primary replica ID.
//     */
//    public int getCurrentView() {
//        return currentView;
//    }
//
//    /**
//     * Updates the current view to a specified value. Typically used during view synchronization.
//     *
//     * @param newView The new view ID to set.
//     */
//    public void updateView(int newView) {
//        this.currentView = newView;
//        System.out.println("View updated to: " + currentView);
//    }
//
//    /**
//     * Validates if the current primary is responsive based on feedback from replicas.
//     *
//     * @param primaryId The ID of the current primary.
//     * @param responsiveReplicas List of replica IDs that confirm responsiveness.
//     * @return True if the primary is validated as responsive, false otherwise.
//     */
//    public boolean validatePrimary(int primaryId, List<Integer> responsiveReplicas) {
//        boolean isResponsive = responsiveReplicas.contains(primaryId);
//        if (isResponsive) {
//            System.out.println("Primary " + primaryId + " is validated as responsive.");
//        } else {
//            System.out.println("Primary " + primaryId + " is unresponsive and may require a view change.");
//        }
//        return isResponsive;
//    }
//}





package org.example.app.core.pbftconsensus;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * PrimaryElection handles the election of a new primary replica in the event of faults or view changes.
 */
public class PrimaryElection {
    private static final Logger logger = Logger.getLogger(PrimaryElection.class.getName());

    private int currentView; // Tracks the current view (or primary)
    private final ReadWriteLock lock; // Ensures thread-safe view updates

    /**
     * Constructor to initialize the primary election process.
     *
     * @param initialView The initial primary replica ID.
     * @throws IllegalArgumentException if initialView is negative
     */
    public PrimaryElection(int initialView) {
        if (initialView < 0) {
            throw new IllegalArgumentException("Initial view cannot be negative");
        }
        this.currentView = initialView;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Elects a new primary replica based on the current view and total number of replicas.
     *
     * @param totalReplicas The total number of replicas in the system.
     * @return The ID of the newly elected primary.
     * @throws IllegalArgumentException if totalReplicas is not positive
     */
    public int electNewPrimary(int totalReplicas) {
        if (totalReplicas <= 0) {
            throw new IllegalArgumentException("Total replicas must be positive");
        }

        lock.writeLock().lock();
        try {
            currentView = (currentView + 1) % totalReplicas; // Round-robin election
            logger.info("New primary elected: Replica " + currentView);
            return currentView;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the current view (or primary replica ID).
     *
     * @return The current primary replica ID.
     */
    public int getCurrentView() {
        lock.readLock().lock();
        try {
            return currentView;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Updates the current view to a specified value. Typically used during view synchronization.
     *
     * @param newView The new view ID to set.
     * @throws IllegalArgumentException if newView is negative
     */
    public void updateView(int newView) {
        if (newView < 0) {
            throw new IllegalArgumentException("New view cannot be negative");
        }

        lock.writeLock().lock();
        try {
            this.currentView = newView;
            logger.info("View updated to: " + currentView);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Validates if the current primary is responsive based on feedback from replicas.
     *
     * @param primaryId The ID of the current primary.
     * @param responsiveReplicas List of replica IDs that confirm responsiveness.
     * @return True if the primary is validated as responsive, false otherwise.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public boolean validatePrimary(int primaryId, List<Integer> responsiveReplicas) {
        if (primaryId < 0) {
            throw new IllegalArgumentException("Primary ID cannot be negative");
        }
        if (responsiveReplicas == null) {
            throw new IllegalArgumentException("Responsive replicas list cannot be null");
        }

        boolean isResponsive = responsiveReplicas.contains(primaryId);
        if (isResponsive) {
            logger.info("Primary " + primaryId + " is validated as responsive.");
        } else {
            logger.warning("Primary " + primaryId + " is unresponsive and may require a view change.");
        }
        return isResponsive;
    }

    /**
     * Determines if a replica is currently the primary.
     *
     * @param replicaId The ID of the replica to check
     * @return true if the replica is the current primary, false otherwise
     */
    public boolean isPrimary(int replicaId) {
        lock.readLock().lock();
        try {
            return replicaId == currentView;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the next expected primary based on the round-robin rotation.
     *
     * @param totalReplicas The total number of replicas
     * @return The ID of the next expected primary
     * @throws IllegalArgumentException if totalReplicas is not positive
     */
    public int getNextExpectedPrimary(int totalReplicas) {
        if (totalReplicas <= 0) {
            throw new IllegalArgumentException("Total replicas must be positive");
        }

        lock.readLock().lock();
        try {
            return (currentView + 1) % totalReplicas;
        } finally {
            lock.readLock().unlock();
        }
    }
}


