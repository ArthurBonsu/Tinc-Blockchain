package org.example.app.core.smartcontract;

import org.example.app.core.block.Block;
import org.example.app.core.consensus.Blockchain;
import org.example.app.core.types.Address;
import org.example.app.core.block.Transaction;
import org.example.app.core.types.Hash;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainEVM {
    public static void main(String[] args) {
        try {
            // Initialize blockchain and genesis block
            Logger logger = Logger.getLogger(MainEVM.class.getName());
            Blockchain blockchain = new Blockchain(); // Assuming default constructor

            // Initialize EVM state
            EvmState evmState = new EvmState();

            // Create genesis block
            Block genesisBlock = createGenesisBlock();

            // Deploy a sample contract using a transaction
            byte[] contractBytecode = new byte[]{
                    (byte) Evm.Opcode.ADD.ordinal(),
                    (byte) Evm.Opcode.RETURN.ordinal()
            };
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
        return new Block(
                null,           // hash
                null,           // parentHash
                "genesis_miner",// miner
                System.currentTimeMillis(), // timestamp
                0,              // difficulty
                0,              // block number
                new ArrayList<>() // empty transactions list
        );
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
        byte[] addressBytes = hexStringToByteArray(hexAddress);

        // Modify using existing methods
        tx.setRecipient(new String(addressBytes)); // Use setter for recipient
        tx.setValue(0L); // Use setter for value
        tx.setGasLimit(10_000); // Use setter for gasLimit

        return tx;
    }

    private static Block createBlock(Blockchain blockchain, Transaction tx) throws Exception {
        // Create a block with the transaction
        return new Block(
                null,           // hash
                null,           // parentHash
                "block_miner",  // miner
                System.currentTimeMillis(), // timestamp
                0,              // difficulty
                1,              // block number
                List.of(tx)     // transactions list
        );
    }
}