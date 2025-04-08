//package org.example.app.core.network;
//
//import org.example.app.core.block.Block;
//import java.util.List;
//
//public class Messages {
//
//    // Represents a request to fetch blocks within a range.
//    public static class GetBlocksMessage {
//        private final int from;
//        private final int to;
//
//        /**
//         * Constructor for GetBlocksMessage.
//         *
//         * @param from Starting block height.
//         * @param to   Ending block height. If 0, the maximum blocks will be returned.
//         */
//        public GetBlocksMessage(int from, int to) {
//            this.from = from;
//            this.to = to;
//        }
//
//        public int getFrom() {
//            return from;
//        }
//
//        public int getTo() {
//            return to;
//        }
//    }
//
//    // Represents a response containing blocks.
//    public static class BlocksMessage {
//        private final List<Block> blocks;
//
//        /**
//         * Constructor for BlocksMessage.
//         *
//         * @param blocks List of Block objects.
//         */
//        public BlocksMessage(List<Block> blocks) {
//            this.blocks = blocks;
//        }
//
//        public List<Block> getBlocks() {
//            return blocks;
//        }
//    }
//
//    // Represents a request for status information.
//    public static class GetStatusMessage {
//        // Empty request type.
//    }
//
//    // Represents the status information of the server.
//    public static class StatusMessage {
//        private final String id;
//        private final int version;
//        private final int currentHeight;
//
//        /**
//         * Constructor for StatusMessage.
//         *
//         * @param id            Unique ID of the server.
//         * @param version       Version number of the server.
//         * @param currentHeight Current block height on the server.
//         */
//        public StatusMessage(String id, int version, int currentHeight) {
//            this.id = id;
//            this.version = version;
//            this.currentHeight = currentHeight;
//        }
//
//        public String getId() {
//            return id;
//        }
//
//        public int getVersion() {
//            return version;
//        }
//
//        public int getCurrentHeight() {
//            return currentHeight;
//        }
//    }
//}






package org.example.app.core.network;

import org.example.app.core.block.Block;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines message types used for network communication between nodes.
 * All message classes implement serialization for network transport.
 */
public class Messages {
    private static final Logger logger = Logger.getLogger(Messages.class.getName());

    // Message type constants for identification during deserialization
    private static final byte TYPE_GET_BLOCKS = 1;
    private static final byte TYPE_BLOCKS = 2;
    private static final byte TYPE_GET_STATUS = 3;
    private static final byte TYPE_STATUS = 4;

    // Version of the message format, for future compatibility
    private static final int CURRENT_VERSION = 1;

    /**
     * Base class for all network messages with serialization support.
     */
    public static abstract class Message implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final int version;

        protected Message() {
            this.version = CURRENT_VERSION;
        }

        /**
         * Gets the message format version.
         *
         * @return The version number
         */
        public int getVersion() {
            return version;
        }

        /**
         * Gets the type identifier for this message.
         *
         * @return The message type byte
         */
        public abstract byte getType();

