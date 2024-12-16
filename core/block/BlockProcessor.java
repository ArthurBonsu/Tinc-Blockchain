package core.block;

import core.state.StateTransition;
import core.eth.Blockchain;

public class BlockProcessor {
    private Blockchain blockchain;
    private StateTransition stateTransition;

    public BlockProcessor(Blockchain blockchain, StateTransition stateTransition) {
        this.blockchain = blockchain;
        this.stateTransition = stateTransition;
    }

    // Process the incoming block and add it to the chain
    public boolean processBlock(Block block) {
        // Validate block (simplified)
        if (!isValidBlock(block)) {
            System.out.println("Invalid Block!");
            return false;
        }

        // Apply state transition logic
        stateTransition.applyStateTransition(block);

        // Add block to blockchain
        blockchain.addBlock(block);
        return true;
    }

    // Simple block validation (e.g., check parent hash and difficulty)
    private boolean isValidBlock(Block block) {
        Block previousBlock = blockchain.getBlockByHash(block.getParentHash());
        if (previousBlock == null) {
            return false; // Parent block not found
        }

        if (block.getDifficulty() < previousBlock.getDifficulty()) {
            return false; // Difficulty must increase or remain the same
        }

        return true;
    }
}
