package org.example.app.core.p2p;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Represents a peer-to-peer network for communication and coordination among peers.
 */
public class  PeerNetwork {
    private final String peerId;         // Unique identifier for the peer in the network
    private final String host;           // Host address where the peer listens
    private final int port;              // Port number for communication
    private final Peer peer;             // Peer instance for handling local peer actions
    private final Set<String> knownPeers; // Known peers in the network

    public PeerNetwork(String peerId, String host, int port) {
        if (peerId == null || peerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Peer ID cannot be null or empty.");
        }
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty.");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

        this.peerId = peerId;
        this.host = host;
        this.port = port;
        this.peer = new Peer(peerId, host + ":" + port);
        this.knownPeers = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Starts the server, allowing the peer to listen for incoming connections.
     */
    public void startServer() {
        peer.startServer();
        System.out.println("PeerNetwork: Server started on " + host + ":" + port);
    }

    /**
     * Accepts incoming connections (handled by the Peer class).
     */
    public void acceptConnections() {
        System.out.println("PeerNetwork: Accepting connections...");
        peer.startServer(); // Start the server to accept incoming connections
    }

    /**
     * Discovers peers using a static list or discovery protocol.
     */
    public void discoverPeers() {
        // Example hardcoded peers or discovery mechanism
        List<String> discoveredPeers = Arrays.asList("127.0.0.1:8081", "127.0.0.1:8082");
        for (String peerAddress : discoveredPeers) {
            addPeerToNetwork(peerAddress);
        }
        System.out.println("PeerNetwork: Discovered peers: " + discoveredPeers);
    }

    /**
     * Adds a peer to the network.
     *
     * @param peerAddress The address of the peer to add.
     */
    public void addPeerToNetwork(String peerAddress) {
        if (peerAddress == null || !peerAddress.contains(":")) {
            System.err.println("PeerNetwork: Invalid peer address: " + peerAddress);
            return;
        }

        peer.addPeer(peerAddress);
        knownPeers.add(peerAddress);
        System.out.println("PeerNetwork: Added peer to network: " + peerAddress);
    }

    /**
     * Sends a broadcast message to all known peers.
     *
     * @param message The message to broadcast.
     */
    public void sendBroadcast(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty.");
        }

        System.out.println("PeerNetwork: Broadcasting message: " + message);
        peer.broadcast(message);
    }

    /**
     * Handles communication with a connected peer.
     *
     * @param clientSocket   The connected client's socket.
     * @param clientAddress  The address of the connected client.
     */
    public void handlePeerConnection(Socket clientSocket, String clientAddress) {
        if (clientSocket == null || clientAddress == null || clientAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid client socket or address.");
        }

        try {
            System.out.println("PeerNetwork: Handling connection from " + clientAddress);
            byte[] data = NetworkUtils.receiveData(clientSocket);
            Message message = Message.deserialize(data);
            System.out.println("PeerNetwork: Received message from " + clientAddress + ": " + message);

            // Example of responding to a message
            Message response = new Message(peerId, "Acknowledged: " + message.getContent());
            NetworkUtils.sendData(clientSocket, response.serialize());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("PeerNetwork: Error handling peer connection from " + clientAddress + ": " + e.getMessage());
        } finally {
            try {
                NetworkUtils.closeSocket(clientSocket);
            } catch (IOException e) {
                System.err.println("PeerNetwork: Error closing connection from " + clientAddress + ": " + e.getMessage());
            }
        }
    }

    /**
     * Main method for running the PeerNetwork.
     */
    public static void main(String[] args) {
        // Initialize PeerNetwork
        PeerNetwork network = new PeerNetwork("Peer1", "127.0.0.1", 8080);
        PeerNetwork network1 = new PeerNetwork("Peer1", "127.0.0.1", 8081);

        // Discover peers
        network.discoverPeers();

        // Start the server to accept incoming connections
        network.startServer();

        // Accept incoming connections in a separate thread
        new Thread(network::acceptConnections).start();

        // Broadcast a message to all known peers
        network.sendBroadcast("Hello, Peer Network!");
    }
}
