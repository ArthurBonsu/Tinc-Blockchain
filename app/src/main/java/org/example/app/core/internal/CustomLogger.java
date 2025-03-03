package org.example.app.core.internal;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger as JavaLogger; // Use an alias to avoid naming conflict

public class CustomLogger { // Rename the class to avoid conflict

    private JavaLogger logger; // Use the aliased import

    public CustomLogger(String name) { // Update constructor name
        this.logger = JavaLogger.getLogger(name);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
    }

    // Log an info message
    public void info(String message) {
        logger.info(message);
    }

    // Log a debug message
    public void debug(String message) {
        logger.fine(message);
    }

    // Log a warning message
    public void warn(String message) {
        logger.warning(message);
    }

    // Log an error message
    public void error(String message) {
        logger.severe(message);
    }
}