//package org.example.app.core.p2p;
//
//import java.util.HashMap;
//import java.util.Map;
//import org.example.app.core.pbftconsensus.PBFTMessage;
//
//
//public class CrossShardRelay {
//    private Map<Integer, RobustP2PManager> shardManagers = new HashMap<>();
//
//    public void registerShard(int shardId, RobustP2PManager p2pManager) {
//        shardManagers.put(shardId, p2pManager);
//    }
//
//    public void forwardMessage(int targetShard, PBFTMessage message) {
//        if (shardManagers.containsKey(targetShard)) {
//            System.out.println("Forwarding message to shard " + targetShard);
//            shardManagers.get(targetShard).handleMessage(String.valueOf(message));
//        } else {
//            System.out.println("Shard " + targetShard + " is not registered.");
//        }
//    }
//}
//
//





package org.example.app.core.p2p;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.app.core.pbftconsensus.PBFTMessage;


/**
 * CrossShardRelay handles message forwarding between different shards in a
 * sharded blockchain network.
 */
public class CrossShardRelay {
    private static final Logger logger = Logger.getLogger(CrossShardRelay.class.getName());

    private final Map<Integer, RobustP2PManager> shardManagers;
    private final ReadWriteLock shardLock;

    /**
     * Constructor for CrossShardRelay.
     */
    public CrossShardRelay() {
        this.shardManagers = new ConcurrentHashMap<>();
        this.shardLock = new ReentrantReadWriteLock();
    }

    /**
     * Registers a P2P manager for a specific shard.
     *
     * @param shardId The ID of the shard
     * @param p2pManager The P2P manager for the shard
     * @throws IllegalArgumentException if p2pManager is null
     */
    public void registerShard(int shardId, RobustP2PManager p2pManager) {
        if (p2pManager == null) {
            throw new IllegalArgumentException("P2P manager cannot be null");
        }

        shardLock.writeLock().lock();
        try {
            shardManagers.put(shardId, p2pManager);
            logger.info("Registered P2P manager for shard: " + shardId);
        } finally {
            shardLock.writeLock().unlock();
        }
    }

    /**
     * Unregisters a P2P manager for a specific shard.
     *
     * @param shardId The ID of the shard to unregister
     * @return true if the shard was registered and is now unregistered, false otherwise
     */
    public boolean unregisterShard(int shardId) {
        shardLock.writeLock().lock();
        try {
            RobustP2PManager removed = shardManagers.remove(shardId);
            if (removed != null) {
                logger.info("Unregistered P2P manager for shard: " + shardId);
                return true;
            }
            return false;
        } finally {
            shardLock.writeLock().unlock();
        }
    }

    /**
     * Forwards a PBFT message to the target shard.
     *
     * @param targetShard The ID of the target shard
     * @param message The PBFT message to forward
     * @throws IllegalArgumentException if message is null
     */
    public void forwardMessage(int targetShard, PBFTMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        shardLock.readLock().lock();
        try {
            RobustP2PManager targetManager = shardManagers.get(targetShard);
            if (targetManager != null) {
                // Serialize the message for forwarding
                String serializedMessage = message.serialize();

                targetManager.handleMessage(serializedMessage);
                logger.info("Forwarded message to shard " + targetShard);
            } else {
                logger.warning("Shard " + targetShard + " is not registered");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error forwarding message to shard " + targetShard, e);
        } finally {
            shardLock.readLock().unlock();
        }
    }

    /**
     * Broadcasts a PBFT message to all registered shards.
     *
     * @param message The PBFT message to broadcast
     * @param originShard The originating shard ID (to prevent loops)
     * @throws IllegalArgumentException if message is null
     */
    public void broadcastToAllShards(PBFTMessage message, int originShard) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        shardLock.readLock().lock();
        try {
            // Serialize the message once for efficiency
            String serializedMessage = message.serialize();

            for (Map.Entry<Integer, RobustP2PManager> entry : shardManagers.entrySet()) {
                int shardId = entry.getKey();

                // Skip the originating shard to prevent loops
                if (shardId != originShard) {
                    try {
                        RobustP2PManager manager = entry.getValue();
                        manager.handleMessage(serializedMessage);
                        logger.info("Broadcasted message to shard " + shardId);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error broadcasting to shard " + shardId, e);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error broadcasting message to shards", e);
        } finally {
            shardLock.readLock().unlock();
        }
    }

    /**
     * Checks if a shard is registered.
     *
     * @param shardId The ID of the shard to check
     * @return true if the shard is registered, false otherwise
     */
    public boolean isShardRegistered(int shardId) {
        shardLock.readLock().lock();
        try {
            return shardManagers.containsKey(shardId);
        } finally {
            shardLock.readLock().unlock();
        }
    }

    /**
     * Gets the number of registered shards.
     *
     * @return The number of registered shards
     */
    public int getRegisteredShardCount() {
        shardLock.readLock().lock();
        try {
            return shardManagers.size();
        } finally {
            shardLock.readLock().unlock();
        }
    }

    /**
     * Gets a map of all registered shard IDs.
     *
     * @return A copy of the registered shard IDs map
     */
    public Map<Integer, RobustP2PManager> getRegisteredShards() {
        shardLock.readLock().lock();
        try {
            return new HashMap<>(shardManagers);
        } finally {
            shardLock.readLock().unlock();
        }
    }

    /**
     * Clears all registered shards.
     */
    public void clearAllShards() {
        shardLock.writeLock().lock();
        try {
            shardManagers.clear();
            logger.info("Cleared all registered shards");
        } finally {
            shardLock.writeLock().unlock();
        }
    }
}