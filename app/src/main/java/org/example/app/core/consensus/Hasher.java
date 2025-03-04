package org.example.app.core.consensus;

import org.example.app.core.types.Hash;
import org.example.app.core.block.Block;
import org.example.app.core.block.BlockHeader;
import org.example.app.core.block.Transaction;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.math.BigInteger;

// Generic Hasher interface
interface Hasher<T> {
    Hash hash(T obj) throws Exception;
}

// BlockHasher implementation
class BlockHasher implements Hasher<BlockHeader> {
    @Override
    public Hash hash(BlockHeader header) throws Exception {
        // Combine header properties to create a hash input
        String hashInput = header.getParentHash() +
                header.getMiner() +
                header.getTimestamp() +
                header.getDifficulty();

        byte[] hashBytes = sha256(hashInput.getBytes());
        return new Hash(hashBytes);
    }

    private byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }
}

// TxHasher implementation
class TxHasher implements Hasher<Transaction> {
    @Override
    public Hash hash(Transaction tx) throws Exception {
        // Create hash input from transaction properties
        String hashInput =
                (tx.getSender() != null ? tx.getSender() : "") +
                        (tx.getRecipient() != null ? tx.getRecipient() : "") +
                        (tx.getValue() != null ? tx.getValue().toString() : "0") +
                        (tx.getNonce() != null ? tx.getNonce().toString() : "0") +
                        (tx.getData() != null ? Arrays.toString(tx.getData()) : "");

        byte[] hashBytes = sha256(hashInput.getBytes());
        return new Hash(hashBytes);
    }

    private byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }
}