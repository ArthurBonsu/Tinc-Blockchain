// RPCRequest.java
package org.example.app.core.api.rpc;

public class RPCRequest {
    private String jsonrpc = "2.0";
    private String method;
    private Object[] params;
    private String id;

    public String getMethod() { return method; }
    public Object[] getParams() { return params; }
    public String getId() { return id; }
}