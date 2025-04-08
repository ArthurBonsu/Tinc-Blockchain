//package org.example.app.core.p2p;
//
//import org.example.app.core.crypto.Keypair;
//
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import java.util.concurrent.*;
//
//// Robust Peer-to-Peer Communication Class
//public class RobustP2PManager {
//    private final Peer peer;
//    private final BroadcastManager broadcastManager;
//    private final UDPManager udpManager;
//    private final XMLPeerDiscovery xmlPeerDiscovery;
//    private final ExecutorService executorService;
//
//    public RobustP2PManager(Peer peer, UDPManager udpManager, XMLPeerDiscovery xmlPeerDiscovery) {
//        this.peer = peer;
//        this.udpManager = udpManager;
//        this.xmlPeerDiscovery = xmlPeerDiscovery;
//        this.broadcastManager = new BroadcastManager(peer, new HashSet<>());
//        this.executorService = Executors.newCachedThreadPool();
//    }
//
//    // Initialize the P2P network
//    public void initializeNetwork(String xmlFilePath, String udpBroadcastAddress) throws Exception {
//        // Discover peers from XML
//        peer.startServer(); // Ensure the server starts before broadcasting
//        List<String> peers = xmlPeerDiscovery.discoverPeers();
//        for (String peerAddress : peers) {
//            peer.addPeer(peerAddress);
//            broadcastManager.addPeer(peerAddress);
//        }
//        System.out.println("RobustP2PManager: Peers added from XML discovery.");
//
//        // Start UDP listener
//        executorService.execute(() -> {
//            try {
//                udpManager.listen(this::handleMessage);
//            } catch (IOException e) {
//                System.err.println("RobustP2PManager: UDP listener failed: " + e.getMessage());
//            }
//        });
//
//        // Broadcast initialization message
//        udpManager.broadcast("Peer Initialization: " + peer.getPeerId(), udpBroadcastAddress);
//        System.out.println("RobustP2PManager: Initialization message broadcasted.");
//    }
//
//    // Send a broadcast message
//    public void sendBroadcast(String messageContent) {
//        broadcastManager.broadcastMessage(messageContent);
//    }
//
//    // Handle incoming messages
//    void handleMessage(String message) {
//        System.out.println("RobustP2PManager: Received message: " + message);
//        // Logic to handle messages, e.g., update peer list, forward message, etc.
//    }
//
//    // Send a direct message
//    public void sendDirectMessage(String peerAddress, String messageContent) {
//        peer.sendMessage(messageContent, peerAddress);
//    }
//
//    // Handle acknowledgment from peers
//    public void acknowledgeMessage(String peerAddress) {
//        broadcastManager.acknowledgeReceipt(peerAddress);
//    }
//
//    // Add a new peer dynamically
//    public void addPeer(String peerAddress) {
//        peer.addPeer(peerAddress);
//        broadcastManager.addPeer(peerAddress);
//    }
//
//    // Remove an existing peer dynamically
//    public void removePeer(String peerAddress) {
//        peer.removePeer(peerAddress);
//        broadcastManager.removePeer(peerAddress);
//    }
//
//    // Shut down the network gracefully
//    public void shutdown() {
//        peer.shutdown();
//        udpManager.close();
//        executorService.shutdown();
//        System.out.println("RobustP2PManager: Network shut down.");
//    }
//
//    // Start a TCP server for one-to-one communication
//    public void startDirectConnectionServer(int port) {
//        executorService.execute(() -> {
//            try (ServerSocket serverSocket = new ServerSocket(port)) {
//                System.out.println("RobustP2PManager: Direct connection server started on port " + port);
//                while (!executorService.isShutdown()) {
//                    Socket clientSocket = serverSocket.accept();
//                    executorService.execute(() -> handleDirectConnection(clientSocket));
//                }
//            } catch (IOException e) {
//                System.err.println("RobustP2PManager: Direct connection server failed: " + e.getMessage());
//            }
//        });
//    }
//
//
//    private void handleDirectConnection(Socket clientSocket) {
//
//    }
//
//    public Keypair getReplicaKeypair(int senderId) {
//    }
//
//    public void connectToPeer(String s) {
//    }
//}



package org.example.app.core.p2p;

