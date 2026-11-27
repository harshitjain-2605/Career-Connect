package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Application;
import com.campus.placement.model.ApplicationStatus;
import com.campus.placement.model.Drive;
import com.campus.placement.service.ApplicationService;
import com.campus.placement.service.DriveService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * Controller for {@code admin_applicants.fxml} managing stage transitions and offer creation.
 */
public class AdminApplicantsController {

    @FXML private ComboBox<Drive> driveCombo;
    @FXML private TableView<Application> applicantsTable;
    @FXML private TableColumn<Application, String> colRoll;
    @FXML private TableColumn<Application, String> colName;
    @FXML private TableColumn<Application, String> colBranch;
    @FXML private TableColumn<Application, Number> colCgpa;
    @FXML private TableColumn<Application, String> colStatus;
    @FXML private TextField remarksField;
    @FXML private DatePicker joiningDatePicker;

    private final DriveService driveService = new DriveService();
    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    private void initialize() {
        applicantsTable.setPlaceholder(new Label("No applicants for the selected drive."));
        colRoll.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentRollNo()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colBranch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentBranch()));
        colCgpa.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getStudentCgpa()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        joiningDatePicker.setValue(LocalDate.now().plusMonths(6));

        SceneManager.runAsync(driveService::getAllDrives, drives -> {
            driveCombo.getItems().setAll(drives);
            if (!drives.isEmpty()) {
                driveCombo.getSelectionModel().selectFirst();
                loadApplicants();
            }
        }, err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    @FXML
    private void loadApplicants() {
        Drive d = driveCombo.getValue();
        if (d == null) {
            return;
        }
        SceneManager.runAsync(() -> applicationService.getApplicationsByDrive(d.getId()),
                list -> applicantsTable.getItems().setAll(list),
                err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    @FXML private void markShortlisted() { transitionTo(ApplicationStatus.SHORTLISTED); }
    @FXML private void markTest() { transitionTo(ApplicationStatus.TEST); }
    @FXML private void markInterview() { transitionTo(ApplicationStatus.INTERVIEW); }
    @FXML private void markSelected() { transitionTo(ApplicationStatus.SELECTED); }
    @FXML private void markRejected() { transitionTo(ApplicationStatus.REJECTED); }

    private void transitionTo(ApplicationStatus target) {
        Application selected = applicantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            SceneManager.showError("No Selection", "Please select an applicant first.");
            return;
        }
        if (target == ApplicationStatus.REJECTED && !SceneManager.showConfirm(
                "Confirm Rejection", "Reject " + selected.getStudentName() + "? This cannot be undone.")) {
            return;
        }
        int actorId = SceneManager.getCurrentUser().getId();
        String remarks = remarksField.getText();
        LocalDate joining = joiningDatePicker.getValue();

        SceneManager.runAsync(() -> {
            applicationService.updateStage(selected.getId(), target, actorId, remarks, joining);
            return null;
        }, ok -> {
            remarksField.clear();
            loadApplicants();
            if (target == ApplicationStatus.SELECTED) {
                SceneManager.showInfo("Offer Issued",
                        "Student selected and Offer issued to " + selected.getStudentName() + ".");
            }
        }, err -> SceneManager.showError("Invalid Transition", err.getMessage()));
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/admin_dashboard.fxml"); }
    @FXML private void navCompanies() { SceneManager.switchToFxml("/fxml/admin_companies.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/admin_drives.fxml"); }
    @FXML private void navApplicants() { SceneManager.switchToFxml("/fxml/admin_applicants.fxml"); }
    @FXML private void navStudents() { SceneManager.switchToFxml("/fxml/admin_students.fxml"); }
    @FXML private void navReports() { SceneManager.switchToFxml("/fxml/admin_reports.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/admin_applicants.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }
}
