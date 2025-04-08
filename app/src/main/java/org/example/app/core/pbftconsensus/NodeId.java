//package org.tinc.app.core.pbftconsensus;
//
//public class NodeId {
//    private final int id;
//    private final int shardId;
//
//    public NodeId(int id, int shardId) {
//        this.id = id;
//        this.shardId = shardId;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public int getShardId() {
//        return shardId;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || getClass() != obj.getClass()) return false;
//        NodeId nodeId = (NodeId) obj;
//        return id == nodeId.id && shardId == nodeId.shardId;
//    }
//
//    @Override
//    public int hashCode() {
//        return 31 * id + shardId;
//    }
//
//    @Override
//    public String toString() {
//        return "NodeId{id=" + id + ", shardId=" + shardId + '}';
//    }
//
//}
//



package org.example.app.core.pbftconsensus;

/**
 * NodeId represents a unique identifier for a node in the PBFT network,
 * including both the node's ID and its shard ID for sharded environments.
 */
public class NodeId {
    private final int id;
    private final int shardId;

    /**
     * Constructor to initialize a NodeId.
     *
     * @param id      The unique ID of the node
     * @param shardId The shard ID to which the node belongs
     */
    public NodeId(int id, int shardId) {
        this.id = id;
        this.shardId = shardId;
    }

    /**
     * Gets the node ID.
     *
     * @return The node ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the shard ID.
     *
     * @return The shard ID
     */
    public int getShardId() {
        return shardId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NodeId nodeId = (NodeId) obj;
        return id == nodeId.id && shardId == nodeId.shardId;
    }

    @Override
    public int hashCode() {
        return 31 * id + shardId;
    }

    @Override
    public String toString() {
        return "NodeId{id=" + id + ", shardId=" + shardId + '}';
    }
}


