package org.example.app.core.state;

import java.util.HashMap;
import java.util.Map;

public class WorldState {

    private Map<String, StateObject> stateObjects;

    public WorldState() {
        this.stateObjects = new HashMap<>();
    }

    // Add or update a state object in the world state
    public void put(String address, StateObject stateObject) {
        stateObjects.put(address, stateObject);
    }

    // Retrieve a state object by address
    public StateObject get(String address) {
        return stateObjects.get(address);
    }

    // Get all state objects in the world state
    public Map<String, StateObject> getAll() {
        return stateObjects;
    }
}
