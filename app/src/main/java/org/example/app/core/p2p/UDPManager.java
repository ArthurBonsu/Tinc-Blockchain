//package org.example.app.core.p2p;
//
//import java.io.*;
//import java.net.*;
//import java.util.*;
//import javax.xml.parsers.*;
//import org.w3c.dom.*;
//import java.util.function.Consumer;
//
//
//
//// Class for managing UDP communication
//public class UDPManager {
//    private final int port;
//    private DatagramSocket socket;
//
//    public UDPManager(int port) throws SocketException {
//        this.port = port;
//        this.socket = new DatagramSocket(port);
//    }
//
//    // Broadcast a message
//    public void broadcast(String message, String broadcastAddress) throws IOException {
//        byte[] data = message.getBytes();
//        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(broadcastAddress), port);
//        socket.send(packet);
//        System.out.println("UDPManager: Broadcast message sent.");
//    }
//
//    // Listen for incoming messages
//    public void listen(Consumer<String> messageHandler) throws IOException {
//        byte[] buffer = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//        while (true) {
//            socket.receive(packet);
//            String message = new String(packet.getData(), 0, packet.getLength());
//            messageHandler.accept(message);
//        }
//    }
//
//    public void close() {
//        socket.close();
//    }
//}
//
//// Class for one-to-one communication
//class DirectConnection {
//    public void sendMessage(String host, int port, String message) throws IOException {
//        try (Socket socket = new Socket(host, port);
//             OutputStream os = socket.getOutputStream()) {
//            os.write(message.getBytes());
//            os.flush();
//            System.out.println("DirectConnection: Message sent to " + host + ":" + port);
//        }
//    }
//
//    public void startListening(int port, Consumer<String> messageHandler) throws IOException {
//        try (ServerSocket serverSocket = new ServerSocket(port)) {
//            while (true) {
//                try (Socket clientSocket = serverSocket.accept();
//                     InputStream is = clientSocket.getInputStream()) {
//                    byte[] buffer = new byte[1024];
//                    int bytesRead = is.read(buffer);
//                    String message = new String(buffer, 0, bytesRead);
//                    messageHandler.accept(message);
//                }
//            }
//        }
//    }
//}
//
//// Class to manage TCP, UDP, and XML-based communication
//class LANPeerManager {
//    private final Peer peer;
//    private final UDPManager udpManager;
//    private final XMLPeerDiscovery xmlPeerDiscovery;
//
//    public LANPeerManager(Peer peer, UDPManager udpManager, XMLPeerDiscovery xmlPeerDiscovery) {
//        this.peer = peer;
//        this.udpManager = udpManager;
//        this.xmlPeerDiscovery = xmlPeerDiscovery;
//    }
//
//    // Start managing the network
//    public void start() throws Exception {
//        List<String> peers = xmlPeerDiscovery.discoverPeers();
//        for (String peerAddress : peers) {
//            peer.addPeer(peerAddress);
//        }
//        System.out.println("LANPeerManager: Peers discovered and added.");
//
//        // Example UDP listening
//        new Thread(() -> {
//            try {
//                udpManager.listen(message -> {
//                    System.out.println("LANPeerManager: UDP message received: " + message);
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    // Broadcast a message using UDP
//    public void broadcast(String message, String broadcastAddress) throws IOException {
//        udpManager.broadcast(message, broadcastAddress);
//    }
//}
//
//// Abstract communication handler
//class CommunicationHandler {
//    private final Peer peer;
//    private final UDPManager udpManager;
//    private final DirectConnection directConnection;
//
//    public CommunicationHandler(Peer peer, UDPManager udpManager, DirectConnection directConnection) {
//        this.peer = peer;
//        this.udpManager = udpManager;
//        this.directConnection = directConnection;
//    }
//
//    public void sendBroadcast(String message, String broadcastAddress) throws IOException {
//        udpManager.broadcast(message, broadcastAddress);
//    }
//
//    public void sendDirectMessage(String host, int port, String message) throws IOException {
//        directConnection.sendMessage(host, port, message);
//    }
//
//    public void startListeningForDirectMessages(int port) throws IOException {
//        new Thread(() -> {
//            try {
//                directConnection.startListening(port, message -> {
//                    System.out.println("CommunicationHandler: Direct message received: " + message);
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//}




package org.example.app.core.p2p;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UDPManager handles UDP communication for peer discovery and broadcast messaging.
 */
public class UDPManager {
    private static final Logger logger = Logger.getLogger(UDPManager.class.getName());

    private static final int DEFAULT_PORT = 9876;
    private static final int BUFFER_SIZE = 8192;

    private final int port;
    private DatagramSocket socket;
    private ExecutorService executorService;
    private final AtomicBoolean running;

