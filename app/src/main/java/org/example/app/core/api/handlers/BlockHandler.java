// BlockHandler.java
package org.example.app.core.api.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.app.core.consensus.Blockchain;
import org.example.app.core.block.Block;
import java.io.IOException;
import java.io.OutputStream;
import com.google.gson.Gson;

public class BlockHandler implements HttpHandler {
    private final Blockchain blockchain;
    private final Gson gson;

    public BlockHandler(Blockchain blockchain) {
        this.blockchain = blockchain;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetRequest(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String blockHash = getQueryParam(query, "hash");
        
        try {
            Block block = blockchain.getBlock(blockHash);
            if (block != null) {
                String response = gson.toJson(block);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "Block not found");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request: " + e.getMessage());
        }
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