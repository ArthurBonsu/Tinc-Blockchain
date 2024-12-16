package org.tinc.smartcontract;

import org.tinc.core.Block;
import org.tinc.core.Blockchain;
import org.tinc.core.Transaction;
import org.tinc.types.Address;
import org.tinc.types.Hash;
import org.tinc.types.List;

import java.util.logging.Logger;

public class MainEVM {

    public static void main(String[] args) {
        try {
            // Initialize blockchain and genesis block
            Logger logger = Logger.getLogger(MainEVM.class.getName());
            Blockchain blockchain = new Blockchain(logger, createGenesisBlock());

            // Initialize EVM state
            EvmState evmState = new EvmState();

            // Deploy a sample contract using a transaction
            byte[] contractBytecode = new byte[]{(byte) Opcode.ADD.getValue(), (byte) Opcode.RETURN.getValue()};
            Transaction contractTx = createTransaction(contractBytecode);

            // Add the transaction to the blockchain
            blockchain.addBlock(createBlock(blockchain, contractTx));

            // Initialize EVM and execute the contract
            Evm evm = new Evm(evmState);
            evm.execute(contractBytecode, contractTx);

            // Print remaining gas after execution
            System.out.println("EVM execution completed. Gas remaining: " + evm.getGasManager().getGasRemaining());
        } catch (Exception e) {
            System.err.println("Error during EVM execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Block createGenesisBlock() throws Exception {
        // Create a genesis block with dummy data
        Block.Header header = new Block.Header(
                1,                                // Block number
                new Hash(new byte[32]),           // Previous block hash
                new Hash(new byte[32]),           // State root
                0,                                // Difficulty
                System.currentTimeMillis()        // Timestamp
        );
        return new Block(header, new List<>());
    }
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static Transaction createTransaction(byte[] bytecode) {
        // Create a transaction object and initialize it
        Transaction tx = new Transaction(bytecode);

        // Decode a 20-byte address from the hex string
        String hexAddress = "0000000000000000000000000000000000000001"; // Remove "0x" prefix
        byte[] addressBytes = hexStringToByteArray(hexAddress); // Helper function to decode
        tx.setToAddress(new Address(addressBytes)); // Use decoded 20-byte array

        tx.setValue(0); // Set value to 0
        tx.setGasLimit(10_000); // Set gas limit
        return tx;
    }


    private static Block createBlock(Blockchain blockchain, Transaction tx) throws Exception {
        // Create a new block based on the previous header
        Block.Header prevHeader = blockchain.getHeader(blockchain.getHeight());
        List<Transaction> transactions = new List<>();
        transactions.insert(tx);
        return Block.createFromPrevHeader(prevHeader, transactions);
    }
}
