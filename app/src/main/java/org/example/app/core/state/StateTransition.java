package org.example.app.core.state;

import org.example.app.core.block.Block;
import org.example.app.core.transaction.Transaction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigInteger;

public class StateTransition {
    private Map<String, AccountState> worldState;
    
    public StateTransition() {
        this.worldState = new ConcurrentHashMap<>();
    }
    
    public void applyStateTransition(Block block) {
        // 1. Validate block state transition
        if (!validateStateTransition(block)) {
            throw new IllegalStateException("Invalid state transition");
        }
        
        // 2. Process each transaction in the block
        for (Transaction tx : block.getTransactions()) {
            processTransaction(tx);
        }
        
        // 3. Apply block rewards
        applyBlockReward(block.getMiner());
        
        // 4. Update state root
        updateStateRoot(block);
    }
    
    private boolean validateStateTransition(Block block) {
        // Validate basic requirements
        if (block == null || block.getTransactions() == null) {
            return false;
        }
        
        // Check if all transactions are valid
        for (Transaction tx : block.getTransactions()) {
            if (!validateTransaction(tx)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean validateTransaction(Transaction tx) {
        // Get sender's account state
        AccountState senderState = worldState.get(tx.getSender());
        
        // Check if sender has enough balance
        if (senderState == null || senderState.getBalance().compareTo(tx.getValue().add(tx.getFee())) < 0) {
            return false;
        }
        
        // Check nonce
        if (tx.getNonce() != senderState.getNonce() + 1) {
            return false;
        }
        
        return true;
    }
    
    private void processTransaction(Transaction tx) {
        // Update sender's account
        AccountState senderState = worldState.computeIfAbsent(tx.getSender(), k -> new AccountState());
        senderState.decreaseBalance(tx.getValue().add(tx.getFee()));
        senderState.incrementNonce();
        
        // Update recipient's account
        AccountState recipientState = worldState.computeIfAbsent(tx.getRecipient(), k -> new AccountState());
        recipientState.increaseBalance(tx.getValue());
        
        // Handle contract creation or execution if necessary
        if (tx.isContractCreation()) {
            createContract(tx);
        } else if (tx.isContractCall()) {
            executeContract(tx);
        }
    }
    
    private void applyBlockReward(String minerAddress) {
        BigInteger blockReward = BigInteger.valueOf(2000000000000000000L); // 2 ETH reward
        AccountState minerState = worldState.computeIfAbsent(minerAddress, k -> new AccountState());
        minerState.increaseBalance(blockReward);
    }
    
    private void updateStateRoot(Block block) {
        // Calculate new state root using Merkle Patricia Trie
        byte[] newStateRoot = calculateStateRoot();
        block.setStateRoot(newStateRoot);
    }
    
    private byte[] calculateStateRoot() {
        // Implementation of Merkle Patricia Trie root calculation
        // This is a simplified version - in practice, you'd use a proper MPT implementation
        return new byte[32]; // Placeholder
    }
    
    // Helper class for account state
    private static class AccountState {
        private BigInteger balance;
        private long nonce;
        private byte[] storageRoot;
        private byte[] codeHash;
        
        public AccountState() {
            this.balance = BigInteger.ZERO;
            this.nonce = 0;
            this.storageRoot = new byte[32];
            this.codeHash = new byte[32];
        }
        
        public void increaseBalance(BigInteger amount) {
            this.balance = this.balance.add(amount);
        }
        
        public void decreaseBalance(BigInteger amount) {
            this.balance = this.balance.subtract(amount);
        }
        
        public void incrementNonce() {
            this.nonce++;
        }
        
        public BigInteger getBalance() {
            return balance;
        }
        
        public long getNonce() {
            return nonce;
        }
    }
    
    private void createContract(Transaction tx) {
        // Contract creation logic
        byte[] contractAddress = generateContractAddress(tx.getSender(), tx.getNonce());
        AccountState contractState = new AccountState();
        contractState.codeHash = generateCodeHash(tx.getData());
        worldState.put(new String(contractAddress), contractState);
    }
    
    private void executeContract(Transaction tx) {
        // Contract execution logic
        // This would involve running the EVM
        // Simplified implementation
    }
    
    private byte[] generateContractAddress(String sender, long nonce) {
        // Simple contract address generation
        // In practice, this would use RLP encoding and Keccak-256
        return new byte[20];
    }
    
    private byte[] generateCodeHash(byte[] code) {
        // Generate hash of contract code
        // In practice, this would use Keccak-256
        return new byte[32];
    }
}