package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Application;
import com.campus.placement.model.ApplicationStatus;
import com.campus.placement.model.ApplicationStatusHistory;
import com.campus.placement.util.DateUtil;
import com.campus.placement.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link ApplicationDao}.
 * Uses {@link LinkedList} for status history timeline retrieval.
 */
// [Unit 2: LinkedList] & [Unit 4: JDBC CRUD & transactions]
public class JdbcApplicationDao implements ApplicationDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcApplicationDao.class);

    private static final String BASE_SELECT =
            "SELECT a.id, a.student_id, a.drive_id, a.status, a.applied_at, a.updated_at, "
            + "s.roll_no AS student_roll_no, s.full_name AS student_name, s.branch AS student_branch, s.cgpa AS student_cgpa, "
            + "c.name AS company_name, d.job_role, d.package_lpa "
            + "FROM applications a "
            + "JOIN students s ON a.student_id = s.id "
            + "JOIN drives d ON a.drive_id = d.id "
            + "JOIN companies c ON d.company_id = c.id ";

    @Override
    public Application save(Application app) throws DataAccessException {
        return ConnectionManager.executeInTransaction(conn -> {
            String sql = "INSERT INTO applications (student_id, drive_id, status) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, app.getStudentId());
                ps.setInt(2, app.getDriveId());
                ps.setString(3, app.getStatus().name());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        app.setId(keys.getInt(1));
                    }
                }
            }
            insertHistory(conn, app.getId(), null, app.getStatus(), null, "Student applied");
            return app;
        });
    }

    @Override
    public Optional<Application> findById(Integer id) throws DataAccessException {
        List<Application> list = queryList(BASE_SELECT + "WHERE a.id = ?", ps -> ps.setInt(1, id));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Application> findByStudentAndDrive(int studentId, int driveId) throws DataAccessException {
        List<Application> list = queryList(BASE_SELECT + "WHERE a.student_id = ? AND a.drive_id = ?", ps -> {
            ps.setInt(1, studentId);
            ps.setInt(2, driveId);
        });
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Application> findAll() throws DataAccessException {
        return queryList(BASE_SELECT + "ORDER BY a.applied_at DESC", ps -> {});
    }

    @Override
    public List<Application> findByDriveId(int driveId) throws DataAccessException {
        return queryList(BASE_SELECT + "WHERE a.drive_id = ? ORDER BY s.cgpa DESC, s.roll_no ASC",
                ps -> ps.setInt(1, driveId));
    }

    @Override
    public List<Application> findByStudentId(int studentId) throws DataAccessException {
        return queryList(BASE_SELECT + "WHERE a.student_id = ? ORDER BY a.applied_at DESC",
                ps -> ps.setInt(1, studentId));
    }

    @Override
    public List<ApplicationStatusHistory> findHistoryByApplicationId(int applicationId) throws DataAccessException {
        String sql = "SELECT id, application_id, from_status, to_status, changed_by_user_id, remarks, changed_at "
                + "FROM application_status_history WHERE application_id = ? ORDER BY id ASC";
        List<ApplicationStatusHistory> history = new LinkedList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ApplicationStatusHistory h = new ApplicationStatusHistory();
                    h.setId(rs.getInt("id"));
                    h.setApplicationId(rs.getInt("application_id"));
                    String fromStr = rs.getString("from_status");
                    h.setFromStatus(fromStr == null ? null : ApplicationStatus.valueOf(fromStr));
                    h.setToStatus(ApplicationStatus.valueOf(rs.getString("to_status")));
                    int changedBy = rs.getInt("changed_by_user_id");
                    h.setChangedByUserId(rs.wasNull() ? null : changedBy);
                    h.setRemarks(rs.getString("remarks"));
                    h.setChangedAt(DateUtil.parseDateTime(rs.getString("changed_at")));
                    history.add(h);
                }
            }
            return history;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load status history for applicationId=" + applicationId, e);
            throw new DataAccessException("Failed to load status history", e);
        }
    }

    @Override
    public void updateStatusInTransaction(Connection conn, int applicationId, ApplicationStatus fromStatus,
                                          ApplicationStatus toStatus, Integer changedByUserId, String remarks)
            throws DataAccessException {
        String sql = "UPDATE applications SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, toStatus.name());
            ps.setInt(2, applicationId);
            ps.executeUpdate();
            insertHistory(conn, applicationId, fromStatus, toStatus, changedByUserId, remarks);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update application status", e);
        }
    }

    @Override
    public boolean update(Application app) throws DataAccessException {
        String sql = "UPDATE applications SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, app.getStatus().name());
            ps.setInt(2, app.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update application id=" + app.getId(), e);
            throw new DataAccessException("Failed to update application", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM applications WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete application id=" + id, e);
            throw new DataAccessException("Failed to delete application", e);
        }
    }

    private void insertHistory(Connection conn, int applicationId, ApplicationStatus fromStatus,
                               ApplicationStatus toStatus, Integer changedByUserId, String remarks)
            throws SQLException {
        String histSql = "INSERT INTO application_status_history "
                + "(application_id, from_status, to_status, changed_by_user_id, remarks) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(histSql)) {
            if (fromStatus != null) {
                ps.setString(1 + 1 - 1, null); // placeholder clarity below
            }
            ps.setInt(1, applicationId);
            if (fromStatus != null) {
                ps.setString(2, fromStatus.name());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, toStatus.name());
            if (changedByUserId != null) {
                ps.setInt(4, changedByUserId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, remarks);
            ps.executeUpdate();
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Application> queryList(String sql, Binder binder) throws DataAccessException {
        List<Application> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Application query failed", e);
            throw new DataAccessException("Application query failed: " + e.getMessage(), e);
        }
    }

    private Application mapRow(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setDriveId(rs.getInt("drive_id"));
        a.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        a.setAppliedAt(DateUtil.parseDateTime(rs.getString("applied_at")));
        a.setUpdatedAt(DateUtil.parseDateTime(rs.getString("updated_at")));
        a.setStudentRollNo(rs.getString("student_roll_no"));
        a.setStudentName(rs.getString("student_name"));
        a.setStudentBranch(rs.getString("student_branch"));
        a.setStudentCgpa(rs.getDouble("student_cgpa"));
        a.setCompanyName(rs.getString("company_name"));
        a.setJobRole(rs.getString("job_role"));
        a.setPackageLpa(rs.getDouble("package_lpa"));
        return a;
    }
}
