package org.tinc.smartcontract;


public class GasManager {
    private int gasRemaining;

    public void setInitialGas(int gasLimit) {
        this.gasRemaining = gasLimit;
    }

    public void consumeGas(int amount) {
        if (gasRemaining < amount) {
            throw new RuntimeException("Out of gas");
        }
        gasRemaining -= amount;
    }

    public int getGasRemaining() {
        return gasRemaining;
    }


}
