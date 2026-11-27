package com.campus.placement.service;

import com.campus.placement.config.AppConfig;
import com.campus.placement.dao.*;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.*;
import com.campus.placement.util.CsvUtil;
import com.campus.placement.util.LoggerUtil;
import com.campus.placement.util.SerializationUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service generating dashboard KPIs, placement reports, CSV exports, and serialized backups.
 */
// [Unit 2: Math, Iterator, HashMap] & [Unit 3: CSV & Serialization]
public class ReportService {

    private static final Logger LOGGER = LoggerUtil.getLogger(ReportService.class);

    private final StudentDao studentDao;
    private final CompanyDao companyDao;
    private final DriveDao driveDao;
    private final OfferDao offerDao;

    public ReportService() {
        this(new JdbcStudentDao(), new JdbcCompanyDao(), new JdbcDriveDao(), new JdbcOfferDao());
    }

    public ReportService(StudentDao studentDao, CompanyDao companyDao, DriveDao driveDao, OfferDao offerDao) {
        this.studentDao = studentDao;
        this.companyDao = companyDao;
        this.driveDao = driveDao;
        this.offerDao = offerDao;
    }

    /**
     * Computes live dashboard KPIs and chart data.
     */
    public DashboardStats getDashboardStats() throws DataAccessException {
        DashboardStats stats = new DashboardStats();
        List<Student> students = studentDao.findAll();
        List<Drive> drives = driveDao.findAll();
        List<Offer> offers = offerDao.findAll();

        stats.setTotalStudents(students.size());
        long activeDrives = drives.stream().filter(d -> d.getStatus() == DriveStatus.OPEN).count();
        stats.setActiveDrives((int) activeDrives);
        stats.setTotalOffers(offers.size());

        Set<Integer> placedStudentIds = new HashSet<>();
        double sumPkg = 0.0;
        double maxPkg = 0.0;
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("< 8 LPA", 0);
        dist.put("8 - 15 LPA", 0);
        dist.put("15 - 30 LPA", 0);
        dist.put("30+ LPA", 0);

        Iterator<Offer> it = offers.iterator();
        while (it.hasNext()) {
            Offer o = it.next();
            if (o.getStatus() != OfferStatus.DECLINED) {
                placedStudentIds.add(o.getStudentId());
                double pkg = o.getPackageLpa();
                sumPkg += pkg;
                maxPkg = Math.max(maxPkg, pkg);
                if (pkg < 8.0) {
                    dist.put("< 8 LPA", dist.get("< 8 LPA") + 1);
                } else if (pkg < 15.0) {
                    dist.put("8 - 15 LPA", dist.get("8 - 15 LPA") + 1);
                } else if (pkg < 30.0) {
                    dist.put("15 - 30 LPA", dist.get("15 - 30 LPA") + 1);
                } else {
                    dist.put("30+ LPA", dist.get("30+ LPA") + 1);
                }
            }
        }

        int validOffers = (int) offers.stream().filter(o -> o.getStatus() != OfferStatus.DECLINED).count();
        stats.setPlacedStudents(placedStudentIds.size());
        stats.setAveragePackageLpa(validOffers == 0 ? 0.0 : sumPkg / validOffers);
        stats.setHighestPackageLpa(maxPkg);
        stats.setPlacedByBranch(offerDao.countPlacedStudentsByBranch());
        stats.setPackageDistribution(dist);
        return stats;
    }

    public Map<String, Integer> getOffersByCompany() throws DataAccessException {
        return offerDao.countOffersByCompany();
    }

