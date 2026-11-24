package com.ledgepro.dao;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.CustomerNotFoundException;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.exception.InvalidPaymentException;
import com.ledgepro.model.LedgerEntry;
import com.ledgepro.model.Payment;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LedgerDAOImpl implements LedgerDAO {

    private final DatabaseConnectionFactory connectionFactory;

    public LedgerDAOImpl(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<LedgerEntry> findByCustomerId(Long customerId) {
        String sql = """
            SELECT le.*, c.name as customer_name 
            FROM ledger_entries le 
            JOIN customers c ON le.customer_id = c.id 
            WHERE le.customer_id = ? 
            ORDER BY le.id ASC
        """;
        List<LedgerEntry> entries = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToLedgerEntry(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching ledger entries for customer: " + customerId, e);
        }
        return entries;
    }

    @Override
    public List<LedgerEntry> findRecentActivity(Long businessId, int limit) {
        String sql = """
            SELECT le.*, c.name as customer_name 
            FROM ledger_entries le 
            JOIN customers c ON le.customer_id = c.id 
            WHERE le.business_id = ? 
            ORDER BY le.id DESC 
            LIMIT ?
        """;
        List<LedgerEntry> entries = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToLedgerEntry(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching recent ledger activity", e);
        }
        return entries;
    }

    /**
     * Atomic Khata Settlement with JDBC Transactions
     */
    @Override
    public Payment recordKhataPayment(Long businessId, Long customerId, BigDecimal amount, 
                                      String paymentMethod, String referenceNumber, String notes) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero.");
        }

        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false);

            // 1. Check customer and current balance
            BigDecimal currentBalance = BigDecimal.ZERO;
            String checkCustomerSql = "SELECT name, current_balance FROM customers WHERE id = ? AND business_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkCustomerSql)) {
                psCheck.setLong(1, customerId);
                psCheck.setLong(2, businessId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getBigDecimal("current_balance");
                    } else {
                        throw new CustomerNotFoundException(customerId);
                    }
                }
            }

            // 2. Decrement customer balance
            BigDecimal newBalance = currentBalance.subtract(amount);
            String updateCustSql = "UPDATE customers SET current_balance = ? WHERE id = ?";
            try (PreparedStatement psCust = conn.prepareStatement(updateCustSql)) {
                psCust.setBigDecimal(1, newBalance);
                psCust.setLong(2, customerId);
                psCust.executeUpdate();
            }

            // 3. Record in ledger_entries
            String insertLedgerSql = """
                INSERT INTO ledger_entries (business_id, customer_id, entry_type, amount, 
                                           running_balance, reference_invoice_id, description, created_at) 
                VALUES (?, ?, 'PAYMENT_RECEIVED', ?, ?, NULL, ?, CURRENT_TIMESTAMP)
            """;
            try (PreparedStatement psLedger = conn.prepareStatement(insertLedgerSql)) {
                psLedger.setLong(1, businessId);
                psLedger.setLong(2, customerId);
                psLedger.setBigDecimal(3, amount);
                psLedger.setBigDecimal(4, newBalance);
                psLedger.setString(5, notes != null && !notes.trim().isEmpty() ? notes : "Payment received via " + paymentMethod);
                psLedger.executeUpdate();
            }

            // 4. Record in payments table
            Payment payment = new Payment(businessId, customerId, null, amount, paymentMethod, referenceNumber);
            payment.setNotes(notes);
            String insertPaymentSql = """
                INSERT INTO payments (business_id, customer_id, invoice_id, amount, payment_method, 
                                     reference_number, notes, created_at) 
                VALUES (?, ?, NULL, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
            try (PreparedStatement psPay = conn.prepareStatement(insertPaymentSql, Statement.RETURN_GENERATED_KEYS)) {
                psPay.setLong(1, businessId);
                psPay.setLong(2, customerId);
                psPay.setBigDecimal(3, amount);
                psPay.setString(4, paymentMethod);
                psPay.setString(5, referenceNumber);
                psPay.setString(6, notes);
                psPay.executeUpdate();

                try (ResultSet keys = psPay.getGeneratedKeys()) {
                    if (keys.next()) {
                        payment.setId(keys.getLong(1));
                    }
                }
            }

            conn.commit();
            payment.setCreatedAt(LocalDateTime.now());
            return payment;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    // Suppress
                }
            }
            if (e instanceof InvalidPaymentException || e instanceof CustomerNotFoundException) {
                throw e;
            }
            throw new DatabaseOperationException("Failed to record khata payment: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Ignored
                }
            }
        }
    }

    @Override
    public LedgerEntry recordAdjustment(Long businessId, Long customerId, String entryType, 
                                        BigDecimal amount, String description) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Adjustment amount must be positive.");
        }

        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false);

            BigDecimal currentBalance = BigDecimal.ZERO;
            String checkCustomerSql = "SELECT current_balance FROM customers WHERE id = ? AND business_id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkCustomerSql)) {
                psCheck.setLong(1, customerId);
                psCheck.setLong(2, businessId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getBigDecimal("current_balance");
                    } else {
                        throw new CustomerNotFoundException(customerId);
                    }
                }
            }

            BigDecimal newBalance;
            if ("CREDIT_ADJUSTMENT".equalsIgnoreCase(entryType)) {
                // Customer credit increased
                newBalance = currentBalance.add(amount);
            } else {
                // Debit adjustment / discount / waiver
                newBalance = currentBalance.subtract(amount);
            }

            String updateCustSql = "UPDATE customers SET current_balance = ? WHERE id = ?";
            try (PreparedStatement psCust = conn.prepareStatement(updateCustSql)) {
                psCust.setBigDecimal(1, newBalance);
                psCust.setLong(2, customerId);
                psCust.executeUpdate();
            }

            LedgerEntry entry = new LedgerEntry(businessId, customerId, entryType.toUpperCase(), amount, newBalance, null, description);
            String insertLedgerSql = """
                INSERT INTO ledger_entries (business_id, customer_id, entry_type, amount, 
                                           running_balance, reference_invoice_id, description, created_at) 
                VALUES (?, ?, ?, ?, ?, NULL, ?, CURRENT_TIMESTAMP)
            """;
            try (PreparedStatement psLedger = conn.prepareStatement(insertLedgerSql, Statement.RETURN_GENERATED_KEYS)) {
                psLedger.setLong(1, businessId);
                psLedger.setLong(2, customerId);
                psLedger.setString(3, entryType.toUpperCase());
                psLedger.setBigDecimal(4, amount);
                psLedger.setBigDecimal(5, newBalance);
                psLedger.setString(6, description);
                psLedger.executeUpdate();

                try (ResultSet keys = psLedger.getGeneratedKeys()) {
                    if (keys.next()) {
                        entry.setId(keys.getLong(1));
                    }
                }
            }

            conn.commit();
            entry.setCreatedAt(LocalDateTime.now());
            return entry;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    // Suppress
                }
            }
            throw new DatabaseOperationException("Adjustment transaction failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Ignored
                }
            }
        }
    }

    @Override
    public BigDecimal getTotalPaymentsReceived(Long businessId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating total payments", e);
        }
        return BigDecimal.ZERO;
    }

    private LedgerEntry mapResultSetToLedgerEntry(ResultSet rs) throws SQLException {
        LedgerEntry le = new LedgerEntry();
        le.setId(rs.getLong("id"));
        le.setBusinessId(rs.getLong("business_id"));
        le.setCustomerId(rs.getLong("customer_id"));
        le.setCustomerName(rs.getString("customer_name"));
        le.setEntryType(rs.getString("entry_type"));
        le.setAmount(rs.getBigDecimal("amount"));
        le.setRunningBalance(rs.getBigDecimal("running_balance"));

        long refInv = rs.getLong("reference_invoice_id");
        if (!rs.wasNull()) {
            le.setReferenceInvoiceId(refInv);
        }
        le.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            le.setCreatedAt(createdAt.toLocalDateTime());
        }
        return le;
    }
}
