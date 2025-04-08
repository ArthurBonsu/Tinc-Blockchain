// NetworkConfig.java
package org.example.app.core.config;

public class NetworkConfig {
    private final String networkId;
    private final int maxPeers;
    private final int port;
    private final String[] bootstrapNodes;
    private final long pingInterval;
    private final long syncInterval;

    public static class Builder {
        private String networkId = "1";
        private int maxPeers = 50;
        private int port = 30303;
        private String[] bootstrapNodes = new String[]{};
        private long pingInterval = 15000; // 15 seconds
        private long syncInterval = 30000; // 30 seconds

        public Builder networkId(String networkId) {
            this.networkId = networkId;
            return this;
        }

        public Builder maxPeers(int maxPeers) {
            this.maxPeers = maxPeers;
            return this;
        }

        public NetworkConfig build() {
            return new NetworkConfig(this);
        }
    }

    private NetworkConfig(Builder builder) {
        this.networkId = builder.networkId;
        this.maxPeers = builder.maxPeers;
        this.port = builder.port;
        this.bootstrapNodes = builder.bootstrapNodes;
        this.pingInterval = builder.pingInterval;
        this.syncInterval = builder.syncInterval;
    }

    // Getters
    public String getNetworkId() { return networkId; }
    public int getMaxPeers() { return maxPeers; }
    public int getPort() { return port; }
    public String[] getBootstrapNodes() { return bootstrapNodes; }
    public long getPingInterval() { return pingInterval; }
    public long getSyncInterval() { return syncInterval; }
}