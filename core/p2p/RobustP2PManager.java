package org.tinc.p2p;

import org.tinc.crypto.Keypair;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// Robust Peer-to-Peer Communication Class
public class RobustP2PManager {
    private final Peer peer;
    private final BroadcastManager broadcastManager;
    private final UDPManager udpManager;
    private final XMLPeerDiscovery xmlPeerDiscovery;
    private final ExecutorService executorService;

    public RobustP2PManager(Peer peer, UDPManager udpManager, XMLPeerDiscovery xmlPeerDiscovery) {
        this.peer = peer;
        this.udpManager = udpManager;
        this.xmlPeerDiscovery = xmlPeerDiscovery;
        this.broadcastManager = new BroadcastManager(peer, new HashSet<>());
        this.executorService = Executors.newCachedThreadPool();
    }

    // Initialize the P2P network
    public void initializeNetwork(String xmlFilePath, String udpBroadcastAddress) throws Exception {
        // Discover peers from XML
        peer.startServer(); // Ensure the server starts before broadcasting
        List<String> peers = xmlPeerDiscovery.discoverPeers();
        for (String peerAddress : peers) {
            peer.addPeer(peerAddress);
            broadcastManager.addPeer(peerAddress);
        }
        System.out.println("RobustP2PManager: Peers added from XML discovery.");

        // Start UDP listener
        executorService.execute(() -> {
            try {
                udpManager.listen(this::handleMessage);
            } catch (IOException e) {
                System.err.println("RobustP2PManager: UDP listener failed: " + e.getMessage());
            }
        });

        // Broadcast initialization message
        udpManager.broadcast("Peer Initialization: " + peer.getPeerId(), udpBroadcastAddress);
        System.out.println("RobustP2PManager: Initialization message broadcasted.");
    }

    // Send a broadcast message
    public void sendBroadcast(String messageContent) {
        broadcastManager.broadcastMessage(messageContent);
    }

    // Handle incoming messages
    private void handleMessage(String message) {
        System.out.println("RobustP2PManager: Received message: " + message);
        // Logic to handle messages, e.g., update peer list, forward message, etc.
    }

    // Send a direct message
    public void sendDirectMessage(String peerAddress, String messageContent) {
        peer.sendMessage(messageContent, peerAddress);
    }

    // Handle acknowledgment from peers
    public void acknowledgeMessage(String peerAddress) {
        broadcastManager.acknowledgeReceipt(peerAddress);
    }

    // Add a new peer dynamically
    public void addPeer(String peerAddress) {
        peer.addPeer(peerAddress);
        broadcastManager.addPeer(peerAddress);
    }

    // Remove an existing peer dynamically
    public void removePeer(String peerAddress) {
        peer.removePeer(peerAddress);
        broadcastManager.removePeer(peerAddress);
    }

    // Shut down the network gracefully
    public void shutdown() {
        peer.shutdown();
        udpManager.close();
        executorService.shutdown();
        System.out.println("RobustP2PManager: Network shut down.");
    }

    // Start a TCP server for one-to-one communication
    public void startDirectConnectionServer(int port) {
        executorService.execute(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("RobustP2PManager: Direct connection server started on port " + port);
                while (!executorService.isShutdown()) {
                    Socket clientSocket = serverSocket.accept();
                    executorService.execute(() -> handleDirectConnection(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("RobustP2PManager: Direct connection server failed: " + e.getMessage());
            }
        });
    }


    private void handleDirectConnection(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            Message message = Message.deserialize(Arrays.copyOf(buffer, bytesRead));

            System.out.println("RobustP2PManager: Received direct message: " + message.getContent());

            String response = "Acknowledged: " + message.getContent();
            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("RobustP2PManager: Failed to handle direct connection: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("RobustP2PManager: Failed to close client socket: " + e.getMessage());
            }
        }
    }

}
