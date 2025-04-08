package org.example.app.core.state;

import java.util.HashMap;
import java.util.Map;

public class StateDB {

    private Map<String, StateObject> stateDatabase;

    public StateDB() {
        this.stateDatabase = new HashMap<>();
    }

    // Add or update a state object in the database
    public void put(String address, StateObject stateObject) {
        stateDatabase.put(address, stateObject);
    }

    // Retrieve a state object by its address
    public StateObject get(String address) {
        return stateDatabase.get(address);
    }

    // Remove a state object from the database
    public void remove(String address) {
        stateDatabase.remove(address);
    }

    // Get all state objects in the database
    public Map<String, StateObject> getAll() {
        return stateDatabase;
    }
}
