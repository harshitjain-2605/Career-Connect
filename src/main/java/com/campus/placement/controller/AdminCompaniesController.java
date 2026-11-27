package com.campus.placement.controller;

import com.campus.placement.app.SceneManager;
import com.campus.placement.model.Company;
import com.campus.placement.service.CompanyService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for {@code admin_companies.fxml} providing full Company CRUD.
 */
public class AdminCompaniesController {

    @FXML private TextField searchField;
    @FXML private TableView<Company> companiesTable;
    @FXML private TableColumn<Company, String> colName;
    @FXML private TableColumn<Company, String> colIndustry;
    @FXML private TableColumn<Company, String> colContact;
    @FXML private TableColumn<Company, String> colEmail;
    @FXML private TableColumn<Company, String> colPhone;

    @FXML private TextField nameField;
    @FXML private TextField industryField;
    @FXML private TextField websiteField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel;

    private final CompanyService companyService = new CompanyService();
    private Company selectedCompany;

    @FXML
    private void initialize() {
        companiesTable.setPlaceholder(new Label("No companies found."));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colIndustry.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIndustry()));
        colContact.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContactPerson()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContactEmail()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHrPhone()));

        companiesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
            selectedCompany = c;
            if (c != null) {
                nameField.setText(c.getName());
                industryField.setText(c.getIndustry());
                websiteField.setText(c.getWebsite());
                contactField.setText(c.getContactPerson());
                emailField.setText(c.getContactEmail());
                phoneField.setText(c.getHrPhone());
                errorLabel.setText("");
            }
        });

        loadCompanies();
    }

    @FXML
    private void loadCompanies() {
        String kw = searchField.getText();
        SceneManager.runAsync(() -> companyService.searchCompanies(kw),
                list -> companiesTable.getItems().setAll(list),
                err -> SceneManager.showError("Load Error", err.getMessage()));
    }

    @FXML
    private void handleAdd() {
        errorLabel.setText("");
        Company c = readForm(new Company());
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> companyService.createCompany(c, actorId),
                saved -> {
                    handleClear();
                    loadCompanies();
                },
                err -> errorLabel.setText(err.getMessage()));
    }

    @FXML
    private void handleUpdate() {
        if (selectedCompany == null) {
            errorLabel.setText("Select a company to update.");
            return;
        }
        errorLabel.setText("");
        Company c = readForm(selectedCompany);
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> {
            companyService.updateCompany(c, actorId);
            return null;
        }, ok -> loadCompanies(), err -> errorLabel.setText(err.getMessage()));
    }

    @FXML
    private void handleDelete() {
        if (selectedCompany == null) {
            errorLabel.setText("Select a company to delete.");
            return;
        }
        if (!SceneManager.showConfirm("Delete Company",
                "Delete " + selectedCompany.getName() + "? This will also remove its drives.")) {
            return;
        }
        int id = selectedCompany.getId();
        int actorId = SceneManager.getCurrentUser().getId();
        SceneManager.runAsync(() -> {
            companyService.deleteCompany(id, actorId);
            return null;
        }, ok -> {
            handleClear();
            loadCompanies();
        }, err -> SceneManager.showError("Delete Error", err.getMessage()));
    }

    @FXML
    private void handleClear() {
        selectedCompany = null;
        companiesTable.getSelectionModel().clearSelection();
        nameField.clear();
        industryField.clear();
        websiteField.clear();
        contactField.clear();
        emailField.clear();
        phoneField.clear();
        errorLabel.setText("");
    }

    private Company readForm(Company c) {
        c.setName(nameField.getText());
        c.setIndustry(industryField.getText());
        c.setWebsite(websiteField.getText());
        c.setContactPerson(contactField.getText());
        c.setContactEmail(emailField.getText());
        c.setHrPhone(phoneField.getText());
        return c;
    }

    @FXML private void navDashboard() { SceneManager.switchToFxml("/fxml/admin_dashboard.fxml"); }
    @FXML private void navCompanies() { SceneManager.switchToFxml("/fxml/admin_companies.fxml"); }
    @FXML private void navDrives() { SceneManager.switchToFxml("/fxml/admin_drives.fxml"); }
    @FXML private void navApplicants() { SceneManager.switchToFxml("/fxml/admin_applicants.fxml"); }
    @FXML private void navStudents() { SceneManager.switchToFxml("/fxml/admin_students.fxml"); }
    @FXML private void navReports() { SceneManager.switchToFxml("/fxml/admin_reports.fxml"); }
    @FXML private void navPolicy() {
        SceneManager.showAboutPolicyView(() -> SceneManager.switchToFxml("/fxml/admin_companies.fxml"));
    }
    @FXML private void handleLogout() { SceneManager.logout(); }
}
