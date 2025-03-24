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