import org.example.app.core.crypto.Keypair;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robust Peer-to-Peer Communication Manager
 *
 * Handles network communication between peers in a distributed system,
 * providing reliability and fault tolerance.
 */
public class RobustP2PManager {
    private static final Logger logger = Logger.getLogger(RobustP2PManager.class.getName());

    private final Peer peer;
    private final BroadcastManager broadcastManager;
    private final UDPManager udpManager;
    private final XMLPeerDiscovery xmlPeerDiscovery;
    private final ExecutorService executorService;
    private final Map<Integer, Keypair> replicaKeypairs;
    private final ReadWriteLock keypairLock;
    private volatile boolean isRunning;
    private int nodeId = 0;

    /**
     * Constructor for RobustP2PManager.
     *
     * @param peer The peer instance for this node
     * @param udpManager The UDP manager for broadcasts
     * @param xmlPeerDiscovery The XML-based peer discovery service
     * @throws IllegalArgumentException if any parameter is null
     */
    public RobustP2PManager(Peer peer, UDPManager udpManager, XMLPeerDiscovery xmlPeerDiscovery) {
        if (peer == null) {
            throw new IllegalArgumentException("Peer cannot be null");
        }
        if (udpManager == null) {
            throw new IllegalArgumentException("UDP Manager cannot be null");
        }
        if (xmlPeerDiscovery == null) {
            throw new IllegalArgumentException("XML Peer Discovery cannot be null");
        }

        this.peer = peer;
        this.udpManager = udpManager;
        this.xmlPeerDiscovery = xmlPeerDiscovery;
        this.broadcastManager = new BroadcastManager(peer, new HashSet<>());
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "P2PManagerThread-" + UUID.randomUUID().toString().substring(0, 8));
            t.setDaemon(true);
            return t;
        });
        this.replicaKeypairs = new ConcurrentHashMap<>();
        this.keypairLock = new ReentrantReadWriteLock();
        this.isRunning = false;
    }

    /**
     * Default constructor for RobustP2PManager.
     * Creates default instances of required components.
     */
    public RobustP2PManager() {
        this(new Peer(), new UDPManager(), new XMLPeerDiscovery("peers.xml"));
    }

    /**
     * Initialize the P2P network by discovering peers and starting listeners.
     *
     * @param xmlFilePath Path to XML file containing peer information
     * @param udpBroadcastAddress UDP broadcast address for peer discovery
     * @throws IllegalArgumentException if parameters are invalid
     * @throws Exception if initialization fails
     */
    public void initializeNetwork(String xmlFilePath, String udpBroadcastAddress) throws Exception {
        if (xmlFilePath == null || xmlFilePath.isEmpty()) {
            throw new IllegalArgumentException("XML file path cannot be null or empty");
        }
        if (udpBroadcastAddress == null || udpBroadcastAddress.isEmpty()) {
            throw new IllegalArgumentException("UDP broadcast address cannot be null or empty");
        }

        try {
            // Set the XML file path for peer discovery
            xmlPeerDiscovery.setXmlFilePath(xmlFilePath);

            // Start the peer server
            peer.startServer();
            isRunning = true;

            // Discover peers from XML
            List<String> peers = xmlPeerDiscovery.discoverPeers();
            for (String peerAddress : peers) {
                peer.addPeer(peerAddress);
                broadcastManager.addPeer(peerAddress);
            }
            logger.info("Peers added from XML discovery: " + peers.size() + " peers found");

            // Start UDP listener
            startUDPListener();

            // Broadcast initialization message
            udpManager.broadcast("Peer Initialization: " + peer.getPeerId(), udpBroadcastAddress);
            logger.info("Initialization message broadcasted to: " + udpBroadcastAddress);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize network", e);
            shutdown();
            throw new Exception("Network initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Start the UDP listener in a separate thread.
     */
    private void startUDPListener() {
        if (!isRunning) {
            return;
        }

        executorService.execute(() -> {
            try {
                udpManager.listen(this::handleMessage);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "UDP listener failed", e);

                // Try to restart the listener after a delay if still running
                if (isRunning) {
                    try {
                        Thread.sleep(5000);
                        startUDPListener();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        logger.info("UDP listener started");
    }

    /**
     * Send a broadcast message to all peers.
     *
     * @param messageContent The message content to broadcast
     * @throws IllegalArgumentException if messageContent is null or empty
     */
    public void sendBroadcast(String messageContent) {
        if (messageContent == null || messageContent.isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }

        try {
            broadcastManager.broadcastMessage(messageContent);
            logger.fine("Broadcast message sent: " + messageContent);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to broadcast message", e);
            throw new RuntimeException("Failed to broadcast message: " + e.getMessage(), e);
        }
    }

    /**
     * Handle incoming messages from the network.
     *
     * @param message The received message
     */
    void handleMessage(String message) {
        if (message == null || message.isEmpty()) {
            logger.warning("Received empty message, ignoring");
            return;
        }

        try {
            logger.fine("Received message: " + message);

            // Parse the message to determine its type and take appropriate action
            if (message.startsWith("Peer Initialization:")) {
                // Handle new peer introduction
                String peerId = message.substring("Peer Initialization:".length()).trim();
                logger.info("New peer discovered: " + peerId);
                // Could add logic to automatically add the peer if desired
            } else if (message.startsWith("ACK:")) {
                // Handle acknowledgment
                String peerId = message.substring("ACK:".length()).trim();
                acknowledgeMessage(peerId);
            } else {
                // Regular message handling can be extended based on protocol
                logger.info("Regular message received: " + message);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing received message", e);
        }
    }

    /**
     * Send a direct message to a specific peer.
     *
     * @param peerAddress The address of the peer to send the message to
     * @param messageContent The message content to send
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
            peer.sendMessage(messageContent, peerAddress);
            logger.fine("Direct message sent to " + peerAddress + ": " + messageContent);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to send direct message to " + peerAddress, e);
            throw new RuntimeException("Failed to send direct message: " + e.getMessage(), e);
        }
    }

    /**
     * Handle acknowledgment from peers.
     *
     * @param peerAddress The address of the peer that sent the acknowledgment
     * @throws IllegalArgumentException if peerAddress is null or empty
     */
    public void acknowledgeMessage(String peerAddress) {
        if (peerAddress == null || peerAddress.isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }

        try {
            broadcastManager.acknowledgeReceipt(peerAddress);
            logger.fine("Acknowledgment received from: " + peerAddress);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to process acknowledgment from " + peerAddress, e);
        }
    }

    /**
     * Add a new peer dynamically to the network.
     *
     * @param peerAddress The address of the peer to add
     * @throws IllegalArgumentException if peerAddress is null or empty
     */
    public void addPeer(String peerAddress) {
        if (peerAddress == null || peerAddress.isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }

        try {
            peer.addPeer(peerAddress);
            broadcastManager.addPeer(peerAddress);
            logger.info("Peer added: " + peerAddress);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to add peer: " + peerAddress, e);
            throw new RuntimeException("Failed to add peer: " + e.getMessage(), e);
        }
    }

    /**
     * Remove an existing peer dynamically from the network.
     *
     * @param peerAddress The address of the peer to remove
     * @throws IllegalArgumentException if peerAddress is null or empty
     */
    public void removePeer(String peerAddress) {
        if (peerAddress == null || peerAddress.isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }

        try {
            peer.removePeer(peerAddress);
            broadcastManager.removePeer(peerAddress);
            logger.info("Peer removed: " + peerAddress);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to remove peer: " + peerAddress, e);
            throw new RuntimeException("Failed to remove peer: " + e.getMessage(), e);
        }
    }

    /**
     * Shut down the network gracefully.
     */
    public void shutdown() {
        isRunning = false;

        try {
            peer.shutdown();
            udpManager.close();

            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }

            logger.info("P2P Network shut down successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during P2P network shutdown", e);
            executorService.shutdownNow();
        }
    }

    /**
     * Start a TCP server for one-to-one direct communication.
     *
     * @param port The port to listen on
     * @throws IllegalArgumentException if port is invalid
     */
    public void startDirectConnectionServer(int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }

        executorService.execute(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                logger.info("Direct connection server started on port " + port);

                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executorService.execute(() -> handleDirectConnection(clientSocket));
                    } catch (IOException e) {
                        if (isRunning) {
                            logger.log(Level.WARNING, "Error accepting client connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Direct connection server failed on port " + port, e);
            }
        });
    }

    /**
     * Handle a direct TCP connection from another peer.
     *
     * @param clientSocket The socket for the client connection
     */
    private void handleDirectConnection(Socket clientSocket) {
        if (clientSocket == null) {
            return;
        }

        try {
            String clientAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
            logger.fine("Handling direct connection from: " + clientAddress);

            // Set socket timeout to prevent hanging
            clientSocket.setSoTimeout(30000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    logger.fine("Received direct message: " + inputLine);

                    // Process the message according to the protocol
                    String response = processDirectMessage(inputLine);

                    // Send response if needed
                    if (response != null && !response.isEmpty()) {
                        out.println(response);
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            logger.fine("Connection timed out: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error handling direct connection", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing client socket", e);
            }
        }
    }

    /**
     * Process a direct message from a peer.
     *
     * @param message The message to process
     * @return The response message, or null if no response is needed
     */
    private String processDirectMessage(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        // Simple echo protocol for now - can be extended based on application needs
        return "ACK: " + message;
    }

    /**
     * Get the keypair for a specific replica.
     *
     * @param senderId The ID of the replica
     * @return The keypair for the replica
     * @throws RuntimeException if keypair retrieval fails
     */
    public Keypair getReplicaKeypair(int senderId) {
        keypairLock.readLock().lock();
        try {
            Keypair keypair = replicaKeypairs.get(senderId);

            if (keypair == null) {
                keypairLock.readLock().unlock();
                keypairLock.writeLock().lock();

                try {
                    // Double-check in case another thread created it
                    keypair = replicaKeypairs.get(senderId);
                    if (keypair == null) {
                        // Generate a new keypair for this sender
                        keypair = Keypair.generate();
                        replicaKeypairs.put(senderId, keypair);
                        logger.info("Generated new keypair for replica: " + senderId);
                    }

                    return keypair;
                } finally {
                    keypairLock.writeLock().unlock();
                    keypairLock.readLock().lock();
                }
            }

            return keypair;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving keypair for replica: " + senderId, e);
            throw new RuntimeException("Failed to retrieve keypair: " + e.getMessage(), e);
        } finally {
            keypairLock.readLock().unlock();
        }
    }

    /**
     * Connect to a peer using its address or connection string.
     *
     * @param connectionString The connection string in the format "host:port" or a peer identifier
     * @throws IllegalArgumentException if connectionString is null or empty
     */
    public void connectToPeer(String connectionString) {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        try {
            // Parse the connection string if needed
            String peerAddress = connectionString;

            // Add the peer
            addPeer(peerAddress);

            // Try to establish a connection
            if (peerAddress.contains(":")) {
                String[] parts = peerAddress.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);

                // Attempt to open a direct connection to test connectivity
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), 5000);
                    logger.info("Successfully connected to peer: " + peerAddress);
                } catch (IOException e) {
                    logger.warning("Connection test to " + peerAddress + " failed. Peer added but may not be reachable.");
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to connect to peer: " + connectionString, e);
            throw new RuntimeException("Failed to connect to peer: " + e.getMessage(), e);
        }
    }

    /**
     * Get the node ID of this peer.
     *
     * @return The node ID
     */
    public int getNodeId() {
        return this.nodeId;
    }

    /**
     * Set the node ID for this peer.
     *
     * @param nodeId The node ID to set
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Check if the P2P manager is currently running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get all currently connected peers.
     *
     * @return A list of peer addresses
     */
    public List<String> getConnectedPeers() {
        return peer.getAllPeers();
    }

    /**
     * Register a keypair for a specific replica.
     *
     * @param senderId The ID of the replica
     * @param keypair The keypair to register
     * @throws IllegalArgumentException if keypair is null
     */
    public void registerReplicaKeypair(int senderId, Keypair keypair) {
        if (keypair == null) {
            throw new IllegalArgumentException("Keypair cannot be null");
        }

        keypairLock.writeLock().lock();
        try {
            replicaKeypairs.put(senderId, keypair);
            logger.info("Registered keypair for replica: " + senderId);
        } finally {
            keypairLock.writeLock().unlock();
        }
    }
}