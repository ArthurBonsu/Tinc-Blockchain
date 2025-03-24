package org.tinc.app.core.smartcontract;

import java.nio.ByteBuffer;
import java.util.Stack;

enum Instruction {
    InstrPushInt(0x0a),
    InstrAdd(0x0b),
    InstrPushByte(0x0c),
    InstrPack(0x0d),
    InstrSub(0x0e),
    InstrStore(0x0f);

    private final int value;

    Instruction(int value) {
        this.value = value;
    }

    public static Instruction fromByte(byte b) {
        for (Instruction instr : values()) {
            if (instr.value == (b & 0xFF)) {
                return instr;
            }
        }
        throw new IllegalArgumentException("Unknown instruction: " + b);
    }
}

class StackVM {
    private final Object[] data;
    private int sp; // Stack pointer

    public StackVM(int size) {
        this.data = new Object[size];
        this.sp = 0;
    }

    public void push(Object value) {
        if (sp >= data.length) {
            throw new IllegalStateException("Stack overflow");
        }
        data[sp++] = value;
    }

    public Object pop() {
        if (sp <= 0) {
            throw new IllegalStateException("Stack underflow");
        }
        return data[--sp];
    }
}

public class VM {
    private final byte[] data;
    private int ip; // Instruction pointer
    private final StackVM stack;
    private final State contractState;

    public VM(byte[] data, State contractState) {
        this.data = data;
        this.ip = 0;
        this.stack = new StackVM(128); // Initialize with stack size
        this.contractState = contractState;
    }

    public void run() throws Exception {
        while (ip < data.length) {
            Instruction instr = Instruction.fromByte(data[ip++]);
            execute(instr);
        }
    }

    private void execute(Instruction instr) throws Exception {
        switch (instr) {
            case InstrStore:
                byte[] key = (byte[]) stack.pop();
                Object value = stack.pop();
                byte[] serializedValue;

                if (value instanceof Integer) {
                    serializedValue = serializeInt64((int) value);
                } else {
                    throw new IllegalArgumentException("Unknown type: " + value.getClass());
                }

                contractState.put(key, serializedValue);
                break;

            case InstrPushInt:
                stack.push((int) data[ip++]);
                break;

            case InstrPushByte:
                stack.push((byte) data[ip++]);
                break;

            case InstrPack:
                int n = (int) stack.pop();
                byte[] packedBytes = new byte[n];
                for (int i = 0; i < n; i++) {
                    packedBytes[i] = (byte) stack.pop();
                }
                stack.push(packedBytes);
                break;

            case InstrSub:
                int b = (int) stack.pop();
                int a = (int) stack.pop();
                stack.push(a - b);
                break;

            case InstrAdd:
                b = (int) stack.pop();
                a = (int) stack.pop();
                stack.push(a + b);
                break;

            default:
                throw new IllegalArgumentException("Unsupported instruction: " + instr);
        }
    }

    private byte[] serializeInt64(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(value);
        return buffer.array();
    }

    private int deserializeInt64(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return (int) buffer.getLong();
    }

}


