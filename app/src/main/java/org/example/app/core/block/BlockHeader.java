package org.example.app.core.block;

public class BlockHeader {
    private String parentHash;
    private String miner;
    private long timestamp;
    private long difficulty;
    private long number;
    private String[] transactions;

    public BlockHeader(String parentHash, String miner, long timestamp, long difficulty, long number, String[] transactions) {
        this.parentHash = parentHash;
        this.miner = miner;
        this.timestamp = timestamp;
        this.difficulty = difficulty;
        this.number = number;
        this.transactions = transactions;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(long difficulty) {
        this.difficulty = difficulty;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String[] getTransactions() {
        return transactions;
    }

    public void setTransactions(String[] transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "parentHash='" + parentHash + '\'' +
                ", miner='" + miner + '\'' +
                ", timestamp=" + timestamp +
                ", difficulty=" + difficulty +
                ", number=" + number +
                ", transactions=" + String.join(", ", transactions) +
                '}';
    }
}
