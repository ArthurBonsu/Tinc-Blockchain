//package org.tinc.consensus.pbft;
//
//import org.tinc.crypto.Keypair;
//import org.tinc.crypto.Keypair.SignatureResult;
//import java.util.Base64;
//
///**
// * PBFTMessageHandler handles encoding and decoding of PBFT-specific messages for P2P transport,
// * ensuring integrity, format validation, and optional message batching for optimization.
// */
//public class PBFTMessageHandler {
//
//    /**
//     * Encodes a PBFTMessage into a format suitable for P2P transmission.
//     *
//     * @param pbftMessage The PBFTMessage to encode.
//     * @return A serialized string representation of the PBFTMessage.
//     */
//    public String encodePBFTMessage(PBFTMessage pbftMessage) {
//        StringBuilder serializedMessage = new StringBuilder();
//        serializedMessage.append(pbftMessage.getType()).append("|")
//                .append(pbftMessage.getSenderId()).append("|")
//                .append(pbftMessage.getDigest()).append("|")
//                .append(serializeSignature(pbftMessage.getSignature()));
//
//        System.out.println("Encoded PBFT message: " + serializedMessage);
//        return serializedMessage.toString();
//    }
//
//    /**
//     * Decodes a serialized PBFTMessage back into a PBFTMessage object.
//     *
//     * @param serializedMessage The serialized string representation of a PBFTMessage.
//     * @return The decoded PBFTMessage object.
//     */
//    public PBFTMessage decodePBFTMessage(String serializedMessage) {
//        String[] parts = serializedMessage.split("\\|");
//        if (parts.length < 4) {
//            throw new IllegalArgumentException("Invalid PBFT message format.");
//        }
//
//        String type = parts[0];
//        int senderId = Integer.parseInt(parts[1]);
//        String digest = parts[2];
//        SignatureResult signature = deserializeSignature(parts[3]);
//
//        PBFTMessage decodedMessage = new PBFTMessage(type, senderId, digest, signature);
//        System.out.println("Decoded PBFT message: " + decodedMessage);
//        return decodedMessage;
//    }
//
//    /**
//     * Validates the integrity and correctness of a PBFTMessage.
//     *
//     * @param message The PBFTMessage to validate.
//     * @param keypair The Keypair object for signature verification.
//     * @return True if the message is valid, false otherwise.
//     */
//    public boolean validateMessage(PBFTMessage message, Keypair keypair) {
//        byte[] dataToVerify = (message.getType() + message.getSenderId() + message.getDigest()).getBytes();
//        boolean isValid = keypair.verify(message.getSignature(), dataToVerify);
//
//        System.out.println("Message validation result for sender " + message.getSenderId() + ": " + isValid);
//        return isValid;
//    }
//
//    /**
//     * Serializes a SignatureResult into a Base64-encoded string for transport.
//     *
//     * @param signature The SignatureResult to serialize.
//     * @return A Base64-encoded string representation of the signature.
//     */
//    private String serializeSignature(SignatureResult signature) {
//        if (signature == null) return "";
//        return Base64.getEncoder().encodeToString(signature.getR().toByteArray()) + ":" +
//                Base64.getEncoder().encodeToString(signature.getS().toByteArray());
//    }
//
//    /**
//     * Deserializes a Base64-encoded string back into a SignatureResult object.
//     *
//     * @param serializedSignature The Base64-encoded string representation of a signature.
//     * @return The deserialized SignatureResult object.
//     */
//    private SignatureResult deserializeSignature(String serializedSignature) {
//        if (serializedSignature == null || serializedSignature.isEmpty()) return null;
//
//        String[] parts = serializedSignature.split(":");
//        if (parts.length != 2) {
//            throw new IllegalArgumentException("Invalid signature format.");
//        }
//
//        byte[] rBytes = Base64.getDecoder().decode(parts[0]);
//        byte[] sBytes = Base64.getDecoder().decode(parts[1]);
//        return new SignatureResult(new java.math.BigInteger(rBytes), new java.math.BigInteger(sBytes));
//    }
//
//    /**
//     * Batches multiple PBFTMessages into a single serialized string for performance optimization.
//     *
//     * @param messages An array of PBFTMessage objects to batch.
//     * @return A serialized string representing the batched messages.
//     */
//    public String batchMessages(PBFTMessage[] messages) {
//        StringBuilder batchedMessages = new StringBuilder();
//        for (PBFTMessage message : messages) {
//            batchedMessages.append(encodePBFTMessage(message)).append("\n");
//        }
//        System.out.println("Batched messages: " + batchedMessages);
//        return batchedMessages.toString();
//    }
//
//    /**
//     * Splits a batch of serialized messages back into individual PBFTMessage objects.
//     *
//     * @param batchedMessages The serialized string representing the batched messages.
//     * @return An array of decoded PBFTMessage objects.
//     */
//    public PBFTMessage[] unbatchMessages(String batchedMessages) {
//        String[] messageLines = batchedMessages.split("\n");
//        PBFTMessage[] messages = new PBFTMessage[messageLines.length];
//
//        for (int i = 0; i < messageLines.length; i++) {
//            messages[i] = decodePBFTMessage(messageLines[i]);
//        }
//        System.out.println("Unbatched messages: " + messages.length);
//        return messages;
//    }
//}



package org.example.app.core.pbftconsensus;
import org.example.app.core.crypto.Keypair;
import org.example.app.core.crypto.Keypair.SignatureResult;
import java.util.Base64;
import java.math.BigInteger;

