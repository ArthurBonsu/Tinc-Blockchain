# Project Setup & Testing Guide

## **1. Load Project Structure**
Before proceeding, ensure that you provide the AI agent with the project structure by uploading **ProjectStructure.txt**. This will help maintain consistency across the project, especially between the `main` and `test` folders.

## **2. Create Base Test File**
A **BaseTest** file should be included in the `test` folder, following the provided format. The test file should align with the structure and purpose of the files in the `main` folder.

## **3. Provide a Sample Test Folder**
Before generating test files, ensure that the AI agent understands the folder structure by sharing a sample of the test folder. This helps in maintaining accuracy when requesting a test file.

## **4. Ensure Consistency Between Main and Test Folders**
- Any test files created must correspond with files in the `main` folder, not just by name but by purpose.
- Additional files, such as interfaces or supporting classes, may be included in the `test` folder if required for context, but they must align with the purpose of the corresponding `main` folder.
- Clearly indicate which files you are testing, such as:
  ```
  ├───resources
  │   │                   
  │   │── peers.xml
  ```
- Supporting test files should be structured as:
  ```
  src/test/java/org/example/app/core/resources/TestMessageInterface.java
  src/test/java/org/example/app/core/resources/RealNetworkManager.java
  ```

## **5. Implement Logging with SLF4J**
Logging must be done using `org.slf4j.Logger` and `org.slf4j.LoggerFactory`.

Example setup:
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @BeforeEach
    void setUp() {
        logger.info("===== INITIALIZING TEST =====");
        logger.info("Network Mode: {}", NETWORK_MODE ? "NETWORK" : "LOCAL");
        logger.info("Peer Count: {}", TEST_PEER_COUNT);

        networkManager = NETWORK_MODE ? new RealNetworkManager() : new LocalNetworkManager();
        networkManager.initialize(TEST_PEER_COUNT);
        networkManager.start();

        logger.info("Network manager initialized in {} mode with {} peers", 
                NETWORK_MODE ? "NETWORK" : "LOCAL", TEST_PEER_COUNT);
    }
}
```

## **6. Update Gradle Dependencies**
Search for the keyword **"gradle"** and update `build.gradle` files (excluding utilities and list build files) with the following dependencies:

```gradle
dependencies {
    implementation 'org.slf4j:slf4j-api:1.7.36'
    implementation 'org.slf4j:slf4j-simple:1.7.36' // Choose a suitable implementation

    testImplementation 'org.slf4j:slf4j-simple:1.7.36'
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.0'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.0'
}
```

## **7. Ensure Gradle Build Success**
Continue debugging until you achieve a **BUILD SUCCESSFUL** status using Gradle.

## **8. Display Test Results in the Console**
Search for **"gradle"** and modify the relevant `build.gradle` files (excluding utilities and list) to include:

```gradle
test {
    useJUnitPlatform()
    
    testLogging {
        events "passed", "skipped", "failed", "standardOut", "standardError"
        showExceptions true
        showCauses true
        showStackTraces true
        
        // Show standard output and error streams
        showStandardStreams = true
    }
}
```
Run gradle test.Eg
gradle test --tests "org.example.app.core.resources.NetworkP2PTest"

## **9. Record and Review Test Results**
- Capture and store test results.
- Review logs to confirm the expected behavior.
- Paste results here or keep them for future reference.

---
This guide ensures consistency, proper logging, and structured testing for the project.
