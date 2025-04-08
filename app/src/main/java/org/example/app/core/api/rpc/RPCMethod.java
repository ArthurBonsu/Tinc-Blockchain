// RPCMethod.java
package org.example.app.core.api.rpc;

import org.example.app.core.consensus.Blockchain;

public interface RPCMethod {
    Object execute(Blockchain blockchain, Object[] params) throws Exception;
}