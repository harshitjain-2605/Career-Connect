package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.DashboardStats;
import com.campus.placement.service.ReportService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

/**
 * Controller for {@code admin_dashboard.fxml} displaying live KPIs and JavaFX charts.
 */
public class AdminDashboardController {

    @FXML private Label totalStudentsLabel;
    @FXML private Label placedPctLabel;
    @FXML private Label activeDrivesLabel;
    @FXML private Label totalOffersLabel;
    @FXML private Label avgMaxLabel;
    @FXML private PieChart branchPieChart;
    @FXML private BarChart<String, Number> packageBarChart;

    private final ReportService reportService = new ReportService();

    @FXML
    private void initialize() {
        SceneManager.runAsync(reportService::getDashboardStats, this::renderStats,
                err -> SceneManager.showError("Dashboard Error", err.getMessage()));
    }

    private void renderStats(DashboardStats stats) {
        totalStudentsLabel.setText(String.valueOf(stats.getTotalStudents()));
        placedPctLabel.setText(String.format("%.1f%% (%d)", stats.getPlacedPercentage(), stats.getPlacedStudents()));
        activeDrivesLabel.setText(String.valueOf(stats.getActiveDrives()));
        totalOffersLabel.setText(String.valueOf(stats.getTotalOffers()));
        avgMaxLabel.setText(String.format("%.1f / %.1f", stats.getAveragePackageLpa(), stats.getHighestPackageLpa()));

        branchPieChart.getData().clear();
        stats.getPlacedByBranch().forEach((branch, count) ->
                branchPieChart.getData().add(new PieChart.Data(branch + " (" + count + ")", count)));

        packageBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Offers");
        stats.getPackageDistribution().forEach((band, count) ->
                series.getData().add(new XYChart.Data<>(band, count)));
        packageBarChart.getData().add(series);
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/admin_dashboard.fxml"); }
    @FXML private void navCompanies() { SceneManager.switchToFxml("/fxml/admin_companies.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/admin_drives.fxml"); }
    @FXML private void navApplicants() { SceneManager.switchToFxml("/fxml/admin_applicants.fxml"); }
    @FXML private void navStudents() { SceneManager.switchToFxml("/fxml/admin_students.fxml"); }
    @FXML private void navReports() { SceneManager.switchToFxml("/fxml/admin_reports.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/admin_dashboard.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }
}
