package org.tinc.network;

import org.tinc.core.Block;
import java.util.List;

public class Messages {

    // Represents a request to fetch blocks within a range.
    public static class GetBlocksMessage {
        private final int from;
        private final int to;

        /**
         * Constructor for GetBlocksMessage.
         *
         * @param from Starting block height.
         * @param to   Ending block height. If 0, the maximum blocks will be returned.
         */
        public GetBlocksMessage(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }
    }

    // Represents a response containing blocks.
    public static class BlocksMessage {
        private final List<Block> blocks;

        /**
         * Constructor for BlocksMessage.
         *
         * @param blocks List of Block objects.
         */
        public BlocksMessage(List<Block> blocks) {
            this.blocks = blocks;
        }

        public List<Block> getBlocks() {
            return blocks;
        }
    }

    // Represents a request for status information.
    public static class GetStatusMessage {
        // Empty request type.
    }

    // Represents the status information of the server.
    public static class StatusMessage {
        private final String id;
        private final int version;
        private final int currentHeight;

        /**
         * Constructor for StatusMessage.
         *
         * @param id            Unique ID of the server.
         * @param version       Version number of the server.
         * @param currentHeight Current block height on the server.
         */
        public StatusMessage(String id, int version, int currentHeight) {
            this.id = id;
            this.version = version;
            this.currentHeight = currentHeight;
        }

        public String getId() {
            return id;
        }

        public int getVersion() {
            return version;
        }

        public int getCurrentHeight() {
            return currentHeight;
        }
    }
}
