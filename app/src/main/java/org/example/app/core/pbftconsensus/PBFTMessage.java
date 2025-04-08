//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//
//public class PBFTMessage {
//    public static final int MESSAGE_TYPE_ID = 1001; // Unique ID for PBFT messages
//
//    private final String type;
//    private final int senderId;
//    private final String digest;
//    private final Keypair.SignatureResult signature;
//    private String content; // Added field for message content
//
//    public PBFTMessage(String type, int senderId, String digest, String signature) {
//        this.type = type;
//        this.senderId = senderId;
//        this.digest = digest;
//        this.signature = signature;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public int getSenderId() {
//        return senderId;
//    }
//
//    public String getDigest() {
//        return digest;
//    }
//
//    public Keypair.SignatureResult getSignature() {
//        return signature;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    @Override
//    public String toString() {
//        return "PBFTMessage{" +
//                "type='" + type + '\'' +
//                ", senderId=" + senderId +
//                ", digest='" + digest + '\'' +
//                ", content='" + content + '\'' +
//                ", signature=" + signature +
//                '}';
//    }
//
//    public String serialize() {
//    }
//}


package org.example.app.core.pbftconsensus;

import org.example.app.core.crypto.Keypair;
import java.util.Base64;

/**
 * Represents a message in the PBFT protocol.
 * This class handles the core message format used throughout the consensus process.
 */
public class PBFTMessage {
    public static final int MESSAGE_TYPE_ID = 1001; // Unique ID for PBFT messages

    private final String type;
    private final int senderId;
    private final String digest;
    private final Keypair.SignatureResult signature;
    private String content; // Message content

    /**
     * Constructor to initialize a PBFT message.
     *
     * @param type      The message type (e.g., "PRE-PREPARE", "PREPARE", "COMMIT")
     * @param senderId  The ID of the sending replica
     * @param digest    The message digest for integrity verification
     * @param signature The signature for message authentication
     */
    public PBFTMessage(String type, int senderId, String digest, Keypair.SignatureResult signature) {
        this.type = type;
        this.senderId = senderId;
        this.digest = digest;
        this.signature = signature;
    }

    /**
     * Gets the message type.
     *
     * @return The message type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the sender's ID.
     *
     * @return The sender's ID
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Gets the message digest.
     *
     * @return The message digest
     */
    public String getDigest() {
        return digest;
    }

    /**
     * Gets the message signature.
     *
     * @return The signature
     */
    public Keypair.SignatureResult getSignature() {
        return signature;
    }

    /**
     * Gets the message content.
     *
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the message content.
     *
     * @param content The content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Serializes the message for transmission over the network.
     *
     * @return The serialized message as a string
     */
    public String serialize() {
        StringBuilder serialized = new StringBuilder();
        serialized.append(type).append("|")
                .append(senderId).append("|")
                .append(digest).append("|");

        // Add content if present
        if (content != null) {
            serialized.append(content).append("|");
        } else {
            serialized.append("|");
        }

        // Add signature if present
        if (signature != null) {
            serialized.append(Base64.getEncoder().encodeToString(signature.getR().toByteArray()))
                    .append(":")
                    .append(Base64.getEncoder().encodeToString(signature.getS().toByteArray()));
        }

        return serialized.toString();
    }

    @Override
    public String toString() {
        return "PBFTMessage{" +
                "type='" + type + '\'' +
                ", senderId=" + senderId +
                ", digest='" + digest + '\'' +
                ", content='" + content + '\'' +
                ", signature=" + signature +
                '}';
    }
}