package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Student;
import com.campus.placement.service.StudentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller for {@code student_profile.fxml} supporting profile editing and
 * stream-based resume file upload into {@code resumes/}.
 */
public class StudentProfileController {

    @FXML private TextField rollNoField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> branchCombo;
    @FXML private TextField batchField;
    @FXML private TextField cgpaField;
    @FXML private TextField backlogsField;
    @FXML private TextArea skillsArea;
    @FXML private Label resumePathLabel;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final StudentService studentService = new StudentService();

    @FXML
    private void initialize() {
        branchCombo.getItems().addAll("CSE", "ECE", "ME", "EE", "CHE");
        errorLabel.setText("");

        Student s = SceneManager.getCurrentStudent();
        if (s != null) {
            rollNoField.setText(s.getRollNo());
            emailField.setText(s.getEmail());
            fullNameField.setText(s.getFullName());
            phoneField.setText(s.getPhone());
            branchCombo.setValue(s.getBranch());
            batchField.setText(String.valueOf(s.getBatchYear()));
            cgpaField.setText(String.format("%.2f", s.getCgpa()));
            backlogsField.setText(String.valueOf(s.getActiveBacklogs()));
            skillsArea.setText(s.getSkills());
            resumePathLabel.setText(s.getResumePath() != null && !s.getResumePath().isBlank()
                    ? s.getResumePath() : "No resume uploaded");
        }
    }

    @FXML
    private void handleSaveProfile() {
        errorLabel.setText("");
        Student s = SceneManager.getCurrentStudent();
        if (s == null) {
            return;
        }
        try {
            s.setFullName(fullNameField.getText());
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

        saveButton.setDisable(true);
        SceneManager.runAsync(() -> {
            studentService.updateStudent(s, s.getUserId());
            return s;
        }, updated -> {
            saveButton.setDisable(false);
            SceneManager.setCurrentStudent(updated);
            SceneManager.showInfo("Profile Saved", "Your profile has been updated.");
        }, err -> {
            saveButton.setDisable(false);
            errorLabel.setText(err.getMessage());
        });
    }

    @FXML
    private void handleUploadResume() {
        Student s = SceneManager.getCurrentStudent();
        if (s == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Resume");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resume Files", "*.pdf", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = chooser.showOpenDialog(SceneManager.getPrimaryStage());
        if (file == null) {
            return;
        }

        SceneManager.runAsync(
                () -> studentService.uploadResume(s, file, s.getUserId()),
                storedPath -> {
                    resumePathLabel.setText(storedPath);
                    SceneManager.showInfo("Resume Uploaded", "Resume saved to: " + storedPath);
                },
                err -> errorLabel.setText(err.getMessage())
        );
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/student_dashboard.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/student_drives.fxml"); }
    @FXML private void navApplications() { SceneManager.switchToFxml("/fxml/student_applications.fxml"); }
    @FXML private void navProfile() { SceneManager.switchToFxml("/fxml/student_profile.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/student_profile.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }
}
