package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.*;
import com.campus.placement.service.ApplicationService;
import com.campus.placement.service.DriveService;
import com.campus.placement.service.EligibilityService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for {@code student_drives.fxml} with live eligibility computation and apply flow.
 */
public class StudentDrivesController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private TableView<DriveRow> drivesTable;
    @FXML private TableColumn<DriveRow, String> colCompany;
    @FXML private TableColumn<DriveRow, String> colRole;
    @FXML private TableColumn<DriveRow, Number> colPackage;
    @FXML private TableColumn<DriveRow, String> colType;
    @FXML private TableColumn<DriveRow, String> colLastDate;
    @FXML private TableColumn<DriveRow, String> colStatus;
    @FXML private TableColumn<DriveRow, String> colEligibility;
    @FXML private Label selectedInfoLabel;
    @FXML private Button applyButton;

    private final DriveService driveService = new DriveService();
    private final EligibilityService eligibilityService = new EligibilityService();
    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    private void initialize() {
        statusFilterCombo.getItems().addAll("ALL", "OPEN", "UPCOMING", "CLOSED", "COMPLETED");
        statusFilterCombo.setValue("OPEN");

        sortCombo.getItems().addAll("DRIVE_DATE", "PACKAGE_DESC", "LAST_DATE_ASC");
        sortCombo.setValue("DRIVE_DATE");
        drivesTable.setPlaceholder(new Label("No placement drives match your filter."));

        colCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().drive.getCompanyName()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().drive.getJobRole()));
        colPackage.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().drive.getPackageLpa()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().drive.getJobType().getLabel()));
        colLastDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().drive.getLastDate() != null ? c.getValue().drive.getLastDate().toString() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().drive.getStatus().name()));
        colEligibility.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().eligibilityText));

        colEligibility.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.startsWith("Eligible")
                            ? "-fx-text-fill: #166534; -fx-font-weight: bold;"
                            : "-fx-text-fill: #991b1b;");
                }
            }
        });

        drivesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedInfoLabel.setText(newV.drive.getCompanyName() + " — " + newV.drive.getJobRole()
                        + " (" + newV.eligibilityText + ")");
            }
        });

        handleSearch();
    }

    @FXML
    private void handleSearch() {
        Student student = SceneManager.getCurrentStudent();
        if (student == null) {
            return;
        }
        String keyword = searchField.getText();
        String statusVal = statusFilterCombo.getValue();
        DriveStatus status = "ALL".equals(statusVal) ? null : DriveStatus.valueOf(statusVal);
        String sortBy = sortCombo.getValue();

        SceneManager.runAsync(() -> {
            List<Drive> drives = driveService.searchDrives(keyword, status, sortBy);
            List<DriveRow> rows = new ArrayList<>();
            for (Drive d : drives) {
                EligibilityResult res = eligibilityService.checkEligibility(student, d);
                String text = res.isEligible() ? "Eligible" : "Not eligible: " + res.getReason();
                rows.add(new DriveRow(d, res.isEligible(), text));
            }
            return rows;
        }, rows -> drivesTable.getItems().setAll(rows),
                err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    @FXML
    private void handleApply() {
        DriveRow selected = drivesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            SceneManager.showError("No Selection", "Please select a drive first.");
            return;
        }
        Student student = SceneManager.getCurrentStudent();
        if (student == null) {
            return;
        }

        if (!SceneManager.showConfirm("Confirm Application",
                "Apply to " + selected.drive.getCompanyName() + " for " + selected.drive.getJobRole() + "?")) {
            return;
        }

        applyButton.setDisable(true);
        SceneManager.runAsync(
                () -> applicationService.apply(student, selected.drive.getId()),
                app -> {
                    applyButton.setDisable(false);
                    SceneManager.showInfo("Application Submitted",
                            "Successfully applied to " + selected.drive.getCompanyName() + "!");
                },
                err -> {
                    applyButton.setDisable(false);
                    SceneManager.showError("Cannot Apply", err.getMessage());
                }
        );
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/student_dashboard.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/student_drives.fxml"); }
    @FXML private void navApplications() { SceneManager.switchToFxml("/fxml/student_applications.fxml"); }
    @FXML private void navProfile() { SceneManager.switchToFxml("/fxml/student_profile.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/student_drives.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }

    public static class DriveRow {
        final Drive drive;
        final boolean eligible;
        final String eligibilityText;

        DriveRow(Drive drive, boolean eligible, String eligibilityText) {
            this.drive = drive;
            this.eligible = eligible;
            this.eligibilityText = eligibilityText;
        }
    }
}
