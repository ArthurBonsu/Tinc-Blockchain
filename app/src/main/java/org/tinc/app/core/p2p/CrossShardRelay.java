package org.tinc.app.core.p2p;

import java.util.HashMap;
import java.util.Map;
import org.tinc.consensus.pbft.PBFTMessage;

public class CrossShardRelay {
    private Map<Integer, RobustP2PManager> shardManagers = new HashMap<>();

    public void registerShard(int shardId, RobustP2PManager p2pManager) {
        shardManagers.put(shardId, p2pManager);
    }

    public void forwardMessage(int targetShard, PBFTMessage message) {
        if (shardManagers.containsKey(targetShard)) {
            System.out.println("Forwarding message to shard " + targetShard);
            shardManagers.get(targetShard).handleMessage(String.valueOf(message));
        } else {
            System.out.println("Shard " + targetShard + " is not registered.");
        }
    }
}


