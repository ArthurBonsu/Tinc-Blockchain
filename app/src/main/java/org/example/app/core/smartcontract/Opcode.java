package org.example.app.core.smartcontract;

public enum Opcode {
    STOP(0x00, 0),
    ADD(0x01, 3),
    MUL(0x02, 5),
    SLOAD(0x54, 200),
    SSTORE(0x55, 5000),
    CALL(0xF1, 700),
    RETURN(0xF3, 0);

    private final int value;
    private final int gasCost;

    Opcode(int value, int gasCost) {
        this.value = value;
        this.gasCost = gasCost;
    }

    public int getValue() {
        return value;
    }

    public int getGasCost() {
        return gasCost;
    }

    public static Opcode fromValue(int value) {
        for (Opcode opcode : values()) {
            if (opcode.value == value) {
                return opcode;
            }
        }
        throw new IllegalArgumentException("Unknown opcode: " + value);
    }
}
