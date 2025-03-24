package org.example.app.core.state;

import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger; // Add import for BigInteger

public class StateManager {
    private StateDB stateDB;

    public StateManager(StateDB stateDB) {
        this.stateDB = stateDB;
    }

    // Update a state object in the database
    public void updateState(String address, BigInteger newBalance, byte[] newCode) {
        StateObject currentState = stateDB.get(address);
        if (currentState != null) {
            currentState.setBalance(newBalance);
            currentState.setCode(newCode);
            stateDB.put(address, currentState);
        } else {
            // If the state object doesn't exist, create a new one
            stateDB.put(address, new StateObject(address, newBalance, newCode));
        }
    }

    // Get the state object for a specific address
    public StateObject getState(String address) {
        return stateDB.get(address);
    }

    // Remove a state object
    public void removeState(String address) {
        stateDB.remove(address);
    }

    // Check if a state exists for an address
    public boolean stateExists(String address) {
        return stateDB.get(address) != null;
    }

    // Update balance for an existing state
    public void updateBalance(String address, BigInteger balanceChange) {
        StateObject currentState = stateDB.get(address);
        if (currentState != null) {
            BigInteger currentBalance = currentState.getBalance();
            currentState.setBalance(currentBalance.add(balanceChange));
            stateDB.put(address, currentState);
        }
    }
}