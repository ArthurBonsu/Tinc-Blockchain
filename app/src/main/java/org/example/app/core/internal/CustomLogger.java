package org.example.app.core.internal;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CustomLogger {
    private final Logger logger;

    // Private constructor to prevent direct instantiation
    private CustomLogger(String name) {
        this.logger = Logger.getLogger(name);

        // Configure console handler with a custom formatter
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleLogFormatter());
        consoleHandler.setLevel(Level.ALL);

        // Remove existing handlers to prevent duplicate logging
        Logger rootLogger = Logger.getLogger("");
        if (rootLogger.getHandlers().length > 0) {
            rootLogger.getHandlers()[0].setLevel(Level.OFF);
        }

        // Add our custom handler
        logger.addHandler(consoleHandler);

        // Set logger level
        logger.setLevel(Level.ALL);

        // Prevent logging messages from being sent to parent loggers
        logger.setUseParentHandlers(false);
    }

    // Factory method for creating loggers
    public static CustomLogger getLogger(String name) {
        return new CustomLogger(name);
    }

    // Overloaded factory method to get logger for a specific class
    public static CustomLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
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

    // Log an error with an exception
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    // Custom log formatter for consistent logging output
    private static class SimpleLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format(
                    "[%1$tF %1$tT] [%2$s] %3$s: %4$s%n",
                    record.getMillis(),
                    record.getLevel(),
                    record.getSourceClassName(),
                    formatMessage(record)
            );
        }
    }
}