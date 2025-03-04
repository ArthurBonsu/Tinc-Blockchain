package org.example.app.core.block;

import org.example.app.core.types.Hash;
import org.example.app.core.consensus.Blockchain;
import org.example.app.core.consensus.HashUtils;
import java.util.List;
import java.math.BigInteger;

public class BlockValidator implements Validator {
    private final Blockchain blockchain;

    // Validation constants
    private static final long MAX_BLOCK_FUTURE_TIME = 15 * 60 * 1000; // 15 minutes in milliseconds
    private static final long MAX_TRANSACTIONS_PER_BLOCK = 1000;
    private static final BigInteger MAX_BLOCK_GAS_LIMIT = BigInteger.valueOf(30_000_000); // Example gas limit
    private static final long MAX_NONCE = Long.MAX_VALUE / 2;
    private static final int MAX_BLOCK_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

    public BlockValidator(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public boolean validateBlock(Block block) throws Exception {
        try {
            // Comprehensive block validation
            validateBlockBasics(block);
            validateBlockHeight(block);
            validatePreviousHash(block);
            validateTimestamp(block);
            validateTransactions(block);
            validateBlockSize(block);
            validateBlockReward(block);
            validateProofOfWork(block);

            return true;
        } catch (Exception e) {
            System.err.println("Block validation failed: " + e.getMessage());
            return false;
        }
    }

    // Basic block validation methods
    private void validateBlockBasics(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }

        if (block.getMiner() == null || block.getMiner().isEmpty()) {
            throw new IllegalStateException("Block must have a valid miner address");
        }
    }

    private void validateBlockHeight(Block block) {
        long blockHeight = block.getNumber();

        // Ensure block height is sequential
        if (blockHeight != getCurrentBlockchainHeight() + 1) {
            throw new IllegalStateException(
                    String.format(
                            "Invalid block height. Expected: %d, Found: %d",
                            getCurrentBlockchainHeight() + 1,
                            blockHeight
                    )
            );
        }
    }

    private void validatePreviousHash(Block block) {
        Hash expectedPrevHash = getPreviousBlockHash(block.getNumber() - 1);
        if (!expectedPrevHash.equals(block.getParentHash())) {
            throw new IllegalStateException(
                    String.format(
                            "Previous block hash mismatch. Expected: %s, Found: %s",
                            expectedPrevHash,
                            block.getParentHash()
                    )
            );
        }
    }

    private void validateTimestamp(Block block) {
        long currentTime = System.currentTimeMillis();
        long blockTime = block.getTimestamp();

        // Check if block timestamp is in the future
        if (blockTime > currentTime + MAX_BLOCK_FUTURE_TIME) {
            throw new IllegalStateException("Block timestamp is too far in the future");
        }

        // Check if block timestamp is earlier than previous block
        long previousBlockTime = getPreviousBlockTimestamp();
        if (blockTime <= previousBlockTime) {
            throw new IllegalStateException("Block timestamp must be later than previous block");
        }
    }

    private void validateTransactions(Block block) {
        List<Transaction> transactions = block.getTransactions();

        // Check transaction count
        if (transactions == null) {
            throw new IllegalStateException("Block must have a transaction list");
        }

        if (transactions.size() > MAX_TRANSACTIONS_PER_BLOCK) {
            throw new IllegalStateException("Exceeded maximum transactions per block");
        }

        // Validate each transaction
        BigInteger totalGasUsed = BigInteger.ZERO;
        for (Transaction tx : transactions) {
            validateSingleTransaction(tx);
            totalGasUsed = totalGasUsed.add(calculateTransactionGas(tx));
        }

        // Check total block gas limit
        if (totalGasUsed.compareTo(MAX_BLOCK_GAS_LIMIT) > 0) {
            throw new IllegalStateException("Block gas limit exceeded");
        }
    }

    private void validateSingleTransaction(Transaction tx) {
        if (tx == null) {
            throw new IllegalStateException("Transaction cannot be null");
        }

        if (tx.getSender() == null || tx.getSender().isEmpty()) {
            throw new IllegalStateException("Transaction must have a valid sender");
        }

        if (tx.getRecipient() == null || tx.getRecipient().isEmpty()) {
            throw new IllegalStateException("Transaction must have a valid recipient");
        }
    }

    private void validateBlockSize(Block block) {
        int blockSize = calculateBlockSize(block);
        if (blockSize > MAX_BLOCK_SIZE_BYTES) {
            throw new IllegalStateException("Block size exceeds maximum limit");
        }
    }

    private void validateBlockReward(Block block) {
        long blockNumber = block.getNumber();
        BigInteger expectedBlockReward = calculateBlockReward(blockNumber);
        // Additional block reward validation can be added here
    }

    private void validateProofOfWork(Block block) {
        if (!isProofOfWorkValid(block)) {
            throw new IllegalStateException("Invalid proof of work");
        }
    }

    // Helper methods for blockchain and block-related operations
    private long getCurrentBlockchainHeight() {
        // Placeholder - replace with actual blockchain height retrieval
        return 0;
    }

    private Hash getPreviousBlockHash(long previousBlockHeight) {
        // Placeholder - replace with actual previous block hash retrieval
        return new Hash(new byte[32]);
    }

    private long getPreviousBlockTimestamp() {
        // Placeholder - replace with actual previous block timestamp retrieval
        return 0;
    }

    private int calculateBlockSize(Block block) {
        return block.toString().getBytes().length;
    }

    private BigInteger calculateBlockReward(long blockNumber) {
        // Simplified block reward calculation
        return BigInteger.valueOf(2_000_000_000_000_000_000L); // 2 ETH
    }

    private BigInteger calculateTransactionGas(Transaction tx) {
        BigInteger gasPrice = tx.getGasPrice();
        long gasLimit = 21000; // Default gas limit
        return gasPrice.multiply(BigInteger.valueOf(gasLimit));
    }

    // Proof of Work Validation Methods
    private boolean isProofOfWorkValid(Block block) {
        String blockHash = block.getHash();
        long difficulty = block.getDifficulty();

        // Validate hash format
        if (blockHash == null || blockHash.isEmpty()) {
            return false;
        }

        // Convert hash to numeric representation
        BigInteger hashNumeric = convertHashToBigInteger(blockHash);

        // Calculate target based on difficulty
        BigInteger target = calculateDifficultyTarget(difficulty);

        // Proof of work validation: hash must be less than target
        return hashNumeric.compareTo(target) < 0;
    }

    private BigInteger convertHashToBigInteger(String hash) {
        try {
            // Remove '0x' prefix if present
            if (hash.startsWith("0x")) {
                hash = hash.substring(2);
            }

            // Ensure consistent length and handle potential truncation
            hash = hash.length() > 64 ? hash.substring(0, 64) : hash;

            return new BigInteger(hash, 16);
        } catch (NumberFormatException e) {
            // Fallback to alternative conversion if hex parsing fails
            return new BigInteger(hash.getBytes());
        }
    }

    private BigInteger calculateDifficultyTarget(long difficulty) {
        // Basic difficulty calculation: Target = 2^256 / difficulty
        BigInteger maxTarget = BigInteger.valueOf(2).pow(256);

        // Prevent division by zero
        if (difficulty <= 0) {
            return maxTarget;
        }

        return maxTarget.divide(BigInteger.valueOf(difficulty));
    }
}