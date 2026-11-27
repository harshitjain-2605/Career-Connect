package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Application;
import com.campus.placement.model.ApplicationStatusHistory;
import com.campus.placement.model.Offer;
import com.campus.placement.model.Student;
import com.campus.placement.service.ApplicationService;
import com.campus.placement.service.OfferService;
import com.campus.placement.util.DateUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for {@code student_applications.fxml} displaying application status timelines
 * and transactional offer acceptance/decline.
 */
public class StudentApplicationsController {

    @FXML private TableView<Application> applicationsTable;
    @FXML private TableColumn<Application, String> colAppCompany;
    @FXML private TableColumn<Application, String> colAppRole;
    @FXML private TableColumn<Application, Number> colAppPkg;
    @FXML private TableColumn<Application, String> colAppStatus;
    @FXML private ListView<String> timelineListView;

    @FXML private TableView<Offer> offersTable;
    @FXML private TableColumn<Offer, String> colOfferCompany;
    @FXML private TableColumn<Offer, String> colOfferRole;
    @FXML private TableColumn<Offer, Number> colOfferPkg;
    @FXML private TableColumn<Offer, String> colOfferJoining;
    @FXML private TableColumn<Offer, String> colOfferStatus;

    private final ApplicationService applicationService = new ApplicationService();
    private final OfferService offerService = new OfferService();

    @FXML
    private void initialize() {
        applicationsTable.setPlaceholder(new javafx.scene.control.Label("You have not applied to any drives yet."));
        timelineListView.setPlaceholder(new javafx.scene.control.Label("Select an application to view its timeline."));
        offersTable.setPlaceholder(new javafx.scene.control.Label("No job offers issued yet."));
        colAppCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompanyName()));
        colAppRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJobRole()));
        colAppPkg.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPackageLpa()));
        colAppStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        colOfferCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompanyName()));
        colOfferRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJobRole()));
        colOfferPkg.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPackageLpa()));
        colOfferJoining.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getJoiningDate() != null ? c.getValue().getJoiningDate().toString() : "-"));
        colOfferStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        applicationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, app) -> {
            if (app != null) {
                loadTimeline(app.getId());
            }
        });

        loadData();
    }

    private void loadData() {
        Student s = SceneManager.getCurrentStudent();
        if (s == null) {
            return;
        }
        SceneManager.runAsync(() -> {
            List<Application> apps = applicationService.getApplicationsByStudent(s.getId());
            List<Offer> offers = offerService.getOffersForStudent(s.getId());
            return new Payload(apps, offers);
        }, p -> {
            applicationsTable.getItems().setAll(p.apps);
            offersTable.getItems().setAll(p.offers);
            timelineListView.getItems().clear();
        }, err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    private void loadTimeline(int applicationId) {
        SceneManager.runAsync(() -> {
            List<ApplicationStatusHistory> history = applicationService.getStatusHistory(applicationId);
            List<String> lines = new ArrayList<>();
            for (ApplicationStatusHistory h : history) {
                lines.add("[" + DateUtil.formatDisplayDateTime(h.getChangedAt()) + "] " + h);
            }
            return lines;
        }, lines -> timelineListView.getItems().setAll(lines),
                err -> SceneManager.showError("Timeline Error", err.getMessage()));
    }

    @FXML
    private void handleAcceptOffer() {
        respondOffer(true);
    }

    @FXML
    private void handleDeclineOffer() {
        respondOffer(false);
    }

    private void respondOffer(boolean accept) {
        Offer selected = offersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            SceneManager.showError("No Selection", "Please select an offer first.");
            return;
        }
        String verb = accept ? "ACCEPT" : "DECLINE";
        if (!SceneManager.showConfirm("Confirm Offer Response",
                "Are you sure you want to " + verb + " the offer from " + selected.getCompanyName() + "?")) {
            return;
        }
        Student s = SceneManager.getCurrentStudent();
        SceneManager.runAsync(() -> {
            offerService.respondToOffer(selected.getId(), s.getUserId(), accept);
            return null;
        }, ok -> {
            SceneManager.showInfo("Offer Updated", "Offer status updated to " + (accept ? "ACCEPTED" : "DECLINED") + ".");
            loadData();
        }, err -> SceneManager.showError("Offer Error", err.getMessage()));
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/student_dashboard.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/student_drives.fxml"); }
    @FXML private void navApplications() { SceneManager.switchToFxml("/fxml/student_applications.fxml"); }
    @FXML private void navProfile() { SceneManager.switchToFxml("/fxml/student_profile.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/student_applications.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }

    private record Payload(List<Application> apps, List<Offer> offers) {}
}
