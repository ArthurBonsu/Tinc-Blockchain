//package org.example.app.core.p2p;
//
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * Represents a peer in the peer-to-peer network.
// */
//public class Peer {
//    private final String peerId;          // Unique ID for the peer
//    private final String peerAddress;     // Network address of the peer (host:port)
//    private final Set<String> knownPeers; // List of known peers' addresses
//    private final ExecutorService threadPool;
//    private ServerSocket serverSocket;
//    private volatile boolean isRunning;   // Flag to indicate if the server is running
//
//    public Peer(String peerId, String peerAddress) {
//        if (peerId == null || peerId.trim().isEmpty()) {
//            throw new IllegalArgumentException("Peer ID cannot be null or empty.");
//        }
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port.");
//        }
//
//        this.peerId = peerId;
//        this.peerAddress = peerAddress;
//        this.knownPeers = Collections.synchronizedSet(new HashSet<>());
//        this.threadPool = Executors.newCachedThreadPool();
//        this.isRunning = false;
//    }
//
//    public String getPeerId() {
//        return peerId;
//    }
//
//    public synchronized void startServer() {
//        if (isRunning) {
//            System.out.println(peerId + " server is already running.");
//            return;
//        }
//
//        threadPool.execute(() -> {
//            try {
//                String[] addressParts = peerAddress.split(":");
//                String host = addressParts[0];
//                int port = Integer.parseInt(addressParts[1]);
//
//                serverSocket = NetworkUtils.createSocket(host, port);
//                isRunning = true;
//                System.out.println(peerId + " server started on " + host + ":" + port);
//
//                while (isRunning) {
//                    Socket clientSocket = NetworkUtils.listenForConnections(serverSocket);
//                    threadPool.execute(new PeerConnectionHandler(clientSocket));
//                }
//            } catch (IOException e) {
//                if (isRunning) {
//                    System.err.println(peerId + " server error: " + e.getMessage());
//                }
//            } finally {
//                shutdown();
//            }
//        });
//    }
//
//    public void connectToPeer(String peerAddress, String content) {
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port.");
//        }
//
//        String[] addressParts = peerAddress.split(":");
//        String host = addressParts[0];
//        int port = Integer.parseInt(addressParts[1]);
//
//        int retries = 3;
//        while (retries-- > 0) {
//            try (Socket socket = NetworkUtils.connectToAddress(host, port)) {
//                System.out.println(peerId + " connected to peer at: " + peerAddress);
//                if (content != null) {
//                    NetworkUtils.sendData(socket, new Message(peerId, content).serialize());
//                }
//                break;
//            } catch (IOException e) {
//                System.err.println(peerId + " failed to connect to peer at: " + peerAddress + " Retrying...");
//                try {
//                    Thread.sleep(1000); // Wait before retrying
//                } catch (InterruptedException ignored) {}
//            }
//        }
//    }
//
//    public void sendMessage(String messageContent, String peerAddress) {
//        if (messageContent == null || messageContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be null or empty.");
//        }
//        connectToPeer(peerAddress, messageContent);
//    }
//
//    public void addPeer(String peerAddress) {
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            System.err.println(peerId + " invalid peer address: " + peerAddress);
//            return;
//        }
//        knownPeers.add(peerAddress);
//        System.out.println(peerId + " added peer: " + peerAddress);
//    }
//
//    public void removePeer(String peerAddress) {
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            System.err.println(peerId + " invalid peer address: " + peerAddress);
//            return;
//        }
//        if (knownPeers.remove(peerAddress)) {
//            System.out.println(peerId + " removed peer: " + peerAddress);
//        } else {
//            System.err.println(peerId + " peer not found: " + peerAddress);
//        }
//    }
//
//    public void broadcast(String messageContent) {
//        if (messageContent == null || messageContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be null or empty.");
//        }
//        for (String peerAddress : knownPeers) {
//            sendMessage(messageContent, peerAddress);
//        }
//        System.out.println(peerId + " broadcasted message: " + messageContent);
//    }
//
//    public synchronized void shutdown() {
//        if (!isRunning) return;
//        isRunning = false;
//        try {
//            if (serverSocket != null && !serverSocket.isClosed()) {
//                serverSocket.close();
//            }
//            threadPool.shutdown();
//            System.out.println(peerId + " server shut down.");
//        } catch (IOException e) {
//            System.err.println(peerId + " error shutting down server: " + e.getMessage());
//        }
//    }
//
//    private class PeerConnectionHandler implements Runnable {
//        private final Socket clientSocket;
//
//        public PeerConnectionHandler(Socket clientSocket) {
//            if (clientSocket == null || clientSocket.isClosed()) {
//                throw new IllegalArgumentException("Client socket must not be null or closed.");
//            }
//            this.clientSocket = clientSocket;
//        }
//
//        @Override
//        public void run() {
//            try {
//                byte[] data = NetworkUtils.receiveData(clientSocket);
//                Message message = Message.deserialize(data);
//                System.out.println(peerId + " received: " + message);
//            } catch (Exception e) {
//                System.err.println(peerId + " error handling connection: " + e.getMessage());
//            } finally {
//                try {
//                    NetworkUtils.closeSocket(clientSocket);
//                } catch (IOException e) {
//                    System.err.println(peerId + " error closing socket: " + e.getMessage());
//                }
//            }
//        }
//    }
//}




