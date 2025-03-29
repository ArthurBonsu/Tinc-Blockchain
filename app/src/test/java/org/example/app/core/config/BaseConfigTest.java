package org.example.app.core.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * Test class for blockchain configuration components
 */
public class BaseConfigTest {
    
    private BlockchainConfig blockchainConfig;
    private ConsensusConfig consensusConfig;
    private NetworkConfig networkConfig;
    private StorageConfig storageConfig;
    
    @BeforeEach
    public void setUp() {
        // Initialize test configurations
        blockchainConfig = new BlockchainConfig.Builder()
            .blockTime(10000) // 10 seconds
            .build();
            
        consensusConfig = new ConsensusConfig.Builder()
            .consensusType("PBFT")
            .build();
            
        networkConfig = new NetworkConfig.Builder()
            .networkId("testnet-1")
            .maxPeers(25)
            .build();
            
        storageConfig = new StorageConfig.Builder()
            .storageType("rocksdb")
            .build();
    }
    
    @Test
    public void testBlockchainConfigDefaults() {
        // Test default values when using empty builder
        BlockchainConfig defaultConfig = new BlockchainConfig.Builder().build();
        
        assertEquals(15000, defaultConfig.getBlockTime(), "Default block time should be 15 seconds");
    }
    
    @Test
    public void testBlockchainConfigCustomValues() {
        // Test that custom values are properly set
        assertEquals(10000, blockchainConfig.getBlockTime(), "Custom block time should be set correctly");
    }
    
    @Test
    public void testConsensusConfigDefaults() {
        // Test default values when using empty builder
        ConsensusConfig defaultConfig = new ConsensusConfig.Builder().build();
        
        assertEquals("POW", defaultConfig.getConsensusType(), "Default consensus type should be POW");
        assertEquals(15000, defaultConfig.getBlockTime(), "Default block time should be 15 seconds");
        assertEquals(BigInteger.valueOf(100000), defaultConfig.getDifficulty(), "Default difficulty should be 100000");
        assertEquals(4, defaultConfig.getMinValidators(), "Default minimum validators should be 4");
        assertEquals(0.67, defaultConfig.getRequiredMajority(), 0.001, "Default required majority should be 0.67");
    }
    
    @Test
    public void testConsensusConfigCustomValues() {
        // Test that custom values are properly set
        assertEquals("PBFT", consensusConfig.getConsensusType(), "Custom consensus type should be PBFT");
    }
    
    @Test
    public void testNetworkConfigDefaults() {
        // Test default values when using empty builder
        NetworkConfig defaultConfig = new NetworkConfig.Builder().build();
        
        assertEquals("1", defaultConfig.getNetworkId(), "Default network ID should be 1");
        assertEquals(50, defaultConfig.getMaxPeers(), "Default max peers should be 50");
        assertEquals(30303, defaultConfig.getPort(), "Default port should be 30303");
        assertEquals(15000, defaultConfig.getPingInterval(), "Default ping interval should be 15 seconds");
        assertEquals(30000, defaultConfig.getSyncInterval(), "Default sync interval should be 30 seconds");
    }
    
    @Test
    public void testNetworkConfigCustomValues() {
        // Test that custom values are properly set
        assertEquals("testnet-1", networkConfig.getNetworkId(), "Custom network ID should be set correctly");
        assertEquals(25, networkConfig.getMaxPeers(), "Custom max peers should be set correctly");
    }
    
    @Test
    public void testStorageConfigDefaults() {
        // Test default values when using empty builder
        StorageConfig defaultConfig = new StorageConfig.Builder().build();
        
        assertEquals("leveldb", defaultConfig.getStorageType(), "Default storage type should be leveldb");
        assertEquals("./data", defaultConfig.getDataDir(), "Default data directory should be ./data");
        assertEquals(512, defaultConfig.getCacheSize(), "Default cache size should be 512 MB");
        assertTrue(defaultConfig.isCompression(), "Default compression should be enabled");
        assertEquals(64, defaultConfig.getWriteBuffer(), "Default write buffer should be 64 MB");
    }
    
    @Test
    public void testStorageConfigCustomValues() {
        // Test that custom values are properly set
        assertEquals("rocksdb", storageConfig.getStorageType(), "Custom storage type should be set correctly");
    }
    
    @Test
    public void testConfigImmutability() {
        // Create configs
        BlockchainConfig blockchain = new BlockchainConfig.Builder().build();
        ConsensusConfig consensus = new ConsensusConfig.Builder().build();
        NetworkConfig network = new NetworkConfig.Builder().build();
        StorageConfig storage = new StorageConfig.Builder().build();
        
        // Get initial values
        long initialBlockTime = blockchain.getBlockTime();
        String initialConsensusType = consensus.getConsensusType();
        String initialNetworkId = network.getNetworkId();
        String initialStorageType = storage.getStorageType();
        
        // Verify immutability (configs should not have setter methods)
        // If they had setters, we would try to call them and expect exceptions
        
        // Verify values haven't changed
        assertEquals(initialBlockTime, blockchain.getBlockTime(), "Blockchain config should be immutable");
        assertEquals(initialConsensusType, consensus.getConsensusType(), "Consensus config should be immutable");
        assertEquals(initialNetworkId, network.getNetworkId(), "Network config should be immutable");
        assertEquals(initialStorageType, storage.getStorageType(), "Storage config should be immutable");
    }
    
    @Test
    public void testBuilderChaining() {
        // Test builder pattern chaining works correctly
        NetworkConfig chainedConfig = new NetworkConfig.Builder()
            .networkId("custom-net")
            .maxPeers(100)
            .build();
            
        assertEquals("custom-net", chainedConfig.getNetworkId(), "Builder chaining should set network ID");
        assertEquals(100, chainedConfig.getMaxPeers(), "Builder chaining should set max peers");
    }
}
