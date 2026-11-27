package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Student;
import com.campus.placement.service.StudentService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for {@code admin_students.fxml} supporting search, filter, sort, edit, block/unblock, and delete.
 */
public class AdminStudentsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> branchFilterCombo;
    @FXML private TextField minCgpaField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> colRoll;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colBranch;
    @FXML private TableColumn<Student, Number> colCgpa;
    @FXML private TableColumn<Student, Number> colBacklogs;
    @FXML private TableColumn<Student, String> colBlocked;

    @FXML private TextField editNameField;
    @FXML private TextField editPhoneField;
    @FXML private ComboBox<String> editBranchCombo;
    @FXML private TextField editCgpaField;
    @FXML private TextField editBacklogsField;
    @FXML private TextArea editSkillsArea;
    @FXML private Label errorLabel;

    private final StudentService studentService = new StudentService();
    private Student selectedStudent;

    @FXML
    private void initialize() {
        studentsTable.setPlaceholder(new Label("No students match your filter."));
        branchFilterCombo.getItems().addAll("ALL", "CSE", "ECE", "ME", "EE", "CHE");
        branchFilterCombo.setValue("ALL");
        sortCombo.getItems().addAll("CGPA_DESC", "NAME_ASC", "ROLL_ASC");
        sortCombo.setValue("CGPA_DESC");
        editBranchCombo.getItems().addAll("CSE", "ECE", "ME", "EE", "CHE");

        colRoll.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRollNo()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colBranch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBranch()));
        colCgpa.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getCgpa()));
        colBacklogs.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getActiveBacklogs()));
        colBlocked.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isBlocked() ? "YES" : "NO"));

        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            selectedStudent = s;
            if (s != null) {
                editNameField.setText(s.getFullName());
                editPhoneField.setText(s.getPhone());
                editBranchCombo.setValue(s.getBranch());
                editCgpaField.setText(String.valueOf(s.getCgpa()));
                editBacklogsField.setText(String.valueOf(s.getActiveBacklogs()));
                editSkillsArea.setText(s.getSkills());
                errorLabel.setText("");
            }
        });

        loadStudents();
    }

    @FXML
    private void loadStudents() {
        String kw = searchField.getText();
        String branch = branchFilterCombo.getValue();
        String sortBy = sortCombo.getValue();
        double minCgpa = 0.0;
        if (minCgpaField.getText() != null && !minCgpaField.getText().isBlank()) {
            try {
                minCgpa = Double.parseDouble(minCgpaField.getText().trim());
            } catch (NumberFormatException ignored) {
                errorLabel.setText("Invalid Min CGPA filter.");
                return;
            }
        }
        double finalMinCgpa = minCgpa;
        SceneManager.runAsync(() -> studentService.searchStudents(branch, finalMinCgpa, kw, sortBy),
                list -> studentsTable.getItems().setAll(list),
                err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    @FXML
    private void handleSave() {
        if (selectedStudent == null) {
            errorLabel.setText("Select a student first.");
            return;
        }
        errorLabel.setText("");
        try {
            selectedStudent.setFullName(editNameField.getText());
            selectedStudent.setPhone(editPhoneField.getText());
            selectedStudent.setBranch(editBranchCombo.getValue());
            selectedStudent.setCgpa(Double.parseDouble(editCgpaField.getText().trim()));
            selectedStudent.setActiveBacklogs(Integer.parseInt(editBacklogsField.getText().trim()));
            selectedStudent.setSkills(editSkillsArea.getText());
        } catch (NumberFormatException e) {
            errorLabel.setText("CGPA and Backlogs must be valid numbers.");
            return;
        }
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> {
            studentService.updateStudent(selectedStudent, actorId);
            return null;
        }, ok -> loadStudents(), err -> errorLabel.setText(err.getMessage()));
    }

    @FXML
    private void handleToggleBlock() {
        if (selectedStudent == null) {
            errorLabel.setText("Select a student first.");
            return;
        }
        boolean next = !selectedStudent.isBlocked();
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> {
            studentService.setBlocked(selectedStudent.getId(), next, actorId);
            return null;
        }, ok -> loadStudents(), err -> SceneManager.showError("Error", err.getMessage()));
    }

    @FXML
    private void handleDelete() {
        if (selectedStudent == null) {
            errorLabel.setText("Select a student first.");
            return;
        }
        if (!SceneManager.showConfirm("Delete Student",
                "Delete " + selectedStudent.getFullName() + " (" + selectedStudent.getRollNo() + ")?")) {
            return;
        }
        int id = selectedStudent.getId();
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> {
            studentService.deleteStudent(id, actorId);
            return null;
        }, ok -> {
            selectedStudent = null;
            loadStudents();
        }, err -> SceneManager.showError("Delete Error", err.getMessage()));
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/admin_dashboard.fxml"); }
    @FXML private void navCompanies() { SceneManager.switchToFxml("/fxml/admin_companies.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/admin_drives.fxml"); }
    @FXML private void navApplicants() { SceneManager.switchToFxml("/fxml/admin_applicants.fxml"); }
    @FXML private void navStudents() { SceneManager.switchToFxml("/fxml/admin_students.fxml"); }
    @FXML private void navReports() { SceneManager.switchToFxml("/fxml/admin_reports.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/admin_students.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }
}
