package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Drive;
import com.campus.placement.model.DriveStatus;
import com.campus.placement.model.JobType;
import com.campus.placement.util.DateUtil;
import com.campus.placement.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link DriveDao} managing both {@code drives} and
 * {@code drive_branches} join table.
 */
public class JdbcDriveDao implements DriveDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcDriveDao.class);

    private static final String BASE_SELECT =
            "SELECT d.id, d.company_id, c.name AS company_name, d.job_role, d.package_lpa, d.location, "
            + "d.job_type, d.drive_date, d.last_date, d.min_cgpa, d.max_backlogs, d.batch_year, "
            + "d.status, d.description, d.created_at "
            + "FROM drives d JOIN companies c ON d.company_id = c.id ";

    @Override
    public Drive save(Drive drive) throws DataAccessException {
        return ConnectionManager.executeInTransaction(conn -> {
            String sql = "INSERT INTO drives (company_id, job_role, package_lpa, location, job_type, "
                    + "drive_date, last_date, min_cgpa, max_backlogs, batch_year, status, description) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindDrive(ps, drive);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        drive.setId(keys.getInt(1));
                    }
                }
            }
            replaceBranches(conn, drive.getId(), drive.getAllowedBranches());
            return drive;
        });
    }

    @Override
    public Optional<Drive> findById(Integer id) throws DataAccessException {
        List<Drive> list = queryDrives(BASE_SELECT + "WHERE d.id = ?", ps -> ps.setInt(1, id));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Drive> findAll() throws DataAccessException {
        return queryDrives(BASE_SELECT + "ORDER BY d.drive_date ASC, d.id DESC", ps -> {});
    }

    @Override
    public List<Drive> findByStatus(DriveStatus status) throws DataAccessException {
        return queryDrives(BASE_SELECT + "WHERE d.status = ? ORDER BY d.drive_date ASC",
                ps -> ps.setString(1, status.name()));
    }

    @Override
    public List<Drive> findByCompanyId(int companyId) throws DataAccessException {
        return queryDrives(BASE_SELECT + "WHERE d.company_id = ? ORDER BY d.drive_date DESC",
                ps -> ps.setInt(1, companyId));
    }

    @Override
    public List<Drive> search(String keyword, DriveStatus status) throws DataAccessException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<String> params = new ArrayList<>();
        if (status != null) {
            sql.append("AND d.status = ? ");
            params.add(status.name());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(c.name) LIKE ? OR LOWER(d.job_role) LIKE ? OR LOWER(d.location) LIKE ?) ");
            String pat = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }
        sql.append("ORDER BY d.drive_date ASC");
        return queryDrives(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
        });
    }

    @Override
    public boolean update(Drive drive) throws DataAccessException {
        return ConnectionManager.executeInTransaction(conn -> {
            String sql = "UPDATE drives SET company_id = ?, job_role = ?, package_lpa = ?, location = ?, "
                    + "job_type = ?, drive_date = ?, last_date = ?, min_cgpa = ?, max_backlogs = ?, "
                    + "batch_year = ?, status = ?, description = ? WHERE id = ?";
            int updated;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindDrive(ps, drive);
                ps.setInt(13, drive.getId());
                updated = ps.executeUpdate();
            }
            replaceBranches(conn, drive.getId(), drive.getAllowedBranches());
            return updated > 0;
        });
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM drives WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete drive id=" + id, e);
            throw new DataAccessException("Failed to delete drive", e);
        }
    }

    private void bindDrive(PreparedStatement ps, Drive d) throws SQLException {
        ps.setInt(1, d.getCompanyId());
        ps.setString(2, d.getJobRole());
        ps.setDouble(3, d.getPackageLpa());
        ps.setString(4, d.getLocation());
        ps.setString(5, d.getJobType().name());
        ps.setString(6, DateUtil.formatDate(d.getDriveDate()));
        ps.setString(7, DateUtil.formatDate(d.getLastDate()));
        ps.setDouble(8, d.getMinCgpa());
        ps.setInt(9, d.getMaxBacklogs());
        ps.setInt(10, d.getBatchYear());
        ps.setString(11, d.getStatus().name());
        ps.setString(12, d.getDescription());
    }

    private void replaceBranches(Connection conn, int driveId, Set<String> branches) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM drive_branches WHERE drive_id = ?")) {
            del.setInt(1, driveId);
            del.executeUpdate();
        }
        if (branches == null || branches.isEmpty()) {
            return;
        }
        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO drive_branches (drive_id, branch) VALUES (?, ?)")) {
            for (String branch : branches) {
                ins.setInt(1, driveId);
                ins.setString(2, branch.trim().toUpperCase());
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Drive> queryDrives(String sql, Binder binder) throws DataAccessException {
        List<Drive> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Drive d = mapRow(rs);
                    d.setAllowedBranches(loadBranches(conn, d.getId()));
                    list.add(d);
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Drive query failed", e);
            throw new DataAccessException("Drive query failed: " + e.getMessage(), e);
        }
    }

    private Set<String> loadBranches(Connection conn, int driveId) throws SQLException {
        Set<String> branches = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT branch FROM drive_branches WHERE drive_id = ?")) {
            ps.setInt(1, driveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    branches.add(rs.getString("branch"));
                }
            }
        }
        return branches;
    }

    private Drive mapRow(ResultSet rs) throws SQLException {
        Drive d = new Drive();
        d.setId(rs.getInt("id"));
        d.setCompanyId(rs.getInt("company_id"));
        d.setCompanyName(rs.getString("company_name"));
        d.setJobRole(rs.getString("job_role"));
        d.setPackageLpa(rs.getDouble("package_lpa"));
        d.setLocation(rs.getString("location"));
        d.setJobType(JobType.valueOf(rs.getString("job_type")));
        d.setDriveDate(DateUtil.parseDate(rs.getString("drive_date")));
        d.setLastDate(DateUtil.parseDate(rs.getString("last_date")));
        d.setMinCgpa(rs.getDouble("min_cgpa"));
        d.setMaxBacklogs(rs.getInt("max_backlogs"));
        d.setBatchYear(rs.getInt("batch_year"));
        d.setStatus(DriveStatus.valueOf(rs.getString("status")));
        d.setDescription(rs.getString("description"));
        d.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        return d;
    }
}