package org.example.app.core.p2p;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a node in the P2P network and handles direct communication
 * with other peers.
 */
public class Peer {
    private static final Logger logger = Logger.getLogger(Peer.class.getName());

    private static final int DEFAULT_PORT = 8000;
    private static final int DEFAULT_TIMEOUT = 10000; // 10 seconds

    private final String peerId;
    private final String peerAddress;
    private final Set<String> knownPeers;
    private final Map<String, Socket> activeSockets;
    private final ReadWriteLock peersLock;
    private final ReadWriteLock socketsLock;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;

    /**
     * Constructor for Peer with a specified ID and address.
     *
     * @param peerId The unique ID for this peer
     * @param peerAddress The address of this peer in the format "host:port"
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Peer(String peerId, String peerAddress) {
        if (peerId == null || peerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Peer ID cannot be null or empty");
        }
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port");
        }

        this.peerId = peerId;
        this.peerAddress = peerAddress;
        this.knownPeers = ConcurrentHashMap.newKeySet();
        this.activeSockets = new ConcurrentHashMap<>();
        this.peersLock = new ReentrantReadWriteLock();
        this.socketsLock = new ReentrantReadWriteLock();
        this.threadPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "PeerThread-" + UUID.randomUUID().toString().substring(0, 8));
            t.setDaemon(true);
            return t;
        });
        this.isRunning = false;

        logger.info("Peer created with ID: " + peerId + " and address: " + peerAddress);
    }

    /**
     * Constructor for Peer with a specified ID and host/port.
     *
     * @param peerId The unique ID for this peer
     * @param host The host address
     * @param port The port number
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Peer(String peerId, String host, int port) {
        this(peerId, host + ":" + port);
    }

    /**
     * Default constructor for Peer. Creates a peer with a random ID and localhost address.
     */
    public Peer() {
        this("Peer-" + UUID.randomUUID().toString().substring(0, 8), "localhost:" + DEFAULT_PORT);
    }

    /**
     * Gets the peer ID.
     *
     * @return The peer ID
     */
    public String getPeerId() {
        return peerId;
    }

    /**
     * Gets the peer address.
     *
     * @return The peer address in the format "host:port"
     */
    public String getPeerAddress() {
        return peerAddress;
    }

