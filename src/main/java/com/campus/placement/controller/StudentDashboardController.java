package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Application;
import com.campus.placement.model.Notification;
import com.campus.placement.model.Offer;
import com.campus.placement.model.Student;
import com.campus.placement.service.ApplicationService;
import com.campus.placement.service.NotificationService;
import com.campus.placement.service.OfferService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

/**
 * Controller for {@code student_dashboard.fxml}.
 */
public class StudentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label branchBatchLabel;
    @FXML private Label cgpaLabel;
    @FXML private Label appCountLabel;
    @FXML private Label offerCountLabel;
    @FXML private ListView<Notification> notificationsListView;

    private final ApplicationService applicationService = new ApplicationService();
    private final OfferService offerService = new OfferService();
    private final NotificationService notificationService = new NotificationService();

    @FXML
    private void initialize() {
        Student s = SceneManager.getCurrentStudent();
        if (s == null) {
            return;
        }
        welcomeLabel.setText("Welcome, " + s.getFullName() + " (" + s.getRollNo() + ")");
        branchBatchLabel.setText(s.getBranch() + " • " + s.getBatchYear());
        cgpaLabel.setText(String.format("%.2f / %d", s.getCgpa(), s.getActiveBacklogs()));
        notificationsListView.setPlaceholder(new Label("No notifications yet."));

        loadDashboardData(s);
    }

    private void loadDashboardData(Student s) {
        SceneManager.runAsync(() -> {
            List<Application> apps = applicationService.getApplicationsByStudent(s.getId());
            List<Offer> offers = offerService.getOffersForStudent(s.getId());
            List<Notification> notifs = notificationService.getForUser(s.getUserId());
            return new DashboardPayload(apps.size(), offers.size(), notifs);
        }, payload -> {
            appCountLabel.setText(String.valueOf(payload.appCount));
            offerCountLabel.setText(String.valueOf(payload.offerCount));
            notificationsListView.getItems().setAll(payload.notifs);
        }, err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    @FXML
    private void handleMarkRead() {
        Notification selected = notificationsListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isRead()) {
            return;
        }
        SceneManager.runAsync(() -> {
            notificationService.markAsRead(selected.getId());
            return null;
        }, ok -> {
            selected.setRead(true);
            notificationsListView.refresh();
        }, err -> SceneManager.showError("Error", err.getMessage()));
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/student_dashboard.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/student_drives.fxml"); }
    @FXML private void navApplications() { SceneManager.switchToFxml("/fxml/student_applications.fxml"); }
    @FXML private void navProfile() { SceneManager.switchToFxml("/fxml/student_profile.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/student_dashboard.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }

    private record DashboardPayload(int appCount, int offerCount, List<Notification> notifs) {}
}
