//package org.tinc.consensus.pbft;
//
//import org.tinc.p2p.RobustP2PManager;
//import org.tinc.p2p.UDPManager;
//import java.util.function.Consumer;
//
///**
// * PBFTNetwork handles communication between replicas in the PBFT protocol.
// * - Manages broadcast and direct messaging.
// * - Relays PBFT messages to the appropriate handler.
// */
//public class PBFTNetwork {
//
//    private final RobustP2PManager p2pManager;       // Peer-to-peer network manager
//    private final PBFTHandler pbftHandler;           // Handles PBFT message processing
//    private static int nodeId = 0;                        // Node ID of this replica
//
//    /**
//     * Constructor to initialize the PBFTNetwork.
//     *
//     * @param p2pManager The RobustP2PManager instance for communication.
//     * @param pbftHandler The PBFTHandler to process incoming messages.
//     * @param nodeId      The unique ID of this node.
//     */
//    public PBFTNetwork(RobustP2PManager p2pManager, PBFTHandler pbftHandler, int nodeId) {
//        this.p2pManager = p2pManager;
//        this.pbftHandler = pbftHandler;
//        this.nodeId = nodeId;
//    }
//
//    /**
//     * Starts the network server to listen for incoming messages.
//     *
//     * @param udpManager The UDPManager instance for listening to UDP messages.
//     */
//    public void startServer(UDPManager udpManager) {
//        try {
//            udpManager.listen(this::handleIncomingMessage);
//            System.out.println("PBFTNetwork: Listening for incoming messages...");
//        } catch (Exception e) {
//            System.err.println("PBFTNetwork: Error starting server - " + e.getMessage());
//        }
//    }
//
//    /**
//     * Handles an incoming message and relays it to the PBFTHandler.
//     *
//     * @param serializedMessage The incoming serialized message.
//     */
//    private void handleIncomingMessage(String serializedMessage) {
//        System.out.println("PBFTNetwork: Received message: " + serializedMessage);
//        pbftHandler.handleMessage(serializedMessage);
//    }
//
//    /**
//     * Broadcasts a PBFT message to all peers.
//     *
//     * @param messageContent The message content to broadcast.
//     */
//    public void broadcastPBFTMessage(String messageContent) {
//        try {
//            p2pManager.sendBroadcast(messageContent);
//            System.out.println("PBFTNetwork: Broadcasted PBFT message.");
//        } catch (Exception e) {
//            System.err.println("PBFTNetwork: Error broadcasting message - " + e.getMessage());
//        }
//    }
//
//    /**
//     * Sends a PBFT message directly to a specific peer.
//     *
//     * @param peerAddress The address of the target peer.
//     * @param messageContent The message content to send.
//     */
//    public void sendDirectMessage(String peerAddress, String messageContent) {
//        try {
//            p2pManager.sendDirectMessage(peerAddress, messageContent);
//            System.out.println("PBFTNetwork: Sent direct message to peer: " + peerAddress);
//        } catch (Exception e) {
//            System.err.println("PBFTNetwork: Error sending direct message - " + e.getMessage());
//        }
//    }
//
//    /**
//     * Shuts down the PBFT network.
//     */
//    public void shutdown() {
//        try {
//            p2pManager.shutdown();
//            System.out.println("PBFTNetwork: Shutdown completed.");
//        } catch (Exception e) {
//            System.err.println("PBFTNetwork: Error during shutdown - " + e.getMessage());
//        }
//    }
//
//    /**
//     * Retrieves the unique node ID of this replica.
//     *
//     * @return The node ID.
//     */
//    public static int getNodeId() {
//        return nodeId;
//    }
//
//    /**
//     * Connects to a peer.
//     *
//     * @param peerAddress The address of the peer to connect to.
//     * @param port        The port to connect to.
//     */
//    public void connectToPeer(String peerAddress, int port) {
//        try {
//            p2pManager.connectToPeer(peerAddress + ":" + port);
//            System.out.println("PBFTNetwork: Connected to peer at " + peerAddress + ":" + port);
//        } catch (Exception e) {
//            System.err.println("PBFTNetwork: Error connecting to peer - " + e.getMessage());
//        }
//    }
//}





package org.example.app.core.pbftconsensus;
import org.example.app.core.p2p.RobustP2PManager;
import org.example.app.core.p2p.UDPManager;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PBFTNetwork handles communication between replicas in the PBFT protocol.
 * - Manages broadcast and direct messaging.
 * - Relays PBFT messages to the appropriate handler.
 */
public class PBFTNetwork {
    private static final Logger logger = Logger.getLogger(PBFTNetwork.class.getName());

    private final RobustP2PManager p2pManager;       // Peer-to-peer network manager
    private final PBFTHandler pbftHandler;           // Handles PBFT message processing
    private static final AtomicInteger nodeId = new AtomicInteger(0); // Node ID of this replica
    private boolean isRunning;                       // Flag to track server status

