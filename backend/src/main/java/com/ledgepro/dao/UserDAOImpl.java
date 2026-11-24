package com.ledgepro.dao;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.model.Business;
import com.ledgepro.model.User;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {

    private final DatabaseConnectionFactory connectionFactory;

    public UserDAOImpl(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier.trim());
            ps.setString(2, identifier.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error looking up user by identifier: " + identifier, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error checking username availability", e);
        }
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error checking email availability", e);
        }
        return false;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole() != null ? user.getRole() : "ADMIN");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating user account", e);
        }
    }

    @Override
    public Business createBusinessForUser(Long userId, String businessName, String phone) {
        String sql = """
            INSERT INTO businesses (user_id, name, tagline, phone, currency, currency_symbol, invoice_prefix, next_invoice_number, tax_rate, low_stock_threshold, created_at) 
            VALUES (?, ?, 'Your Business. One Smart Ledger.', ?, 'INR', '₹', 'INV-', 1001, 18.00, 10, CURRENT_TIMESTAMP)
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setString(2, businessName);
            ps.setString(3, phone != null ? phone : "");
            ps.executeUpdate();
            Business b = new Business();
            b.setUserId(userId);
            b.setName(businessName);
            b.setPhone(phone != null ? phone : "");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    b.setId(keys.getLong(1));
                }
            }
            return b;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error creating business profile for user", e);
        }
    }

    @Override
    public Optional<Business> getBusinessByUserId(Long userId) {
        String sql = "SELECT * FROM businesses WHERE user_id = ? ORDER BY id ASC LIMIT 1";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Business b = new Business();
                    b.setId(rs.getLong("id"));
                    b.setUserId(rs.getLong("user_id"));
                    b.setName(rs.getString("name"));
                    b.setTagline(rs.getString("tagline"));
                    b.setTaxId(rs.getString("tax_id"));
                    b.setPhone(rs.getString("phone"));
                    b.setEmail(rs.getString("email"));
                    b.setAddress(rs.getString("address"));
                    b.setCurrency(rs.getString("currency"));
                    b.setCurrencySymbol(rs.getString("currency_symbol"));
                    b.setInvoicePrefix(rs.getString("invoice_prefix"));
                    b.setNextInvoiceNumber(rs.getInt("next_invoice_number"));
                    b.setTaxRate(rs.getBigDecimal("tax_rate"));
                    b.setLowStockThreshold(rs.getInt("low_stock_threshold"));
                    return Optional.of(b);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching business for user", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding user by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll(Long businessId) {
        String sql = "SELECT * FROM users ORDER BY id ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error listing users", e);
        }
        return users;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setLong(3, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating user profile", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting user: " + id, e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }
}
