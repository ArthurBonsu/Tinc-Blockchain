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
