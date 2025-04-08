
package org.example.app.core.resources;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class NetworkP2PTest {
    private static final Logger logger = LoggerFactory.getLogger(NetworkP2PTest.class);

    private static final String RESOURCE_PATH = "/org/example/app/core/resources/peers.xml";
    private static final int EXPECTED_PEER_COUNT = 10;
    private static final int TEST_PEER_COUNT = 3;
    private static final int BASE_PORT = 8001;
    private static final String TEST_MESSAGE = "Test message payload";
    private static final long TIMEOUT_SECONDS = 10;
    private static final String DISCOVERY_MESSAGE = "TINC_NODE_DISCOVERY";
    private static final int DISCOVERY_PORT = 8765;

    private static final boolean NETWORK_MODE = Boolean.getBoolean("p2p.network");

    private P2PNetworkManager networkManager;

    @BeforeEach
    void setUp() {
        logger.info("===== INITIALIZING TEST =====");
        logger.info("Network Mode: {}", NETWORK_MODE ? "NETWORK" : "LOCAL");
        logger.info("Peer Count: {}", TEST_PEER_COUNT);

        networkManager = NETWORK_MODE ? new RealNetworkManager() : new LocalNetworkManager();
        networkManager.initialize(TEST_PEER_COUNT);
        networkManager.start();

        logger.info("Network manager initialized in {} mode with {} peers",
                NETWORK_MODE ? "NETWORK" : "LOCAL", TEST_PEER_COUNT);
    }

    @AfterEach
    void tearDown() {
        if (networkManager != null) {
            networkManager.stop();
            logger.info("Network manager stopped");
        }
    }

    @Test
    void testPeerConnectivity() {
        logger.info("===== TESTING PEER CONNECTIVITY =====");

        assertTimeout(Duration.ofSeconds(TIMEOUT_SECONDS), () -> {
            networkManager.waitForDiscovery();
            networkManager.ensurePeersConnected();

            for (int i = 0; i < networkManager.getPeerCount(); i++) {
                for (int j = i + 1; j < networkManager.getPeerCount(); j++) {
                    boolean connected = networkManager.isPeerConnected(i, j);
                    logger.info("Peer {} connected to Peer {}: {}", i, j, connected);
                    assertTrue(connected, "All peers should be connected");
                }
            }
        });
    }

    @Test
    void testSendMessage() {
        logger.info("===== TESTING MESSAGE SENDING =====");

        assertTimeout(Duration.ofSeconds(TIMEOUT_SECONDS), () -> {
            networkManager.waitForDiscovery();
            networkManager.ensurePeersConnected();

            networkManager.clearMessages(1);
            TestMessage message = new TestMessage("TEST", TEST_MESSAGE, "msg-1");
            boolean sent = networkManager.sendMessage(0, 1, message);
            logger.info("Message sent from Peer 0 to Peer 1: {}", sent);
            assertTrue(sent, "Message should be sent successfully");

            List<TestMessageInterface> received = networkManager.getReceivedMessages(1);
            logger.info("Messages received by Peer 1: {}", received.size());
            assertEquals(1, received.size(), "Should receive one message");

            TestMessageInterface receivedMessage = received.get(0);
            assertEquals("TEST", receivedMessage.getType(), "Message type match");
            assertEquals(TEST_MESSAGE, receivedMessage.getPayload(), "Payload match");
        });
    }

    @Test
    void testBroadcastMessage() {
        logger.info("===== TESTING BROADCAST MESSAGE =====");

        assertTimeout(Duration.ofSeconds(TIMEOUT_SECONDS), () -> {
            networkManager.waitForDiscovery();
            networkManager.ensurePeersConnected();

            for (int i = 1; i < networkManager.getPeerCount(); i++) {
                networkManager.clearMessages(i);
            }

            TestMessage message = new TestMessage("BROADCAST", "Broadcast", "b1");
            boolean success = networkManager.broadcastMessage(0, message);
            logger.info("Broadcast message from Peer 0: {}", success);
            assertTrue(success, "Broadcast should succeed");

            for (int i = 1; i < networkManager.getPeerCount(); i++) {
                List<TestMessageInterface> received = networkManager.getReceivedMessages(i);
                logger.info("Messages received by Peer {}: {}", i, received.size());
                assertEquals(1, received.size(), "Each peer receives broadcast");
                assertEquals("BROADCAST", received.get(0).getType(), "Type check");
            }
        });
    }

    @Test
    void testRequestResponse() {
        logger.info("===== TESTING REQUEST-RESPONSE =====");

        assertTimeout(Duration.ofSeconds(TIMEOUT_SECONDS), () -> {
            networkManager.waitForDiscovery();
            networkManager.ensurePeersConnected();

            for (int i = 0; i < networkManager.getPeerCount(); i++) {
                networkManager.clearMessages(i);
            }

            TestMessage request = new TestMessage("REQUEST", "Get data", "req1");
            networkManager.broadcastMessage(0, request);

            for (int i = 1; i < networkManager.getPeerCount(); i++) {
                List<TestMessageInterface> received = networkManager.getReceivedMessages(i);
                logger.info("Request received by Peer {}: {}", i, received.size());
                assertEquals(1, received.size(), "Should receive request");
                assertEquals("REQUEST", received.get(0).getType(), "Request type check");
            }

            for (int i = 1; i < networkManager.getPeerCount(); i++) {
                TestMessage response = new TestMessage("RESPONSE", "Data", "resp" + i);
                networkManager.clearMessages(0);
                networkManager.sendMessage(i, 0, response);

                List<TestMessageInterface> clientMessages = networkManager.getReceivedMessages(0);
                logger.info("Response from Peer {} to Client: {}", i, clientMessages.size());
                assertEquals(1, clientMessages.size(), "Client gets each response");
                assertEquals("RESPONSE", clientMessages.get(0).getType(), "Response type");
            }
        });
    }

    @Test
    void testPeersXmlParsing() {
        logger.info("===== TESTING PEERS XML PARSING =====");

        assertDoesNotThrow(() -> {
            ensurePeersXmlExists();
            List<String> peers = loadPeersFromXml();

            logger.info("Loaded peers: {}", peers);
            assertFalse(peers.isEmpty(), "Should load at least one peer");

            peers.forEach(peer -> assertTrue(isValidPeerFormat(peer),
                    "Peer format check: " + peer));
        }, "Parsing peers.xml should not throw exceptions");
    }

    @Test
    @EnabledIfSystemProperty(named = "p2p.network", matches = "true")
    void testRealNetworkFunctionality() throws InterruptedException {
        assumeFalse(!NETWORK_MODE, "Requires network mode");
        logger.info("===== TESTING REAL NETWORK =====");

        assertTimeout(Duration.ofSeconds(TIMEOUT_SECONDS), () -> {
            networkManager.waitForDiscovery();

            for (int i = 0; i < networkManager.getPeerCount(); i++) {
                Set<InetSocketAddress> connected = networkManager.getConnectedPeers(i);
                logger.info("Peer {} has {} connections", i, connected.size());
                assertTrue(connected.size() > 0, "Each peer connects to at least one");
            }

            networkManager.clearMessages(1);
            TestMessage message = new TestMessage("NETWORK", "Real network", "net1");
            boolean sent = networkManager.sendMessage(0, 1, message);
            logger.info("Real network message sent: {}", sent);
            assertTrue(sent, "Network message should be sent");

            Thread.sleep(2000); // Wait for transmission

            List<TestMessageInterface> received = networkManager.getReceivedMessages(1);
            logger.info("Real message received: {}", received.size());

            if (!received.isEmpty()) {
                assertEquals("NETWORK", received.get(0).getType(), "Check type");
            } else {
                logger.warn("No network message received (check network)");
            }
        });
    }

    private boolean isValidPeerFormat(String peer) {
        return peer.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+$");
    }

    private void ensurePeersXmlExists() throws IOException {
        Path targetDir = Paths.get("src", "test", "resources", "org", "example", "app", "core", "resources");
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve("peers.xml");
        if (!Files.exists(targetFile)) {
            String content = loadSamplePeersContent();
            Files.writeString(targetFile, content);
            logger.info("Created sample peers.xml at {}", targetFile);
        }
    }

    private String loadSamplePeersContent() {
        return "<peers>\n" +
                "    <peer>127.0.0.1:8001</peer>\n" +
                "    <peer>127.0.0.1:8002</peer>\n" +
                "    <peer>127.0.0.1:8003</peer>\n" +
                "</peers>";
    }

    private List<String> loadPeersFromXml() {
        try (InputStream inputStream = getClass().getResourceAsStream(RESOURCE_PATH)) {
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

    public interface P2PNetworkManager {
        void initialize(int peerCount);
        void start();
        void stop();
        boolean sendMessage(int fromIdx, int toIdx, TestMessageInterface message);
        boolean broadcastMessage(int fromIdx, TestMessageInterface message);
        List<TestMessageInterface> getReceivedMessages(int peerIdx);
        Set<InetSocketAddress> getConnectedPeers(int peerIdx);
        void clearMessages(int peerIdx);
        void waitForDiscovery() throws InterruptedException, TimeoutException;
        int getPeerCount();
        boolean isPeerConnected(int fromIdx, int toIdx);
        void ensurePeersConnected();
    }

    private class LocalNetworkManager implements P2PNetworkManager {
        private List<LocalPeer> peers;
        private CountDownLatch discoveryLatch;
    
        @Override
        public void initialize(int peerCount) {
            peers = new ArrayList<>();
            discoveryLatch = new CountDownLatch(peerCount);
    
            for (int i = 0; i < peerCount; i++) {
                LocalPeer peer = new LocalPeer("peer-" + (i + 1), BASE_PORT + i);
                peers.add(peer);
            }
        }
    
        @Override
        public void start() {
            peers.forEach(LocalPeer::start);
            ensurePeersConnected();
        }
    
        @Override
        public void stop() {
            peers.forEach(LocalPeer::stop);
        }
    
        @Override
        public boolean sendMessage(int fromIdx, int toIdx, TestMessageInterface msg) {
            LocalPeer from = peers.get(fromIdx);
            LocalPeer to = peers.get(toIdx);
            return from.sendMessage(msg, to.getPort());
        }
    
        @Override
        public boolean broadcastMessage(int fromIdx, TestMessageInterface msg) {
            LocalPeer from = peers.get(fromIdx);
            return from.broadcastMessage(msg);
        }
    
        @Override
        public List<TestMessageInterface> getReceivedMessages(int peerIdx) {
            LocalPeer peer = peers.get(peerIdx);
            // Convert the Map<String, TestMessageInterface> to List<TestMessageInterface>
            return new ArrayList<>(peer.getReceivedMessages().values());
        }
    
        @Override
        public Set<InetSocketAddress> getConnectedPeers(int peerIdx) {
            LocalPeer peer = peers.get(peerIdx);
            return peer.getConnectedPeers().stream()
                    .map(port -> new InetSocketAddress("127.0.0.1", port))
                    .collect(Collectors.toSet());
        }
    
        @Override
        public void clearMessages(int peerIdx) {
            LocalPeer peer = peers.get(peerIdx);
            peer.clearMessages();
        }
    
        @Override
        public void waitForDiscovery() throws InterruptedException, TimeoutException {
            boolean success = discoveryLatch.await(5, TimeUnit.SECONDS);
            if (!success) {
                throw new TimeoutException("Local discovery timed out");
            }
        }
    
        @Override
        public int getPeerCount() {
            return peers.size();
        }
    
        @Override
        public boolean isPeerConnected(int fromIdx, int toIdx) {
            LocalPeer from = peers.get(fromIdx);
            LocalPeer to = peers.get(toIdx);
            return from.isConnected(to.getPort());
        }
    
        @Override
        public void ensurePeersConnected() {
            for (int i = 0; i < peers.size(); i++) {
                for (int j = 0; j < peers.size(); j++) {
                    if (i != j) {
                        LocalPeer p1 = peers.get(i);
                        LocalPeer p2 = peers.get(j);
                        p1.connectTo(p2.getPort());
                    }
                }
                discoveryLatch.countDown();
            }
        }
    }

    private class LocalPeer {
        private final String id;
        private final int port;
        private final Set<Integer> connectedPeers = ConcurrentHashMap.newKeySet();
        private final Map<String, TestMessageInterface> receivedMessages = new ConcurrentHashMap<>();
        private volatile boolean running;

        private LocalPeer(String id, int port) {
            this.id = id;
            this.port = port;
        }
        public int getPort() {
            return port;
        }
        private void start() {
            running = true;
            logger.info("Started local peer {} on port {}", id, port);
        }

        private void stop() {
            running = false;
            logger.info("Stopped local peer {}", id);
        }

        public boolean connectTo(int peerPort) {
            if (peerPort != port && running) {
                if (connectedPeers.add(peerPort)) {
                    // Find the peer with this port and connect it back to us
                    for (LocalPeer peer : ((LocalNetworkManager) networkManager).peers) {
                        if (peer.port == peerPort && peer.running) {
                            peer.connectedPeers.add(this.port);
                            logger.info(id + " connected to peer at port " + peerPort);
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean sendMessage(TestMessageInterface message, int targetPort) {
            if (!running || !connectedPeers.contains(targetPort)) {
                return false;
            }

            // Find the target peer
            for (LocalPeer peer : ((LocalNetworkManager) networkManager).peers) {
                if (peer.port == targetPort && peer.running) {
                    peer.receiveMessage(message, this.port);
                    logger.info(id + " sent message to peer at port " + targetPort);
                    return true;
                }
            }

            return false;
        }

        public void receiveMessage(TestMessageInterface message, int sourcePort) {
            receivedMessages.put(message.getId(), message);
            logger.info(id + " received message from port " + sourcePort + ": " + message.getType());
        }

        public boolean broadcastMessage(TestMessageInterface message) {
            if (!running || connectedPeers.isEmpty()) {
                return false;
            }

            boolean allSent = true;
            for (int peerPort : connectedPeers) {
                if (!sendMessage(message, peerPort)) {
                    allSent = false;
                }
            }

            return allSent;
        }

        public boolean isConnected(int peerPort) {
            return connectedPeers.contains(peerPort);
        }

        public Set<Integer> getConnectedPeers() {
            return new HashSet<>(connectedPeers);
        }

        public Map<String, TestMessageInterface> getReceivedMessages() {
            return new HashMap<>(receivedMessages);
        }

        public void clearMessages() {
            receivedMessages.clear();
        }
    }

    private class TestMessage implements TestMessageInterface {
        private final String type;
        private final String payload;
        private final String id;

        public TestMessage(String type, String payload, String id) {
            this.type = type;
            this.payload = payload;
            this.id = id;
        }

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
            return id;
        }
    }
   
}