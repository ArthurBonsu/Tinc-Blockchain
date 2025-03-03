package org.example.app.core.consensus;

import org.example.app.core.types.Hash;
import org.example.app.core.block.Block;
import org.example.app.core.block.BlockHeader; // Import BlockHeader directly
import org.example.app.core.block.Transaction; // Import Transaction
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

// Generic Hasher interface
interface Hasher<T> {
    Hash hash(T obj) throws Exception;
}

// BlockHasher implementation
class BlockHasher implements Hasher<BlockHeader> { // Change to use BlockHeader directly
    @Override
    public Hash hash(BlockHeader header) throws Exception { // Change parameter type
        byte[] headerBytes = header.toBytes();
        byte[] hash = sha256(headerBytes);
        return new Hash(hash);
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
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Adjust the size as necessary

        // Writing Transaction fields into the buffer
        buffer.put(tx.getData());
        buffer.put(tx.getToAddress().toSlice()); // Assuming PublicKey has a method to get encoded bytes
        buffer.putLong(tx.getValue());
        buffer.put(tx.getFromAddress().toSlice()); // Assuming PublicKey has a method to get encoded bytes
        buffer.putLong(tx.getNonce());

        // Perform the SHA-256 hash
        byte[] hash = sha256(Arrays.copyOf(buffer.array(), buffer.position())); // Only use filled bytes
        return new Hash(hash);
    }

    private byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }
}