//package org.example.app.core.p2p;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//// Class for peer discovery using XML
//public class XMLPeerDiscovery {
//    private final String xmlFilePath;
//
//    public XMLPeerDiscovery(String xmlFilePath) {
//        this.xmlFilePath = xmlFilePath;
//    }
//
//    // Parse peers from XML
//    public List<String> discoverPeers() throws Exception {
//        List<String> peers = new ArrayList<>();
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse(new File(xmlFilePath));
//
//        NodeList peerNodes = doc.getElementsByTagName("peer");
//        for (int i = 0; i < peerNodes.getLength(); i++) {
//            Element peerElement = (Element) peerNodes.item(i);
//            peers.add(peerElement.getTextContent());
//        }
//        return peers;
//    }
//
//    public Map<String, List<String>> discoverPeersByShard() {
//    }
//
//    public List<String> discoverAllPeers() {
//    }
//}





package org.example.app.core.p2p;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XMLPeerDiscovery handles peer discovery using an XML configuration file.
 * It can read peer lists, organize peers by shards, and provide various
 * discovery mechanisms for P2P network initialization.
 */
public class XMLPeerDiscovery {
    private static final Logger logger = Logger.getLogger(XMLPeerDiscovery.class.getName());

    private String xmlFilePath;
    private final ReadWriteLock lock;
    private volatile Document cachedDocument;
    private volatile long lastLoadTime;
    private static final long CACHE_TIMEOUT = 60000; // 1 minute

    /**
     * Constructor for XMLPeerDiscovery.
     *
     * @param xmlFilePath Path to the XML file containing peer information
     * @throws IllegalArgumentException if xmlFilePath is null or empty
     */
    public XMLPeerDiscovery(String xmlFilePath) {
        if (xmlFilePath == null || xmlFilePath.isEmpty()) {
            throw new IllegalArgumentException("XML file path cannot be null or empty");
        }

        this.xmlFilePath = xmlFilePath;
        this.lock = new ReentrantReadWriteLock();
        this.lastLoadTime = 0;
    }

    /**
     * Default constructor.
     * Uses "peers.xml" as the default file path.
     */
    public XMLPeerDiscovery() {
        this("peers.xml");
    }

