//package org.tinc.core;
//
//import org.tinc.crypto.Keypair;
//import org.tinc.crypto.Keypair.SignatureResult;
//import org.tinc.types.Address;
//import org.tinc.types.Hash;
//
//import java.math.BigInteger;
//import java.nio.ByteBuffer;
//import java.security.MessageDigest;
//import java.security.PublicKey;
//import java.util.Arrays;
//import java.util.Random;
//
//public class Transaction implements java.io.Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    public enum TxType {
//        COLLECTION(0),
//        MINT(1);
//
//        private final int value;
//
//        TxType(int value) {
//            this.value = value;
//        }
//
//        public int getValue() {
//            return value;
//        }
//    }
//
//    public static class CollectionTx implements java.io.Serializable {
//        private static final long serialVersionUID = 1L;
//
//        private long fee;
//        private byte[] metaData;
//
//        public CollectionTx(long fee, byte[] metaData) {
//            this.fee = fee;
//            this.metaData = metaData;
//        }
//
//        public long getFee() {
//            return fee;
//        }
//
//        public byte[] getMetaData() {
//            return metaData;
//        }
//    }
//
//    public static class MintTx implements java.io.Serializable {
//        private static final long serialVersionUID = 1L;
//
//        private long fee;
//        private Hash nft;
//        private Hash collection;
//        private byte[] metaData;
//        private PublicKey collectionOwner;
//        private SignatureResult signature;
//
//        public MintTx(long fee, Hash nft, Hash collection, byte[] metaData, PublicKey collectionOwner, SignatureResult signature) {
//            this.fee = fee;
//            this.nft = nft;
//            this.collection = collection;
//            this.metaData = metaData;
//            this.collectionOwner = collectionOwner;
//            this.signature = signature;
//        }
//
//        public long getFee() {
//            return fee;
//        }
//
//        public Hash getNft() {
//            return nft;
//        }
//
//        public Hash getCollection() {
//            return collection;
//        }
//
//        public byte[] getMetaData() {
//            return metaData;
//        }
//
//        public PublicKey getCollectionOwner() {
//            return collectionOwner;
//        }
//
//        public SignatureResult getSignature() {
//            return signature;
//        }
//    }
//
//    private Object txInner; // Can hold CollectionTx or MintTx
//    private byte[] data;
//    public Address to;
//    private long value;
//    private Address from;
//    private SignatureResult signature;
//    private long nonce;
//    private Hash hash;
//
//    public Transaction(byte[] data) {
//        this.data = data;
//        this.nonce = new Random().nextLong();
//    }
//
//    public Hash calculateHash(Hasher<Transaction> hasher) throws Exception {
//        if (hash == null || hash.isZero()) {
//            hash = hasher.hash(this);
//        }
//        return hash;
//    }
//
//    public void sign(Keypair keypair) throws Exception {
//        Hash txHash = calculateHash(new TxHasher());
//        this.signature = keypair.sign(txHash.toSlice());
//        this.from = getAddressFromKey(keypair.getPublicKey());
//    }
//
//    public boolean verify(Keypair keypair) throws Exception {
//        if (this.signature == null) {
//            throw new IllegalStateException("Transaction has no signature");
//        }
//
//        Hash txHash = calculateHash(new TxHasher());
//        return keypair.verify(this.signature, txHash.toSlice());
//    }
//
//    public byte[] toBytes() {
//        ByteBuffer buffer = ByteBuffer.allocate(1024); // Adjust size as necessary
//        buffer.put(this.getData());
//        if (this.getToAddress() != null) {
//            buffer.put(this.getToAddress().toSlice()); // Address's byte slice
//        }
//        buffer.putLong(this.getValue());
//        if (this.getFromAddress() != null) {
//            buffer.put(this.getFromAddress().toSlice()); // Address's byte slice
//        }
//        buffer.putLong(this.getNonce());
//        return Arrays.copyOf(buffer.array(), buffer.position());
//    }
//
//    // Utility method to convert PublicKey to Address
//    public static Address getAddressFromKey(PublicKey key) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(key.getEncoded());
//            return new Address(Arrays.copyOf(hash, 20)); // Use the first 20 bytes
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to generate address: " + e.getMessage(), e);
//        }
//    }
//
//    // Setters and Getters
//    public void setTo(Address to) {
//        this.to = to;
//    }
//
//    public void setFrom(Address from) {
//        this.from = from;
//    }
//
//    public void setValue(BigInteger value) {
//        this.value = value;
//    }
//
//    public void setTxInner(Object txInner) {
//        this.txInner = txInner;
//    }
//
//    public Object getTxInner() {
//        return txInner;
//    }
//
//    public byte[] getData() {
//        return data;
//    }
//
//    public long getValue() {
//        return value;
//    }
//
//    public Address getToAddress() {
//        return to;
//    }
//
//    public long getNonce() {
//        return nonce;
//    }
//
//    public SignatureResult getSignature() {
//        return signature;
//    }
//
//    public Address getFromAddress() {
//        return from;
//    }
//
//    public Hash getHash() {
//        return hash;
//    }
//
//    // Static block for serialization registration if needed
//    static {
//        try {
//            System.out.println("Transaction class registered for serialization.");
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to register Transaction class for serialization: " + e.getMessage(), e);
//        }
//    }
//
//    static class TxHasher implements Hasher<Transaction> {
//        @Override
//        public Hash hash(Transaction tx) {
//            try {
//                MessageDigest digest = MessageDigest.getInstance("SHA-256");
//                digest.update(tx.toBytes());
//                return new Hash(digest.digest());
//            } catch (Exception e) {
//                throw new RuntimeException("Hashing failed: " + e.getMessage(), e);
//            }
//        }
//    }
//}

