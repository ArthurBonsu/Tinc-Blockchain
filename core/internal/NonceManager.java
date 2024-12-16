package core.internal;

import java.util.HashMap;
import java.util.Map;

public class NonceManager {

    private Map<String, Long> accountNonces;

    public NonceManager() {
        this.accountNonces = new HashMap<>();
    }

    // Get the current nonce for an account
    public long getNonce(String accountAddress) {
        return accountNonces.getOrDefault(accountAddress, 0L);
    }

    // Increment the nonce for an account
    public void incrementNonce(String accountAddress) {
        accountNonces.put(accountAddress, getNonce(accountAddress) + 1);
    }

    // Set a specific nonce for an account (useful for resuming from a failed state)
    public void setNonce(String accountAddress, long nonce) {
        accountNonces.put(accountAddress, nonce);
    }
}
