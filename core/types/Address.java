package org.tinc.types;

import java.util.Arrays;

public class Address {
    private static final int ADDRESS_LENGTH = 20; // Define the standard address length
    private final byte[] value;

    // Constructor to initialize Address with a byte array
    public Address(byte[] value) {
        if (value == null || value.length != ADDRESS_LENGTH) {
            throw new IllegalArgumentException(
                    "Invalid address length: expected " + ADDRESS_LENGTH + " bytes, but got " + (value == null ? "null" : value.length)
            );
        }
        this.value = Arrays.copyOf(value, ADDRESS_LENGTH);
    }

    // Converts Address to a byte slice (copy)
    public byte[] toSlice() {
        return Arrays.copyOf(value, ADDRESS_LENGTH);
    }

    // Converts Address to a hexadecimal string representation
    @Override
    public String toString() {
        StringBuilder hexString = new StringBuilder(ADDRESS_LENGTH * 2);
        for (byte b : value) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    // Factory method to create Address from a byte array
    public static Address fromBytes(byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException("Input byte array cannot be null");
        }
        return new Address(b);
    }

    // Retrieves a copy of the raw address value
    public byte[] getValue() {
        return Arrays.copyOf(value, ADDRESS_LENGTH);
    }

    // Compares Address objects for equality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Address address = (Address) obj;
        return Arrays.equals(value, address.value);
    }

    // Generates a hash code for the Address object
    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    // Utility method to create an Address from a hexadecimal string
    public static Address fromHexString(String hex) {
        if (hex == null || hex.length() != ADDRESS_LENGTH * 2) {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal string: expected " + (ADDRESS_LENGTH * 2) + " characters, but got " + (hex == null ? "null" : hex.length())
            );
        }
        byte[] bytes = new byte[ADDRESS_LENGTH];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return new Address(bytes);
    }
}
