//package org.tinc.consensus.pbft;
//
//
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
///**
// * TimeoutManager manages timeouts for various PBFT phases,
// * ensuring progress and preventing deadlocks.
// */
//public class TimeoutManager {
//
//    private final ConcurrentMap<String, Timer> activeTimers; // Tracks active timers for different operations
//
//    /**
//     * Constructor to initialize the TimeoutManager.
//     */
//    public TimeoutManager() {
//        this.activeTimers = new ConcurrentHashMap<>();
//    }
//
//    /**
//     * Starts a timeout for a specific operation.
//     *
//     * @param operationId A unique identifier for the operation (e.g., "PREPARE_PHASE", "VIEW_CHANGE").
//     * @param duration Time in milliseconds after which the action is executed if not canceled.
//     * @param action The action to execute after the timeout.
//     */
//    public void startTimeout(String operationId, long duration, Runnable action) {
//        cancelTimeout(operationId); // Ensure no duplicate timer for the same operation
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("Timeout triggered for operation: " + operationId);
//                action.run();
//            }
//        }, duration);
//
//        activeTimers.put(operationId, timer);
//        System.out.println("Timeout started for operation: " + operationId + " with duration: " + duration + "ms");
//    }
//
//    /**
//     * Cancels an active timeout for a specific operation.
//     *
//     * @param operationId The unique identifier for the operation whose timer should be canceled.
//     */
//    public void cancelTimeout(String operationId) {
//        Timer timer = activeTimers.remove(operationId);
//        if (timer != null) {
//            timer.cancel();
//            System.out.println("Timeout canceled for operation: " + operationId);
//        }
//    }
//
//    /**
//     * Adjusts the timeout dynamically for a specific operation based on external conditions.
//     *
//     * @param operationId The unique identifier for the operation.
//     * @param newDuration The new duration in milliseconds for the timeout.
//     * @param action The action to execute after the adjusted timeout.
//     */
//    public void adjustTimeout(String operationId, long newDuration, Runnable action) {
//        System.out.println("Adjusting timeout for operation: " + operationId + " to new duration: " + newDuration + "ms");
//        startTimeout(operationId, newDuration, action); // Restart with new duration
//    }
//
//    /**
//     * Cancels all active timeouts managed by this TimeoutManager.
//     */
//    public void cancelAllTimeouts() {
//        for (String operationId : activeTimers.keySet()) {
//            cancelTimeout(operationId);
//        }
//        System.out.println("All timeouts have been canceled.");
//    }
//}




package org.example.app.core.pbftconsensus;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * TimeoutManager manages timeouts for various PBFT phases,
 * ensuring progress and preventing deadlocks.
 */
public class TimeoutManager {
    private static final Logger logger = Logger.getLogger(TimeoutManager.class.getName());

    private final ConcurrentMap<String, Timer> activeTimers; // Tracks active timers for different operations

    /**
     * Constructor to initialize the TimeoutManager.
     */
    public TimeoutManager() {
        this.activeTimers = new ConcurrentHashMap<>();
    }

    /**
     * Starts a timeout for a specific operation.
     *
     * @param operationId A unique identifier for the operation (e.g., "PREPARE_PHASE", "VIEW_CHANGE").
     * @param duration Time in milliseconds after which the action is executed if not canceled.
     * @param action The action to execute after the timeout.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void startTimeout(String operationId, long duration, Runnable action) {
        if (operationId == null || operationId.isEmpty()) {
            throw new IllegalArgumentException("Operation ID cannot be null or empty");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        cancelTimeout(operationId); // Ensure no duplicate timer for the same operation

        Timer timer = new Timer(true); // Set as daemon timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("Timeout triggered for operation: " + operationId);
                    action.run();
                } catch (Exception e) {
                    logger.severe("Error executing timeout action for " + operationId + ": " + e.getMessage());
                } finally {
                    // Automatically remove from active timers after execution
                    activeTimers.remove(operationId);
                }
            }
        }, duration);

        activeTimers.put(operationId, timer);
        logger.info("Timeout started for operation: " + operationId + " with duration: " + duration + "ms");
    }

    /**
     * Cancels an active timeout for a specific operation.
     *
     * @param operationId The unique identifier for the operation whose timer should be canceled.
     * @return true if a timer was canceled, false otherwise
     * @throws IllegalArgumentException if operationId is null or empty
     */
    public boolean cancelTimeout(String operationId) {
        if (operationId == null || operationId.isEmpty()) {
            throw new IllegalArgumentException("Operation ID cannot be null or empty");
        }

        Timer timer = activeTimers.remove(operationId);
        if (timer != null) {
            timer.cancel();
            logger.info("Timeout canceled for operation: " + operationId);
            return true;
        }
        return false;
    }

    /**
     * Adjusts the timeout dynamically for a specific operation based on external conditions.
     *
     * @param operationId The unique identifier for the operation.
     * @param newDuration The new duration in milliseconds for the timeout.
     * @param action The action to execute after the adjusted timeout.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void adjustTimeout(String operationId, long newDuration, Runnable action) {
        if (operationId == null || operationId.isEmpty()) {
            throw new IllegalArgumentException("Operation ID cannot be null or empty");
        }
        if (newDuration <= 0) {
            throw new IllegalArgumentException("New duration must be positive");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        logger.info("Adjusting timeout for operation: " + operationId + " to new duration: " + newDuration + "ms");
        startTimeout(operationId, newDuration, action); // Restart with new duration
    }

    /**
     * Cancels all active timeouts managed by this TimeoutManager.
     */
    public void cancelAllTimeouts() {
        int count = 0;
        for (String operationId : activeTimers.keySet()) {
            if (cancelTimeout(operationId)) {
                count++;
            }
        }
        logger.info("Canceled " + count + " active timeouts.");
    }

    /**
     * Checks if a timeout is currently active for the given operation.
     *
     * @param operationId The operation ID to check
     * @return true if a timeout is active, false otherwise
     * @throws IllegalArgumentException if operationId is null or empty
     */
    public boolean isTimeoutActive(String operationId) {
        if (operationId == null || operationId.isEmpty()) {
            throw new IllegalArgumentException("Operation ID cannot be null or empty");
        }
        return activeTimers.containsKey(operationId);
    }

    /**
     * Gets the number of currently active timeouts.
     *
     * @return The count of active timeouts
     */
    public int getActiveTimeoutCount() {
        return activeTimers.size();
    }

    /**
     * Shuts down this TimeoutManager, canceling all active timeouts.
     */
    public void shutdown() {
        cancelAllTimeouts();
        logger.info("TimeoutManager shut down");
    }
}


