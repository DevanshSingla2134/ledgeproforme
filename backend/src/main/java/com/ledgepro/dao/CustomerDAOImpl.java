package com.ledgepro.dao;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.model.Customer;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerDAOImpl implements CustomerDAO {

    private final DatabaseConnectionFactory connectionFactory;

    public CustomerDAOImpl(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        String sql = """
            SELECT c.*, 
                   COALESCE((SELECT SUM(i.grand_total) FROM invoices i WHERE i.customer_id = c.id), 0) as total_purchases,
                   COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customer_id = c.id), 0) as total_payments,
                   (SELECT MAX(created_at) FROM ledger_entries le WHERE le.customer_id = c.id) as last_txn_date
            FROM customers c 
            WHERE c.id = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding customer by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByPhone(Long businessId, String phone) {
        String sql = """
            SELECT c.*, 
                   COALESCE((SELECT SUM(i.grand_total) FROM invoices i WHERE i.customer_id = c.id), 0) as total_purchases,
                   COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customer_id = c.id), 0) as total_payments,
                   (SELECT MAX(created_at) FROM ledger_entries le WHERE le.customer_id = c.id) as last_txn_date
            FROM customers c 
            WHERE c.business_id = ? AND c.phone = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            ps.setString(2, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding customer by phone: " + phone, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll(Long businessId) {
        String sql = """
            SELECT c.*, 
                   COALESCE((SELECT SUM(i.grand_total) FROM invoices i WHERE i.customer_id = c.id), 0) as total_purchases,
                   COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customer_id = c.id), 0) as total_payments,
                   (SELECT MAX(created_at) FROM ledger_entries le WHERE le.customer_id = c.id) as last_txn_date
            FROM customers c 
            WHERE c.business_id = ? 
            ORDER BY c.name ASC
        """;
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all customers", e);
        }
        return customers;
    }

    @Override
    public List<Customer> search(Long businessId, String query) {
        String sql = """
            SELECT c.*, 
                   COALESCE((SELECT SUM(i.grand_total) FROM invoices i WHERE i.customer_id = c.id), 0) as total_purchases,
                   COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customer_id = c.id), 0) as total_payments,
                   (SELECT MAX(created_at) FROM ledger_entries le WHERE le.customer_id = c.id) as last_txn_date
            FROM customers c 
            WHERE c.business_id = ? AND (LOWER(c.name) LIKE ? OR c.phone LIKE ?)
            ORDER BY c.name ASC
        """;
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            String pattern = "%" + query.trim().toLowerCase() + "%";
            ps.setString(2, pattern);
            ps.setString(3, "%" + query.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error searching customers", e);
        }
        return customers;
    }

    @Override
    public List<Customer> findWithOutstandingBalance(Long businessId) {
        String sql = """
            SELECT c.*, 
                   COALESCE((SELECT SUM(i.grand_total) FROM invoices i WHERE i.customer_id = c.id), 0) as total_purchases,
                   COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.customer_id = c.id), 0) as total_payments,
                   (SELECT MAX(created_at) FROM ledger_entries le WHERE le.customer_id = c.id) as last_txn_date
            FROM customers c 
            WHERE c.business_id = ? AND c.current_balance > 0 
            ORDER BY c.current_balance DESC
        """;
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching customers with outstanding balance", e);
        }
        return customers;
    }

    @Override
    public Customer save(Customer customer) {
        String sql = """
            INSERT INTO customers (business_id, name, phone, email, address, opening_balance, current_balance, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, customer.getBusinessId());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getAddress());
            ps.setBigDecimal(6, customer.getOpeningBalance() != null ? customer.getOpeningBalance() : BigDecimal.ZERO);
            ps.setBigDecimal(7, customer.getCurrentBalance() != null ? customer.getCurrentBalance() : BigDecimal.ZERO);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        customer.setId(keys.getLong(1));
                    }
                }
            }
            return customer;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error saving customer: " + customer.getName(), e);
        }
    }

    @Override
    public boolean update(Customer customer) {
        String sql = """
            UPDATE customers 
            SET name = ?, phone = ?, email = ?, address = ?, opening_balance = ?, current_balance = ? 
            WHERE id = ? AND business_id = ?
        """;
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setBigDecimal(5, customer.getOpeningBalance());
            ps.setBigDecimal(6, customer.getCurrentBalance());
            ps.setLong(7, customer.getId());
            ps.setLong(8, customer.getBusinessId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating customer: " + customer.getId(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting customer: " + id, e);
        }
    }

    @Override
    public boolean updateBalance(Connection conn, Long customerId, BigDecimal balanceDelta) throws SQLException {
        String sql = "UPDATE customers SET current_balance = current_balance + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, balanceDelta);
            ps.setLong(2, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int getTotalCustomerCount(Long businessId) {
        String sql = "SELECT COUNT(*) FROM customers WHERE business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting customers", e);
        }
        return 0;
    }

    @Override
    public BigDecimal getTotalOutstandingBalance(Long businessId) {
        String sql = "SELECT COALESCE(SUM(current_balance), 0) FROM customers WHERE business_id = ? AND current_balance > 0";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating outstanding balance", e);
        }
        return BigDecimal.ZERO;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setBusinessId(rs.getLong("business_id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setOpeningBalance(rs.getBigDecimal("opening_balance"));
        c.setCurrentBalance(rs.getBigDecimal("current_balance"));

        c.setTotalPurchases(rs.getBigDecimal("total_purchases"));
        c.setTotalPayments(rs.getBigDecimal("total_payments"));

        Timestamp lastTxn = rs.getTimestamp("last_txn_date");
        if (lastTxn != null) {
            c.setLastTransactionDate(lastTxn.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            c.setCreatedAt(createdAt.toLocalDateTime());
        }
        return c;
    }
}
