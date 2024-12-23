package org.tinc.p2p;

import java.io.*;
import java.util.Objects;

/**
 * Represents a message exchanged between peers.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // For serialization consistency
    private final String sender; // Sender identifier
    private final String content; // Message content

    /**
     * Constructs a Message object with validation.
     *
     * @param sender  The sender's identifier.
     * @param content The content of the message.
     * @throws IllegalArgumentException If sender or content is null or blank.
     */
    public Message(String sender, String content) {
        if (sender == null || sender.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender cannot be null or blank.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or blank.");
        }
        this.sender = sender;
        this.content = content;
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
        }
    }

    /**
     * Deserializes a byte array into a Message object.
     *
     * @param data The byte array to deserialize.
     * @return The deserialized Message object.
     * @throws IOException            If an error occurs during deserialization.
     * @throws ClassNotFoundException If the Message class is not found.
     * @throws IllegalArgumentException If the input data is null or empty.
     */
    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be null or empty.");
        }
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (Message) objectStream.readObject();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Message message = (Message) obj;
        return sender.equals(message.sender) && content.equals(message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, content);
    }

    @Override
    public String toString() {
        return "Message from " + sender + ": " + content;
    }
}
