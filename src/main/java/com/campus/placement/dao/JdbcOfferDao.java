package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Offer;
import com.campus.placement.model.OfferStatus;
import com.campus.placement.util.DateUtil;
import com.campus.placement.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link OfferDao}.
 */
public class JdbcOfferDao implements OfferDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcOfferDao.class);

    private static final String BASE_SELECT =
            "SELECT o.id, o.application_id, o.student_id, o.drive_id, o.package_lpa, o.joining_date, "
            + "o.status, o.issued_at, o.responded_at, "
            + "c.name AS company_name, d.job_role, s.full_name AS student_name, s.roll_no AS student_roll_no "
            + "FROM offers o "
            + "JOIN drives d ON o.drive_id = d.id "
            + "JOIN companies c ON d.company_id = c.id "
            + "JOIN students s ON o.student_id = s.id ";

    @Override
    public Offer save(Offer offer) throws DataAccessException {
        try (Connection conn = ConnectionManager.getConnection()) {
            return saveWithConnection(conn, offer);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save offer", e);
            throw new DataAccessException("Failed to save offer: " + e.getMessage(), e);
        }
    }

    /**
     * Saves an offer using an existing transactional {@link Connection}.
     */
    public Offer saveInTransaction(Connection conn, Offer offer) throws DataAccessException {
        try {
            return saveWithConnection(conn, offer);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save offer in transaction", e);
        }
    }

    private Offer saveWithConnection(Connection conn, Offer offer) throws SQLException {
        String sql = "INSERT INTO offers (application_id, student_id, drive_id, package_lpa, joining_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, offer.getApplicationId());
            ps.setInt(2, offer.getStudentId());
            ps.setInt(3, offer.getDriveId());
            ps.setDouble(4, offer.getPackageLpa());
            ps.setString(5, DateUtil.formatDate(offer.getJoiningDate()));
            ps.setString(6, offer.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    offer.setId(keys.getInt(1));
                }
            }
            return offer;
        }
    }

    @Override
    public Optional<Offer> findById(Integer id) throws DataAccessException {
        List<Offer> list = queryList(BASE_SELECT + "WHERE o.id = ?", ps -> ps.setInt(1, id));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Offer> findByApplicationId(int applicationId) throws DataAccessException {
        List<Offer> list = queryList(BASE_SELECT + "WHERE o.application_id = ?", ps -> ps.setInt(1, applicationId));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Offer> findByStudentId(int studentId) throws DataAccessException {
        return queryList(BASE_SELECT + "WHERE o.student_id = ? ORDER BY o.issued_at DESC",
                ps -> ps.setInt(1, studentId));
    }

    @Override
    public Optional<Offer> findAcceptedOfferByStudentId(int studentId) throws DataAccessException {
        List<Offer> list = queryList(
                BASE_SELECT + "WHERE o.student_id = ? AND o.status = 'ACCEPTED' ORDER BY o.package_lpa DESC",
                ps -> ps.setInt(1, studentId));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Offer> findAll() throws DataAccessException {
        return queryList(BASE_SELECT + "ORDER BY o.issued_at DESC", ps -> {});
    }

    @Override
    public void updateStatusInTransaction(Connection conn, int offerId, OfferStatus status) throws DataAccessException {
        String sql = "UPDATE offers SET status = ?, responded_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, offerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update offer status", e);
        }
    }

    @Override
    public boolean update(Offer offer) throws DataAccessException {
        String sql = "UPDATE offers SET package_lpa = ?, joining_date = ?, status = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, offer.getPackageLpa());
            ps.setString(2, DateUtil.formatDate(offer.getJoiningDate()));
            ps.setString(3, offer.getStatus().name());
            ps.setInt(4, offer.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update offer id=" + offer.getId(), e);
            throw new DataAccessException("Failed to update offer", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM offers WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete offer id=" + id, e);
            throw new DataAccessException("Failed to delete offer", e);
        }
    }

    @Override
    public Map<String, Integer> countPlacedStudentsByBranch() throws DataAccessException {
        String sql = "SELECT s.branch, COUNT(DISTINCT o.student_id) AS cnt "
                + "FROM offers o JOIN students s ON o.student_id = s.id "
                + "WHERE o.status IN ('ACCEPTED', 'PENDING') GROUP BY s.branch ORDER BY s.branch";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("branch"), rs.getInt("cnt"));
            }
            return map;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to aggregate placed students by branch", e);
            throw new DataAccessException("Failed to aggregate placement by branch", e);
        }
    }

    @Override
    public Map<String, Integer> countOffersByCompany() throws DataAccessException {
        String sql = "SELECT c.name, COUNT(o.id) AS cnt "
                + "FROM offers o JOIN drives d ON o.drive_id = d.id JOIN companies c ON d.company_id = c.id "
                + "GROUP BY c.name ORDER BY cnt DESC";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getInt("cnt"));
            }
            return map;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to aggregate offers by company", e);
            throw new DataAccessException("Failed to aggregate offers by company", e);
        }
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Offer> queryList(String sql, Binder binder) throws DataAccessException {
        List<Offer> list = new ArrayList<>();
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
            LOGGER.log(Level.SEVERE, "Offer query failed", e);
            throw new DataAccessException("Offer query failed", e);
        }
    }

    private Offer mapRow(ResultSet rs) throws SQLException {
        Offer o = new Offer();
        o.setId(rs.getInt("id"));
        o.setApplicationId(rs.getInt("application_id"));
        o.setStudentId(rs.getInt("student_id"));
        o.setDriveId(rs.getInt("drive_id"));
        o.setPackageLpa(rs.getDouble("package_lpa"));
        o.setJoiningDate(DateUtil.parseDate(rs.getString("joining_date")));
        o.setStatus(OfferStatus.valueOf(rs.getString("status")));
        o.setIssuedAt(DateUtil.parseDateTime(rs.getString("issued_at")));
        o.setRespondedAt(DateUtil.parseDateTime(rs.getString("responded_at")));
        o.setCompanyName(rs.getString("company_name"));
        o.setJobRole(rs.getString("job_role"));
        o.setStudentName(rs.getString("student_name"));
        o.setStudentRollNo(rs.getString("student_roll_no"));
        return o;
    }
}
