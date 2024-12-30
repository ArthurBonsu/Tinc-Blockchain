// RESTServer.java
package org.example.app.core.api.rest;

import com.sun.net.httpserver.HttpServer;
import org.example.app.core.consensus.Blockchain;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RESTServer {
    private final HttpServer server;
    private final Blockchain blockchain;
    private final int port;

    public RESTServer(int port, Blockchain blockchain) throws Exception {
        this.port = port;
        this.blockchain = blockchain;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.setExecutor(Executors.newFixedThreadPool(10));
        setupEndpoints();
    }

    private void setupEndpoints() {
        // Block endpoints
        server.createContext("/api/v1/block", new BlockHandler(blockchain));
        server.createContext("/api/v1/block/latest", new LatestBlockHandler(blockchain));
        
        // Transaction endpoints
        server.createContext("/api/v1/tx", new TransactionHandler(blockchain));
        server.createContext("/api/v1/tx/pending", new PendingTransactionHandler(blockchain));
        
        // Account endpoints
        server.createContext("/api/v1/account", new AccountHandler(blockchain));
    }

    public void start() {
        server.start();
        System.out.println("REST Server started on port " + port);
    }

    public void stop() {
        server.stop(0);
    }
}