package com.ledgepro.dao;

import com.ledgepro.config.DatabaseConnectionFactory;
import com.ledgepro.exception.DatabaseOperationException;
import com.ledgepro.model.Expense;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ExpenseDAOImpl implements ExpenseDAO {

    private final DatabaseConnectionFactory connectionFactory;

    public ExpenseDAOImpl(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Expense> findById(Long id) {
        String sql = "SELECT * FROM expenses WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToExpense(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding expense", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Expense> findAll(Long businessId) {
        String sql = "SELECT * FROM expenses WHERE business_id = ? ORDER BY expense_date DESC";
        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) expenses.add(mapResultSetToExpense(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching expenses", e);
        }
        return expenses;
    }

    @Override
    public List<Expense> findByCategory(Long businessId, String category) {
        String sql = "SELECT * FROM expenses WHERE business_id = ? AND category = ? ORDER BY expense_date DESC";
        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) expenses.add(mapResultSetToExpense(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching expenses by category", e);
        }
        return expenses;
    }

    @Override
    public Expense save(Expense expense) {
        String sql = "INSERT INTO expenses (business_id, category, amount, description, payment_method, expense_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, expense.getBusinessId());
            ps.setString(2, expense.getCategory());
            ps.setBigDecimal(3, expense.getAmount());
            ps.setString(4, expense.getDescription());
            ps.setString(5, expense.getPaymentMethod());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) expense.setId(keys.getLong(1));
            }
            return expense;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error saving expense", e);
        }
    }

    @Override
    public boolean update(Expense expense) {
        String sql = "UPDATE expenses SET category = ?, amount = ?, description = ?, payment_method = ? WHERE id = ? AND business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getCategory());
            ps.setBigDecimal(2, expense.getAmount());
            ps.setString(3, expense.getDescription());
            ps.setString(4, expense.getPaymentMethod());
            ps.setLong(5, expense.getId());
            ps.setLong(6, expense.getBusinessId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating expense", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting expense", e);
        }
    }

    @Override
    public BigDecimal getTotalExpenses(Long businessId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE business_id = ?";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating expenses", e);
        }
        return BigDecimal.ZERO;
    }

    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getLong("id"));
        e.setBusinessId(rs.getLong("business_id"));
        e.setCategory(rs.getString("category"));
        e.setAmount(rs.getBigDecimal("amount"));
        e.setDescription(rs.getString("description"));
        e.setPaymentMethod(rs.getString("payment_method"));
        Timestamp dt = rs.getTimestamp("expense_date");
        if (dt != null) e.setExpenseDate(dt.toLocalDateTime());
        return e;
    }
}
