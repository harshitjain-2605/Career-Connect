package com.campus.placement;

import com.campus.placement.dao.*;
import com.campus.placement.db.ConnectionManager;
import com.campus.placement.db.DatabaseInitializer;
import com.campus.placement.model.*;
import com.campus.placement.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for all JDBC DAO implementations against an isolated temporary SQLite database.
 */
class JdbcDaoTest {

    @TempDir
    Path tempDir;

    private UserDao userDao;
    private StudentDao studentDao;
    private CompanyDao companyDao;
    private DriveDao driveDao;
    private ApplicationDao applicationDao;
    private OfferDao offerDao;
    private NotificationDao notificationDao;
    private AuditLogDao auditLogDao;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = tempDir.resolve("test_placement.db");
        ConnectionManager.configureOverride("jdbc:sqlite:" + dbFile.toAbsolutePath(), "", "");
        DatabaseInitializer.initializeSchemaOnly();

        userDao = new JdbcUserDao();
        studentDao = new JdbcStudentDao();
        companyDao = new JdbcCompanyDao();
        driveDao = new JdbcDriveDao();
        applicationDao = new JdbcApplicationDao();
        offerDao = new JdbcOfferDao();
        notificationDao = new JdbcNotificationDao();
        auditLogDao = new JdbcAuditLogDao();
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.clearOverride();
    }

    @Test
    void fullCrudAndCustomQueries_succeedAcrossAllDaos() throws Exception {
        // 1. User CRUD
        User stuUser = new User();
        stuUser.setEmail("test.student@campus.edu");
        stuUser.setPasswordHash(PasswordUtil.hashPassword("Secret@123"));
        stuUser.setRole(Role.STUDENT);
        stuUser.setActive(true);
        userDao.save(stuUser);
        assertTrue(stuUser.getId() > 0);
        assertTrue(userDao.findByEmail("TEST.STUDENT@campus.edu").isPresent());
        assertTrue(userDao.setActive(stuUser.getId(), false));
        assertFalse(userDao.findById(stuUser.getId()).orElseThrow().isActive());

        // 2. Student CRUD + search + block
        Student s = new Student();
        s.setUserId(stuUser.getId());
        s.setRollNo("102299001");
        s.setFullName("Test Student");
        s.setEmail("test.student@campus.edu");
        s.setPhone("9876543210");
        s.setBranch("CSE");
        s.setBatchYear(2026);
        s.setCgpa(8.75);
        s.setActiveBacklogs(0);
        s.setSkills("Java, JDBC");
        studentDao.save(s);
        assertTrue(s.getId() > 0);
        assertEquals(1, studentDao.search("CSE", 8.0, "JDBC").size());
        assertEquals(0, studentDao.search("ECE", 8.0, "").size());
        assertTrue(studentDao.setBlocked(s.getId(), true));
        assertTrue(studentDao.findByRollNo("102299001").orElseThrow().isBlocked());

        // 3. Company CRUD
        Company c = new Company();
        c.setName("Acme Tech");
        c.setIndustry("Software");
        c.setWebsite("https://acme.example");
        c.setContactPerson("HR Lead");
        c.setContactEmail("hr@acme.example");
        c.setHrPhone("9988776655");
        companyDao.save(c);
        assertTrue(c.getId() > 0);
        assertTrue(companyDao.findByName("acme tech").isPresent());
        c.setIndustry("Cloud");
        assertTrue(companyDao.update(c));
        assertEquals("Cloud", companyDao.findById(c.getId()).orElseThrow().getIndustry());

        // 4. Drive CRUD + drive_branches
        Drive d = new Drive();
        d.setCompanyId(c.getId());
        d.setJobRole("Backend Engineer");
        d.setPackageLpa(14.5);
        d.setLocation("Bengaluru");
        d.setJobType(JobType.FULL_TIME);
        d.setDriveDate(LocalDate.now().plusDays(10));
        d.setLastDate(LocalDate.now().plusDays(5));
        d.setMinCgpa(7.5);
        d.setMaxBacklogs(0);
        d.setBatchYear(2026);
        d.setStatus(DriveStatus.OPEN);
        d.setAllowedBranches(Set.of("CSE", "ECE"));
        driveDao.save(d);
        assertTrue(d.getId() > 0);
        Drive loadedDrive = driveDao.findById(d.getId()).orElseThrow();
        assertEquals(Set.of("CSE", "ECE"), loadedDrive.getAllowedBranches());
        assertEquals(1, driveDao.findByStatus(DriveStatus.OPEN).size());
        assertEquals(1, driveDao.findByCompanyId(c.getId()).size());

        // 5. Application CRUD + history
        Application app = new Application();
        app.setStudentId(s.getId());
        app.setDriveId(d.getId());
        app.setStatus(ApplicationStatus.APPLIED);
        applicationDao.save(app);
        assertTrue(app.getId() > 0);
        assertEquals(1, applicationDao.findByDriveId(d.getId()).size());
        assertEquals(1, applicationDao.findByStudentId(s.getId()).size());
        assertEquals(1, applicationDao.findHistoryByApplicationId(app.getId()).size());

        // 6. Offer CRUD + aggregates
        Offer offer = new Offer();
        offer.setApplicationId(app.getId());
        offer.setStudentId(s.getId());
        offer.setDriveId(d.getId());
        offer.setPackageLpa(14.5);
        offer.setJoiningDate(LocalDate.now().plusMonths(6));
        offer.setStatus(OfferStatus.ACCEPTED);
        offerDao.save(offer);
        assertTrue(offer.getId() > 0);
        assertTrue(offerDao.findAcceptedOfferByStudentId(s.getId()).isPresent());
        Map<String, Integer> byBranch = offerDao.countPlacedStudentsByBranch();
        assertEquals(1, byBranch.get("CSE"));
        Map<String, Integer> byCompany = offerDao.countOffersByCompany();
        assertEquals(1, byCompany.get("Acme Tech"));

        // 7. Notification + AuditLog
        Notification notif = new Notification(stuUser.getId(), "Offer", "You got an offer");
        notificationDao.save(notif);
        assertFalse(notificationDao.findByUserId(stuUser.getId()).get(0).isRead());
        assertTrue(notificationDao.markAsRead(notif.getId()));
        assertTrue(notificationDao.findById(notif.getId()).orElseThrow().isRead());

        AuditLog log = new AuditLog(stuUser.getId(), "CREATE", "OFFER", offer.getId(), "Test offer");
        auditLogDao.save(log);
        List<AuditLog> recent = auditLogDao.findRecent(10);
        assertEquals(1, recent.size());
        assertEquals("CREATE", recent.get(0).getAction());
    }
}
