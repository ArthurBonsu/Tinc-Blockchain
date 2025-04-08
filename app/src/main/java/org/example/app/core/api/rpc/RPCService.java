// RPCService.java
package org.example.app.core.api.rpc;

import com.google.gson.Gson;
import org.example.app.core.consensus.Blockchain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RPCService {
    private final Blockchain blockchain;
    private final Gson gson;
    private final Map<String, RPCMethod> methods;

    public RPCService(Blockchain blockchain) {
        this.blockchain = blockchain;
        this.gson = new Gson();
        this.methods = new ConcurrentHashMap<>();
        registerMethods();
    }

    private void registerMethods() {
        methods.put("eth_getBalance", new GetBalanceMethod());
        methods.put("eth_getBlock", new GetBlockMethod());
        methods.put("eth_sendTransaction", new SendTransactionMethod());
        methods.put("eth_getTransactionCount", new GetTransactionCountMethod());
    }

    public String handleRequest(String requestJson) {
        RPCRequest request = gson.fromJson(requestJson, RPCRequest.class);
        RPCMethod method = methods.get(request.getMethod());
        
        if (method == null) {
            return createErrorResponse("Method not found", -32601);
        }

        try {
            Object result = method.execute(blockchain, request.getParams());
            return createSuccessResponse(result, request.getId());
        } catch (Exception e) {
            return createErrorResponse(e.getMessage(), -32000);
        }
    }

    private String createSuccessResponse(Object result, String id) {
        RPCResponse response = new RPCResponse(result, null, id);
        return gson.toJson(response);
    }

    private String createErrorResponse(String message, int code) {
        RPCError error = new RPCError(code, message);
        RPCResponse response = new RPCResponse(null, error, "");
        return gson.toJson(response);
    }
}