    /**
     * Exports all offers to a CSV file.
     */
    public void exportOffersCsv(Path targetFile) throws DataAccessException {
        List<Offer> offers = offerDao.findAll();
        String[] headers = {"OfferID", "RollNo", "StudentName", "Company", "Role", "PackageLPA", "Status", "JoiningDate"};
        List<String[]> rows = new ArrayList<>();
        for (Offer o : offers) {
            rows.add(new String[]{
                    String.valueOf(o.getId()),
                    o.getStudentRollNo(),
                    o.getStudentName(),
                    o.getCompanyName(),
                    o.getJobRole(),
                    String.format("%.2f", o.getPackageLpa()),
                    o.getStatus().name(),
                    o.getJoiningDate() != null ? o.getJoiningDate().toString() : ""
            });
        }
        try {
            CsvUtil.writeCsv(targetFile, headers, rows);
        } catch (IOException e) {
            throw new DataAccessException("Failed to export offers CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a serialized binary snapshot of core entities in {@code backups/}.
     */
    public Path createSnapshotBackup() throws DataAccessException {
        PlacementSnapshot snapshot = new PlacementSnapshot();
        snapshot.setCompanies(companyDao.findAll());
        snapshot.setStudents(studentDao.findAll());
        snapshot.setDrives(driveDao.findAll());
        snapshot.setOffers(offerDao.findAll());

        Path target = Paths.get(AppConfig.getInstance().getBackupsDir(),
                "snapshot_" + System.currentTimeMillis() + ".ser");
        try {
            SerializationUtil.saveSnapshot(snapshot, target);
            return target;
        } catch (IOException e) {
            throw new DataAccessException("Failed to save snapshot backup: " + e.getMessage(), e);
        }
    }

    /**
     * Reads a serialized {@link PlacementSnapshot} from disk.
     */
    public PlacementSnapshot loadSnapshotBackup(Path sourceFile) throws DataAccessException {
        try {
            return SerializationUtil.loadSnapshot(sourceFile);
        } catch (IOException | ClassNotFoundException e) {
            throw new DataAccessException("Failed to load snapshot backup: " + e.getMessage(), e);
        }
    }

    /**
     * Imports student records from a CSV file (roll_no, full_name, email, phone, branch, batch_year, cgpa, backlogs, skills)
     * using character streams ({@link CsvUtil#readCsv(Path)}). Skips header row and existing duplicates.
     *
     * @return number of newly imported students
     */
    // [Unit 3: Character streams (CSV import)]
    public int importStudentsCsv(Path csvFile, AuthService authService) throws DataAccessException {
        List<String[]> rows;
        try {
            rows = CsvUtil.readCsv(csvFile);
        } catch (IOException e) {
            throw new DataAccessException("Failed to read CSV file: " + e.getMessage(), e);
        }

        int imported = 0;
        for (int i = 0; i < rows.size(); i++) {
            String[] cols = rows.get(i);
            if (cols.length < 8) {
                continue;
            }
            // Skip header row if present
            if (i == 0 && cols[0].equalsIgnoreCase("roll_no")) {
                continue;
            }
            try {
                Student s = new Student();
                s.setRollNo(cols[0].trim());
                s.setFullName(cols[1].trim());
                s.setEmail(cols[2].trim());
                s.setPhone(cols[3].trim());
                s.setBranch(cols[4].trim());
                s.setBatchYear(Integer.parseInt(cols[5].trim()));
                s.setCgpa(Double.parseDouble(cols[6].trim()));
                s.setActiveBacklogs(Integer.parseInt(cols[7].trim()));
                s.setSkills(cols.length > 8 ? cols[8].trim() : "");
                authService.registerStudent(s, "Student@123");
                imported++;
            } catch (Exception ex) {
                LOGGER.warning("Skipped CSV row " + (i + 1) + ": " + ex.getMessage());
            }
        }
        return imported;
    }

    /**
     * Generates a formatted printable summary text report using {@link PrintWriter} / {@link BufferedWriter}.
     *
     * @param targetFile destination {@code .txt} path
     * @return generated report text
     */
    // [Unit 3: Character streams (PrintWriter / BufferedWriter)]
    public String generatePrintableSummaryReport(Path targetFile) throws DataAccessException {
        DashboardStats stats = getDashboardStats();
        Map<String, Integer> byCompany = getOffersByCompany();

        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("          CAMPUS PLACEMENT SUMMARY REPORT\n");
        sb.append("          Generated: ").append(LocalDateTime.now()).append("\n");
        sb.append("============================================================\n\n");
        sb.append(String.format("Total Students     : %d%n", stats.getTotalStudents()));
        sb.append(String.format("Placed Students    : %d (%.1f%%)%n", stats.getPlacedStudents(), stats.getPlacedPercentage()));
        sb.append(String.format("Active Drives      : %d%n", stats.getActiveDrives()));
        sb.append(String.format("Total Offers       : %d%n", stats.getTotalOffers()));
        sb.append(String.format("Average Package    : %.2f LPA%n", stats.getAveragePackageLpa()));
        sb.append(String.format("Highest Package    : %.2f LPA%n\n", stats.getHighestPackageLpa()));

        sb.append("--- Placed Students by Branch ---\n");
        stats.getPlacedByBranch().forEach((b, cnt) -> sb.append(String.format("  %-10s : %d%n", b, cnt)));
        sb.append("\n--- Offers by Company ---\n");
        byCompany.forEach((c, cnt) -> sb.append(String.format("  %-30s : %d%n", c, cnt)));
        sb.append("\n============================================================\n");

        String content = sb.toString();
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(targetFile.toFile(), StandardCharsets.UTF_8)))) {
            out.print(content);
        } catch (IOException e) {
            throw new DataAccessException("Failed to write printable report: " + e.getMessage(), e);
        }
        return content;
    }
}
