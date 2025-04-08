public enum Opcode {
    STOP(0x00, 0),
    ADD(0x01, 3),
    MUL(0x02, 5),
    SUB(0x03, 3),
    DIV(0x04, 5),
    PUSH1(0x60, 3),
    SLOAD(0x54, 2100),
    SSTORE(0x55, 20000),
    RETURN(0xF3, 0),
    JUMP(0x56, 8),
    JUMPI(0x57, 10);

    private final int value;
    private final int gasCost;

    Opcode(int value, int gasCost) {
        this.value = value;
        this.gasCost = gasCost;
    }

    public static Opcode fromValue(byte value) {
        for (Opcode opcode : values()) {
            if (opcode.value == (value & 0xFF)) {
                return opcode;
            }
        }
        throw new IllegalArgumentException("Unknown opcode: " + String.format("0x%02X", value));
    }

    public int getGasCost() {
        return gasCost;
    }
}