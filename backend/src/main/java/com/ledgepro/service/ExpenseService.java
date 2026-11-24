package com.ledgepro.service;

import com.ledgepro.dao.ExpenseDAO;
import com.ledgepro.model.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseDAO expenseDAO;

    public ExpenseService(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    public List<Expense> getAllExpenses(Long businessId) {
        return expenseDAO.findAll(businessId);
    }

    public List<Expense> getExpensesByCategory(Long businessId, String category) {
        return expenseDAO.findByCategory(businessId, category);
    }

    public Expense createExpense(Expense expense) {
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense category is required.");
        }
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        return expenseDAO.save(expense);
    }

    public boolean deleteExpense(Long id) {
        return expenseDAO.deleteById(id);
    }

    public BigDecimal getTotalExpenses(Long businessId) {
        return expenseDAO.getTotalExpenses(businessId);
    }
}