    /**
     * Set the XML file path.
     *
     * @param xmlFilePath The new file path
     * @throws IllegalArgumentException if xmlFilePath is null or empty
     */
    public void setXmlFilePath(String xmlFilePath) {
        if (xmlFilePath == null || xmlFilePath.isEmpty()) {
            throw new IllegalArgumentException("XML file path cannot be null or empty");
        }

        lock.writeLock().lock();
        try {
            this.xmlFilePath = xmlFilePath;
            // Invalidate cache
            this.cachedDocument = null;
            this.lastLoadTime = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Discover peers from the XML file.
     *
     * @return A list of peer addresses
     * @throws IOException if the XML file cannot be read
     * @throws ParserConfigurationException if the XML parser cannot be configured
     * @throws SAXException if the XML file is invalid
     * @throws FileNotFoundException if the XML file does not exist
     */
    public List<String> discoverPeers() throws IOException, ParserConfigurationException, SAXException {
        lock.readLock().lock();
        try {
            Document doc = getDocument();
            List<String> peers = new ArrayList<>();

            NodeList peerNodes = doc.getElementsByTagName("peer");
            for (int i = 0; i < peerNodes.getLength(); i++) {
                Element peerElement = (Element) peerNodes.item(i);
                String peerAddress = peerElement.getTextContent().trim();
                if (!peerAddress.isEmpty()) {
                    peers.add(peerAddress);
                }
            }

            logger.info("Discovered " + peers.size() + " peers from XML file: " + xmlFilePath);
            return peers;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Discover peers organized by shard from the XML file.
     *
     * @return A map of shard IDs to lists of peer addresses
     * @throws IOException if the XML file cannot be read
     * @throws ParserConfigurationException if the XML parser cannot be configured
     * @throws SAXException if the XML file is invalid
     * @throws FileNotFoundException if the XML file does not exist
     */
    public Map<String, List<String>> discoverPeersByShard() throws IOException, ParserConfigurationException, SAXException {
        lock.readLock().lock();
        try {
            Document doc = getDocument();
            Map<String, List<String>> shardPeers = new HashMap<>();

            // First check for shard-specific structure
            NodeList shardNodes = doc.getElementsByTagName("shard");

            if (shardNodes.getLength() > 0) {
                // XML has shard organization
                for (int i = 0; i < shardNodes.getLength(); i++) {
                    Element shardElement = (Element) shardNodes.item(i);
                    String shardId = shardElement.getAttribute("id");
                    if (shardId.isEmpty()) {
                        shardId = String.valueOf(i); // Use index as fallback
                    }

                    List<String> peers = new ArrayList<>();
                    NodeList peerNodes = shardElement.getElementsByTagName("peer");

                    for (int j = 0; j < peerNodes.getLength(); j++) {
                        Element peerElement = (Element) peerNodes.item(j);
                        String peerAddress = peerElement.getTextContent().trim();
                        if (!peerAddress.isEmpty()) {
                            peers.add(peerAddress);
                        }
                    }

                    shardPeers.put(shardId, peers);
                }
            } else {
                // No shard organization, try to get shard info from peer attributes
                NodeList peerNodes = doc.getElementsByTagName("peer");

                for (int i = 0; i < peerNodes.getLength(); i++) {
                    Element peerElement = (Element) peerNodes.item(i);
                    String shardId = peerElement.getAttribute("shard");

                    if (shardId.isEmpty()) {
                        shardId = "default"; // Use default shard
                    }

                    // Get or create the list for this shard
                    List<String> peers = shardPeers.computeIfAbsent(shardId, k -> new ArrayList<>());

                    String peerAddress = peerElement.getTextContent().trim();
                    if (!peerAddress.isEmpty()) {
                        peers.add(peerAddress);
                    }
                }
            }

            logger.info("Discovered peers from " + shardPeers.size() + " shards in XML file: " + xmlFilePath);
            return shardPeers;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Discover all peers from the XML file regardless of shard.
     *
     * @return A list of all peer addresses
     * @throws IOException if the XML file cannot be read
     * @throws ParserConfigurationException if the XML parser cannot be configured
     * @throws SAXException if the XML file is invalid
     * @throws FileNotFoundException if the XML file does not exist
     */
    public List<String> discoverAllPeers() throws IOException, ParserConfigurationException, SAXException {
        lock.readLock().lock();
        try {
            Document doc = getDocument();
            Set<String> allPeers = new HashSet<>(); // Use a set to avoid duplicates

            // Get peers from peer elements
            NodeList peerNodes = doc.getElementsByTagName("peer");
            for (int i = 0; i < peerNodes.getLength(); i++) {
                Element peerElement = (Element) peerNodes.item(i);
                String peerAddress = peerElement.getTextContent().trim();
                if (!peerAddress.isEmpty()) {
                    allPeers.add(peerAddress);
                }
            }

            // Also check alternate formats
            NodeList nodeNodes = doc.getElementsByTagName("node");
            for (int i = 0; i < nodeNodes.getLength(); i++) {
                Element nodeElement = (Element) nodeNodes.item(i);

                // Try different possible formats
                String addressAttr = nodeElement.getAttribute("address");
                if (!addressAttr.isEmpty()) {
                    allPeers.add(addressAttr);
                    continue;
                }

                String hostAttr = nodeElement.getAttribute("host");
                String portAttr = nodeElement.getAttribute("port");
                if (!hostAttr.isEmpty() && !portAttr.isEmpty()) {
                    allPeers.add(hostAttr + ":" + portAttr);
                    continue;
                }

                // Try to get from content
                String content = nodeElement.getTextContent().trim();
                if (!content.isEmpty()) {
                    allPeers.add(content);
                }
            }

            logger.info("Discovered " + allPeers.size() + " total peers from XML file: " + xmlFilePath);
            return new ArrayList<>(allPeers);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get or load the XML document.
     *
     * @return The parsed XML document
     * @throws IOException if the XML file cannot be read
     * @throws ParserConfigurationException if the XML parser cannot be configured
     * @throws SAXException if the XML file is invalid
     * @throws FileNotFoundException if the XML file does not exist
     */
    private Document getDocument() throws IOException, ParserConfigurationException, SAXException {
        long currentTime = System.currentTimeMillis();

        // Check if we have a valid cached document
        if (cachedDocument != null && (currentTime - lastLoadTime) < CACHE_TIMEOUT) {
            return cachedDocument;
        }

        // Need to load or reload the document
        lock.writeLock().lock();
        try {
            // Double-check after acquiring write lock
            if (cachedDocument != null && (currentTime - lastLoadTime) < CACHE_TIMEOUT) {
                return cachedDocument;
            }

            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists()) {
                throw new FileNotFoundException("XML file not found: " + xmlFilePath);
            }

            if (!xmlFile.canRead()) {
                throw new IOException("Cannot read XML file: " + xmlFilePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable external entity processing to prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            cachedDocument = doc;
            lastLoadTime = currentTime;

            logger.info("XML file loaded successfully: " + xmlFilePath);
            return doc;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Check if the XML file exists.
     *
     * @return true if the file exists and is readable, false otherwise
     */
    public boolean xmlFileExists() {
        File xmlFile = new File(xmlFilePath);
        return xmlFile.exists() && xmlFile.canRead();
    }

    /**
     * Get the current XML file path.
     *
     * @return The XML file path
     */
    public String getXmlFilePath() {
        lock.readLock().lock();
        try {
            return xmlFilePath;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get peer addresses for a specific shard.
     *
     * @param shardId The ID of the shard to get peers for
     * @return A list of peer addresses for the specified shard
     * @throws IOException if the XML file cannot be read
     * @throws ParserConfigurationException if the XML parser cannot be configured
     * @throws SAXException if the XML file is invalid
     * @throws FileNotFoundException if the XML file does not exist
     */
    public List<String> getPeersForShard(String shardId) throws IOException, ParserConfigurationException, SAXException {
        if (shardId == null || shardId.isEmpty()) {
            throw new IllegalArgumentException("Shard ID cannot be null or empty");
        }

        Map<String, List<String>> shardPeers = discoverPeersByShard();
        return shardPeers.getOrDefault(shardId, new ArrayList<>());
    }

    /**
     * Invalidate the document cache to force a reload on next access.
     */
    public void invalidateCache() {
        lock.writeLock().lock();
        try {
            cachedDocument = null;
            lastLoadTime = 0;
            logger.fine("XML document cache invalidated");
        } finally {
            lock.writeLock().unlock();
        }
    }
}