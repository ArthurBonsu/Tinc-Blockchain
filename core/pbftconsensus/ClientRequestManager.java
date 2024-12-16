//package org.tinc.consensus.pbft;
//
//import org.tinc.p2p.RobustP2PManager;
//
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * ClientRequestManager handles client interactions with the PBFT network.
// * - Receives client requests and forwards them to the primary replica.
// * - Tracks the status of requests and provides responses to clients.
// * - Implements retry mechanisms for failed or delayed requests.
// */
//public class ClientRequestManager {
//
//    private final RobustP2PManager p2pManager; // Robust P2P network manager
//    private final AtomicInteger requestIdGenerator; // Unique ID generator for requests
//    private final ConcurrentMap<Integer, RequestStatus> requestStatusMap; // Tracks request statuses
//    private final ScheduledExecutorService retryExecutor; // Scheduler for handling retries
//    private final int retryLimit; // Maximum number of retries for a request
//    private final long retryInterval; // Retry interval in milliseconds
//
//    /**
//     * Constructor to initialize ClientRequestManager.
//     *
//     * @param p2pManager    The RobustP2PManager instance for communication.
//     * @param retryLimit    Maximum number of retries for a request.
//     * @param retryInterval Retry interval in milliseconds.
//     */
//    public ClientRequestManager(RobustP2PManager p2pManager, int retryLimit, long retryInterval) {
//        this.p2pManager = p2pManager;
//        this.requestIdGenerator = new AtomicInteger(0);
//        this.requestStatusMap = new ConcurrentHashMap<>();
//        this.retryExecutor = Executors.newScheduledThreadPool(2);
//        this.retryLimit = retryLimit;
//        this.retryInterval = retryInterval;
//    }
//
//    /**
//     * Receives a client request and forwards it to the primary replica.
//     *
//     * @param clientRequest The client request to process.
//     * @return The unique ID assigned to the request.
//     */
//    public int receiveRequest(String clientRequest) {
//        int requestId = requestIdGenerator.incrementAndGet();
//        RequestStatus status = new RequestStatus(requestId, clientRequest);
//        requestStatusMap.put(requestId, status);
//
//        forwardRequestToPrimary(requestId, clientRequest);
//        scheduleRetry(requestId);
//
//        System.out.println("Received and forwarded client request: " + clientRequest + " with ID: " + requestId);
//        return requestId;
//    }
//
//    /**
//     * Forwards a client request to the primary replica.
//     *
//     * @param requestId     The unique ID of the request.
//     * @param clientRequest The client request to forward.
//     */
//    private void forwardRequestToPrimary(int requestId, String clientRequest) {
//        PBFTMessage message = new PBFTMessage(
//                "CLIENT-REQUEST",
//                PBFTNetwork.getNodeId(), // Sender ID from the P2P manager
//                clientRequest,
//                null // Optional signature can be added here
//        );
//
//        p2pManager.sendBroadcast(message.serialize());
//        System.out.println("Forwarded client request with ID " + requestId + " to the primary replica.");
//    }
//
//    /**
//     * Updates the status of a request and provides a response to the client if completed.
//     *
//     * @param requestId The unique ID of the request.
//     * @param response  The response received for the request.
//     */
//    public void updateRequestStatus(int requestId, String response) {
//        RequestStatus status = requestStatusMap.get(requestId);
//        if (status != null) {
//            status.setResponse(response);
//            status.setCompleted(true);
//            System.out.println("Request " + requestId + " completed with response: " + response);
//        } else {
//            System.err.println("Request ID " + requestId + " not found in status map.");
//        }
//    }
//
//    /**
//     * Implements a retry mechanism for failed or delayed requests.
//     *
//     * @param requestId The unique ID of the request.
//     */
//    private void scheduleRetry(int requestId) {
//        retryExecutor.scheduleAtFixedRate(() -> {
//            RequestStatus status = requestStatusMap.get(requestId);
//            if (status != null && !status.isCompleted() && status.getRetryCount() < retryLimit) {
//                status.incrementRetryCount();
//                System.out.println("Retrying client request with ID " + requestId + " (Attempt " + status.getRetryCount() + ")");
//                forwardRequestToPrimary(requestId, status.getRequest());
//            } else if (status != null && status.getRetryCount() >= retryLimit) {
//                System.err.println("Request " + requestId + " failed after maximum retries.");
//                requestStatusMap.remove(requestId);
//            }
//        }, retryInterval, retryInterval, TimeUnit.MILLISECONDS);
//    }
//
//    /**
//     * Retrieves the status of a client request.
//     *
//     * @param requestId The unique ID of the request.
//     * @return The status of the request.
//     */
//    public String getRequestStatus(int requestId) {
//        RequestStatus status = requestStatusMap.get(requestId);
//        if (status == null) {
//            return "Request ID " + requestId + " not found.";
//        } else if (status.isCompleted()) {
//            return "Request ID " + requestId + " completed with response: " + status.getResponse();
//        } else {
//            return "Request ID " + requestId + " is in progress (Retries: " + status.getRetryCount() + ")";
//        }
//    }
//
//    /**
//     * Stops the retry executor service.
//     */
//    public void shutdown() {
//        retryExecutor.shutdown();
//        System.out.println("ClientRequestManager shut down.");
//    }
//
//    /**
//     * Inner class to track the status of a client request.
//     */
//    private static class RequestStatus {
//        private final int requestId;
//        private final String request;
//        private String response;
//        private boolean completed;
//        private int retryCount;
//
//        public RequestStatus(int requestId, String request) {
//            this.requestId = requestId;
//            this.request = request;
//            this.completed = false;
//            this.retryCount = 0;
//        }
//
//        public int getRequestId() {
//            return requestId;
//        }
//
//        public String getRequest() {
//            return request;
//        }
//
//        public String getResponse() {
//            return response;
//        }
//
//        public void setResponse(String response) {
//            this.response = response;
//        }
//
//        public boolean isCompleted() {
//            return completed;
//        }
//
//        public void setCompleted(boolean completed) {
//            this.completed = completed;
//        }
//
//        public int getRetryCount() {
//            return retryCount;
//        }
//
//        public void incrementRetryCount() {
//            this.retryCount++;
//        }
//    }
//}
