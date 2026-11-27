package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Application;
import com.campus.placement.model.ApplicationStatus;
import com.campus.placement.model.Drive;
import com.campus.placement.model.User;
import com.campus.placement.service.ApplicationService;
import com.campus.placement.service.DriveService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Controller for {@code recruiter_dashboard.fxml} allowing recruiters to view
 * only their company's drives and update shortlist decisions.
 */
public class RecruiterDashboardController {

    @FXML private Label headerTitle;
    @FXML private ComboBox<Drive> driveCombo;
    @FXML private TableView<Application> applicantsTable;
    @FXML private TableColumn<Application, String> colRoll;
    @FXML private TableColumn<Application, String> colName;
    @FXML private TableColumn<Application, String> colBranch;
    @FXML private TableColumn<Application, Number> colCgpa;
    @FXML private TableColumn<Application, String> colStatus;
    @FXML private TextField remarksField;

    private final DriveService driveService = new DriveService();
    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    private void initialize() {
        applicantsTable.setPlaceholder(new Label("No applicants for this drive yet."));
        colRoll.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentRollNo()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colBranch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentBranch()));
        colCgpa.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getStudentCgpa()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        User u = SceneManager.getCurrentUser();
        if (u == null || u.getCompanyId() == null) {
            return;
        }
        headerTitle.setText("Recruiter Portal (" + u.getEmail() + ")");

        SceneManager.runAsync(() -> driveService.getDrivesByCompany(u.getCompanyId()),
                (List<Drive> drives) -> {
                    driveCombo.getItems().setAll(drives);
                    if (!drives.isEmpty()) {
                        driveCombo.getSelectionModel().selectFirst();
                        loadApplicants();
                    }
                },
                err -> SceneManager.showError("Load Error", err.getMessage()));
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

    @FXML
    private void handleShortlist() {
        updateStage(ApplicationStatus.SHORTLISTED);
    }

    @FXML
    private void handleReject() {
        updateStage(ApplicationStatus.REJECTED);
    }

    private void updateStage(ApplicationStatus target) {
        Application selected = applicantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            SceneManager.showError("No Selection", "Please select an applicant.");
            return;
        }
        int actorId = SceneManager.getCurrentUser().getId();
        String remarks = remarksField.getText();
        SceneManager.runAsync(() -> {
            applicationService.updateStage(selected.getId(), target, actorId, remarks, null);
            return null;
        }, ok -> {
            remarksField.clear();
            loadApplicants();
        }, err -> SceneManager.showError("Update Error", err.getMessage()));
    }

    @FXML
    private void handleLogout() {
        SceneManager.logout();
    }
}
