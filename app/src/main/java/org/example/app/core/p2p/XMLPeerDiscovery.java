package org.example.app.core.p2p;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Class for peer discovery using XML
public class XMLPeerDiscovery {
    private final String xmlFilePath;

    public XMLPeerDiscovery(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    // Parse peers from XML
    public List<String> discoverPeers() throws Exception {
        List<String> peers = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlFilePath));

        NodeList peerNodes = doc.getElementsByTagName("peer");
        for (int i = 0; i < peerNodes.getLength(); i++) {
            Element peerElement = (Element) peerNodes.item(i);
            peers.add(peerElement.getTextContent());
        }
        return peers;
    }
}
