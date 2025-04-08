// StorageConfig.java
package org.example.app.core.config;

public class StorageConfig {
    private final String storageType; // "leveldb", "rocksdb", etc.
    private final String dataDir;
    private final int cacheSize;
    private final boolean compression;
    private final int writeBuffer;

    public static class Builder {
        private String storageType = "leveldb";
        private String dataDir = "./data";
        private int cacheSize = 512; // MB
        private boolean compression = true;
        private int writeBuffer = 64; // MB

        public Builder storageType(String type) {
            this.storageType = type;
            return this;
        }

        public StorageConfig build() {
            return new StorageConfig(this);
        }
    }

    private StorageConfig(Builder builder) {
        this.storageType = builder.storageType;
        this.dataDir = builder.dataDir;
        this.cacheSize = builder.cacheSize;
        this.compression = builder.compression;
        this.writeBuffer = builder.writeBuffer;
    }

    // Getters
    public String getStorageType() { return storageType; }
    public String getDataDir() { return dataDir; }
    public int getCacheSize() { return cacheSize; }
    public boolean isCompression() { return compression; }
    public int getWriteBuffer() { return writeBuffer; }
}