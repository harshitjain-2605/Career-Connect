package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.*;
import com.campus.placement.service.CompanyService;
import com.campus.placement.service.DriveService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for {@code admin_drives.fxml} managing drives and eligibility rules.
 */
public class AdminDrivesController {

    @FXML private TableView<Drive> drivesTable;
    @FXML private TableColumn<Drive, String> colCompany;
    @FXML private TableColumn<Drive, String> colRole;
    @FXML private TableColumn<Drive, Number> colPkg;
    @FXML private TableColumn<Drive, Number> colMinCgpa;
    @FXML private TableColumn<Drive, String> colBranches;
    @FXML private TableColumn<Drive, String> colStatus;

    @FXML private ComboBox<Company> companyCombo;
    @FXML private TextField roleField;
    @FXML private TextField packageField;
    @FXML private TextField locationField;
    @FXML private ComboBox<JobType> jobTypeCombo;
    @FXML private ComboBox<DriveStatus> statusCombo;
    @FXML private DatePicker lastDatePicker;
    @FXML private DatePicker driveDatePicker;
    @FXML private TextField minCgpaField;
    @FXML private TextField maxBacklogsField;
    @FXML private TextField batchField;
    @FXML private CheckBox chkCse, chkEce, chkMe, chkEe, chkChe;
    @FXML private TextArea descArea;
    @FXML private Label errorLabel;

    private final DriveService driveService = new DriveService();
    private final CompanyService companyService = new CompanyService();
    private Drive selectedDrive;

    @FXML
    private void initialize() {
        drivesTable.setPlaceholder(new Label("No placement drives found."));
        jobTypeCombo.getItems().setAll(JobType.values());
        jobTypeCombo.setValue(JobType.FULL_TIME);
        statusCombo.getItems().setAll(DriveStatus.values());
        statusCombo.setValue(DriveStatus.OPEN);

        colCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompanyName()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJobRole()));
        colPkg.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPackageLpa()));
        colMinCgpa.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getMinCgpa()));
        colBranches.setCellValueFactory(c -> new SimpleStringProperty(String.join(", ", c.getValue().getAllowedBranches())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        drivesTable.getSelectionModel().selectedItemProperty().addListener((obs, o, d) -> {
            selectedDrive = d;
            if (d != null) {
                populateForm(d);
            }
        });

        loadInitialData();
    }

    private void loadInitialData() {
        SceneManager.runAsync(() -> {
            List<Company> companies = companyService.getAllCompanies();
            List<Drive> drives = driveService.getAllDrives();
            return new Payload(companies, drives);
        }, p -> {
            companyCombo.getItems().setAll(p.companies);
            drivesTable.getItems().setAll(p.drives);
        }, err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    private void populateForm(Drive d) {
        companyCombo.getItems().stream()
                .filter(c -> c.getId() == d.getCompanyId())
                .findFirst()
                .ifPresent(companyCombo::setValue);
        roleField.setText(d.getJobRole());
        packageField.setText(String.valueOf(d.getPackageLpa()));
        locationField.setText(d.getLocation());
        jobTypeCombo.setValue(d.getJobType());
        statusCombo.setValue(d.getStatus());
        lastDatePicker.setValue(d.getLastDate());
        driveDatePicker.setValue(d.getDriveDate());
        minCgpaField.setText(String.valueOf(d.getMinCgpa()));
        maxBacklogsField.setText(String.valueOf(d.getMaxBacklogs()));
        batchField.setText(String.valueOf(d.getBatchYear()));
        descArea.setText(d.getDescription());

        Set<String> br = d.getAllowedBranches();
        chkCse.setSelected(br.contains("CSE"));
        chkEce.setSelected(br.contains("ECE"));
        chkMe.setSelected(br.contains("ME"));
        chkEe.setSelected(br.contains("EE"));
        chkChe.setSelected(br.contains("CHE"));
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        errorLabel.setText("");
        Drive d = readForm(new Drive());
        if (d == null) {
            return;
        }
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> driveService.createDrive(d, actorId),
                saved -> {
                    handleClear();
                    loadInitialData();
                },
                err -> errorLabel.setText(err.getMessage()));
    }

    @FXML
    private void handleUpdate() {
        if (selectedDrive == null) {
            errorLabel.setText("Select a drive to update.");
            return;
        }
        errorLabel.setText("");
        Drive d = readForm(selectedDrive);
        if (d == null) {
            return;
        }
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> {
            driveService.updateDrive(d, actorId);
            return null;
        }, ok -> loadInitialData(), err -> errorLabel.setText(err.getMessage()));
    }

    @FXML
    private void handleDelete() {
        if (selectedDrive == null) {
            errorLabel.setText("Select a drive to delete.");
            return;
        }
        if (!SceneManager.showConfirm("Delete Drive",
                "Delete " + selectedDrive.getCompanyName() + " - " + selectedDrive.getJobRole() + "?")) {
            return;
        }
        int id = selectedDrive.getId();
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> {
            driveService.deleteDrive(id, actorId);
            return null;
        }, ok -> {
            handleClear();
            loadInitialData();
        }, err -> SceneManager.showError("Delete Error", err.getMessage()));
    }

    @FXML
    private void handleClear() {
        selectedDrive = null;
        drivesTable.getSelectionModel().clearSelection();
        roleField.clear();
        packageField.clear();
        locationField.clear();
        minCgpaField.clear();
        maxBacklogsField.setText("0");
        batchField.setText("2026");
        descArea.clear();
        lastDatePicker.setValue(null);
        driveDatePicker.setValue(null);
        errorLabel.setText("");
    }

    private Drive readForm(Drive d) {
        try {
            Company c = companyCombo.getValue();
            d.setCompanyId(c != null ? c.getId() : 0);
            d.setJobRole(roleField.getText());
            d.setPackageLpa(Double.parseDouble(packageField.getText().trim()));
            d.setLocation(locationField.getText());
            d.setJobType(jobTypeCombo.getValue());
            d.setStatus(statusCombo.getValue());
            d.setLastDate(lastDatePicker.getValue());
            d.setDriveDate(driveDatePicker.getValue());
            d.setMinCgpa(Double.parseDouble(minCgpaField.getText().trim()));
            d.setMaxBacklogs(Integer.parseInt(maxBacklogsField.getText().trim()));
            d.setBatchYear(Integer.parseInt(batchField.getText().trim()));
            d.setDescription(descArea.getText());

            Set<String> branches = new HashSet<>();
            if (chkCse.isSelected()) branches.add("CSE");
            if (chkEce.isSelected()) branches.add("ECE");
            if (chkMe.isSelected()) branches.add("ME");
            if (chkEe.isSelected()) branches.add("EE");
            if (chkChe.isSelected()) branches.add("CHE");
            d.setAllowedBranches(branches);
            return d;
        } catch (NumberFormatException e) {
            errorLabel.setText("Package, Min CGPA, Backlogs, and Batch must be valid numbers.");
            return null;
        }
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/admin_dashboard.fxml"); }
    @FXML private void navCompanies() { SceneManager.switchToFxml("/fxml/admin_companies.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/admin_drives.fxml"); }
    @FXML private void navApplicants() { SceneManager.switchToFxml("/fxml/admin_applicants.fxml"); }
    @FXML private void navStudents() { SceneManager.switchToFxml("/fxml/admin_students.fxml"); }
    @FXML private void navReports() { SceneManager.switchToFxml("/fxml/admin_reports.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/admin_drives.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }

    private record Payload(List<Company> companies, List<Drive> drives) {}
}
