package org.example.app.core.smartcontract;

import org.example.app.core.block.Transaction;
import org.example.app.core.types.Address;
import java.util.Stack;

public class Evm {
    private final Stack<Integer> stack = new Stack<>();
    private final Memory memory = new Memory();
    private final GasManager gasManager = new GasManager();
    private final EvmState state;
    private int pc = 0; // Program Counter
    private boolean running = true;

    // Enum for Opcodes
    public enum Opcode {
        STOP(0x00, 0),
        ADD(0x01, 3),
        MUL(0x02, 5),
        SSTORE(0x55, 20000),
        SLOAD(0x54, 2100),
        RETURN(0xf3, 0),
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

    public Evm(EvmState state) {
        this.state = state;
    }

    // Provide access to GasManager
    public GasManager getGasManager() {
        return gasManager;
    }

    public void execute(byte[] bytecode, Transaction tx) {
        // Use gasLimit field directly
        int gasLimit = tx.getGasLimit();
        gasManager.setInitialGas(gasLimit);

        while (running && pc < bytecode.length) {
            try {
                // Fetch the next opcode byte
                Opcode opcode = Opcode.fromValue(bytecode[pc]);
                pc++;

                // Check and consume the appropriate gas
                gasManager.consumeGas(opcode.getGasCost());

                switch (opcode) {
                    case STOP:
                        running = false;
                        break;
                    case ADD:
                        // POP two operands, ADD them, then PUSH the result
                        stack.push(stack.pop() + stack.pop());
                        break;
                    case MUL:
                        // POP two operands, MULTIPLY them, then PUSH the result
                        stack.push(stack.pop() * stack.pop());
                        break;
                    case SSTORE:
                        // POP key and value, store them in state
                        int storeKey = stack.pop();
                        int storeValue = stack.pop();
                        // Use recipient as contract address
                        state.store(
                                tx.getRecipient(),
                                String.valueOf(storeKey),
                                String.valueOf(storeValue)
                        );
                        break;
                    case SLOAD:
                        // POP key, load the value from state and PUSH it
                        int loadKey = stack.pop();
                        String loadedValue = state.load(
                                tx.getRecipient(),
                                String.valueOf(loadKey)
                        );
                        stack.push(Integer.parseInt(loadedValue));
                        break;
                    case RETURN:
                        // Return opcode to stop execution
                        running = false;
                        break;
                    case JUMP:
                        // Jumps to a specific program counter address
                        pc = stack.pop();
                        break;
                    case JUMPI:
                        // Conditional jump, based on a value on the stack
                        int condition = stack.pop();
                        int jumpAddress = stack.pop();
                        if (condition != 0) {
                            pc = jumpAddress;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown opcode: " + opcode);
                }
            } catch (Exception e) {
                System.err.println("EVM Execution Error: " + e.getMessage());
                e.printStackTrace();
                running = false;
            }
        }
    }
}