/**
 * PBFTMessageHandler handles encoding and decoding of PBFT-specific messages for P2P transport,
 * ensuring integrity, format validation, and optional message batching for optimization.
 */
public class PBFTMessageHandler {

    /**
     * Encodes a PBFTMessage into a format suitable for P2P transmission.
     *
     * @param pbftMessage The PBFTMessage to encode.
     * @return A serialized string representation of the PBFTMessage.
     */
    public String encodePBFTMessage(PBFTMessage pbftMessage) {
        if (pbftMessage == null) {
            throw new IllegalArgumentException("Cannot encode null message");
        }

        return pbftMessage.serialize();
    }

    /**
     * Decodes a serialized PBFTMessage back into a PBFTMessage object.
     *
     * @param serializedMessage The serialized string representation of a PBFTMessage.
     * @return The decoded PBFTMessage object.
     * @throws IllegalArgumentException If the message format is invalid
     */
    public PBFTMessage decodePBFTMessage(String serializedMessage) {
        if (serializedMessage == null || serializedMessage.isEmpty()) {
            throw new IllegalArgumentException("Serialized message cannot be null or empty");
        }

        String[] parts = serializedMessage.split("\\|");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid PBFT message format: insufficient parts");
        }

        String type = parts[0];
        int senderId;
        try {
            senderId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid sender ID format", e);
        }
        String digest = parts[2];
        String content = parts.length > 3 ? parts[3] : null;
        SignatureResult signature = null;

        // Check for signature part
        if (parts.length > 4 && !parts[4].isEmpty()) {
            signature = deserializeSignature(parts[4]);
        }

        PBFTMessage decodedMessage = new PBFTMessage(type, senderId, digest, signature);
        if (content != null && !content.isEmpty()) {
            decodedMessage.setContent(content);
        }

        System.out.println("Decoded PBFT message: " + decodedMessage);
        return decodedMessage;
    }

    /**
     * Validates the integrity and correctness of a PBFTMessage.
     *
     * @param message The PBFTMessage to validate.
     * @param keypair The Keypair object for signature verification.
     * @return True if the message is valid, false otherwise.
     */
    public boolean validateMessage(PBFTMessage message, Keypair keypair) {
        if (message == null || keypair == null) {
            return false;
        }

        // If no signature is present, we can't validate authenticity
        if (message.getSignature() == null) {
            // Some PBFT implementations might allow unsigned messages in certain contexts
            // Here we're being strict and requiring signatures
            return false;
        }

        // Create the data that was originally signed
        String dataString = message.getType() + message.getSenderId() + message.getDigest();
        if (message.getContent() != null) {
            dataString += message.getContent();
        }

        byte[] dataToVerify = dataString.getBytes();
        boolean isValid = keypair.verify(message.getSignature(), dataToVerify);

        System.out.println("Message validation result for sender " + message.getSenderId() + ": " + isValid);
        return isValid;
    }

    /**
     * Deserializes a Base64-encoded string back into a SignatureResult object.
     *
     * @param serializedSignature The Base64-encoded string representation of a signature.
     * @return The deserialized SignatureResult object.
     * @throws IllegalArgumentException If the signature format is invalid
     */
    private SignatureResult deserializeSignature(String serializedSignature) {
        if (serializedSignature == null || serializedSignature.isEmpty()) {
            return null;
        }

        String[] parts = serializedSignature.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid signature format: expected r:s format");
        }

        try {
            byte[] rBytes = Base64.getDecoder().decode(parts[0]);
            byte[] sBytes = Base64.getDecoder().decode(parts[1]);
            return new SignatureResult(new BigInteger(rBytes), new BigInteger(sBytes));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid signature encoding", e);
        }
    }

    /**
     * Batches multiple PBFTMessages into a single serialized string for performance optimization.
     *
     * @param messages An array of PBFTMessage objects to batch.
     * @return A serialized string representing the batched messages.
     * @throws IllegalArgumentException If the messages array is null or empty
     */
    public String batchMessages(PBFTMessage[] messages) {
        if (messages == null || messages.length == 0) {
            throw new IllegalArgumentException("Messages array cannot be null or empty");
        }

        StringBuilder batchedMessages = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            PBFTMessage message = messages[i];
            if (message == null) {
                throw new IllegalArgumentException("Message at index " + i + " is null");
            }
            batchedMessages.append(encodePBFTMessage(message));
            if (i < messages.length - 1) {
                batchedMessages.append("\n");
            }
        }

        System.out.println("Batched " + messages.length + " messages");
        return batchedMessages.toString();
    }

    /**
     * Splits a batch of serialized messages back into individual PBFTMessage objects.
     *
     * @param batchedMessages The serialized string representing the batched messages.
     * @return An array of decoded PBFTMessage objects.
     * @throws IllegalArgumentException If the batched messages string is null or empty
     */
    public PBFTMessage[] unbatchMessages(String batchedMessages) {
        if (batchedMessages == null || batchedMessages.isEmpty()) {
            throw new IllegalArgumentException("Batched messages string cannot be null or empty");
        }

        String[] messageLines = batchedMessages.split("\n");
        PBFTMessage[] messages = new PBFTMessage[messageLines.length];

        for (int i = 0; i < messageLines.length; i++) {
            messages[i] = decodePBFTMessage(messageLines[i]);
        }

        System.out.println("Unbatched " + messages.length + " messages");
        return messages;
    }
}
