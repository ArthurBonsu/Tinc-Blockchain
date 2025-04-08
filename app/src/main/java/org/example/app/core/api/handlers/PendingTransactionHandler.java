package org.example.app.core.api.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.app.core.consensus.Blockchain;
import org.example.app.core.block.Transaction;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PendingTransactionHandler implements HttpHandler {
    private final Blockchain blockchain;
    private final Gson gson;

    public PendingTransactionHandler(Blockchain blockchain) {
        this.blockchain = blockchain;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Transaction> pendingTransactions = getPendingTransactions();
                
                String response = gson.toJson(pendingTransactions);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private List<Transaction> getPendingTransactions() {
        // Placeholder method - implement actual pending transaction retrieval logic
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}