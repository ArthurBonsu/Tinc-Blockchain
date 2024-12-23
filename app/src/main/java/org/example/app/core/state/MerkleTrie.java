package core.state;

import java.util.HashMap;
import java.util.Map;

public class MerkleTrie {

    private Map<String, String> trieData;

    public MerkleTrie() {
        this.trieData = new HashMap<>();
    }

    // Insert or update a key-value pair in the Merkle Trie
    public void put(String key, String value) {
        trieData.put(key, value);
    }

    // Retrieve a value by key from the Merkle Trie
    public String get(String key) {
        return trieData.get(key);
    }

    // Remove a key-value pair from the Merkle Trie
    public void remove(String key) {
        trieData.remove(key);
    }

    // Get all the data in the trie
    public Map<String, String> getAll() {
        return trieData;
    }
}
