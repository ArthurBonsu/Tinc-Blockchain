package core.state;

import java.math.BigInteger;

public class StateObject {

    private String address;
    private BigInteger balance;
    private byte[] code;  // contract code, if applicable

    public StateObject(String address, BigInteger balance, byte[] code) {
        this.address = address;
        this.balance = balance;
        this.code = code;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }
}
