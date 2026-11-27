package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Student;
import com.campus.placement.util.DateUtil;
import com.campus.placement.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link StudentDao}.
 */
public class JdbcStudentDao implements StudentDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcStudentDao.class);

    private static final String BASE_SELECT =
            "SELECT id, user_id, roll_no, full_name, email, phone, branch, batch_year, "
            + "cgpa, active_backlogs, skills, resume_path, blocked, created_at FROM students ";

    @Override
    public Student save(Student s) throws DataAccessException {
        String sql = "INSERT INTO students (user_id, roll_no, full_name, email, phone, branch, "
                + "batch_year, cgpa, active_backlogs, skills, resume_path, blocked) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getRollNo());
            ps.setString(3, s.getFullName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getBranch());
            ps.setInt(7, s.getBatchYear());
            ps.setDouble(8, s.getCgpa());
            ps.setInt(9, s.getActiveBacklogs());
            ps.setString(10, s.getSkills());
            ps.setString(11, s.getResumePath());
            ps.setInt(12, s.isBlocked() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1));
                }
            }
            return s;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save student roll=" + s.getRollNo(), e);
            throw new DataAccessException("Failed to save student: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Student> findById(Integer id) throws DataAccessException {
        return findSingle(BASE_SELECT + "WHERE id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public Optional<Student> findByUserId(int userId) throws DataAccessException {
        return findSingle(BASE_SELECT + "WHERE user_id = ?", ps -> ps.setInt(1, userId));
    }

    @Override
    public Optional<Student> findByRollNo(String rollNo) throws DataAccessException {
        return findSingle(BASE_SELECT + "WHERE UPPER(roll_no) = UPPER(?)", ps -> ps.setString(1, rollNo));
    }

    @Override
    public Optional<Student> findByEmail(String email) throws DataAccessException {
        return findSingle(BASE_SELECT + "WHERE LOWER(email) = LOWER(?)", ps -> ps.setString(1, email));
    }

    @Override
    public List<Student> findAll() throws DataAccessException {
        return findList(BASE_SELECT + "ORDER BY roll_no", ps -> {});
    }

    @Override
    public List<Student> search(String branch, double minCgpa, String keyword) throws DataAccessException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE cgpa >= ? ");
        List<Object> params = new ArrayList<>();
        params.add(minCgpa);

        if (branch != null && !branch.isBlank() && !"ALL".equalsIgnoreCase(branch)) {
            sql.append("AND branch = ? ");
            params.add(branch.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(full_name) LIKE ? OR LOWER(roll_no) LIKE ? OR LOWER(COALESCE(skills,'')) LIKE ?) ");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        sql.append("ORDER BY cgpa DESC, roll_no ASC");

        return findList(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                Object val = params.get(i);
                if (val instanceof Double d) {
                    ps.setDouble(i + 1, d);
                } else {
                    ps.setString(i + 1, val.toString());
                }
            }
        });
    }

    @Override
    public boolean update(Student s) throws DataAccessException {
        String sql = "UPDATE students SET full_name = ?, phone = ?, branch = ?, batch_year = ?, "
                + "cgpa = ?, active_backlogs = ?, skills = ?, resume_path = ?, blocked = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getBranch());
            ps.setInt(4, s.getBatchYear());
            ps.setDouble(5, s.getCgpa());
            ps.setInt(6, s.getActiveBacklogs());
            ps.setString(7, s.getSkills());
            ps.setString(8, s.getResumePath());
            ps.setInt(9, s.isBlocked() ? 1 : 0);
            ps.setInt(10, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update student id=" + s.getId(), e);
            throw new DataAccessException("Failed to update student", e);
        }
    }

    @Override
    public boolean setBlocked(int studentId, boolean blocked) throws DataAccessException {
        String sql = "UPDATE students SET blocked = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, blocked ? 1 : 0);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to set blocked=" + blocked + " for studentId=" + studentId, e);
            throw new DataAccessException("Failed to update student block status", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete student id=" + id, e);
            throw new DataAccessException("Failed to delete student", e);
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<Student> findSingle(String sql, StatementBinder binder) throws DataAccessException {
        List<Student> results = findList(sql, binder);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private List<Student> findList(String sql, StatementBinder binder) throws DataAccessException {
        List<Student> list = new ArrayList<>();
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
            LOGGER.log(Level.SEVERE, "Student query failed", e);
            throw new DataAccessException("Student query failed: " + e.getMessage(), e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setRollNo(rs.getString("roll_no"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setBranch(rs.getString("branch"));
        s.setBatchYear(rs.getInt("batch_year"));
        s.setCgpa(rs.getDouble("cgpa"));
        s.setActiveBacklogs(rs.getInt("active_backlogs"));
        s.setSkills(rs.getString("skills"));
        s.setResumePath(rs.getString("resume_path"));
        s.setBlocked(rs.getInt("blocked") == 1);
        s.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        return s;
    }
}
