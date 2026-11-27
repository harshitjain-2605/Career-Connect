package com.campus.placement.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central application configuration loader backed by {@code config.properties}.
 * Uses a thread-safe singleton so all layers share the same settings.
 */
// [Unit 3: config.properties usage] & [Unit 1: static vs instance members]
public final class AppConfig {

    private static final String CONFIG_RESOURCE = "/config.properties";
    private static AppConfig instance;

    private final Properties properties;

    private AppConfig() {
        this.properties = new Properties();
        loadProperties();
    }

    /**
     * Returns the singleton {@code AppConfig} instance.
     *
     * @return shared AppConfig instance
     */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream in = AppConfig.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + CONFIG_RESOURCE);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_RESOURCE, e);
        }
    }

    /**
     * Returns a property value or a fallback default if absent.
     *
     * @param key          property key
     * @param defaultValue fallback value
     * @return trimmed property value or default
     */
    public String get(String key, String defaultValue) {
        String val = properties.getProperty(key);
        return val != null ? val.trim() : defaultValue;
    }

    /**
     * Returns the configured database dialect ({@code sqlite} or {@code mysql}).
     *
     * @return database dialect string
     */
    public String getDbDialect() {
        return get("db.dialect", "sqlite").toLowerCase();
    }

    /**
     * Returns the JDBC connection URL.
     *
     * @return JDBC URL
     */
    public String getDbUrl() {
        return get("db.url", "jdbc:sqlite:data/placement.db");
    }

    /**
     * Returns the JDBC username (empty for SQLite).
     *
     * @return database username
     */
    public String getDbUsername() {
        return get("db.username", "");
    }

    /**
     * Returns the JDBC password (empty for SQLite).
     *
     * @return database password
     */
    public String getDbPassword() {
        return get("db.password", "");
    }

    /**
     * Returns whether automatic schema/seed initialization is enabled.
     *
     * @return true if auto-init is enabled
     */
    public boolean isAutoInitEnabled() {
        return Boolean.parseBoolean(get("db.auto.init", "true"));
    }

    /**
     * Returns the configured placement policy ({@code SINGLE_OFFER} or {@code UPGRADE_1_5X}).
     *
     * @return placement policy key
     */
    public String getPlacementPolicy() {
        return get("placement.policy", "UPGRADE_1_5X").toUpperCase();
    }

    /**
     * Returns the directory path for storing uploaded student resumes.
     *
     * @return resumes directory path
     */
    public String getResumesDir() {
        return get("app.resumes.dir", "resumes");
    }

    /**
     * Returns the directory path for rotating log files.
     *
     * @return logs directory path
     */
    public String getLogsDir() {
        return get("app.logs.dir", "logs");
    }

    /**
     * Returns the directory path for serialized snapshot backups.
     *
     * @return backups directory path
     */
    public String getBackupsDir() {
        return get("app.backups.dir", "backups");
    }

    /**
     * Returns the application window title.
     *
     * @return application title
     */
    public String getAppTitle() {
        return get("app.title", "Campus Placement Management System");
    }
}