package org.tinc.core;

import org.tinc.crypto.Keypair;
import org.tinc.crypto.Keypair.SignatureResult;
import org.tinc.types.Address;
import org.tinc.types.Hash;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

public class Transaction implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public enum TxType {
        COLLECTION(0),
        MINT(1);

        private final int value;

        TxType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // Nested transaction types
    public static class CollectionTx implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private long fee;
        private byte[] metaData;

        public CollectionTx(long fee, byte[] metaData) {
            this.fee = fee;
            this.metaData = metaData;
        }

        public long getFee() {
            return fee;
        }

        public byte[] getMetaData() {
            return metaData;
        }
    }

    public static class MintTx implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private long fee;
        private Hash nft;
        private Hash collection;
        private byte[] metaData;
        private PublicKey collectionOwner;
        private SignatureResult signature;

        public MintTx(long fee, Hash nft, Hash collection, byte[] metaData, PublicKey collectionOwner, SignatureResult signature) {
            this.fee = fee;
            this.nft = nft;
            this.collection = collection;
            this.metaData = metaData;
            this.collectionOwner = collectionOwner;
            this.signature = signature;
        }

        public long getFee() {
            return fee;
        }

        public Hash getNft() {
            return nft;
        }

        public Hash getCollection() {
            return collection;
        }

        public byte[] getMetaData() {
            return metaData;
        }

        public PublicKey getCollectionOwner() {
            return collectionOwner;
        }

        public SignatureResult getSignature() {
            return signature;
        }
    }

    private Object txInner; // Can hold CollectionTx or MintTx
    private byte[] data;
    private Address to;
    private long value;
    private Address from;
    private SignatureResult signature;
    private long nonce;
    private Hash hash;
    private int gasLimit; // Gas limit for the transaction

    // Constructor
    public Transaction(byte[] data) {
        this.data = data;
        this.nonce = new Random().nextLong();
    }

    // Calculate the hash for the transaction
    public Hash calculateHash(Hasher<Transaction> hasher) throws Exception {
        if (hash == null || hash.isZero()) {
            hash = hasher.hash(this);
        }
        return hash;
    }

    // Sign the transaction with the keypair
    public void sign(Keypair keypair) throws Exception {
        Hash txHash = calculateHash(new TxHasher());
        this.signature = keypair.sign(txHash.toSlice());
        this.from = getAddressFromKey(keypair.getPublicKey());
    }

    // Verify the transaction signature
    public boolean verify(Keypair keypair) throws Exception {
        if (this.signature == null) {
            throw new IllegalStateException("Transaction has no signature");
        }
        Hash txHash = calculateHash(new TxHasher());
        return keypair.verify(this.signature, txHash.toSlice());
    }

    // Convert transaction to bytes for hashing
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(this.getData());
        if (this.getToAddress() != null) {
            buffer.put(this.getToAddress().toSlice());
        }
        buffer.putLong(this.getValue());
        if (this.getFromAddress() != null) {
            buffer.put(this.getFromAddress().toSlice());
        }
        buffer.putLong(this.getNonce());
        // Serialize BigInteger gasLimit as bytes
        int gasLimitBytes = this.gasLimit;
        buffer.putInt(gasLimitBytes); // Store the length of the byte array
        buffer.put((byte) gasLimitBytes); // Store the actual bytes

//
//        buffer.put(this.getGasLimit());
        return Arrays.copyOf(buffer.array(), buffer.position());
    }


    // Utility to derive Address from PublicKey
    public static Address getAddressFromKey(PublicKey key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getEncoded());
            return new Address(Arrays.copyOf(hash, 20)); // Use the first 20 bytes
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate address: " + e.getMessage(), e);
        }
    }

    // Getters and Setters for required fields
    public void setTo(Address to) {
        this.to = to;
    }

        public void setFrom(Address from) {
        this.from = from;
    }
    public void setToAddress(Address to) {
        this.to = to;
    }

    public Address getToAddress() {
        return to;
    }

    // New method to provide compatibility with MainEVM
    public Address getTo() {
        return getToAddress();
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void setGasLimit(int gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getValue() {
        return value;
    }

    public int getGasLimit() {
        return gasLimit;
    }

    public byte[] getData() {
        return data;
    }

    public long getNonce() {
        return nonce;
    }

    public Address getFromAddress() {
        return from;
    }

    public Object getTxInner() {
        return txInner;
    }

    public void setTxInner(Object txInner) {
        this.txInner = txInner;
    }

    public Hash getHash() {
        return hash;
    }

    public SignatureResult getSignature() {
        return signature;
    }

    static class TxHasher implements Hasher<Transaction> {
        @Override
        public Hash hash(Transaction tx) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(tx.toBytes());
                return new Hash(digest.digest());
            } catch (Exception e) {
                throw new RuntimeException("Hashing failed: " + e.getMessage(), e);
            }
        }
    }
}
