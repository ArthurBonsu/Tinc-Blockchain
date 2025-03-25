
package org.example.app.core.resources;

import org.example.app.core.resources.NetworkP2PTest.P2PNetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of P2PNetworkManager for real network communication
 * Uses peers.xml to discover peer addresses
 */
public class RealNetworkManager implements P2PNetworkManager {
    private static final Logger logger = LoggerFactory.getLogger(RealNetworkManager.class);
    private static final String RESOURCE_PATH = "/org/example/app/core/resources/peers.xml";
    private static final int DISCOVERY_PORT = 8765;
    private static final String DISCOVERY_MESSAGE = "TINC_NODE_DISCOVERY";
    
    private List<RealPeer> peers = new ArrayList<>();
    private CountDownLatch discoveryLatch;
    private ExecutorService executorService;
    
    @Override
    public void initialize(int peerCount) {
        this.executorService = Executors.newFixedThreadPool(peerCount);
        this.discoveryLatch = new CountDownLatch(peerCount);
        
        List<String> peerAddresses;
        try {
            peerAddresses = loadPeersFromXml();
        } catch (Exception e) {
            logger.error("Failed to load peers from XML", e);
            peerAddresses = createDefaultPeerList(peerCount);
        }
        
        // Create peers up to the requested count
        for (int i = 0; i < Math.min(peerCount, peerAddresses.size()); i++) {
            String peerAddress = peerAddresses.get(i);
            String[] parts = peerAddress.split(":");
            if (parts.length == 2) {
                try {
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    RealPeer peer = new RealPeer("peer-" + i, host, port);
                    peers.add(peer);
                    logger.info("Created peer {} at {}:{}", i, host, port);
                } catch (Exception e) {
                    logger.error("Failed to create peer {}", i, e);
                }
            }
        }
        
        // If we need more peers than defined in XML, create additional ones
        if (peerCount > peerAddresses.size()) {
            int basePort = 8001 + peerAddresses.size();
            for (int i = peerAddresses.size(); i < peerCount; i++) {
                try {
                    RealPeer peer = new RealPeer("peer-" + i, "127.0.0.1", basePort + i - peerAddresses.size());
                    peers.add(peer);
                    logger.info("Created additional peer {} at 127.0.0.1:{}", i, peer.getPort());
                } catch (Exception e) {
                    logger.error("Failed to create additional peer {}", i, e);
                }
            }
        }
    }
    
    private List<String> loadPeersFromXml() {
        try (InputStream inputStream = getClass().getResourceAsStream(RESOURCE_PATH)) {
            if (inputStream == null) {
                logger.error("Resource not found: {}", RESOURCE_PATH);
                return Collections.emptyList();
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            
            NodeList peerNodes = document.getElementsByTagName("peer");
            return IntStream.range(0, peerNodes.getLength())
                    .mapToObj(i -> peerNodes.item(i).getTextContent().trim())
                    .collect(Collectors.toList());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.error("Failed to parse peers.xml", e);
            return Collections.emptyList();
        }
    }
    
    private List<String> createDefaultPeerList(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> "127.0.0.1:" + (8001 + i))
                .collect(Collectors.toList());
    }
    
    @Override
    public void start() {
        for (int i = 0; i < peers.size(); i++) {
            final RealPeer peer = peers.get(i);
            final int index = i;
            
            executorService.submit(() -> {
                try {
                    peer.start();
                    logger.info("Started peer {} on port {}", index, peer.getPort());
                    discoveryLatch.countDown();
                } catch (Exception e) {
                    logger.error("Failed to start peer {}", index, e);
                }
            });
        }
    }
    
    @Override
    public void stop() {
        for (RealPeer peer : peers) {
            try {
                peer.stop();
            } catch (Exception e) {
                logger.error("Error stopping peer", e);
            }
        }
        
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("Stopped all network peers");
    }
    
    @Override
    public boolean sendMessage(int fromIdx, int toIdx, TestMessageInterface message) {
        if (fromIdx >= peers.size() || toIdx >= peers.size()) {
            return false;
        }
        
        RealPeer sender = peers.get(fromIdx);
        RealPeer receiver = peers.get(toIdx);
        
        return sender.sendMessage(message, receiver.getAddress());
    }
    
