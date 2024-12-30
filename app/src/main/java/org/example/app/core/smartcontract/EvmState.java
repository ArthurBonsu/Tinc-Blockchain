package org.example.app.core.smartcontract;

import java.util.HashMap;
import java.util.Map;

public class EvmState {
    private final Map<String, Account> accounts = new HashMap<>();

    // Retrieve or create an account for the given address
    public Account getAccount(String address) {
        return accounts.computeIfAbsent(address, k -> new Account());
    }

    // Store a key-value pair in the account's storage
    public void store(String address, String key, String value) {
        Account account = getAccount(address);
        account.getStorage().put(key, value);
    }

    // Load a value from the account's storage by key
    public String load(String address, String key) {
        Account account = getAccount(address);
        return account.getStorage().getOrDefault(key, "0");
    }

    // Create a new account in the state
    public void createAccount(String address, Account account) {
        accounts.put(address, account);
    }

    // Check if an account exists
    public boolean hasAccount(String address) {
        return accounts.containsKey(address);
    }
}
