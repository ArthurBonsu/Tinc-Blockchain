Here's the formatted GitHub instruction file based on your requirements:

---

# GitHub Project Instructions

### Done Today:
- Full project committed to GitHub

### TODOS:

#### A) Completion of Configuration
1. **GitHub Configuration**  
2. **IntelliJ Configuration**  
    - Ensure all configurations and builds are set up properly.

---

### Next Stage:

1. **Base Test**
2. **Process Test**
3. **Mergers**

---

#### Base Test Overview:
- The **Base Test** will be the simplest form of testing, to be carried out in the upcoming weeks. It will serve as the foundation for more advanced process tests.

---

### Project Folder Structure

```
pp
    │   │               └───core
    │   │                   ├───account
    │   │                   ├───api
    │   │                   │   ├───handlers
    │   │                   │   ├───rest
    │   │                   │   └───rpc
    │   │                   ├───block
    │   │                   │   └───transaction
    │   │                   ├───build
    │   │                   │   ├───distributions
    │   │                   │   ├───libs
    │   │                   │   ├───reports
    │   │                   │   │   └───problems
    │   │                   │   ├───scripts
    │   │                   │   └───tmp
    │   │                   │       └───jar
    │   │                   ├───config
    │   │                   ├───consensus
    │   │                   ├───crypto
    │   │                   ├───encoder
    │   │                   ├───eth
    │   │                   ├───evm
    │   │                   ├───internal
    │   │                   ├───lib
    │   │                   ├───mempool
    │   │                   ├───metrics
    │   │                   ├───network
    │   │                   ├───p2p
    │   │                   ├───pbftconsensus
    │   │                   ├───resources
    │   │                   ├───secure_keys
    │   │                   ├───smartcontract
    │   │                   ├───state
    │   │                   ├───storage
    │   │                   ├───types
    │   │                   └───wallet
```

---

### Allocation:

- **Elvis**:  
  - Merger of Codebase 1 and Codebase 2  
  - Folder Allocation:  
    - i) `account`  
    - ii) `block`  
- **Sample**:  
  - i) `config`  
  - ii) `consensus`  
  - iii) `crypto`  
- **Edson**:  
  - i) `encoder`  
  - ii) `eth`  
  - iii) `evm`  
- **Wilfred**:  
  - i) `internal`  
  - ii) `lib`  
  - iii) `mempool`  
- **Putra**:  
  - i) `metrics`  
  - ii) `network`  
  - iii) `p2p`  
  - iv) `pbftconsensus`  
- **Arthur**:  
  - i) `resources`  
  - ii) `secure_keys`  
  - iii) `smartcontract`  
  - iv) `state`  
- **Grace**:  
  - i) `storage`  
  - ii) `types`  
  - iii) `wallet`  

---

### Purpose:
To get everything configured and to test files within a specific module.

---

### How to Do the Base Test:

1. **Create a filename**:  
   Create a test file corresponding to the folder name in the main directory. The test file should follow this format: `Base + folder_name + Test`.  
   For example:  
   ```
   └───test
        ├───java
        │   └───org
        │       └───example
        │           └───app
        │               └───core
        │                   └───block
                         BaseBlockTest
   ```

2. **Upload files to an AI tool**:  
   Upload the contents of the folder you are testing (e.g., `block`) into an AI tool like Claude or ChatGPT.  
   Example files in the `block` folder:
   ```
   ├───block
       │   │   Block.java
       │   │   BlockHeader.java
       │   │   BlockProcessor.java
       │   │   BlockValidator.java
       │   │   Miner.java
       │   │   Transaction.java
       │   │   TransactionManager.java
       │   │   Validator.java
       │   └───transaction
               Transaction.java
   ```

3. **Generate a comprehensive test file**:  
   In the AI tool, request a comprehensive test file that can test the components within the folder and output the results.  
   Example prompt to AI:  
   ```
   With the files in the `block` folder provided, generate a comprehensive test file to test the components within and output the results.
   ```

4. **Save the test files**:  
   Save the generated test file inside the `test` folder with the appropriate name, following the naming convention: `Base + folder_name + Test`.

5. **Commit to the branch**:  
   Once the test file is ready, commit it to your assigned branch (not `main`).  
   If you don’t have a branch, I will create one for you.

---

### Notes:
- Please ensure that all files are tested within their respective modules.
- Follow the naming conventions strictly to maintain consistency across the project.
- Be sure to commit to the correct branch unless given explicit permission to merge to `main`.

- Considerations
- Account for uninitialized values
- Other error considerations

--- 
## Wallet Testing Overview and Considerations

This test suite ensures proper wallet initialization, balance verification, local execution, and robust error handling.

### 1. No Wallets Initialized
- Each test starts from a **clean state**, creating a new wallet with a unique identifier.
- If wallet initialization fails, the test will fail with an **appropriate error message**.
- `testCreateAndLoadWallet()` specifically verifies the **wallet initialization process**.

### 2. No Balance
- `testGetAccountBalance()` ensures that a newly created wallet has a **zero balance**.
- The test explicitly asserts this with:
  ```java
  assertEquals(BigInteger.ZERO, balance, "New wallet should have zero balance");
3. Running from a Single PC
The tests are designed to run locally without requiring an external network connection.

A local test directory (test-wallets) is used for storage.

Each test operates independently, avoiding dependencies on external resources.

Each test creates a unique wallet file to prevent conflicts.

Cleanup logic ensures that test files are removed after execution.

4. Error Handling
Try-catch blocks are used to handle and report exceptions gracefully.

testWalletSecurity() explicitly tests for incorrect password handling.

The tearDown() method ensures proper cleanup, even if tests fail

