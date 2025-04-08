package org.example.app.core.block;

import java.util.List;
import java.util.Arrays;

public class Block {
    private String hash;
    private String parentHash;
    private String miner;
    private long timestamp;
    private long difficulty;
    private long number;
    private List<Transaction> transactions;
    private byte[] stateRoot;

    // Constructor
    public Block(String hash, String parentHash, String miner, long timestamp,
                 long difficulty, long number, List<Transaction> transactions) {
        this.hash = hash;
        this.parentHash = parentHash;
        this.miner = miner;
        this.timestamp = timestamp;
        this.difficulty = difficulty;
        this.number = number;
        this.transactions = transactions;
        this.stateRoot = null;
    }

    public static Block deserialize(byte[] blockData) {
        return null;
    }

    // Getters and Setters
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    @Override
    public String toString() {
        return "Block{" +
                "hash='" + hash + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", miner='" + miner + '\'' +
                ", timestamp=" + timestamp +
                ", difficulty=" + difficulty +
                ", number=" + number +
                ", transactions=[" +
                (transactions != null && !transactions.isEmpty() ?
                        transactions.get(0).toString() : "") +
                "], stateRoot=null}";
    }

    public byte[] serialize() {
        return null; 
    }
}