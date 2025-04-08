package org.example.app.core.api.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.app.core.consensus.Blockchain;
import org.example.app.core.block.Transaction;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;

public class TransactionHandler implements HttpHandler {
    private final Blockchain blockchain;
    private final Gson gson;

    public TransactionHandler(Blockchain blockchain) {
        this.blockchain = blockchain;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String txHash = getQueryParam(query, "hash");

                if (txHash != null) {
                    Transaction tx = findTransaction(txHash);
                    if (tx != null) {
                        String response = gson.toJson(tx);
                        sendResponse(exchange, 200, response);
                    } else {
                        sendResponse(exchange, 404, "Transaction not found");
                    }
                } else {
                    sendResponse(exchange, 400, "Transaction hash is required");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private Transaction findTransaction(String txHash) {
        // Placeholder method - implement actual transaction lookup logic
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String getQueryParam(String query, String param) {
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2 && param.equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }
}