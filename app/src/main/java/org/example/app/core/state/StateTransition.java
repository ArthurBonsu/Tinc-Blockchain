package org.example.app.core.state;

import java.math.BigInteger;
import org.example.app.core.state.StateManager;
import org.example.app.core.block.Transaction;

public class StateTransition {

    private StateManager stateManager;

    public StateTransition(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    // Process a transaction and apply state changes
    public void applyTransaction(Transaction tx) throws Exception {
        String sender = tx.getSender();
        String receiver = tx.getReceiver();
        BigInteger value = tx.getValue();
        BigInteger gasFee = tx.getGasFee();

        // Deduct the value and gas fee from the sender's balance
        StateObject senderState = stateManager.getState(sender);
        if (senderState == null || senderState.getBalance().compareTo(value.add(gasFee)) < 0) {
            throw new Exception("Insufficient balance for transaction");
        }
        senderState.setBalance(senderState.getBalance().subtract(value.add(gasFee)));

        // Credit the value to the receiver's balance
        StateObject receiverState = stateManager.getState(receiver);
        if (receiverState == null) {
            receiverState = new StateObject(receiver, BigInteger.ZERO, null);
        }
        receiverState.setBalance(receiverState.getBalance().add(value));

        // Update the state database
        stateManager.updateState(sender, senderState.getBalance(), senderState.getCode());
        stateManager.updateState(receiver, receiverState.getBalance(), receiverState.getCode());
    }
}