    @Override
    public boolean broadcastMessage(int fromIdx, TestMessageInterface message) {
        if (fromIdx >= peers.size()) {
            return false;
        }
        
        RealPeer sender = peers.get(fromIdx);
        return sender.broadcastMessage(message);
    }
    
    @Override
    public List<TestMessageInterface> getReceivedMessages(int peerIdx) {
        if (peerIdx >= peers.size()) {
            return Collections.emptyList();
        }
        
        RealPeer peer = peers.get(peerIdx);
        return peer.getReceivedMessages();
    }
    
    @Override
    public Set<InetSocketAddress> getConnectedPeers(int peerIdx) {
        if (peerIdx >= peers.size()) {
            return Collections.emptySet();
        }
        
        RealPeer peer = peers.get(peerIdx);
        return peer.getConnectedPeers();
    }
    
    @Override
    public void clearMessages(int peerIdx) {
        if (peerIdx < peers.size()) {
            RealPeer peer = peers.get(peerIdx);
            peer.clearMessages();
        }
    }
    
    @Override
    public void waitForDiscovery() throws InterruptedException, TimeoutException {
        boolean success = discoveryLatch.await(30, TimeUnit.SECONDS);
        if (!success) {
            throw new TimeoutException("Network discovery timed out");
        }
        
        // Additional wait time for peer connections to establish
        Thread.sleep(1000);
    }
    
    @Override
    public int getPeerCount() {
        return peers.size();
    }
    
    @Override
    public boolean isPeerConnected(int fromIdx, int toIdx) {
        if (fromIdx >= peers.size() || toIdx >= peers.size()) {
            return false;
        }
        
        RealPeer from = peers.get(fromIdx);
        RealPeer to = peers.get(toIdx);
        
        return from.isConnectedTo(to.getAddress());
    }
    
