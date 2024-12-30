package org.example.app.core.internal;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logger {

    private java.util.logging.Logger logger;

    public Logger(String name) {
        this.logger = java.util.logging.Logger.getLogger(name);
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
