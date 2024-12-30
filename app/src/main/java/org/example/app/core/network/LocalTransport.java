package org.example.app.core.network;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocalTransport {

    private final InetSocketAddress addr;
    private final BlockingQueue<RPC> consumeCh;
    private final Map<InetSocketAddress, LocalTransport> peers;
    private final ReentrantReadWriteLock lock;

    public LocalTransport(InetSocketAddress addr) {
        this.addr = addr;
        this.consumeCh = new LinkedBlockingQueue<>(1024);
        this.peers = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public BlockingQueue<RPC> consume() {
        return consumeCh;
    }

    public void connect(LocalTransport transport) {
        lock.writeLock().lock();
        try {
            peers.put(transport.getAddr(), transport);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void sendMessage(InetSocketAddress to, byte[] payload) throws Exception {
        lock.readLock().lock();
        try {
            if (addr.equals(to)) {
                return;
            }

            LocalTransport peer = peers.get(to);
            if (peer == null) {
                throw new Exception(addr + ": could not send message to unknown peer " + to);
            }

            peer.consumeCh.put(new RPC(addr, new ByteArrayInputStream(payload)));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void broadcast(byte[] payload) throws Exception {
        lock.readLock().lock();
        try {
            for (LocalTransport peer : peers.values()) {
                sendMessage(peer.getAddr(), payload);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public InetSocketAddress getAddr() {
        return addr;
    }

    public static class RPC {
        private final InetSocketAddress from;
        private final ByteArrayInputStream payload;

        public RPC(InetSocketAddress from, ByteArrayInputStream payload) {
            this.from = from;
            this.payload = payload;
        }

        public InetSocketAddress getFrom() {
            return from;
        }

        public ByteArrayInputStream getPayload() {
            return payload;
        }
    }
}
