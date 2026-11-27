package com.campus.placement.db;

import com.campus.placement.config.AppConfig;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.util.LoggerUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes application runtime directories, database schema, and initial seed data
 * on first launch using {@code schema.sql} and {@code seed.sql}.
 */
// [Unit 3: Character streams] & [Unit 4: JDBC initialization]
public final class DatabaseInitializer {

    private static final Logger LOGGER = LoggerUtil.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    /**
     * Creates required runtime directories ({@code resumes/}, {@code logs/}, {@code backups/})
     * and initializes the database schema and seed data if {@code users} is empty.
     *
     * @throws DataAccessException if initialization fails
     */
    public static void initialize() throws DataAccessException {
        AppConfig config = AppConfig.getInstance();
        createAppDirectories(config);

        if (!config.isAutoInitEnabled()) {
            LOGGER.info("Database auto-init is disabled in config.properties.");
            return;
        }

        String dialect = config.getDbDialect();
        String schemaResource = "mysql".equals(dialect) ? "/schema-mysql.sql" : "/schema.sql";
        String seedResource = "mysql".equals(dialect) ? "/seed-mysql.sql" : "/seed.sql";

        try (Connection conn = ConnectionManager.getConnection()) {
            runSqlScript(conn, schemaResource);
            if (isDatabaseEmpty(conn)) {
                LOGGER.info("Empty database detected. Running seed script: " + seedResource);
                runSqlScript(conn, seedResource);
                LOGGER.info("Seed data loaded successfully.");
            } else {
                LOGGER.info("Database already initialized with existing data.");
            }
        } catch (SQLException | IOException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            throw new DataAccessException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /**
     * Runs only the schema script against the current {@link ConnectionManager} target.
     * Useful for JUnit tests using a fresh temporary SQLite database.
     *
     * @throws DataAccessException if schema execution fails
     */
    public static void initializeSchemaOnly() throws DataAccessException {
        try (Connection conn = ConnectionManager.getConnection()) {
            runSqlScript(conn, "/schema.sql");
        } catch (SQLException | IOException e) {
            throw new DataAccessException("Failed to initialize test schema: " + e.getMessage(), e);
        }
    }

    private static void createAppDirectories(AppConfig config) throws DataAccessException {
        try {
            Files.createDirectories(Paths.get(config.getResumesDir()));
            Files.createDirectories(Paths.get(config.getLogsDir()));
            Files.createDirectories(Paths.get(config.getBackupsDir()));
        } catch (IOException e) {
            throw new DataAccessException("Failed to create application directories", e);
        }
    }

    private static boolean isDatabaseEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static void runSqlScript(Connection conn, String resourcePath) throws IOException, SQLException {
        List<String> statements = parseSqlStatements(resourcePath);
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                stmt.execute(sql);
            }
        }
    }

    private static List<String> parseSqlStatements(String resourcePath) throws IOException {
        InputStream in = DatabaseInitializer.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("SQL script not found on classpath: " + resourcePath);
        }

        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                current.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    String sql = current.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1).trim();
                    }
                    if (!sql.isEmpty()) {
                        statements.add(sql);
                    }
                    current.setLength(0);
                }
            }
        }
        if (current.length() > 0) {
            String remaining = current.toString().trim();
            if (!remaining.isEmpty()) {
                statements.add(remaining);
            }
        }
        return statements;
    }
}
