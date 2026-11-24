package com.ledgepro.dao;

import com.ledgepro.model.Expense;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseDAO extends GenericDAO<Expense, Long> {
    List<Expense> findByCategory(Long businessId, String category);
    BigDecimal getTotalExpenses(Long businessId);
}
