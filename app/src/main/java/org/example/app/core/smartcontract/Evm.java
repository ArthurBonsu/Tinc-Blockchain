package org.example.app.core.smartcontract;

import org.example.app.core.block.Transaction;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Evm {
    private static final Logger LOGGER = Logger.getLogger(Evm.class.getName());

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

    public Evm(EvmState state) {
        this.state = state;
    }

    public GasManager getGasManager() {
        return gasManager;
    }

    public void execute(byte[] bytecode, Transaction tx) {
        // Validate inputs
        if (bytecode == null || bytecode.length == 0) {
            throw new IllegalArgumentException("Bytecode cannot be null or empty");
        }

        // Reset EVM state
        stack.clear();
        pc = 0;
        running = true;

        // Set initial gas
        int gasLimit = tx.getGasLimit();
        gasManager.setInitialGas(gasLimit);

        while (running && pc < bytecode.length) {
            try {
                // Fetch the next opcode byte
                Opcode opcode = Opcode.fromValue(bytecode[pc]);
                pc++;

                // Consume gas for the operation
                gasManager.consumeGas(opcode.getGasCost());

                // Process opcode
                processOpcode(opcode, bytecode, tx);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "EVM Execution Error", e);
                running = false;
                throw new RuntimeException("EVM Execution Failed: " + e.getMessage(), e);
            }
        }
    }


    // Add a method to handle SUB operation
    private void executeSub() {
        if (stack.size() < 2) {
            throw new RuntimeException("Insufficient stack elements for SUB operation");
        }
        int b = stack.pop();
        int a = stack.pop();
        stack.push(a - b);
    }

    private void executeDiv() {
        if (stack.size() < 2) {
            throw new RuntimeException("Insufficient stack elements for DIV operation");
        }
        int b = stack.pop();
        int a = stack.pop();
        if (b == 0) {
            throw new RuntimeException("Division by zero");
        }
        stack.push(a / b);
    }

    private void processOpcode(Opcode opcode, byte[] bytecode, Transaction tx) {
        switch (opcode) {
            case STOP:
                running = false;
                break;
            case PUSH1:
                executePush1(bytecode);
                break;
            case ADD:
                executeAdd();
                break;
            case MUL:
                executeMul();
                break;
            case SUB:
                executeSub();
                break;
            case DIV:
                executeDiv();
                break;
            case SSTORE:
                executeSStore(tx);
                break;
            case SLOAD:
                executeSLoad(tx);
                break;
            case RETURN:
                running = false;
                break;
            case JUMP:
                executeJump();
                break;
            case JUMPI:
                executeConditionalJump();
                break;
            default:
                throw new IllegalArgumentException("Unsupported opcode: " + opcode);
        }
    }


    private void executePush1(byte[] bytecode) {
        if (pc >= bytecode.length) {
            throw new RuntimeException("Incomplete PUSH1 instruction");
        }
        int value = bytecode[pc] & 0xFF;
        stack.push(value);
        pc++;
    }

    private void executeAdd() {
        if (stack.size() < 2) {
            throw new RuntimeException("Insufficient stack elements for ADD operation");
        }
        int b = stack.pop();
        int a = stack.pop();
        stack.push(a + b);
    }

    private void executeMul() {
        if (stack.size() < 2) {
            throw new RuntimeException("Insufficient stack elements for MUL operation");
        }
        int b = stack.pop();
        int a = stack.pop();
        stack.push(a * b);
    }

    private void executeSStore(Transaction tx) {
        if (stack.size() < 2) {
            throw new RuntimeException("Insufficient stack elements for SSTORE operation");
        }
        int storeValue = stack.pop();
        int storeKey = stack.pop();
        state.store(
                tx.getRecipient(),
                String.valueOf(storeKey),
                String.valueOf(storeValue)
        );
    }

    private void executeSLoad(Transaction tx) {
        if (stack.isEmpty()) {
            throw new RuntimeException("Insufficient stack elements for SLOAD operation");
        }
        int loadKey = stack.pop();
        String loadedValue = state.load(
                tx.getRecipient(),
                String.valueOf(loadKey)
        );
        stack.push(Integer.parseInt(loadedValue));
    }

    private void executeJump() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Insufficient stack elements for JUMP operation");
        }
        pc = stack.pop();
    }

    private void executeConditionalJump() {
        if (stack.size() < 2) {
            throw new RuntimeException("Insufficient stack elements for JUMPI operation");
        }
        int condition = stack.pop();
        int jumpAddress = stack.pop();
        if (condition != 0) {
            pc = jumpAddress;
        }
    }
}