        /**
         * Serializes this message to a byte array.
         *
         * @return The serialized message
         * @throws IOException if serialization fails
         */
        public byte[] serialize() throws IOException {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 DataOutputStream dos = new DataOutputStream(baos)) {

                // Write type byte and version for proper deserialization
                dos.writeByte(getType());
                dos.writeInt(version);

                // Let subclasses write their specific fields
                serializeBody(dos);

                dos.flush();
                return baos.toByteArray();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to serialize message", e);
                throw e;
            }
        }

        /**
         * Serializes the message-specific fields.
         * To be implemented by subclasses.
         *
         * @param out The output stream
         * @throws IOException if writing fails
         */
        protected abstract void serializeBody(DataOutputStream out) throws IOException;

        /**
         * Deserializes a message from a byte array.
         *
         * @param data The serialized message data
         * @return The deserialized message
         * @throws IOException if deserialization fails
         * @throws ClassNotFoundException if the message type is unknown
         */
        public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
            if (data == null || data.length == 0) {
                throw new IOException("Message data is null or empty");
            }

            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 DataInputStream dis = new DataInputStream(bais)) {

                // Read type and version
                byte type = dis.readByte();
                int version = dis.readInt();

                // Create and populate the appropriate message type
                switch (type) {
                    case TYPE_GET_BLOCKS:
                        return GetBlocksMessage.deserializeBody(dis, version);
                    case TYPE_BLOCKS:
                        return BlocksMessage.deserializeBody(dis, version);
                    case TYPE_GET_STATUS:
                        return new GetStatusMessage(); // No body to deserialize
                    case TYPE_STATUS:
                        return StatusMessage.deserializeBody(dis, version);
                    default:
                        throw new ClassNotFoundException("Unknown message type: " + type);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to deserialize message", e);
                throw e;
            }
        }
    }

    /**
     * Represents a request to fetch blocks within a range.
     */
    public static class GetBlocksMessage extends Message {
        private static final long serialVersionUID = 1L;
        private final int from;
        private final int to;

        /**
         * Constructor for GetBlocksMessage.
         *
         * @param from Starting block height.
         * @param to   Ending block height. If 0, the maximum blocks will be returned.
         * @throws IllegalArgumentException if from is negative or to is less than from (except 0)
         */
        public GetBlocksMessage(int from, int to) {
            super();
            if (from < 0) {
                throw new IllegalArgumentException("From height cannot be negative");
            }
            if (to != 0 && to < from) {
                throw new IllegalArgumentException("To height must be greater than or equal to from height");
            }

            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        @Override
        public byte getType() {
            return TYPE_GET_BLOCKS;
        }

        @Override
        protected void serializeBody(DataOutputStream out) throws IOException {
            out.writeInt(from);
            out.writeInt(to);
        }

        /**
         * Deserializes the body of a GetBlocksMessage.
         *
         * @param in The input stream
         * @param version The message version
         * @return The deserialized GetBlocksMessage
         * @throws IOException if reading fails
         */
        public static GetBlocksMessage deserializeBody(DataInputStream in, int version) throws IOException {
            int from = in.readInt();
            int to = in.readInt();
            return new GetBlocksMessage(from, to);
        }
    }

    /**
     * Represents a response containing blocks.
     */
    public static class BlocksMessage extends Message {
        private static final long serialVersionUID = 1L;
        private final List<Block> blocks;

        /**
         * Constructor for BlocksMessage.
         *
         * @param blocks List of Block objects.
         * @throws IllegalArgumentException if blocks is null
         */
        public BlocksMessage(List<Block> blocks) {
            super();
            if (blocks == null) {
                throw new IllegalArgumentException("Blocks list cannot be null");
            }

            // Create defensive copy
            this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
        }

        public List<Block> getBlocks() {
            return blocks;
        }

        @Override
        public byte getType() {
            return TYPE_BLOCKS;
        }

        @Override
        protected void serializeBody(DataOutputStream out) throws IOException {
            // Write number of blocks
            out.writeInt(blocks.size());

            // Write each block
            for (Block block : blocks) {
                // Assuming Block has a serialize method, or we write its fields directly
                byte[] blockData = block.serialize();
                out.writeInt(blockData.length);
                out.write(blockData);
            }
        }

        /**
         * Deserializes the body of a BlocksMessage.
         *
         * @param in The input stream
         * @param version The message version
         * @return The deserialized BlocksMessage
         * @throws IOException if reading fails
         */
        public static BlocksMessage deserializeBody(DataInputStream in, int version) throws IOException {
            int blockCount = in.readInt();
            List<Block> blocks = new ArrayList<>(blockCount);

            // Read each block
            for (int i = 0; i < blockCount; i++) {
                int blockSize = in.readInt();
                byte[] blockData = new byte[blockSize];
                in.readFully(blockData);

                // Deserialize the block
                Block block = Block.deserialize(blockData);
                blocks.add(block);
            }

            return new BlocksMessage(blocks);
        }
    }

    /**
     * Represents a request for status information.
     */
    public static class GetStatusMessage extends Message {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for GetStatusMessage.
         */
        public GetStatusMessage() {
            super();
        }

        @Override
        public byte getType() {
            return TYPE_GET_STATUS;
        }

        @Override
        protected void serializeBody(DataOutputStream out) throws IOException {
            // No additional fields to serialize
        }
    }

    /**
     * Represents the status information of the server.
     */
    public static class StatusMessage extends Message {
        private static final long serialVersionUID = 1L;
        private final String id;
        private final int protocolVersion;
        private final int currentHeight;

        /**
         * Constructor for StatusMessage.
         *
         * @param id             Unique ID of the server.
         * @param protocolVersion Version number of the server protocol.
         * @param currentHeight  Current block height on the server.
         * @throws IllegalArgumentException if id is null or empty or currentHeight is negative
         */
        public StatusMessage(String id, int protocolVersion, int currentHeight) {
            super();
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Server ID cannot be null or empty");
            }
            if (currentHeight < 0) {
                throw new IllegalArgumentException("Current height cannot be negative");
            }

            this.id = id;
            this.protocolVersion = protocolVersion;
            this.currentHeight = currentHeight;
        }

        public String getId() {
            return id;
        }

        public int getProtocolVersion() {
            return protocolVersion;
        }

        public int getCurrentHeight() {
            return currentHeight;
        }

        @Override
        public byte getType() {
            return TYPE_STATUS;
        }

        @Override
        protected void serializeBody(DataOutputStream out) throws IOException {
            // Write string with length prefix
            byte[] idBytes = id.getBytes("UTF-8");
            out.writeInt(idBytes.length);
            out.write(idBytes);

            out.writeInt(protocolVersion);
            out.writeInt(currentHeight);
        }

        /**
         * Deserializes the body of a StatusMessage.
         *
         * @param in The input stream
         * @param version The message version
         * @return The deserialized StatusMessage
         * @throws IOException if reading fails
         */
        public static StatusMessage deserializeBody(DataInputStream in, int version) throws IOException {
            // Read string with length prefix
            int idLength = in.readInt();
            byte[] idBytes = new byte[idLength];
            in.readFully(idBytes);
            String id = new String(idBytes, "UTF-8");

            int protocolVersion = in.readInt();
            int currentHeight = in.readInt();

            return new StatusMessage(id, protocolVersion, currentHeight);
        }
    }
}