    @Override
    public void ensurePeersConnected() {
        for (int i = 0; i < peers.size(); i++) {
            for (int j = 0; j < peers.size(); j++) {
                if (i != j) {
                    RealPeer from = peers.get(i);
                    RealPeer to = peers.get(j);
                    from.connectTo(to.getAddress());
                }
            }
        }
        
        // Wait a short time for connections to establish
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Implementation of a real network peer
     */
    private class RealPeer {
        private final String id;
        private final String host;
        private final int port;
        private final InetSocketAddress address;
        private final Set<InetSocketAddress> connectedPeers = ConcurrentHashMap.newKeySet();
        private final Map<String, TestMessageInterface> receivedMessages = new ConcurrentHashMap<>();
        private DatagramSocket socket;
        private volatile boolean running = false;
        
        public RealPeer(String id, String host, int port) {
            this.id = id;
            this.host = host;
            this.port = port;
            this.address = new InetSocketAddress(host, port);
        }
        
        public void start() throws SocketException {
            socket = new DatagramSocket(port);
            running = true;
            
            // Start listener thread
            new Thread(() -> {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                while (running) {
                    try {
                        socket.receive(packet);
                        processPacket(packet);
                    } catch (IOException e) {
                        if (running) {
                            logger.error("Error receiving packet on peer {}", id, e);
                        }
                    }
                }
            }).start();
            
            // Send discovery message
            try {
                broadcastDiscovery();
            } catch (IOException e) {
                logger.error("Error broadcasting discovery from peer {}", id, e);
            }
            
            logger.info("Started peer {} on {}:{}", id, host, port);
        }
        
        private void processPacket(DatagramPacket packet) {
            String data = new String(packet.getData(), 0, packet.getLength());
            InetSocketAddress sender = new InetSocketAddress(packet.getAddress(), packet.getPort());
            
            if (data.startsWith(DISCOVERY_MESSAGE)) {
                // Handle discovery message
                connectedPeers.add(sender);
                try {
                    sendDiscoveryAck(sender);
                } catch (IOException e) {
                    logger.error("Error sending discovery ack to {}", sender, e);
                }
            } else if (data.startsWith("MSG:")) {
                // Handle regular message
                String[] parts = data.substring(4).split("\\|", 3);
                if (parts.length == 3) {
                    final String type = parts[0];
                    final String payload = parts[1];
                    final String msgId = parts[2];
                    
                    TestMessageInterface message = new TestMessageInterface() {
                        @Override
                        public String getType() {
                            return type;
                        }
                        
                        @Override
                        public String getPayload() {
                            return payload;
                        }
                        
                        @Override
                        public String getId() {
                            return msgId;
                        }
                    };
                    
                    receivedMessages.put(msgId, message);
                    logger.debug("Peer {} received message from {}: {} ({})", id, sender, type, msgId);
                }
            }
        }
        
        private void broadcastDiscovery() throws IOException {
            String message = DISCOVERY_MESSAGE + "|" + id + "|" + port;
            byte[] buffer = message.getBytes();
            
            // Try both broadcast and specific peer addresses from the XML
            try {
                // Broadcast
                DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, 
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
                socket.send(packet);
            } catch (Exception e) {
                logger.warn("Broadcast failed: {}", e.getMessage());
            }
            
            // Also try to connect to other peers we know about
            for (RealPeer peer : peers) {
                if (!peer.equals(this)) {
                    DatagramPacket packet = new DatagramPacket(
                        buffer, buffer.length,
                        InetAddress.getByName(peer.host), peer.port);
                    socket.send(packet);
                }
            }
        }
        
        private void sendDiscoveryAck(InetSocketAddress target) throws IOException {
            String message = DISCOVERY_MESSAGE + "_ACK|" + id + "|" + port;
            byte[] buffer = message.getBytes();
            
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length,
                target.getAddress(), target.getPort());
            socket.send(packet);
            
            connectedPeers.add(target);
            logger.debug("Peer {} sent discovery ack to {}", id, target);
        }
        
        public void stop() {
            running = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            logger.info("Stopped peer {}", id);
        }
        
        public boolean connectTo(InetSocketAddress peerAddress) {
            try {
                String message = DISCOVERY_MESSAGE + "|" + id + "|" + port;
                byte[] buffer = message.getBytes();
                
                DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length,
                    peerAddress.getAddress(), peerAddress.getPort());
                socket.send(packet);
                
                connectedPeers.add(peerAddress);
                logger.debug("Peer {} connected to {}", id, peerAddress);
                return true;
            } catch (IOException e) {
                logger.error("Error connecting peer {} to {}", id, peerAddress, e);
                return false;
            }
        }
        
        public boolean sendMessage(TestMessageInterface message, InetSocketAddress target) {
            if (!running || !connectedPeers.contains(target)) {
                logger.warn("Peer {} not connected to {} or not running", id, target);
                return false;
            }
            
            try {
                String data = "MSG:" + message.getType() + "|" + message.getPayload() + "|" + message.getId();
                byte[] buffer = data.getBytes();
                
                DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length,
                    target.getAddress(), target.getPort());
                
                socket.send(packet);
                logger.debug("Peer {} sent message to {}: {}", id, target, message.getType());
                return true;
            } catch (IOException e) {
                logger.error("Error sending message from peer {} to {}", id, target, e);
                return false;
            }
        }
        
        public boolean broadcastMessage(TestMessageInterface message) {
            if (!running || connectedPeers.isEmpty()) {
                return false;
            }
            
            boolean allSent = true;
            for (InetSocketAddress peer : connectedPeers) {
                if (!sendMessage(message, peer)) {
                    allSent = false;
                }
            }
            
            return allSent;
        }
        
        public boolean isConnectedTo(InetSocketAddress address) {
            return connectedPeers.contains(address);
        }
        
        public List<TestMessageInterface> getReceivedMessages() {
            return new ArrayList<>(receivedMessages.values());
        }
        
        public void clearMessages() {
            receivedMessages.clear();
        }
        
        public Set<InetSocketAddress> getConnectedPeers() {
            return new HashSet<>(connectedPeers);
        }
        
        public int getPort() {
            return port;
        }
        
        public InetSocketAddress getAddress() {
            return address;
        }
    }
}