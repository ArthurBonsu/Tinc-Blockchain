// RPCResponse.java
package org.example.app.core.api.rpc;

public class RPCResponse {
    private String jsonrpc = "2.0";
    private Object result;
    private RPCError error;
    private String id;

    public RPCResponse(Object result, RPCError error, String id) {
        this.result = result;
        this.error = error;
        this.id = id;
    }
}