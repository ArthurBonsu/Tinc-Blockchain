package org.example.app.core.smartcontract;
import java.util.Arrays;

public class Memory {
    private byte[] memory = new byte[1024];

    public void store(int offset, byte[] data) {
        System.arraycopy(data, 0, memory, offset, data.length);
    }

    public byte[] load(int offset, int length) {
        return Arrays.copyOfRange(memory, offset, offset + length);
    }
}
