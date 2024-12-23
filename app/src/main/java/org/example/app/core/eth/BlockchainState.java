package core.eth;

import java.util.HashMap;
import java.util.Map;

public class BlockchainState {

    private Map<String, Long> accountBalances;
    private Map<String, Object> contractStates;

    public BlockchainState() {
        this.accountBalances = new HashMap<>();
        this.contractStates = new HashMap<>();
    }

    // Get the balance of an account
    public long getAccountBalance(String accountAddress) {
        return accountBalances.getOrDefault(accountAddress, 0L);
    }

    // Set the balance of an account
    public void setAccountBalance(String accountAddress, long balance) {
        accountBalances.put(accountAddress, balance);
    }

    // Get the state of a smart contract
    public Object getContractState(String contractAddress) {
        return contractStates.get(contractAddress);
    }

    // Set the state of a smart contract
    public void setContractState(String contractAddress, Object state) {
        contractStates.put(contractAddress, state);
    }
}
