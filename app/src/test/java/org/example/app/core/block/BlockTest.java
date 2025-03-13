package org.example.app.core.block;

import org.example.app.core.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {
    private Block block;
    private Transaction transaction;
    private Wallet wallet;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Create a wallet and set initial balance
        wallet = new Wallet();

        // Use reflection to set balance (since it's a private field)
        try {
            java.lang.reflect.Field balanceField = Wallet.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.set(wallet, 1000L); // Set a sufficient balance
        } catch (Exception e) {
            throw new RuntimeException("Could not set wallet balance", e);
        }

        // Create a transaction
        transaction = wallet.createTransaction("recipient", 100, 50);

        // Prepare a list of transactions
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Create a block
        block = new Block(
                "testHash",           // hash
                "parentHash",         // parent hash
                wallet.getAddress(),  // miner (using wallet address)
                System.currentTimeMillis(), // timestamp
                10,                   // difficulty
                1,                    // block number
                transactions          // transactions
        );
    }

    @Test
    void testBlockCreation() {
        assertNotNull(block, "Block should not be null");
        assertEquals("testHash", block.getHash(), "Block hash should match");
        assertEquals("parentHash", block.getParentHash(), "Parent hash should match");
        assertNotNull(block.getMiner(), "Miner should not be null");
        assertEquals(10, block.getDifficulty(), "Difficulty should match");
        assertEquals(1, block.getNumber(), "Block number should match");
    }

    @Test
    void testWalletGeneration() {
        assertNotNull(wallet, "Wallet should be created");
        assertNotNull(wallet.getAddress(), "Wallet address should not be null");
        assertTrue(wallet.getAddress().length() > 0, "Wallet address should not be empty");
    }

    @Test
    void testTransactionCreation() {
        assertNotNull(transaction, "Transaction should be created");
        assertEquals("recipient", transaction.getRecipient(), "Transaction recipient should match");
        assertEquals(100L, transaction.getValue(), "Transaction value should match");
    }

    @Test
    void testTransactionManagement() {
        List<Transaction> transactions = block.getTransactions();
        assertNotNull(transactions, "Transactions list should not be null");
        assertFalse(transactions.isEmpty(), "Transactions list should not be empty");
        assertEquals(1, transactions.size(), "Should have exactly one transaction");
        assertEquals(transaction, transactions.get(0), "Transaction should match the created transaction");
    }

    @Test
    void testBlockStateRoot() {
        assertNull(block.getStateRoot(), "Initial state root should be null");

        byte[] newStateRoot = new byte[]{0x01, 0x02, 0x03};
        block.setStateRoot(newStateRoot);
        assertArrayEquals(newStateRoot, block.getStateRoot(), "State root should be settable");
    }

    @Test
    void testBlockSetters() {
        block.setHash("newHash");
        assertEquals("newHash", block.getHash(), "Hash should be updatable");

        block.setParentHash("newParentHash");
        assertEquals("newParentHash", block.getParentHash(), "Parent hash should be updatable");

        block.setMiner("newMiner");
        assertEquals("newMiner", block.getMiner(), "Miner should be updatable");

        long newTimestamp = 123456789L;
        block.setTimestamp(newTimestamp);
        assertEquals(newTimestamp, block.getTimestamp(), "Timestamp should be updatable");

        block.setDifficulty(20);
        assertEquals(20, block.getDifficulty(), "Difficulty should be updatable");

        block.setNumber(2);
        assertEquals(2, block.getNumber(), "Block number should be updatable");
    }

    @Test
    void testTransactionModification() {
        List<Transaction> newTransactions = new ArrayList<>();
        Transaction newTransaction = new Transaction(new byte[]{0x01, 0x02});
        newTransactions.add(newTransaction);

        block.setTransactions(newTransactions);

        assertEquals(newTransactions, block.getTransactions(), "Transactions should be updatable");
        assertEquals(1, block.getTransactions().size(), "New transaction list size should be 1");
        assertEquals(newTransaction, block.getTransactions().get(0), "New transaction should match");
    }

    @Test
    void testToString() {
        String expectedString = "Block{" +
                "hash='testHash', " +
                "parentHash='parentHash', " +
                "miner='" + wallet.getAddress() + "', " +
                "timestamp=" + block.getTimestamp() + ", " +
                "difficulty=10, " +
                "number=1, " +
                "transactions=[" + transaction.toString() + "], " +
                "stateRoot=null}";

        assertEquals(expectedString, block.toString(), "toString method should match expected format");
    }

    @Test
    void testBlockValidation() {
        assertNotNull(block.getHash(), "Block hash should not be null");
        assertTrue(block.getTimestamp() > 0, "Timestamp should be a positive value");
        assertTrue(block.getDifficulty() >= 0, "Difficulty should be non-negative");
        assertNotNull(block.getTransactions(), "Transactions list should not be null");
    }
}