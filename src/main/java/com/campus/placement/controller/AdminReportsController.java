package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.AuditLog;
import com.campus.placement.model.DashboardStats;
import com.campus.placement.model.PlacementSnapshot;
import com.campus.placement.service.AuditService;
import com.campus.placement.service.AuthService;
import com.campus.placement.service.ReportService;
import com.campus.placement.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Controller for {@code admin_reports.fxml} providing CSV export, serialized snapshot
 * backup/inspection, summary reports, and an Audit Log viewer.
 */
public class AdminReportsController {

    @FXML private ListView<String> branchSummaryList;
    @FXML private ListView<String> companySummaryList;
    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, String> colTime;
    @FXML private TableColumn<AuditLog, String> colActor;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colEntity;
    @FXML private TableColumn<AuditLog, String> colDetails;

    private final ReportService reportService = new ReportService();
    private final AuditService auditService = new AuditService();
    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        branchSummaryList.setPlaceholder(new javafx.scene.control.Label("No branch placements yet."));
        companySummaryList.setPlaceholder(new javafx.scene.control.Label("No company offers yet."));
        auditTable.setPlaceholder(new javafx.scene.control.Label("No audit log entries found."));
        colTime.setCellValueFactory(c -> new SimpleStringProperty(DateUtil.formatDisplayDateTime(c.getValue().getCreatedAt())));
        colActor.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getActorUserId() != null ? String.valueOf(c.getValue().getActorUserId()) : "SYSTEM"));
        colAction.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));
        colEntity.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEntityType() + (c.getValue().getEntityId() != null ? "#" + c.getValue().getEntityId() : "")));
        colDetails.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDetails()));

        loadReports();
    }

    private void loadReports() {
        SceneManager.runAsync(() -> {
            DashboardStats stats = reportService.getDashboardStats();
            Map<String, Integer> byComp = reportService.getOffersByCompany();
            List<AuditLog> logs = auditService.getRecentLogs(100);
            return new Payload(stats, byComp, logs);
        }, p -> {
            branchSummaryList.getItems().clear();
            p.stats.getPlacedByBranch().forEach((b, cnt) -> branchSummaryList.getItems().add(b + ": " + cnt + " placed"));
            companySummaryList.getItems().clear();
            p.byComp.forEach((c, cnt) -> companySummaryList.getItems().add(c + ": " + cnt + " offers"));
            auditTable.getItems().setAll(p.logs);
        }, err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    @FXML
    private void handleExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Offers CSV");
        chooser.setInitialFileName("placement_offers.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(SceneManager.getPrimaryStage());
        if (file == null) {
            return;
        }
        SceneManager.runAsync(() -> {
            reportService.exportOffersCsv(file.toPath());
            return file.getAbsolutePath();
        }, path -> SceneManager.showInfo("CSV Exported", "Offers exported to:\n" + path),
                err -> SceneManager.showError("Export Error", err.getMessage()));
    }

    @FXML
    private void handleImportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Students CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(SceneManager.getPrimaryStage());
        if (file == null) {
            return;
        }
        SceneManager.runAsync(() -> reportService.importStudentsCsv(file.toPath(), authService),
                count -> {
                    SceneManager.showInfo("CSV Imported", "Imported " + count + " new student(s).");
                    loadReports();
                },
                err -> SceneManager.showError("Import Error", err.getMessage()));
    }

    @FXML
    private void handlePrintableReport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Printable Summary Report");
        chooser.setInitialFileName("placement_summary.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showSaveDialog(SceneManager.getPrimaryStage());
        if (file == null) {
            return;
        }
        SceneManager.runAsync(() -> reportService.generatePrintableSummaryReport(file.toPath()),
                text -> SceneManager.showInfo("Report Saved", "Summary saved to:\n" + file.getAbsolutePath()),
                err -> SceneManager.showError("Report Error", err.getMessage()));
    }

    @FXML
    private void handleSaveSnapshot() {
        SceneManager.runAsync(reportService::createSnapshotBackup,
                (Path path) -> SceneManager.showInfo("Snapshot Saved", "Serialized backup saved to:\n" + path),
                err -> SceneManager.showError("Backup Error", err.getMessage()));
    }

    @FXML
    private void handleInspectSnapshot() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Snapshot (.ser)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialized Snapshot", "*.ser"));
        File file = chooser.showOpenDialog(SceneManager.getPrimaryStage());
        if (file == null) {
            return;
        }
        SceneManager.runAsync(() -> reportService.loadSnapshotBackup(file.toPath()),
                (PlacementSnapshot snap) -> SceneManager.showInfo("Snapshot Contents",
                        "Created: " + snap.getCreatedAt()
                        + "\nCompanies: " + snap.getCompanies().size()
                        + "\nStudents: " + snap.getStudents().size()
                        + "\nDrives: " + snap.getDrives().size()
                        + "\nOffers: " + snap.getOffers().size()),
                err -> SceneManager.showError("Restore Error", err.getMessage()));
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/admin_dashboard.fxml"); }
    @FXML private void navCompanies() { SceneManager.switchToFxml("/fxml/admin_companies.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/admin_drives.fxml"); }
    @FXML private void navApplicants() { SceneManager.switchToFxml("/fxml/admin_applicants.fxml"); }
    @FXML private void navStudents() { SceneManager.switchToFxml("/fxml/admin_students.fxml"); }
    @FXML private void navReports() { SceneManager.switchToFxml("/fxml/admin_reports.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/admin_reports.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }

    private record Payload(DashboardStats stats, Map<String, Integer> byComp, List<AuditLog> logs) {}
}