    /**
     * Constructor to initialize the PBFTNetwork.
     *
     * @param p2pManager The RobustP2PManager instance for communication.
     * @param pbftHandler The PBFTHandler to process incoming messages.
     * @param initialNodeId The unique ID of this node.
     * @throws IllegalArgumentException if parameters are invalid
     */
    public PBFTNetwork(RobustP2PManager p2pManager, PBFTHandler pbftHandler, int initialNodeId) {
        if (p2pManager == null) {
            throw new IllegalArgumentException("P2P manager cannot be null");
        }
        if (pbftHandler == null) {
            throw new IllegalArgumentException("PBFT handler cannot be null");
        }
        if (initialNodeId < 0) {
            throw new IllegalArgumentException("Node ID cannot be negative");
        }

        this.p2pManager = p2pManager;
        this.pbftHandler = pbftHandler;
        nodeId.set(initialNodeId);
        this.isRunning = false;
    }

    /**
     * Starts the network server to listen for incoming messages.
     *
     * @param udpManager The UDPManager instance for listening to UDP messages.
     * @throws RuntimeException if server start fails
     */
    public void startServer(UDPManager udpManager) {
        if (udpManager == null) {
            throw new IllegalArgumentException("UDP manager cannot be null");
        }

        try {
            udpManager.listen(this::handleIncomingMessage);
            isRunning = true;
            logger.info("PBFTNetwork: Listening for incoming messages...");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "PBFTNetwork: Error starting server", e);
            throw new RuntimeException("Error starting PBFT network server", e);
        }
    }

    /**
     * Starts the network server to listen on a specific port.
     *
     * @param port The port to listen on.
     * @throws RuntimeException if server start fails
     * @throws IllegalArgumentException if port is invalid
     */
    public void startServer(int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }

        try {
            UDPManager udpManager = new UDPManager(port);
            startServer(udpManager);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "PBFTNetwork: Error starting server on port " + port, e);
            throw new RuntimeException("Error starting PBFT network server on port " + port, e);
        }
    }

    /**
     * Handles an incoming message and relays it to the PBFTHandler.
     *
     * @param serializedMessage The incoming serialized message.
     */
    private void handleIncomingMessage(String serializedMessage) {
        if (serializedMessage == null || serializedMessage.isEmpty()) {
            logger.warning("PBFTNetwork: Received empty message, ignoring");
            return;
        }

        try {
            logger.fine("PBFTNetwork: Received message: " + serializedMessage);
            pbftHandler.handleMessage(serializedMessage);
        } catch (Exception e) {
            logger.log(Level.WARNING, "PBFTNetwork: Error handling incoming message", e);
        }
    }

    /**
     * Broadcasts a PBFT message to all peers.
     *
     * @param messageContent The message content to broadcast.
     * @throws RuntimeException if broadcast fails
     */
    public void broadcastPBFTMessage(String messageContent) {
        if (messageContent == null || messageContent.isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }

        try {
            p2pManager.sendBroadcast(messageContent);
            logger.fine("PBFTNetwork: Broadcasted PBFT message.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "PBFTNetwork: Error broadcasting message", e);
            throw new RuntimeException("Error broadcasting PBFT message", e);
        }
    }

    /**
     * Sends a PBFT message directly to a specific peer.
     *
     * @param peerAddress The address of the target peer.
     * @param messageContent The message content to send.
     * @throws RuntimeException if send fails
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void sendDirectMessage(String peerAddress, String messageContent) {
        if (peerAddress == null || peerAddress.isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }
        if (messageContent == null || messageContent.isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }

        try {
            p2pManager.sendDirectMessage(peerAddress, messageContent);
            logger.fine("PBFTNetwork: Sent direct message to peer: " + peerAddress);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "PBFTNetwork: Error sending direct message", e);
            throw new RuntimeException("Error sending direct PBFT message", e);
        }
    }

    /**
     * Shuts down the PBFT network.
     */
    public void shutdown() {
        try {
            p2pManager.shutdown();
            isRunning = false;
            logger.info("PBFTNetwork: Shutdown completed.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "PBFTNetwork: Error during shutdown", e);
            throw new RuntimeException("Error shutting down PBFT network", e);
        }
    }

    /**
     * Retrieves the unique node ID of this replica.
     *
     * @return The node ID.
     */
    public static int getNodeId() {
        return nodeId.get();
    }

    /**
     * Updates the node ID of this replica.
     *
     * @param newNodeId The new node ID to set.
     * @throws IllegalArgumentException if newNodeId is negative
     */
    public static void setNodeId(int newNodeId) {
        if (newNodeId < 0) {
            throw new IllegalArgumentException("Node ID cannot be negative");
        }
        nodeId.set(newNodeId);
    }

    /**
     * Connects to a peer.
     *
     * @param peerAddress The address of the peer to connect to.
     * @param port        The port to connect to.
     * @throws RuntimeException if connection fails
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void connectToPeer(String peerAddress, int port) {
        if (peerAddress == null || peerAddress.isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }

        try {
            p2pManager.connectToPeer(peerAddress + ":" + port);
            logger.info("PBFTNetwork: Connected to peer at " + peerAddress + ":" + port);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "PBFTNetwork: Error connecting to peer", e);
            throw new RuntimeException("Error connecting to peer", e);
        }
    }

    /**
     * Checks if the network server is running.
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets the RobustP2PManager instance.
     *
     * @return the p2pManager
     */
    public RobustP2PManager getP2PManager() {
        return p2pManager;
    }
}