    /**
     * Starts the peer server to listen for incoming connections.
     *
     * @throws IOException if server start fails
     */
    public synchronized void startServer() {
        if (isRunning) {
            logger.info("Server is already running for peer: " + peerId);
            return;
        }

        threadPool.execute(() -> {
            try {
                String[] addressParts = peerAddress.split(":");
                String host = addressParts[0];
                int port = Integer.parseInt(addressParts[1]);

                // Create and bind the server socket
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(DEFAULT_TIMEOUT);
                serverSocket.bind(new InetSocketAddress(host, port));

                isRunning = true;
                logger.info("Server started for peer: " + peerId + " on " + host + ":" + port);

                // Accept connections in a loop
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        clientSocket.setSoTimeout(DEFAULT_TIMEOUT);

                        // Handle the connection in a separate thread
                        threadPool.execute(() -> handleConnection(clientSocket));
                    } catch (SocketTimeoutException e) {
                        // Timeout - just continue listening
                    } catch (IOException e) {
                        if (isRunning) {
                            logger.log(Level.WARNING, "Error accepting connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    logger.log(Level.SEVERE, "Server error for peer: " + peerId, e);
                }
            } finally {
                shutdown();
            }
        });
    }

    /**
     * Handles an incoming connection from another peer.
     *
     * @param clientSocket The client socket
     */
    private void handleConnection(Socket clientSocket) {
        if (clientSocket == null) {
            return;
        }

        String clientAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();

        try {
            logger.fine("Handling connection from: " + clientAddress);

            // Read the message
            InputStream is = clientSocket.getInputStream();
            DataInputStream dis = new DataInputStream(is);

            // Read message length and content
            int length = dis.readInt();
            if (length <= 0 || length > 1024 * 1024) { // Sanity check for message size (1MB max)
                throw new IOException("Invalid message length: " + length);
            }

            byte[] data = new byte[length];
            dis.readFully(data);

            // Deserialize and handle the message
            Message message = Message.deserialize(data);
            logger.fine("Received message from " + clientAddress + ": " + message);

            // Process the message - can be extended based on application needs
            processReceivedMessage(message, clientSocket);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling connection from: " + clientAddress, e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing client socket", e);
            }
        }
    }

    /**
     * Processes a received message. Override or extend this method to implement
     * specific message handling logic.
     *
     * @param message The received message
     * @param clientSocket The client socket
     */
    protected void processReceivedMessage(Message message, Socket clientSocket) {
        // Basic echo response - can be overridden or extended
        try {
            // Send acknowledgment
            String response = "ACK from " + peerId + ": " + message.getContent();
            Message responseMsg = new Message(peerId, response);

            OutputStream os = clientSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            byte[] responseData = responseMsg.serialize();
            dos.writeInt(responseData.length);
            dos.write(responseData);
            dos.flush();

            logger.fine("Sent response to " + message.getSender() + ": " + response);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error sending response", e);
        }
    }

    /**
     * Connects to a peer and optionally sends a message.
     *
     * @param peerAddress The address of the peer to connect to
     * @param messageContent The message content to send, or null for just connection
     * @throws IllegalArgumentException if peerAddress is invalid
     * @throws IOException if connection fails
     */
    public void connectToPeer(String peerAddress, String messageContent) throws IOException {
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port");
        }

        String[] addressParts = peerAddress.split(":");
        if (addressParts.length != 2) {
            throw new IllegalArgumentException("Invalid peer address format: " + peerAddress);
        }

        String host = addressParts[0];
        int port;

        try {
            port = Integer.parseInt(addressParts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port in peer address: " + peerAddress);
        }

        Socket socket = null;

        try {
            // Create and connect the socket
            socket = new Socket();
            socket.setSoTimeout(DEFAULT_TIMEOUT);
            socket.connect(new InetSocketAddress(host, port), DEFAULT_TIMEOUT);

            logger.fine("Connected to peer at: " + peerAddress);

            // Send a message if provided
            if (messageContent != null && !messageContent.isEmpty()) {
                Message message = new Message(peerId, messageContent);

                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);

                byte[] data = message.serialize();
                dos.writeInt(data.length);
                dos.write(data);
                dos.flush();

                logger.fine("Sent message to " + peerAddress + ": " + messageContent);
            }
        } finally {
            // Close the socket
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error closing socket", e);
                }
            }
        }
    }

    /**
     * Sends a message to a specific peer.
     *
     * @param messageContent The message content to send
     * @param peerAddress The address of the recipient peer
     * @throws IllegalArgumentException if parameters are invalid
     */
    public void sendMessage(String messageContent, String peerAddress) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        if (peerAddress == null || peerAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Peer address cannot be null or empty");
        }

        try {
            connectToPeer(peerAddress, messageContent);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send message to " + peerAddress, e);
            throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a peer to the known peers list.
     *
     * @param peerAddress The address of the peer to add
     * @throws IllegalArgumentException if peerAddress is invalid
     */
    public void addPeer(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address: " + peerAddress);
        }

        peersLock.writeLock().lock();
        try {
            if (knownPeers.add(peerAddress)) {
                logger.info("Added peer: " + peerAddress);
            } else {
                logger.fine("Peer already exists: " + peerAddress);
            }
        } finally {
            peersLock.writeLock().unlock();
        }
    }

    /**
     * Removes a peer from the known peers list.
     *
     * @param peerAddress The address of the peer to remove
     * @throws IllegalArgumentException if peerAddress is invalid
     */
    public void removePeer(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address: " + peerAddress);
        }

        peersLock.writeLock().lock();
        try {
            if (knownPeers.remove(peerAddress)) {
                logger.info("Removed peer: " + peerAddress);
            } else {
                logger.fine("Peer not found: " + peerAddress);
            }
        } finally {
            peersLock.writeLock().unlock();
        }
    }

    /**
     * Broadcasts a message to all known peers.
     *
     * @param messageContent The message content to broadcast
     * @throws IllegalArgumentException if messageContent is invalid
     */
    public void broadcast(String messageContent) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }

        peersLock.readLock().lock();
        try {
            Set<String> peers = new HashSet<>(knownPeers); // Copy to avoid modification during iteration

            for (String peerAddress : peers) {
                threadPool.execute(() -> {
                    try {
                        sendMessage(messageContent, peerAddress);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to broadcast to peer: " + peerAddress, e);
                    }
                });
            }

            logger.info("Broadcasted message to " + peers.size() + " peers");
        } finally {
            peersLock.readLock().unlock();
        }
    }

    /**
     * Gets a list of all known peers.
     *
     * @return A list of peer addresses
     */
    public List<String> getAllPeers() {
        peersLock.readLock().lock();
        try {
            return new ArrayList<>(knownPeers);
        } finally {
            peersLock.readLock().unlock();
        }
    }

    /**
     * Shuts down the peer server and cleans up resources.
     */
    public synchronized void shutdown() {
        if (!isRunning) {
            return;
        }

        isRunning = false;

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                logger.info("Server socket closed for peer: " + peerId);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing server socket", e);
            }
        }

        // Close active sockets
        socketsLock.writeLock().lock();
        try {
            for (Socket socket : activeSockets.values()) {
                try {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error closing socket", e);
                }
            }
            activeSockets.clear();
        } finally {
            socketsLock.writeLock().unlock();
        }

        // Shutdown thread pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            threadPool.shutdownNow();
        }

        logger.info("Peer " + peerId + " shut down");
    }

    /**
     * Checks if the peer server is running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}

