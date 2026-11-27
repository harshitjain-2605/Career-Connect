package com.campus.placement.dao;

import com.campus.placement.db.ConnectionManager;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.model.Company;
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
 * JDBC implementation of {@link CompanyDao}.
 */
public class JdbcCompanyDao implements CompanyDao {

    private static final Logger LOGGER = LoggerUtil.getLogger(JdbcCompanyDao.class);

    @Override
    public Company save(Company c) throws DataAccessException {
        String sql = "INSERT INTO companies (name, industry, website, contact_person, contact_email, hr_phone) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getIndustry());
            ps.setString(3, c.getWebsite());
            ps.setString(4, c.getContactPerson());
            ps.setString(5, c.getContactEmail());
            ps.setString(6, c.getHrPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
            return c;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save company: " + c.getName(), e);
            throw new DataAccessException("Failed to save company: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Company> findById(Integer id) throws DataAccessException {
        String sql = "SELECT id, name, industry, website, contact_person, contact_email, hr_phone, created_at FROM companies WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find company id=" + id, e);
            throw new DataAccessException("Failed to find company", e);
        }
    }

    @Override
    public Optional<Company> findByName(String name) throws DataAccessException {
        String sql = "SELECT id, name, industry, website, contact_person, contact_email, hr_phone, created_at "
                + "FROM companies WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find company name=" + name, e);
            throw new DataAccessException("Failed to find company by name", e);
        }
    }

    @Override
    public List<Company> findAll() throws DataAccessException {
        return search(null);
    }

    @Override
    public List<Company> search(String keyword) throws DataAccessException {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String sql = "SELECT id, name, industry, website, contact_person, contact_email, hr_phone, created_at FROM companies "
                + (hasKeyword ? "WHERE LOWER(name) LIKE ? OR LOWER(industry) LIKE ? OR LOWER(contact_person) LIKE ? " : "")
                + "ORDER BY name";
        List<Company> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasKeyword) {
                String pat = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(1, pat);
                ps.setString(2, pat);
                ps.setString(3, pat);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to search companies", e);
            throw new DataAccessException("Failed to search companies", e);
        }
    }

    @Override
    public boolean update(Company c) throws DataAccessException {
        String sql = "UPDATE companies SET name = ?, industry = ?, website = ?, "
                + "contact_person = ?, contact_email = ?, hr_phone = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getIndustry());
            ps.setString(3, c.getWebsite());
            ps.setString(4, c.getContactPerson());
            ps.setString(5, c.getContactEmail());
            ps.setString(6, c.getHrPhone());
            ps.setInt(7, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update company id=" + c.getId(), e);
            throw new DataAccessException("Failed to update company", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DataAccessException {
        String sql = "DELETE FROM companies WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete company id=" + id, e);
            throw new DataAccessException("Failed to delete company", e);
        }
    }

    private Company mapRow(ResultSet rs) throws SQLException {
        Company c = new Company();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setIndustry(rs.getString("industry"));
        c.setWebsite(rs.getString("website"));
        c.setContactPerson(rs.getString("contact_person"));
        c.setContactEmail(rs.getString("contact_email"));
        c.setHrPhone(rs.getString("hr_phone"));
        c.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        return c;
    }
}
