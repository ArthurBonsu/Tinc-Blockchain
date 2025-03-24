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
