package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.User;
import com.campus.placement.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for {@code login.fxml} handling user authentication and role-based routing.
 */
// [Unit 5: FXML Controller & event handlers]
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        errorLabel.setText("");
        loginButton.setDisable(true);

        SceneManager.runAsync(
                () -> authService.login(email, password),
                (User user) -> {
                    loginButton.setDisable(false);
                    SceneManager.loginAndRoute(user);
                },
                err -> {
                    loginButton.setDisable(false);
                    errorLabel.setText(err.getMessage());
                }
        );
    }

    @FXML
    private void handleGoToRegister() {
        SceneManager.switchToFxml("/fxml/register.fxml");
    }

    @FXML
    private void handleAbout() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/login.fxml"));
    }
}
