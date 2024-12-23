//package org.tinc.p2p;
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
//    /**
//     * Returns the unique identifier of the peer.
//     *
//     * @return The peer ID.
//     */
//    public String getPeerId() {
//        return peerId;
//    }
//
//    /**
//     * Starts the server to listen for incoming connections.
//     */
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
//    /**
//     * Connects to another peer using its address.
//     *
//     * @param peerAddress The peer address to connect to.
//     * @param content     The message content to send after connecting.
//     */
//    public void connectToPeer(String peerAddress, String content) {
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port.");
//        }
//
//        String[] addressParts = peerAddress.split(":");
//        String host = addressParts[0];
//        int port = Integer.parseInt(addressParts[1]);
//
//        try (Socket socket = NetworkUtils.connectToAddress(host, port)) {
//            System.out.println(peerId + " connected to peer at: " + peerAddress);
//            if (content != null) {
//                NetworkUtils.sendData(socket, new Message(peerId, content).serialize());
//            }
//        } catch (IOException e) {
//            System.err.println(peerId + " failed to connect to peer at: " + peerAddress + " Error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Sends a message to a specific peer.
//     *
//     * @param messageContent The message content to send.
//     * @param peerAddress    The address of the peer.
//     */
//    public void sendMessage(String messageContent, String peerAddress) {
//        if (messageContent == null || messageContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be null or empty.");
//        }
//
//        connectToPeer(peerAddress, messageContent);
//    }
//
//    /**
//     * Adds a peer to the known peers' list.
//     *
//     * @param peerAddress The address of the peer to add.
//     */
//    public void addPeer(String peerAddress) {
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            System.err.println(peerId + " invalid peer address: " + peerAddress);
//            return;
//        }
//
//        knownPeers.add(peerAddress);
//        System.out.println(peerId + " added peer: " + peerAddress);
//    }
//
//    /**
//     * Broadcasts a message to all known peers.
//     *
//     * @param messageContent The message content to broadcast.
//     */
//    public void broadcast(String messageContent) {
//        if (messageContent == null || messageContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be null or empty.");
//        }
//
//        for (String peerAddress : knownPeers) {
//            sendMessage(messageContent, peerAddress);
//        }
//        System.out.println(peerId + " broadcasted message: " + messageContent);
//    }
//
//    /**
//     * Shuts down the peer server.
//     */
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
//    /**
//     * Handles peer-to-peer connections.
//     */
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
//        public void receiveMessage() {
//            if (!isRunning) {
//                System.err.println(peerId + " cannot listen. Server not running.");
//                return;
//            }
//
//            threadPool.execute(() -> {
//                try {
//                    while (isRunning) {
//                        Socket clientSocket = NetworkUtils.listenForConnections(serverSocket);
//                        threadPool.execute(new PeerConnectionHandler(clientSocket));
//                    }
//                } catch (IOException e) {
//                    if (isRunning) {
//                        System.err.println(peerId + " error while listening for messages: " + e.getMessage());
//                    }
//                }
//            });
//        }
//
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


package org.tinc.p2p;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a peer in the peer-to-peer network.
 */
public class Peer {
    private final String peerId;          // Unique ID for the peer
    private final String peerAddress;     // Network address of the peer (host:port)
    private final Set<String> knownPeers; // List of known peers' addresses
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;   // Flag to indicate if the server is running

    public Peer(String peerId, String peerAddress) {
        if (peerId == null || peerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Peer ID cannot be null or empty.");
        }
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port.");
        }

        this.peerId = peerId;
        this.peerAddress = peerAddress;
        this.knownPeers = Collections.synchronizedSet(new HashSet<>());
        this.threadPool = Executors.newCachedThreadPool();
        this.isRunning = false;
    }

    /**
     * Returns the unique identifier of the peer.
     *
     * @return The peer ID.
     */
    public String getPeerId() {
        return peerId;
    }

    /**
     * Starts the server to listen for incoming connections.
     */
    public synchronized void startServer() {
        if (isRunning) {
            System.out.println(peerId + " server is already running.");
            return;
        }

        threadPool.execute(() -> {
            try {
                String[] addressParts = peerAddress.split(":");
                String host = addressParts[0];
                int port = Integer.parseInt(addressParts[1]);

                serverSocket = NetworkUtils.createSocket(host, port);
                isRunning = true;
                System.out.println(peerId + " server started on " + host + ":" + port);

                while (isRunning) {
                    Socket clientSocket = NetworkUtils.listenForConnections(serverSocket);
                    threadPool.execute(new PeerConnectionHandler(clientSocket));
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println(peerId + " server error: " + e.getMessage());
                }
            } finally {
                shutdown();
            }
        });
    }

    /**
     * Connects to another peer using its address.
     *
     * @param peerAddress The peer address to connect to.
     * @param content     The message content to send after connecting.
     */

    public void connectToPeer(String peerAddress, String content) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port.");
        }

        String[] addressParts = peerAddress.split(":");
        String host = addressParts[0];
        int port = Integer.parseInt(addressParts[1]);

        int retries = 3;
        while (retries-- > 0) {
            try (Socket socket = NetworkUtils.connectToAddress(host, port)) {
                System.out.println(peerId + " connected to peer at: " + peerAddress);
                if (content != null) {
                    NetworkUtils.sendData(socket, new Message(peerId, content).serialize());
                }
                break;
            } catch (IOException e) {
                System.err.println(peerId + " failed to connect to peer at: " + peerAddress + " Retrying...");
                try {
                    Thread.sleep(1000); // Wait before retrying
                } catch (InterruptedException ignored) {}
            }
        }
    }


