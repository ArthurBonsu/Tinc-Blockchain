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
        String blockHashParam = getQueryParam(query, "hash");
        String blockHeightParam = getQueryParam(query, "height");

        try {
            Block block = null;

            // Try to get block by hash
            if (blockHashParam != null) {
                block = findBlockByHash(blockHashParam);
            }
            // Try to get block by height
            else if (blockHeightParam != null) {
                try {
                    int blockHeight = Integer.parseInt(blockHeightParam);
                    block = getBlockByHeight(blockHeight);
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, "Invalid block height");
                    return;
                }
            }
            // If no parameters provided
            else {
                // Get the latest block or return an error
                block = blockchain.getLatestBlock();
            }

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

    // Helper method to find block by hash
    private Block findBlockByHash(String blockHash) {
        // Placeholder method - implement actual block lookup by hash
        // You'll need to add this method to your Blockchain class
        return null;
    }

    // Helper method to get block by height
    private Block getBlockByHeight(int height) {
        // Placeholder method - implement actual block retrieval by height
        // This might use a method like getBlockAtHeight() or similar
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