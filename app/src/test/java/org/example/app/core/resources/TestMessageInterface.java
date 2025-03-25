
package org.example.app.core.resources;

/**
 * Interface for test messages used in P2P network testing
 */
public interface TestMessageInterface {
    /**
     * Get the message type
     * @return String representing message type
     */
    String getType();
    
    /**
     * Get the message payload
     * @return String containing the message payload
     */
    String getPayload();
    
    /**
     * Get the unique message ID
     * @return String containing the message ID
     */
    String getId();
}
