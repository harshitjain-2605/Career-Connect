package com.campus.placement.db;

import com.campus.placement.config.AppConfig;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.util.LoggerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages JDBC {@link Connection} creation, SQLite foreign-key enforcement,
 * and explicit transaction boundaries (commit/rollback).
 */
// [Unit 4: JDBC architecture & Transaction management]
public final class ConnectionManager {

    private static final Logger LOGGER = LoggerUtil.getLogger(ConnectionManager.class);

    private static String overrideUrl;
    private static String overrideUser;
    private static String overridePassword;

    private ConnectionManager() {
    }

    /**
     * Functional interface representing work executed inside a single JDBC transaction.
     *
     * @param <T> return type of the transactional operation
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        /**
         * Executes database operations using the supplied transactional connection.
         *
         * @param connection active connection with auto-commit disabled
         * @return operation result
         * @throws Exception if any SQL or business error occurs
         */
        T execute(Connection connection) throws Exception;
    }

    /**
     * Overrides the JDBC connection parameters (primarily used by JUnit tests
     * pointing to a temporary SQLite database).
     *
     * @param url      JDBC URL
     * @param user     username
     * @param password password
     */
    public static synchronized void configureOverride(String url, String user, String password) {
        overrideUrl = url;
        overrideUser = user;
        overridePassword = password;
    }

    /**
     * Clears any test connection override and reverts to {@link AppConfig}.
     */
    public static synchronized void clearOverride() {
        overrideUrl = null;
        overrideUser = null;
        overridePassword = null;
    }

    /**
     * Opens a new JDBC {@link Connection} using either test overrides or {@link AppConfig}.
     * Enables {@code PRAGMA foreign_keys = ON} for SQLite connections.
     *
     * @return open JDBC connection
     * @throws SQLException if connection creation fails
     */
    public static Connection getConnection() throws SQLException {
        AppConfig config = AppConfig.getInstance();
        String url = overrideUrl != null ? overrideUrl : config.getDbUrl();
        String user = overrideUser != null ? overrideUser : config.getDbUsername();
        String pass = overridePassword != null ? overridePassword : config.getDbPassword();

        ensureSqliteDirectory(url);

        Connection conn = (user == null || user.isEmpty())
                ? DriverManager.getConnection(url)
                : DriverManager.getConnection(url, user, pass);

        if (url.startsWith("jdbc:sqlite:")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return conn;
    }

    /**
     * Executes the given callback inside a single JDBC transaction.
     * Commits on success and rolls back on any error.
     *
     * @param callback transactional work unit
     * @param <T>      return type
     * @return value returned by the callback
     * @throws DataAccessException if the transaction fails and rolls back
     */
    // [Unit 4: Transaction management (commit/rollback)]
    public static <T> T executeInTransaction(TransactionCallback<T> callback) throws DataAccessException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            T result = callback.execute(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.warning("Transaction rolled back due to: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Failed to rollback transaction", rollbackEx);
                }
            }
            if (e instanceof DataAccessException dae) {
                throw dae;
            }
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    LOGGER.log(Level.WARNING, "Failed to close connection", closeEx);
                }
            }
        }
    }

    private static void ensureSqliteDirectory(String url) {
        if (url == null || !url.startsWith("jdbc:sqlite:") || url.contains(":memory:")) {
            return;
        }
        String filePart = url.substring("jdbc:sqlite:".length());
        int queryIdx = filePart.indexOf('?');
        if (queryIdx >= 0) {
            filePart = filePart.substring(0, queryIdx);
        }
        Path dbPath = Paths.get(filePart);
        Path parent = dbPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not create SQLite parent directory: " + parent, e);
            }
        }
    }
}
