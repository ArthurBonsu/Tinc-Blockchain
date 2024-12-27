package org.example.app.core.account;

import org.example.app.core.types.Address;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account {
    private final Address address;
    private long balance;

    public Account(Address address) {
        this.address = address;
        this.balance = 0;
    }

    public Address getAddress() {
        return address;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.valueOf(balance);
    }
}