    /**
     * Constructor for UDPManager with a specified port.
     *
     * @param port The UDP port to use
     * @throws IllegalArgumentException if port is invalid
     */
    public UDPManager(int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }

        this.port = port;
        this.running = new AtomicBoolean(false);
    }

    /**
     * Default constructor for UDPManager.
     * Uses the default port.
     */
    public UDPManager() {
        this(DEFAULT_PORT);
    }

    /**
     * Start listening for UDP messages.
     *
     * @param messageHandler The handler for received messages
     * @throws IOException if the socket cannot be created or bound
     */
    public void listen(Consumer<String> messageHandler) throws IOException {
        if (messageHandler == null) {
            throw new IllegalArgumentException("Message handler cannot be null");
        }

        if (running.getAndSet(true)) {
            logger.warning("UDP listener is already running");
            return;
        }

        try {
            // Create and bind the socket
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
            socket.setSoTimeout(0); // No timeout

            // Create thread pool for message handling
            executorService = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "UDPListener-" + port);
                t.setDaemon(true);
                return t;
            });

            // Start the listener loop
            executorService.execute(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];

                while (running.get() && !socket.isClosed()) {
                    try {
                        // Prepare packet for receiving
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                        // Wait for incoming packet
                        socket.receive(packet);

                        // Process the received packet
                        String message = new String(packet.getData(), 0, packet.getLength());
                        String sender = packet.getAddress().getHostAddress() + ":" + packet.getPort();

                        logger.fine("Received UDP message from " + sender + ": " + message);

                        // Handle the message in a separate thread
                        final String finalMessage = message;
                        executorService.execute(() -> {
                            try {
                                messageHandler.accept(finalMessage);
                            } catch (Exception e) {
                                logger.log(Level.WARNING, "Error handling UDP message", e);
                            }
                        });
                    } catch (SocketTimeoutException e) {
                        // Timeout - just continue
                    } catch (SocketException e) {
                        if (running.get()) {
                            logger.log(Level.WARNING, "Socket error in UDP listener", e);
                        }
                        break;
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "I/O error in UDP listener", e);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Unexpected error in UDP listener", e);
                    }
                }

                logger.info("UDP listener stopped");
            });

            logger.info("UDP listener started on port " + port);
        } catch (Exception e) {
            running.set(false);
            if (socket != null) {
                socket.close();
            }
            throw e;
        }
    }

    /**
     * Send a broadcast UDP message.
     *
     * @param message The message to broadcast
     * @param broadcastAddress The broadcast address to send to
     * @throws IOException if the message cannot be sent
     */
    public void broadcast(String message, String broadcastAddress) throws IOException {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (broadcastAddress == null || broadcastAddress.isEmpty()) {
            throw new IllegalArgumentException("Broadcast address cannot be null or empty");
        }

        // Create the socket if it doesn't exist
        DatagramSocket sendSocket = null;
        try {
            if (socket == null || socket.isClosed()) {
                sendSocket = new DatagramSocket();
                sendSocket.setBroadcast(true);
            } else {
                sendSocket = socket;
            }

            // Create the packet
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(broadcastAddress);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

            // Send the packet
            sendSocket.send(packet);

            logger.fine("Broadcast message sent to " + broadcastAddress + ":" + port + ": " + message);
        } finally {
            // Only close if we created a new socket
            if (sendSocket != null && sendSocket != socket) {
                sendSocket.close();
            }
        }
    }

    /**
     * Send a UDP message to a specific address.
     *
     * @param message The message to send
     * @param address The destination address
     * @param port The destination port
     * @throws IOException if the message cannot be sent
     */
    public void sendMessage(String message, String address, int port) throws IOException {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }

        // Create the socket if it doesn't exist
        DatagramSocket sendSocket = null;
        try {
            if (socket == null || socket.isClosed()) {
                sendSocket = new DatagramSocket();
            } else {
                sendSocket = socket;
            }

            // Create the packet
            byte[] buffer = message.getBytes();
            InetAddress inetAddress = InetAddress.getByName(address);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, port);

            // Send the packet
            sendSocket.send(packet);

            logger.fine("UDP message sent to " + address + ":" + port + ": " + message);
        } finally {
            // Only close if we created a new socket
            if (sendSocket != null && sendSocket != socket) {
                sendSocket.close();
            }
        }
    }

    /**
     * Close the UDP socket and clean up resources.
     */
    public void close() {
        running.set(false);

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
            }
        }

        logger.info("UDP manager closed");
    }

    /**
     * Check if the UDP listener is running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running.get() && socket != null && !socket.isClosed();
    }

    /**
     * Get the port being used.
     *
     * @return The UDP port
     */
    public int getPort() {
        return port;
    }
}