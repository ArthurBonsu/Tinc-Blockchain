package org.example.app.core.consensus;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.example.app.core.block.Block;
import org.example.app.core.block.Transaction;
import org.example.app.core.types.Hash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for blockchain consensus components
 */
@ExtendWith(MockitoExtension.class)
public class BaseConsensusTest {
    
    @Mock
    private Block mockBlock;
    
    @Mock
    private Block mockPreviousBlock;
    
    private Blockchain blockchain;
    private Consensus consensus;
    private EthashConsensus ethashConsensus;
    private ProofOfWork proofOfWork;
    
    @BeforeEach
    public void setUp() {
        // Initialize test objects
        blockchain = new Blockchain();
        consensus = new Consensus();
        ethashConsensus = new EthashConsensus();
        proofOfWork = new ProofOfWork();
        
        // Set up mock blocks
        Mockito.when(mockBlock.getHash()).thenReturn("000000abcdef1234567890");
        Mockito.when(mockBlock.getDifficulty()).thenReturn(1000L);
        Mockito.when(mockBlock.getNumber()).thenReturn(1L);
        Mockito.when(mockBlock.getParentHash()).thenReturn("0000000000000000000000");
        
        Mockito.when(mockPreviousBlock.getHash()).thenReturn("0000000000000000000000");
        Mockito.when(mockPreviousBlock.getNumber()).thenReturn(0L);
    }
    
    @Test
    public void testBlockchainAddBlock() {
        // Test adding a valid block to the blockchain
        Mockito.when(mockBlock.getHash()).thenReturn("0000001234567890abcdef");
        boolean result = blockchain.addBlock(mockBlock);
        
        assertTrue(result, "Valid block should be added to blockchain");
        assertEquals(mockBlock, blockchain.getLatestBlock(), "Latest block should be the added block");
    }
    
    @Test
    public void testBlockchainGetBlockByHash() {
        // Add a block to the blockchain
        blockchain.addBlock(mockBlock);
        
        // Test retrieving the block by hash
        Block retrievedBlock = blockchain.getBlockByHash(mockBlock.getHash());
        
        assertNotNull(retrievedBlock, "Block should be retrievable by hash");
        assertEquals(mockBlock, retrievedBlock, "Retrieved block should match the added block");
        
        // Test retrieving a non-existent block
        Block nonExistentBlock = blockchain.getBlockByHash("non-existent-hash");
        
        assertNull(nonExistentBlock, "Non-existent block should not be found");
    }
    
    @Test
    public void testConsensusValidateBlock() {
        // Test the consensus validation mechanism
        Mockito.when(mockBlock.getHash()).thenReturn("0000000000000000ffff");  // Hash that will pass validation
        
        boolean isValid = consensus.validateBlock(mockBlock);
        
        assertTrue(isValid, "Block should pass consensus validation");
    }
    
    @Test
    public void testConsensusMineBlock() {
        // Test the mining process through consensus
        Block minedBlock = consensus.mineBlock(mockPreviousBlock, "miner-address", 1000L);
        
        assertNotNull(minedBlock, "Mined block should not be null");
        assertEquals(mockPreviousBlock.getHash(), minedBlock.getParentHash(), "Mined block should reference parent block");
        assertEquals(mockPreviousBlock.getNumber() + 1, minedBlock.getNumber(), "Mined block number should be parent number + 1");
        assertEquals("miner-address", minedBlock.getMiner(), "Mined block should have correct miner");
    }
    
    @Test
    public void testEthashConsensusValidation() {
        // Test the Ethash-specific consensus validation
        Mockito.when(mockBlock.getHash()).thenReturn("0000000000000000ffff");  // Hash that will pass validation
        
        boolean isValid = ethashConsensus.validateProofOfWork(mockBlock);
        
        assertTrue(isValid, "Block should pass Ethash validation");
    }
    
    @Test
    public void testEthashConsensusMining() {
        // Test the Ethash-specific mining process
        Block minedBlock = ethashConsensus.mineBlock(mockPreviousBlock, "miner-address", 1000L);
        
        assertNotNull(minedBlock, "Mined block should not be null");
        assertEquals(mockPreviousBlock.getHash(), minedBlock.getParentHash(), "Mined block should reference parent block");
        assertEquals(mockPreviousBlock.getNumber() + 1, minedBlock.getNumber(), "Mined block number should be parent number + 1");
        assertEquals("miner-address", minedBlock.getMiner(), "Mined block should have correct miner");
        assertEquals(1000L, minedBlock.getDifficulty(), "Mined block should have correct difficulty");
    }
    
    @Test
    public void testHashUtils() {
        // Test hash generation
        String input = "test-data";
        String hash = HashUtils.generateHash(input);
        
        assertNotNull(hash, "Generated hash should not be null");
        assertFalse(hash.isEmpty(), "Generated hash should not be empty");
        assertEquals(64, hash.length(), "Hash length should be 64 characters (SHA-256)");
        
        // Test the same input produces the same hash (idempotence)
        String repeatHash = HashUtils.generateHash(input);
        assertEquals(hash, repeatHash, "Same input should produce same hash");
        
        // Test different inputs produce different hashes
        String differentInput = "different-test-data";
        String differentHash = HashUtils.generateHash(differentInput);
        assertNotEquals(hash, differentHash, "Different inputs should produce different hashes");
    }
    
    @Test
    public void testHashBelowTarget() {
        // Test hash difficulty check with a hash that should pass
        String lowHash = "0000000000000001ffffffffffffffffffffffffffffffffffffffffffffffff";
        boolean belowTarget = HashUtils.isHashBelowTarget(lowHash, Long.MAX_VALUE);
        
        assertTrue(belowTarget, "Low hash should be below target");
        
        // Test hash difficulty check with a hash that should fail
        String highHash = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        boolean aboveTarget = HashUtils.isHashBelowTarget(highHash, 1L);
        
        assertFalse(aboveTarget, "High hash should not be below target");
    }
    
    @Test
    public void testProofOfWork() {
        // Test proof of work validation
        Mockito.when(mockBlock.getHash()).thenReturn("0000000000000000ffff");  // Hash that will pass validation
        
        boolean isValid = proofOfWork.validatePoW(mockBlock, 1000L);
        
        assertTrue(isValid, "Block should pass proof of work validation");
        
        // Test mining with proof of work
        String minedHash = proofOfWork.mineBlock(mockBlock, 1000L);
        
        assertNotNull(minedHash, "Mined hash should not be null");
        assertFalse(minedHash.isEmpty(), "Mined hash should not be empty");
    }
}
