package org.example.app.core.eth;

import org.example.app.core.block.Block;

import java.util.ArrayList;
import java.util.List;

public class EthereumNode {

    private EthereumProtocol protocol;
    private List<EthereumNode> peers;

    public EthereumNode() {
        this.protocol = new EthereumProtocol();
        this.peers = new ArrayList<>();
    }

    // Add a new peer to the node's peer list
    public void addPeer(EthereumNode peer) {
        peers.add(peer);
    }

    // Share a new block with all peers
    public void shareBlockWithPeers(Block block) {
        for (EthereumNode peer : peers) {
            peer.receiveBlock(block);
        }
    }

    // Receive a new block from another node
    public void receiveBlock(Block block) {
        protocol.addBlock(block);
    }
}
