package org.example.app.core.p2p;

import java.util.List;
import java.util.Map;

public class PeerDiscoveryApp {
    public static void main(String[] args) {
        try {
            XMLPeerDiscovery peerDiscovery = new XMLPeerDiscovery("resources/peers.xml");

            // Discover peers by shard
            Map<String, List<String>> shardPeers = peerDiscovery.discoverPeersByShard();
            for (String shardId : shardPeers.keySet()) {
                System.out.println("Shard ID: " + shardId);
                for (String peer : shardPeers.get(shardId)) {
                    System.out.println("Peer: " + peer);
                }
                System.out.println();
            }

            // Discover all peers if needed
            List<String> allPeers = peerDiscovery.discoverAllPeers();
            System.out.println("All Peers: " + allPeers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


