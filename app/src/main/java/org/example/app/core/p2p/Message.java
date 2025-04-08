//package org.example.app.core.p2p;
//
//import java.io.*;
//import java.util.Objects;
//
///**
// * Represents a message exchanged between peers.
// */
//public class Message implements Serializable {
//    private static final long serialVersionUID = 1L; // For serialization consistency
//    private final String sender; // Sender identifier
//    private final String content; // Message content
//
//    /**
//     * Constructs a Message object with validation.
//     *
//     * @param sender  The sender's identifier.
//     * @param content The content of the message.
//     * @throws IllegalArgumentException If sender or content is null or blank.
//     */
//    public Message(String sender, String content) {
//        if (sender == null || sender.trim().isEmpty()) {
//            throw new IllegalArgumentException("Sender cannot be null or blank.");
//        }
//        if (content == null || content.trim().isEmpty()) {
//            throw new IllegalArgumentException("Content cannot be null or blank.");
//        }
//        this.sender = sender;
//        this.content = content;
//    }
//
//    /**
//     * Gets the sender's identifier.
//     *
//     * @return The sender's identifier.
//     */
//    public String getSender() {
//        return sender;
//    }
//
//    /**
//     * Gets the message content.
//     *
//     * @return The content of the message.
//     */
//    public String getContent() {
//        return content;
//    }
//
//    /**
//     * Serializes the message object into a byte array.
//     *
//     * @return A byte array representing the serialized message.
//     * @throws IOException If an error occurs during serialization.
//     */
//    public byte[] serialize() throws IOException {
//        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//             ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
//            objectStream.writeObject(this);
//            return byteStream.toByteArray();
//        }
//    }
//
//    /**
//     * Deserializes a byte array into a Message object.
//     *
//     * @param data The byte array to deserialize.
//     * @return The deserialized Message object.
//     * @throws IOException            If an error occurs during deserialization.
//     * @throws ClassNotFoundException If the Message class is not found.
//     * @throws IllegalArgumentException If the input data is null or empty.
//     */
//    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
//        if (data == null || data.length == 0) {
//            throw new IllegalArgumentException("Data cannot be null or empty.");
//        }
//        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
//             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
//            return (Message) objectStream.readObject();
//        }
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || getClass() != obj.getClass()) return false;
//        Message message = (Message) obj;
//        return sender.equals(message.sender) && content.equals(message.content);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(sender, content);
//    }
//
//    @Override
//    public String toString() {
//        return "Message from " + sender + ": " + content;
//    }
//}





package org.example.app.core.p2p;

import java.io.*;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a message exchanged between peers in the P2P network.
 */
public class Message implements Serializable {
    private static final Logger logger = Logger.getLogger(Message.class.getName());
    private static final long serialVersionUID = 1L; // For serialization consistency

    private final String messageId;  // Unique message identifier
    private final String sender;     // Sender identifier
    private final String content;    // Message content
    private final long timestamp;    // Message creation timestamp

    /**
     * Constructs a Message object with validation.
     *
     * @param sender  The sender's identifier.
     * @param content The content of the message.
     * @throws IllegalArgumentException If sender or content is null or blank.
     */
    public Message(String sender, String content) {
        if (sender == null || sender.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender cannot be null or blank");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }

        this.messageId = UUID.randomUUID().toString();
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructs a Message object with a specific message ID.
     *
     * @param messageId The unique message identifier.
     * @param sender The sender's identifier.
     * @param content The content of the message.
     * @param timestamp The message creation timestamp.
     * @throws IllegalArgumentException If any parameter is invalid.
     */
    protected Message(String messageId, String sender, String content, long timestamp) {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be null or blank");
        }
        if (sender == null || sender.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender cannot be null or blank");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }

        this.messageId = messageId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Gets the unique message identifier.
     *
     * @return The message ID.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the sender's identifier.
     *
     * @return The sender's identifier.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the message content.
     *
     * @return The content of the message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the message timestamp.
     *
     * @return The message creation timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Serializes the message object into a byte array.
     *
     * @return A byte array representing the serialized message.
     * @throws IOException If an error occurs during serialization.
     */
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(this);
            return byteStream.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error serializing message", e);
            throw e;
        }
    }

    /**
     * Deserializes a byte array into a Message object.
     *
     * @param data The byte array to deserialize.
     * @return The deserialized Message object.
     * @throws IOException If an error occurs during deserialization.
     * @throws ClassNotFoundException If the Message class is not found.
     * @throws IllegalArgumentException If the input data is null or empty.
     */
    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            Object obj = objectStream.readObject();
            if (!(obj instanceof Message)) {
                throw new ClassCastException("Deserialized object is not a Message");
            }
            return (Message) obj;
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error deserializing message", e);
            throw e;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Message message = (Message) obj;
        return Objects.equals(messageId, message.messageId) &&
                Objects.equals(sender, message.sender) &&
                Objects.equals(content, message.content) &&
                timestamp == message.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, sender, content, timestamp);
    }

    @Override
    public String toString() {
        return "Message{id=" + messageId + ", from=" + sender + ", timestamp=" + timestamp +
                ", content='" + (content.length() > 50 ? content.substring(0, 47) + "..." : content) + "'}";
    }

    /**
     * Creates a response to this message.
     *
     * @param responder The ID of the responder.
     * @param responseContent The response content.
     * @return A new Message representing the response.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    public Message createResponse(String responder, String responseContent) {
        if (responder == null || responder.trim().isEmpty()) {
            throw new IllegalArgumentException("Responder cannot be null or blank");
        }
        if (responseContent == null || responseContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Response content cannot be null or blank");
        }

        return new Message(responder, responseContent);
    }

    /**
     * Creates a message with a reference to the original message ID.
     *
     * @param sender The sender's identifier.
     * @param content The message content.
     * @param referenceId The reference message ID.
     * @return A new Message with reference information.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    public static Message createWithReference(String sender, String content, String referenceId) {
        if (sender == null || sender.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender cannot be null or blank");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
        if (referenceId == null || referenceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reference ID cannot be null or blank");
        }

        return new Message(sender, "REF:" + referenceId + ":" + content);
    }

    /**
     * Checks if this message is a reference to another message.
     *
     * @return true if this is a reference message, false otherwise.
     */
    public boolean isReferenceMessage() {
        return content != null && content.startsWith("REF:");
    }

    /**
     * Gets the reference ID if this is a reference message.
     *
     * @return The reference ID, or null if this is not a reference message.
     */
    public String getReferenceId() {
        if (!isReferenceMessage()) {
            return null;
        }

        String[] parts = content.split(":", 3);
        return parts.length >= 2 ? parts[1] : null;
    }

    /**
     * Gets the actual content without reference prefix.
     *
     * @return The actual message content.
     */
    public String getActualContent() {
        if (!isReferenceMessage()) {
            return content;
        }

        String[] parts = content.split(":", 3);
        return parts.length >= 3 ? parts[2] : content;
    }
}