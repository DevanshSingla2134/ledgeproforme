package com.ledgepro.dao;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.model.Supplier;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SupplierDAOImpl implements SupplierDAO {

    private final DatabaseConnectionFactory connectionFactory;

    public SupplierDAOImpl(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding supplier: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Supplier> findAll(Long businessId) {
        String sql = "SELECT * FROM suppliers WHERE business_id = ? ORDER BY name ASC";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching suppliers", e);
        }
        return suppliers;
    }

    @Override
    public List<Supplier> search(Long businessId, String query) {
        String sql = "SELECT * FROM suppliers WHERE business_id = ? AND (LOWER(name) LIKE ? OR phone LIKE ?) ORDER BY name ASC";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            String pattern = "%" + query.trim().toLowerCase() + "%";
            ps.setString(2, pattern);
            ps.setString(3, "%" + query.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error searching suppliers", e);
        }
        return suppliers;
    }

    @Override
    public Supplier save(Supplier supplier) {
        String sql = "INSERT INTO suppliers (business_id, name, contact_person, phone, email, gstin, address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, supplier.getBusinessId());
            ps.setString(2, supplier.getName());
            ps.setString(3, supplier.getContactPerson());
            ps.setString(4, supplier.getPhone());
            ps.setString(5, supplier.getEmail());
            ps.setString(6, supplier.getGstin());
            ps.setString(7, supplier.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) supplier.setId(keys.getLong(1));
            }
            return supplier;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error saving supplier", e);
        }
    }

    @Override
    public boolean update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, phone = ?, email = ?, gstin = ?, address = ? WHERE id = ? AND business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContactPerson());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getGstin());
            ps.setString(6, supplier.getAddress());
            ps.setLong(7, supplier.getId());
            ps.setLong(8, supplier.getBusinessId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating supplier", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting supplier", e);
        }
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setBusinessId(rs.getLong("business_id"));
        s.setName(rs.getString("name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setGstin(rs.getString("gstin"));
        s.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }
}
