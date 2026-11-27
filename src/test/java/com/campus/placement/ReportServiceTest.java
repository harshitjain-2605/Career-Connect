package com.campus.placement;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.db.DatabaseInitializer;
import com.campus.placement.model.Company;
import com.campus.placement.model.PlacementSnapshot;
import com.campus.placement.service.AuthService;
import com.campus.placement.service.CompanyService;
import com.campus.placement.service.ReportService;
import com.campus.placement.service.StudentService;
import com.campus.placement.util.SerializationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReportService} CSV import, printable text report, and {@link SerializationUtil} round-trip.
 */
class ReportServiceTest {

    @TempDir
    Path tempDir;

    private ReportService reportService;
    private AuthService authService;
    private StudentService studentService;
    private CompanyService companyService;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = tempDir.resolve("report_test.db");
        ConnectionManager.configureOverride("jdbc:sqlite:" + dbFile.toAbsolutePath(), "", "");
        DatabaseInitializer.initializeSchemaOnly();

        reportService = new ReportService();
        authService = new AuthService();
        studentService = new StudentService();
        companyService = new CompanyService();
    }

    @AfterEach
    void tearDown() {
        ConnectionManager.clearOverride();
    }

    @Test
    void importCsv_printableReport_andSerializationRoundTrip() throws Exception {
        Path csv = tempDir.resolve("students.csv");
        Files.writeString(csv,
                "roll_no,full_name,email,phone,branch,batch_year,cgpa,active_backlogs,skills\n"
                + "102203901,Riya Sen,riya.sen@campus.edu,9876543211,CSE,2026,8.90,0,Java\n"
                + "102204902,Amit Verma,amit.v@campus.edu,9876543212,ECE,2026,7.80,0,C++\n");

        int count = reportService.importStudentsCsv(csv, authService);
        assertEquals(2, count);
        assertEquals(2, studentService.getAllStudents().size());

        Path txtReport = tempDir.resolve("summary.txt");
        String text = reportService.generatePrintableSummaryReport(txtReport);
        assertTrue(Files.exists(txtReport));
        assertTrue(text.contains("CAMPUS PLACEMENT SUMMARY REPORT"));
        assertTrue(text.contains("Total Students     : 2"));

        Company c = new Company();
        c.setName("SnapCorp");
        c.setIndustry("IT");
        c.setContactPerson("HR");
        c.setContactEmail("hr@snap.com");
        c.setHrPhone("9988776655");
        companyService.createCompany(c, null);

        PlacementSnapshot snapshot = new PlacementSnapshot();
        snapshot.setCompanies(List.of(c));
        snapshot.setStudents(studentService.getAllStudents());

        Path serFile = tempDir.resolve("backup.ser");
        SerializationUtil.saveSnapshot(snapshot, serFile);
        PlacementSnapshot restored = SerializationUtil.loadSnapshot(serFile);

        assertEquals(1, restored.getCompanies().size());
        assertEquals("SnapCorp", restored.getCompanies().get(0).getName());
        assertEquals(2, restored.getStudents().size());
    }
}
