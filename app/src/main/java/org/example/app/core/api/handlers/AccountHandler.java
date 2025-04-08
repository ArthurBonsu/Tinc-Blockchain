package org.example.app.core.api.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.app.core.consensus.Blockchain;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

public class AccountHandler implements HttpHandler {
    private final Blockchain blockchain;
    private final Gson gson;

    public AccountHandler(Blockchain blockchain) {
        this.blockchain = blockchain;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String address = getQueryParam(query, "address");

                if (address != null) {
                    AccountInfo accountInfo = getAccountInfo(address);
                    
                    if (accountInfo != null) {
                        String response = gson.toJson(accountInfo);
                        sendResponse(exchange, 200, response);
                    } else {
                        sendResponse(exchange, 404, "Account not found");
                    }
                } else {
                    sendResponse(exchange, 400, "Address is required");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private AccountInfo getAccountInfo(String address) {
        // Placeholder method - implement actual account info retrieval logic
        return new AccountInfo(address, BigInteger.ZERO, 0);
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

    // Inner class to represent account information
    public static class AccountInfo {
        private String address;
        private BigInteger balance;
        private long nonce;

        public AccountInfo(String address, BigInteger balance, long nonce) {
            this.address = address;
            this.balance = balance;
            this.nonce = nonce;
        }
    }
}