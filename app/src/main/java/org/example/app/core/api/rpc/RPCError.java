// RPCError.java
package org.example.app.core.api.rpc;

public class RPCError {
    private int code;
    private String message;

    public RPCError(int code, String message) {
        this.code = code;
        this.message = message;
    }
}