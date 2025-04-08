package org.example.app.core.types;

import java.util.Arrays;

public class Hash {
    private final byte[] value;

    public Hash(byte[] value) {
        if (value.length != 32) {
            throw new IllegalArgumentException(
                    "Given bytes with length " + value.length + " should be 32"
            );
        }
        this.value = Arrays.copyOf(value, 32);
    }

    public boolean isZero() {
        for (byte b : value) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public byte[] toSlice() {
        return Arrays.copyOf(value, 32);
    }

    @Override
    public String toString() {
        StringBuilder hexString = new StringBuilder();
        for (byte b : value) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static Hash fromBytes(byte[] b) {
        return new Hash(b);
    }

    // Optional getter for raw value
    public byte[] getValue() {
        return Arrays.copyOf(value, 32);
    }
}
