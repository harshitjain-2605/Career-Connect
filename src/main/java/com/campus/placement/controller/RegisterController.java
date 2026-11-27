package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Student;
import com.campus.placement.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for {@code register.fxml} handling student self-registration with inline validation.
 */
public class RegisterController {

    @FXML private TextField rollNoField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> branchCombo;
    @FXML private TextField batchField;
    @FXML private TextField cgpaField;
    @FXML private TextField backlogsField;
    @FXML private TextArea skillsArea;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        branchCombo.getItems().addAll("CSE", "ECE", "ME", "EE", "CHE");
        branchCombo.getSelectionModel().selectFirst();
        errorLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        errorLabel.setText("");
        Student s = new Student();
        try {
            s.setRollNo(rollNoField.getText());
            s.setFullName(fullNameField.getText());
            s.setEmail(emailField.getText());
            s.setPhone(phoneField.getText());
            s.setBranch(branchCombo.getValue());
            s.setBatchYear(Integer.parseInt(batchField.getText().trim()));
            s.setCgpa(Double.parseDouble(cgpaField.getText().trim()));
            s.setActiveBacklogs(Integer.parseInt(backlogsField.getText().trim()));
            s.setSkills(skillsArea.getText());
        } catch (NumberFormatException e) {
            errorLabel.setText("Batch Year, CGPA, and Backlogs must be valid numbers.");
            return;
        }

        String password = passwordField.getText();
        registerButton.setDisable(true);

        SceneManager.runAsync(
                () -> authService.registerStudent(s, password),
                saved -> {
                    registerButton.setDisable(false);
                    SceneManager.showInfo("Registration Successful",
                            "Account created for " + saved.getFullName() + ". You can now sign in.");
                    SceneManager.switchToFxml("/fxml/login.fxml");
                },
                err -> {
                    registerButton.setDisable(false);
                    errorLabel.setText(err.getMessage());
                }
        );
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.switchToFxml("/fxml/login.fxml");
    }
}
