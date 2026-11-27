package com.campus.placement.util;

import com.campus.placement.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configures {@code java.util.logging} with both a {@link ConsoleHandler} and a
 * rotating {@link FileHandler} writing to {@code logs/app.log}.
 */
// [Unit 3: java.util.logging (file + console handlers)]
public final class LoggerUtil {

    private static final String ROOT_PACKAGE = "com.campus.placement";
    private static boolean initialized = false;

    private LoggerUtil() {
    }

    /**
     * Initializes the root logger for the application package. Safe to call multiple times.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        Logger rootPkgLogger = Logger.getLogger(ROOT_PACKAGE);
        rootPkgLogger.setUseParentHandlers(false);
        rootPkgLogger.setLevel(Level.INFO);

        SimpleFormatter formatter = new SimpleFormatter();

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(formatter);
        rootPkgLogger.addHandler(consoleHandler);

        try {
            Path logsDir = Paths.get(AppConfig.getInstance().getLogsDir());
            Files.createDirectories(logsDir);
            String pattern = logsDir.resolve("app.log").toString();
            // 1 MB per file, keep 3 rotating files, append mode
            FileHandler fileHandler = new FileHandler(pattern, 1_048_576, 3, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(formatter);
            rootPkgLogger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Warning: Could not initialize file logger: " + e.getMessage());
        }

        initialized = true;
        rootPkgLogger.info("Logging initialized (console + rotating logs/app.log)");
    }

    /**
     * Returns a {@link Logger} configured for the given class.
     *
     * @param clazz target class
     * @return class logger
     */
    public static Logger getLogger(Class<?> clazz) {
        if (!initialized) {
            init();
        }
        return Logger.getLogger(clazz.getName());
    }
}
