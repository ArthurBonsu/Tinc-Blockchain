package org.example.app.core.internal;

import java.util.HashSet;
import java.util.Set;

public class NodeDiscovery {

    private Set<String> discoveredNodes;

    public NodeDiscovery() {
        this.discoveredNodes = new HashSet<>();
    }

    // Discover a new node in the network
    public void discoverNode(String nodeAddress) {
        discoveredNodes.add(nodeAddress);
    }

    // Get a list of all discovered nodes
    public Set<String> getDiscoveredNodes() {
        return discoveredNodes;
    }

    // Check if a node is already discovered
    public boolean isNodeDiscovered(String nodeAddress) {
        return discoveredNodes.contains(nodeAddress);
    }
}
