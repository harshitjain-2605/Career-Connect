package com.campus.placement.app;

import com.campus.placement.config.AppConfig;
import com.campus.placement.controller.AboutPolicyView;
import com.campus.placement.model.Role;
import com.campus.placement.model.Student;
import com.campus.placement.model.User;
import com.campus.placement.service.StudentService;
import com.campus.placement.util.LoggerUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central navigation, session state, async {@link Task} execution, and dialog helper.
 */
// [Unit 5: Stage/Scene navigation & JavaFX Task]
public final class SceneManager {

    private static final Logger LOGGER = LoggerUtil.getLogger(SceneManager.class);
    private static final String CSS_PATH = "/css/styles.css";

    private static Stage primaryStage;
    private static User currentUser;
    private static Student currentStudent;

    private SceneManager() {
    }

    /**
     * Initializes the SceneManager with the primary JavaFX {@link Stage}.
     */
    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle(AppConfig.getInstance().getAppTitle());
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(640);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static void setCurrentStudent(Student student) {
        currentStudent = student;
    }

    /**
     * Stores the authenticated user, loads associated student profile if applicable,
     * and routes to the appropriate role dashboard.
     */
    public static void loginAndRoute(User user) {
        currentUser = user;
        currentStudent = null;

        if (user.getRole() == Role.STUDENT) {
            try {
                currentStudent = new StudentService().getByUserId(user.getId()).orElse(null);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load student profile for userId=" + user.getId(), e);
                showError("Profile Error", "Could not load student profile: " + e.getMessage());
                return;
            }
            switchToFxml("/fxml/student_dashboard.fxml");
        } else if (user.getRole() == Role.ADMIN) {
            switchToFxml("/fxml/admin_dashboard.fxml");
        } else if (user.getRole() == Role.RECRUITER) {
            switchToFxml("/fxml/recruiter_dashboard.fxml");
        }
    }

    /**
     * Clears session state and returns to the Login screen.
     */
    public static void logout() {
        currentUser = null;
        currentStudent = null;
        switchToFxml("/fxml/login.fxml");
    }

    /**
     * Loads an FXML view and sets it as the active scene root.
     */
    public static void switchToFxml(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    SceneManager.class.getResource(fxmlPath),
                    "FXML not found: " + fxmlPath));
            Parent root = loader.load();
            setRoot(root);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + fxmlPath, e);
            showError("Navigation Error", "Unable to load screen: " + fxmlPath);
        }
    }

    /**
     * Displays the programmatically constructed Policy & System Info screen.
     */
    public static void showAboutPolicyView(Runnable onBack) {
        Parent root = AboutPolicyView.build(onBack);
        setRoot(root);
    }

    private static void setRoot(Parent root) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1080, 700);
            String css = Objects.requireNonNull(SceneManager.class.getResource(CSS_PATH)).toExternalForm();
            scene.getStylesheets().add(css);
            registerShortcuts(scene);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.show();
    }

    private static void registerShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN),
                () -> {
                    if (currentUser != null) {
                        logout();
                    }
                });
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.F1),
                () -> showAboutPolicyView(() -> {
                    if (currentUser == null) {
                        switchToFxml("/fxml/login.fxml");
                    } else {
                        loginAndRoute(currentUser);
                    }
                }));
    }

    /**
     * Runs a database/IO task on a background daemon thread without freezing the JavaFX UI thread.
     */
    public static <T> void runAsync(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOGGER.log(Level.SEVERE, "Background task failed", ex);
            onError.accept(ex);
        });
        Thread t = new Thread(task, "fx-worker");
        t.setDaemon(true);
        t.start();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }
}
