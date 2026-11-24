package com.ledgepro.controller;

import com.ledgepro.model.Expense;
import com.ledgepro.service.ExpenseService;
import com.ledgepro.util.GenericResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public GenericResponse<List<Expense>> getExpenses(
            @RequestParam(defaultValue = "1") Long businessId,
            @RequestParam(required = false) String category) {
        List<Expense> expenses;
        if (category != null && !category.trim().isEmpty()) {
            expenses = expenseService.getExpensesByCategory(businessId, category);
        } else {
            expenses = expenseService.getAllExpenses(businessId);
        }
        return GenericResponse.success(expenses);
    }

    @PostMapping
    public GenericResponse<Expense> createExpense(@RequestBody Expense expense) {
        if (expense.getBusinessId() == null) expense.setBusinessId(1L);
        return GenericResponse.success(expenseService.createExpense(expense), "Expense recorded successfully");
    }

    @DeleteMapping("/{id}")
    public GenericResponse<Boolean> deleteExpense(@PathVariable Long id) {
        return GenericResponse.success(expenseService.deleteExpense(id), "Expense removed successfully");
    }
}
