package com.campus.placement.app;

import com.campus.placement.db.DatabaseInitializer;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.util.LoggerUtil;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main JavaFX entry point for the Campus Placement Management System.
 * Demonstrates the JavaFX {@link Application} lifecycle ({@code init}, {@code start}, {@code stop}).
 */
// [Unit 5: JavaFX Application lifecycle (init/start/stop)]
public class MainApp extends Application {

    private static final Logger LOGGER = LoggerUtil.getLogger(MainApp.class);

    /**
     * Initializes logging, configuration, and database schema/seed data before the UI starts.
     */
    @Override
    public void init() {
        LoggerUtil.init();
        LOGGER.info("Application init() started.");
        try {
            DatabaseInitializer.initialize();
        } catch (DataAccessException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database during init()", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Initializes {@link SceneManager} and displays the Login screen.
     *
     * @param primaryStage primary application window
     */
    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage);
        SceneManager.switchToFxml("/fxml/login.fxml");
        LOGGER.info("Primary Stage displayed with Login screen.");
    }

    /**
     * Performs cleanup when the JavaFX application exits.
     */
    @Override
    public void stop() {
        LOGGER.info("Application stop() called. Shutting down cleanly.");
    }

    /**
     * Application launcher.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error launching application", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Fatal error: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
