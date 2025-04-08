//package org.example.app.core.network;
//
//import java.io.ByteArrayInputStream;
//import java.net.InetSocketAddress;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//public class LocalTransport {
//
//    private final InetSocketAddress addr;
//    private final BlockingQueue<RPC> consumeCh;
//    private final Map<InetSocketAddress, LocalTransport> peers;
//    private final ReentrantReadWriteLock lock;
//
//    public LocalTransport(InetSocketAddress addr) {
//        this.addr = addr;
//        this.consumeCh = new LinkedBlockingQueue<>(1024);
//        this.peers = new HashMap<>();
//        this.lock = new ReentrantReadWriteLock();
//    }
//
//    public BlockingQueue<RPC> consume() {
//        return consumeCh;
//    }
//
//    public void connect(LocalTransport transport) {
//        lock.writeLock().lock();
//        try {
//            peers.put(transport.getAddr(), transport);
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public void sendMessage(InetSocketAddress to, byte[] payload) throws Exception {
//        lock.readLock().lock();
//        try {
//            if (addr.equals(to)) {
//                return;
//            }
//
//            LocalTransport peer = peers.get(to);
//            if (peer == null) {
//                throw new Exception(addr + ": could not send message to unknown peer " + to);
//            }
//
//            peer.consumeCh.put(new RPC(addr, new ByteArrayInputStream(payload)));
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public void broadcast(byte[] payload) throws Exception {
//        lock.readLock().lock();
//        try {
//            for (LocalTransport peer : peers.values()) {
//                sendMessage(peer.getAddr(), payload);
//            }
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public InetSocketAddress getAddr() {
//        return addr;
//    }
//
//    public static class RPC {
//        private final InetSocketAddress from;
//        private final ByteArrayInputStream payload;
//
//        public RPC(InetSocketAddress from, ByteArrayInputStream payload) {
//            this.from = from;
//            this.payload = payload;
//        }
//
//        public InetSocketAddress getFrom() {
//            return from;
//        }
//
//        public ByteArrayInputStream getPayload() {
//            return payload;
//        }
//    }
//}





package org.example.app.core.network;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LocalTransport provides an in-memory transport implementation for testing
 * or simulating network communication without actual network operations.
 */
public class LocalTransport implements Closeable {
    private static final Logger logger = Logger.getLogger(LocalTransport.class.getName());

    // Default queue capacity
    private static final int DEFAULT_QUEUE_CAPACITY = 1024;
    // Default timeout in milliseconds
    private static final long DEFAULT_TIMEOUT_MS = 5000;

    private final InetSocketAddress addr;
    private final BlockingQueue<RPC> consumeCh;
    private final Map<InetSocketAddress, LocalTransport> peers;
    private final ReentrantReadWriteLock lock;
    private volatile boolean running;

    /**
     * Creates a new LocalTransport with the specified address and default queue capacity.
     *
     * @param addr The address this transport is bound to
     * @throws IllegalArgumentException if addr is null
     */
    public LocalTransport(InetSocketAddress addr) {
        this(addr, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Creates a new LocalTransport with the specified address and queue capacity.
     *
     * @param addr The address this transport is bound to
     * @param queueCapacity The capacity of the message queue
     * @throws IllegalArgumentException if addr is null or queueCapacity is negative
     */
    public LocalTransport(InetSocketAddress addr, int queueCapacity) {
        if (addr == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be positive");
        }

        this.addr = addr;
        this.consumeCh = new LinkedBlockingQueue<>(queueCapacity);
        this.peers = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.running = true;

        logger.info("LocalTransport created at " + addr);
    }

    /**
     * Gets the message queue for consuming incoming messages.
     *
     * @return The blocking queue containing incoming messages
     */
    public BlockingQueue<RPC> consume() {
        return consumeCh;
    }

    /**
     * Attempts to take a message from the queue with a timeout.
     *
     * @param timeoutMs Timeout in milliseconds
     * @return The received RPC message or null if timeout occurs
     * @throws InterruptedException if interrupted while waiting
     */
    public RPC receiveWithTimeout(long timeoutMs) throws InterruptedException {
        if (!running) {
            logger.warning("Attempted to receive message on stopped transport: " + addr);
            return null;
        }

        return consumeCh.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Establishes a two-way connection between this transport and another.
     *
     * @param transport The other transport to connect with
     * @throws IllegalArgumentException if transport is null
     */
    public void connect(LocalTransport transport) {
        if (transport == null) {
            throw new IllegalArgumentException("Transport cannot be null");
        }
        if (transport == this) {
            logger.warning("Attempted to connect transport to itself: " + addr);
            return;
        }

        lock.writeLock().lock();
        try {
            peers.put(transport.getAddr(), transport);
            // Create reciprocal connection
            transport.connectOneWay(this);
            logger.fine("Connected " + addr + " to " + transport.getAddr());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Establishes a one-way connection to another transport.
     * This is used internally by connect() to create reciprocal connections.
     *
     * @param transport The transport to connect to
     */
    private void connectOneWay(LocalTransport transport) {
        if (transport == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            peers.put(transport.getAddr(), transport);
            logger.fine("One-way connection added from " + addr + " to " + transport.getAddr());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Disconnects from a specific peer.
     *
     * @param peerAddr The address of the peer to disconnect from
     * @return true if disconnection was successful, false if the peer wasn't connected
     */
    public boolean disconnect(InetSocketAddress peerAddr) {
        if (peerAddr == null) {
            return false;
        }

        lock.writeLock().lock();
        try {
            LocalTransport peer = peers.remove(peerAddr);
            if (peer != null) {
                // Remove reciprocal connection if it exists
                peer.disconnectOneWay(addr);
                logger.info("Disconnected " + addr + " from " + peerAddr);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a one-way connection from this transport.
     * This is used internally by disconnect() to handle reciprocal disconnection.
     *
     * @param peerAddr The address of the peer to disconnect
     */
    private void disconnectOneWay(InetSocketAddress peerAddr) {
        if (peerAddr == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            peers.remove(peerAddr);
            logger.fine("One-way disconnection from " + addr + " to " + peerAddr);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Sends a message to a specific peer.
     *
     * @param to The address of the recipient
     * @param payload The message payload
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws IllegalStateException if the transport is not running
     * @throws InterruptedException if interrupted while placing message in queue
     * @throws NetworkException if the peer is not found or message delivery fails
     */
    public void sendMessage(InetSocketAddress to, byte[] payload) throws InterruptedException, NetworkException {
        if (!running) {
            throw new IllegalStateException("Transport is stopped: " + addr);
        }
        if (to == null) {
            throw new IllegalArgumentException("Destination address cannot be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Message payload cannot be null");
        }

        // Avoid sending to self
        if (addr.equals(to)) {
            logger.fine("Ignoring message sent to self: " + addr);
            return;
        }

        lock.readLock().lock();
        try {
            LocalTransport peer = peers.get(to);
            if (peer == null) {
                throw new NetworkException("Could not send message from " + addr +
                        " to unknown peer " + to);
            }

            // Try to deliver with timeout
            boolean delivered = peer.deliverMessage(addr, payload, DEFAULT_TIMEOUT_MS);
            if (!delivered) {
                throw new NetworkException("Message queue full or delivery timeout from " +
                        addr + " to " + to);
            }

            logger.fine("Message sent from " + addr + " to " + to);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Delivers a message to this transport's queue.
     *
     * @param from The sender's address
     * @param payload The message payload
     * @param timeoutMs Timeout in milliseconds
     * @return true if delivery was successful, false if timeout occurred
     * @throws InterruptedException if interrupted while waiting
     */
    private boolean deliverMessage(InetSocketAddress from, byte[] payload, long timeoutMs)
            throws InterruptedException {
        if (!running) {
            return false;
        }

        RPC rpc = new RPC(from, new ByteArrayInputStream(payload));
        return consumeCh.offer(rpc, timeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Broadcasts a message to all connected peers.
     *
     * @param payload The message payload
     * @throws IllegalArgumentException if payload is null
     * @throws IllegalStateException if the transport is stopped
     * @throws NetworkException if any broadcast operation fails
     */
    public void broadcast(byte[] payload) throws NetworkException {
        if (!running) {
            throw new IllegalStateException("Transport is stopped: " + addr);
        }
        if (payload == null) {
            throw new IllegalArgumentException("Broadcast payload cannot be null");
        }

        lock.readLock().lock();
        try {
            for (Map.Entry<InetSocketAddress, LocalTransport> entry : peers.entrySet()) {
                try {
                    // Use sendMessage for each peer
                    sendMessage(entry.getKey(), payload);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new NetworkException("Broadcast interrupted from " + addr, e);
                }
            }
            logger.fine("Broadcast from " + addr + " to " + peers.size() + " peers");
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the number of connected peers.
     *
     * @return The peer count
     */
    public int getPeerCount() {
        lock.readLock().lock();
        try {
            return peers.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if this transport is connected to a specific peer.
     *
     * @param peerAddr The address of the peer to check
     * @return true if connected, false otherwise
     */
    public boolean isConnectedTo(InetSocketAddress peerAddr) {
        if (peerAddr == null) {
            return false;
        }

        lock.readLock().lock();
        try {
            return peers.containsKey(peerAddr);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the address this transport is bound to.
     *
     * @return The transport's address
     */
    public InetSocketAddress getAddr() {
        return addr;
    }

    /**
     * Stops this transport and disconnects from all peers.
     */
    @Override
    public void close() {
        if (!running) {
            return;
        }

        running = false;

        // Copy peer addresses to avoid modification during iteration
        InetSocketAddress[] peerAddresses;
        lock.readLock().lock();
        try {
            peerAddresses = peers.keySet().toArray(new InetSocketAddress[0]);
        } finally {
            lock.readLock().unlock();
        }

        // Disconnect from all peers
        for (InetSocketAddress peerAddr : peerAddresses) {
            disconnect(peerAddr);
        }

        // Clear the message queue
        consumeCh.clear();

        logger.info("LocalTransport closed: " + addr);
    }

    /**
     * Checks if this transport is running.
     *
     * @return true if running, false if stopped
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Represents an RPC (Remote Procedure Call) message received from a peer.
     */
    public static class RPC {
        private final InetSocketAddress from;
        private final ByteArrayInputStream payload;
        private final long receivedTime;

        /**
         * Creates a new RPC message.
         *
         * @param from The sender's address
         * @param payload The message payload
         */
        public RPC(InetSocketAddress from, ByteArrayInputStream payload) {
            this.from = from;
            this.payload = payload;
            this.receivedTime = System.currentTimeMillis();
        }

        /**
         * Gets the sender's address.
         *
         * @return The sender's address
         */
        public InetSocketAddress getFrom() {
            return from;
        }

        /**
         * Gets the message payload.
         *
         * @return The payload input stream
         */
        public ByteArrayInputStream getPayload() {
            return payload;
        }

        /**
         * Gets the time when this message was received.
         *
         * @return The received timestamp in milliseconds
         */
        public long getReceivedTime() {
            return receivedTime;
        }

        /**
         * Resets the payload stream to the beginning.
         */
        public void resetPayload() {
            if (payload != null) {
                payload.reset();
            }
        }
    }

    /**
     * Exception thrown when network operations fail.
     */
    public static class NetworkException extends Exception {
        public NetworkException(String message) {
            super(message);
        }

        public NetworkException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