//    public void connectToPeer(String peerAddress, String content) {
//        if (peerAddress == null || !peerAddress.contains(":")) {
//            throw new IllegalArgumentException("Invalid peer address. Must be in the format host:port.");
//        }
//
//        String[] addressParts = peerAddress.split(":");
//        String host = addressParts[0];
//        int port = Integer.parseInt(addressParts[1]);
//
//        try (Socket socket = NetworkUtils.connectToAddress(host, port)) {
//            System.out.println(peerId + " connected to peer at: " + peerAddress);
//            if (content != null) {
//                NetworkUtils.sendData(socket, new Message(peerId, content).serialize());
//            }
//        } catch (IOException e) {
//            System.err.println(peerId + " failed to connect to peer at: " + peerAddress + " Error: " + e.getMessage());
//        }
//    }

    /**
     * Sends a message to a specific peer.
     *
     * @param messageContent The message content to send.
     * @param peerAddress    The address of the peer.
     */
    public void sendMessage(String messageContent, String peerAddress) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty.");
        }

        connectToPeer(peerAddress, messageContent);
    }

    /**
     * Adds a peer to the known peers' list.
     *
     * @param peerAddress The address of the peer to add.
     */
    public void addPeer(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            System.err.println(peerId + " invalid peer address: " + peerAddress);
            return;
        }

        knownPeers.add(peerAddress);
        System.out.println(peerId + " added peer: " + peerAddress);
    }

    /**
     * Removes a peer from the known peers' list.
     *
     * @param peerAddress The address of the peer to remove.
     */
    public void removePeer(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            System.err.println(peerId + " invalid peer address: " + peerAddress);
            return;
        }

        if (knownPeers.remove(peerAddress)) {
            System.out.println(peerId + " removed peer: " + peerAddress);
        } else {
            System.err.println(peerId + " peer not found: " + peerAddress);
        }
    }

    /**
     * Broadcasts a message to all known peers.
     *
     * @param messageContent The message content to broadcast.
     */
    public void broadcast(String messageContent) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty.");
        }

        for (String peerAddress : knownPeers) {
            sendMessage(messageContent, peerAddress);
        }
        System.out.println(peerId + " broadcasted message: " + messageContent);
    }

    /**
     * Shuts down the peer server.
     */
    public synchronized void shutdown() {
        if (!isRunning) return;
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println(peerId + " server shut down.");
        } catch (IOException e) {
            System.err.println(peerId + " error shutting down server: " + e.getMessage());
        }
    }

    /**
     * Handles peer-to-peer connections.
     */
    private class PeerConnectionHandler implements Runnable {
        private final Socket clientSocket;

        public PeerConnectionHandler(Socket clientSocket) {
            if (clientSocket == null || clientSocket.isClosed()) {
                throw new IllegalArgumentException("Client socket must not be null or closed.");
            }
            this.clientSocket = clientSocket;
        }

        public void receiveMessage() {
            if (!isRunning) {
                System.err.println(peerId + " cannot listen. Server not running.");
                return;
            }

            threadPool.execute(() -> {
                try {
                    while (isRunning) {
                        Socket clientSocket = NetworkUtils.listenForConnections(serverSocket);
                        threadPool.execute(new PeerConnectionHandler(clientSocket));
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println(peerId + " error while listening for messages: " + e.getMessage());
                    }
                }
            });
        }

        @Override
        public void run() {
            try {
                byte[] data = NetworkUtils.receiveData(clientSocket);
                Message message = Message.deserialize(data);
                System.out.println(peerId + " received: " + message);
            } catch (Exception e) {
                System.err.println(peerId + " error handling connection: " + e.getMessage());
            } finally {
                try {
                    NetworkUtils.closeSocket(clientSocket);
                } catch (IOException e) {
                    System.err.println(peerId + " error closing socket: " + e.getMessage());
                }
            }
        }
    }
}
