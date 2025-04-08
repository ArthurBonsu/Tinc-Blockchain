package org.example.app.core.api.rpc;
import org.example.app.core.consensus.Blockchain;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class RPCServer {
    private final HttpServer server;
    private final Blockchain blockchain;

    public RPCServer(int port, Blockchain blockchain) throws Exception {
        this.blockchain = blockchain;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        setupEndpoints();
    }

    private void setupEndpoints() {
        server.createContext("/getBlock", (exchange -> {
            String response = "Block data";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }));
    }

    public void start() {
        server.start();
    }
}