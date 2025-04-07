package org.example.app.core.smartcontract;

import org.example.app.core.block.Block;
import org.example.app.core.block.Transaction;
import org.example.app.core.consensus.Blockchain;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SmartContractProcessTest {

    private static final Logger LOGGER = Logger.getLogger(SmartContractProcessTest.class.getName());
    private Blockchain blockchain;
    private EvmState evmState;
    private Evm evm;

    @BeforeEach
    void setUp() throws Exception {
        try {
            // Initialize blockchain
            blockchain = new Blockchain();

            // Initialize EVM state
            evmState = new EvmState();

            // Initialize EVM with state
            evm = new Evm(evmState);

            // Create genesis block with proper initialization
            Block genesisBlock = createGenesisBlock();

            // Generate a valid hexadecimal hash (16 characters of hex)
            String genesisHash = generateValidHexHash("genesis");
            genesisBlock.setHash(genesisHash);

            blockchain.addBlock(genesisBlock);

            LOGGER.info("Test setup completed successfully. Blockchain initialized with genesis block.");
        } catch (Exception e) {
            LOGGER.severe("Error in test setup: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Generate a valid hexadecimal hash string for testing that is compatible
     * with the isHashBelowTarget method in HashUtils.
     *
     * @param prefix A prefix to help identify the hash source
     * @return A valid hexadecimal string with first 16 characters parsable as a long
     */
    private String generateValidHexHash(String prefix) {
        try {
            // Generate a deterministic hash based on the prefix and current time
            String input = prefix + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            // Ensure the first 16 characters can be parsed as a long
            // We'll set the first 8 bytes to small values (0-9) to ensure they're within long range
            for (int i = 0; i < 8; i++) {
                hashBytes[i] = (byte) (Math.abs(hashBytes[i]) % 10);
            }

            // Convert to hexadecimal
            StringBuilder hexHash = new StringBuilder();
            for (byte b : hashBytes) {
                hexHash.append(String.format("%02x", b & 0xFF));
            }

            return hexHash.toString();
        } catch (Exception e) {
            // Simple fallback that's guaranteed to be parsable as a long
            long timestamp = System.currentTimeMillis();
            String fallbackHash = "0000000000000000"; // First 16 chars will be zeros
            fallbackHash += String.format("%016x", timestamp % 10000); // Add some uniqueness
            return fallbackHash;
        }
    }

    @Test
    @DisplayName("Deploy and execute a smart contract that stores and retrieves values")
    void testSmartContractDeployAndExecution() throws Exception {
        // 1. Create contract bytecode for a simple storage contract
        byte[] contractBytecode = createStorageContractBytecode();

        // 2. Create a transaction to deploy the contract
        String contractAddress = "contract123"; // Simplified address for testing
        Transaction deployTx = createContractDeploymentTransaction(contractAddress, contractBytecode);

        // 3. Add transaction to a new block
        Block contractBlock = createBlock(blockchain, deployTx, 1);
        blockchain.addBlock(contractBlock);

        // 4. Execute the contract deployment
        evm.execute(contractBytecode, deployTx);

        // 5. Verify the contract was deployed by checking gas consumption
        int gasConsumed = deployTx.getGasLimit() - evm.getGasManager().getGasRemaining();
        LOGGER.info("Gas consumed during deployment: " + gasConsumed);
        assertTrue(gasConsumed > 0, "Contract deployment should consume gas");
        assertTrue(evm.getGasManager().getGasRemaining() > 0, "Contract should not run out of gas");

        // 6. Create a transaction to interact with the contract (load from storage)
        byte[] interactionBytecode = createContractInteractionBytecode();
        Transaction interactTx = createContractInteractionTransaction(contractAddress, interactionBytecode);

        // 7. Add interaction transaction to a new block
        Block interactionBlock = createBlock(blockchain, interactTx, 2);
        blockchain.addBlock(interactionBlock);

        // 8. Reset EVM and execute the interaction
        evm = new Evm(evmState); // Reset EVM for new transaction
        evm.execute(interactionBytecode, interactTx);

        // 9. Verify the storage state after interaction
        String storedValue = evmState.load(contractAddress, "1");
        assertEquals("42", storedValue, "Contract should store value 42 at key 1");

        // 10. Verify gas consumption for the interaction
        int interactionGasConsumed = interactTx.getGasLimit() - evm.getGasManager().getGasRemaining();
        LOGGER.info("Gas consumed during interaction: " + interactionGasConsumed);
        assertTrue(interactionGasConsumed > 0, "Contract interaction should consume gas");
        assertTrue(interactionGasConsumed < gasConsumed, "Interaction should use less gas than deployment");

        // 11. Verify blocks were added to the blockchain
        assertEquals(3, blockchain.getBlockCount(), "Blockchain should have 3 blocks (genesis + 2 contract blocks)");
    }

    @Test
    @DisplayName("Test smart contract with arithmetic operations")
    void testSmartContractArithmeticOperations() throws Exception {
        // 1. Create contract bytecode for an arithmetic contract
        byte[] contractBytecode = createArithmeticContractBytecode();

        // 2. Create a transaction to deploy the contract
        String contractAddress = "arithmetic_contract";
        Transaction deployTx = createContractDeploymentTransaction(contractAddress, contractBytecode);

        // 3. Add transaction to a new block
        Block contractBlock = createBlock(blockchain, deployTx, 1);
        blockchain.addBlock(contractBlock);

        // 4. Execute the contract
        evm.execute(contractBytecode, deployTx);

        // 5. Verify the stored result is correct (5 + 7 = 12)
        String storedValue = evmState.load(contractAddress, "1");
        assertEquals("12", storedValue, "Contract should store the addition result (12) at key 1");

        // 6. Check gas consumption is appropriate
        int gasConsumed = deployTx.getGasLimit() - evm.getGasManager().getGasRemaining();
        LOGGER.info("Gas consumed for arithmetic contract: " + gasConsumed);
        assertTrue(gasConsumed > 0, "Contract execution should consume gas");
    }

    @Test
    @DisplayName("Test out of gas exception handling")
    void testOutOfGasException() {
        LOGGER.info("Starting testOutOfGasException");

        // 1. Create a gas-intensive contract
        byte[] gasIntensiveCode = createGasIntensiveContract();
        LOGGER.info("Created gas-intensive contract bytecode");

        // 2. Create transaction with insufficient gas
        String contractAddress = "gas_test_contract";
        Transaction tx = new Transaction(gasIntensiveCode);
        tx.setRecipient(contractAddress);
        tx.setValue(0L);
        tx.setGasLimit(100); // Very low gas limit

        // 3. Execute and expect an "Out of gas" RuntimeException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            evm.execute(gasIntensiveCode, tx);
        });

        // 4. Log the exception for debugging
        LOGGER.info("Exception caught: " + exception.getClass().getName() + " - " + exception.getMessage());

        // 5. Exact message check based on the log output
        assertTrue(exception.getMessage().contains("Out of gas"),
                "Exception should indicate out of gas condition");

        LOGGER.info("Successfully caught 'Out of gas' exception");
    }

    // Update the bytecode generation methods

    private byte[] createStorageContractBytecode() {
        return new byte[] {
                (byte) 0x60, 0x2A,  // PUSH1 opcode (0x60) and value 42
                (byte) 0x60, 0x01,  // PUSH1 opcode (0x60) and key 1
                (byte) 0x55,        // SSTORE opcode (0x55)
                (byte) 0x00         // STOP opcode (0x00)
        };
    }

    private byte[] createArithmeticContractBytecode() {
        return new byte[] {
                (byte) 0x60, 0x05,  // PUSH1 opcode (0x60) and first value (5)
                (byte) 0x60, 0x07,  // PUSH1 opcode (0x60) and second value (7)
                (byte) 0x01,        // ADD opcode (0x01)
                (byte) 0x60, 0x01,  // PUSH1 opcode (0x60) and key 1
                (byte) 0x55,        // SSTORE opcode (0x55)
                (byte) 0x00         // STOP opcode (0x00)
        };
    }

    private byte[] createContractInteractionBytecode() {
        return new byte[] {
                (byte) 0x60, 0x01,  // PUSH1 opcode (0x60) and key 1
                (byte) 0x54,        // SLOAD opcode (0x54)
                (byte) 0xF3         // RETURN opcode (0xF3)
        };
    }

    private byte[] createGasIntensiveContract() {
        byte[] code = new byte[50]; // Larger contract for more gas consumption

        // Fill with expensive operations
        for (int i = 0; i < 45; i += 3) {
            code[i] = (byte) 0x55;     // SSTORE - very expensive
            code[i+1] = (byte) 0x54;   // SLOAD - expensive
            code[i+2] = (byte) 0x02;   // MUL - more computation
        }

        // End with return
        code[48] = (byte) 0x01;   // ADD
        code[49] = (byte) 0xF3;   // RETURN

        return code;
    }
    // Transaction and Block Creation Helpers
    private Transaction createContractDeploymentTransaction(String contractAddress, byte[] bytecode) {
        Transaction tx = new Transaction(bytecode);
        tx.setRecipient(contractAddress);
        tx.setValue(0L);
        tx.setGasLimit(50000); // Set appropriate gas limit for deployment
        return tx;
    }

    private Transaction createContractInteractionTransaction(String contractAddress, byte[] bytecode) {
        Transaction tx = new Transaction(bytecode);
        tx.setRecipient(contractAddress);
        tx.setValue(0L);
        tx.setGasLimit(10000); // Less gas needed for interaction
        return tx;
    }

    private Block createGenesisBlock() {
        Block block = new Block(
                null,           // hash
                null,           // parentHash
                "genesis_miner",// miner
                System.currentTimeMillis(), // timestamp
                0,              // difficulty
                0,              // block number
                new ArrayList<>() // empty transactions list
        );
        return block;
    }

    private Block createBlock(Blockchain blockchain, Transaction tx, int blockNumber) {
        Block latestBlock = blockchain.getLatestBlock();

        // If there's no latest block, use a genesis block as parent
        String parentHash = null;
        if (latestBlock != null) {
            parentHash = latestBlock.getHash();
        }

        Block newBlock = new Block(
                null,                    // hash (will be set later)
                parentHash,              // parentHash (null if genesis)
                "test_miner",            // miner
                System.currentTimeMillis(), // timestamp
                0,                       // difficulty
                blockNumber,             // block number
                List.of(tx)              // transactions list with our tx
        );

        // Set a valid hex hash before returning
        String blockHash = generateValidHexHash("block" + blockNumber);
        newBlock.setHash(blockHash);
        return newBlock;
    }
}