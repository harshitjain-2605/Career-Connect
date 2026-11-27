package com.campus.placement;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.db.DatabaseInitializer;
import com.campus.placement.exception.DuplicateEntryException;
import com.campus.placement.exception.NotEligibleException;
import com.campus.placement.exception.ValidationException;
import com.campus.placement.model.*;
import com.campus.placement.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link ApplicationService}, {@link OfferService},
 * and {@link AuthService} business rules against a temporary SQLite DB.
 */
class ApplicationServiceTest {

    @TempDir
    Path tempDir;

    private AuthService authService;
    private CompanyService companyService;
    private DriveService driveService;
    private ApplicationService applicationService;
    private OfferService offerService;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = tempDir.resolve("service_test.db");
        ConnectionManager.configureOverride("jdbc:sqlite:" + dbFile.toAbsolutePath(), "", "");
        DatabaseInitializer.initializeSchemaOnly();

        authService = new AuthService();
        companyService = new CompanyService();
        driveService = new DriveService();
        applicationService = new ApplicationService();
        offerService = new OfferService();
        notificationService = new NotificationService();
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.clearOverride();
    }

    @Test
    void statusTransitions_enforcePipelineRules() {
        assertTrue(applicationService.isValidTransition(ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED));
        assertTrue(applicationService.isValidTransition(ApplicationStatus.SHORTLISTED, ApplicationStatus.TEST));
        assertTrue(applicationService.isValidTransition(ApplicationStatus.TEST, ApplicationStatus.INTERVIEW));
        assertTrue(applicationService.isValidTransition(ApplicationStatus.INTERVIEW, ApplicationStatus.SELECTED));

        assertFalse(applicationService.isValidTransition(ApplicationStatus.APPLIED, ApplicationStatus.SELECTED),
                "Cannot jump from APPLIED straight to SELECTED");
        assertFalse(applicationService.isValidTransition(ApplicationStatus.REJECTED, ApplicationStatus.SHORTLISTED),
                "Cannot change status after REJECTED");
        assertFalse(applicationService.isValidTransition(ApplicationStatus.SELECTED, ApplicationStatus.REJECTED),
                "Cannot change status after SELECTED");
    }

    @Test
    void endToEndApplication_offerCreation_andTransactionalAcceptance() throws Exception {
        // Register student
        Student s = new Student();
        s.setRollNo("102200101");
        s.setFullName("Viva Tester");
        s.setEmail("viva.tester@campus.edu");
        s.setPhone("9876543210");
        s.setBranch("CSE");
        s.setBatchYear(2026);
        s.setCgpa(8.8);
        s.setActiveBacklogs(0);
        Student savedStudent = authService.registerStudent(s, "Pass@123");

        // Login succeeds
        User loggedIn = authService.login("viva.tester@campus.edu", "Pass@123");
        assertEquals(Role.STUDENT, loggedIn.getRole());

        // Create company + open drive
        Company comp = new Company();
        comp.setName("TestCorp");
        comp.setIndustry("IT");
        comp.setContactPerson("HR");
        comp.setContactEmail("hr@testcorp.com");
        comp.setHrPhone("9999988888");
        companyService.createCompany(comp, loggedIn.getId());

        Drive drive = new Drive();
        drive.setCompanyId(comp.getId());
        drive.setJobRole("SDE-1");
        drive.setPackageLpa(12.0);
        drive.setLocation("Bengaluru");
        drive.setJobType(JobType.FULL_TIME);
        drive.setDriveDate(LocalDate.now().plusDays(15));
        drive.setLastDate(LocalDate.now().plusDays(5));
        drive.setMinCgpa(8.0);
        drive.setMaxBacklogs(0);
        drive.setBatchYear(2026);
        drive.setStatus(DriveStatus.OPEN);
        drive.setAllowedBranches(Set.of("CSE"));
        driveService.createDrive(drive, loggedIn.getId());

        // Apply succeeds
        Application app = applicationService.apply(savedStudent, drive.getId());
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());

        // Duplicate apply fails
        assertThrows(DuplicateEntryException.class, () -> applicationService.apply(savedStudent, drive.getId()));

        // Invalid jump fails
        assertThrows(ValidationException.class, () ->
                applicationService.updateStage(app.getId(), ApplicationStatus.SELECTED, loggedIn.getId(), "jump", null));

        // Valid pipeline progression
        applicationService.updateStage(app.getId(), ApplicationStatus.SHORTLISTED, loggedIn.getId(), "r1", null);
        applicationService.updateStage(app.getId(), ApplicationStatus.TEST, loggedIn.getId(), "r2", null);
        applicationService.updateStage(app.getId(), ApplicationStatus.INTERVIEW, loggedIn.getId(), "r3", null);
        applicationService.updateStage(app.getId(), ApplicationStatus.SELECTED, loggedIn.getId(), "hired", LocalDate.now().plusMonths(3));

        // Offer created automatically
        List<Offer> offers = offerService.getOffersForStudent(savedStudent.getId());
        assertEquals(1, offers.size());
        assertEquals(OfferStatus.PENDING, offers.get(0).getStatus());

        // Accept offer in single transaction
        offerService.respondToOffer(offers.get(0).getId(), loggedIn.getId(), true);
        Offer accepted = offerService.getOffersForStudent(savedStudent.getId()).get(0);
        assertEquals(OfferStatus.ACCEPTED, accepted.getStatus());

        // Notifications were written
        assertFalse(notificationService.getForUser(loggedIn.getId()).isEmpty());

        // Low CGPA drive fails with NotEligibleException
        Drive hardDrive = new Drive();
        hardDrive.setCompanyId(comp.getId());
        hardDrive.setJobRole("Principal");
        hardDrive.setPackageLpa(50.0);
        hardDrive.setLocation("Remote");
        hardDrive.setJobType(JobType.FULL_TIME);
        hardDrive.setDriveDate(LocalDate.now().plusDays(15));
        hardDrive.setLastDate(LocalDate.now().plusDays(5));
        hardDrive.setMinCgpa(9.5);
        hardDrive.setMaxBacklogs(0);
        hardDrive.setBatchYear(2026);
        hardDrive.setStatus(DriveStatus.OPEN);
        hardDrive.setAllowedBranches(Set.of("CSE"));
        driveService.createDrive(hardDrive, loggedIn.getId());

        assertThrows(NotEligibleException.class, () -> applicationService.apply(savedStudent, hardDrive.getId()));
    }
}
