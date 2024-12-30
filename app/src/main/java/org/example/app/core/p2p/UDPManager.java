package org.example.app.core.p2p;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.function.Consumer;



// Class for managing UDP communication
public class UDPManager {
    private final int port;
    private DatagramSocket socket;

    public UDPManager(int port) throws SocketException {
        this.port = port;
        this.socket = new DatagramSocket(port);
    }

    // Broadcast a message
    public void broadcast(String message, String broadcastAddress) throws IOException {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(broadcastAddress), port);
        socket.send(packet);
        System.out.println("UDPManager: Broadcast message sent.");
    }

    // Listen for incoming messages
    public void listen(Consumer<String> messageHandler) throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            messageHandler.accept(message);
        }
    }

    public void close() {
        socket.close();
    }
}

// Class for one-to-one communication
class DirectConnection {
    public void sendMessage(String host, int port, String message) throws IOException {
        try (Socket socket = new Socket(host, port);
             OutputStream os = socket.getOutputStream()) {
            os.write(message.getBytes());
            os.flush();
            System.out.println("DirectConnection: Message sent to " + host + ":" + port);
        }
    }

    public void startListening(int port, Consumer<String> messageHandler) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     InputStream is = clientSocket.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead = is.read(buffer);
                    String message = new String(buffer, 0, bytesRead);
                    messageHandler.accept(message);
                }
            }
        }
    }
}

// Class to manage TCP, UDP, and XML-based communication
class LANPeerManager {
    private final Peer peer;
    private final UDPManager udpManager;
    private final XMLPeerDiscovery xmlPeerDiscovery;

    public LANPeerManager(Peer peer, UDPManager udpManager, XMLPeerDiscovery xmlPeerDiscovery) {
        this.peer = peer;
        this.udpManager = udpManager;
        this.xmlPeerDiscovery = xmlPeerDiscovery;
    }

    // Start managing the network
    public void start() throws Exception {
        List<String> peers = xmlPeerDiscovery.discoverPeers();
        for (String peerAddress : peers) {
            peer.addPeer(peerAddress);
        }
        System.out.println("LANPeerManager: Peers discovered and added.");

        // Example UDP listening
        new Thread(() -> {
            try {
                udpManager.listen(message -> {
                    System.out.println("LANPeerManager: UDP message received: " + message);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Broadcast a message using UDP
    public void broadcast(String message, String broadcastAddress) throws IOException {
        udpManager.broadcast(message, broadcastAddress);
    }
}

// Abstract communication handler
class CommunicationHandler {
    private final Peer peer;
    private final UDPManager udpManager;
    private final DirectConnection directConnection;

    public CommunicationHandler(Peer peer, UDPManager udpManager, DirectConnection directConnection) {
        this.peer = peer;
        this.udpManager = udpManager;
        this.directConnection = directConnection;
    }

    public void sendBroadcast(String message, String broadcastAddress) throws IOException {
        udpManager.broadcast(message, broadcastAddress);
    }

    public void sendDirectMessage(String host, int port, String message) throws IOException {
        directConnection.sendMessage(host, port, message);
    }

    public void startListeningForDirectMessages(int port) throws IOException {
        new Thread(() -> {
            try {
                directConnection.startListening(port, message -> {
                    System.out.println("CommunicationHandler: Direct message received: " + message);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
