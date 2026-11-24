package com.ledgepro.controller;

import com.ledgepro.model.Expense;
import com.ledgepro.service.ExpenseService;
import com.ledgepro.util.GenericResponse;
import com.ledgepro.util.TenantUtil;
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
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String category) {
        Long targetBizId = TenantUtil.resolveBusinessId(businessId, headerBizId);
        List<Expense> expenses;
        if (category != null && !category.trim().isEmpty()) {
            expenses = expenseService.getExpensesByCategory(targetBizId, category);
        } else {
            expenses = expenseService.getAllExpenses(targetBizId);
        }
        return GenericResponse.success(expenses);
    }

    @PostMapping
    public GenericResponse<Expense> createExpense(
            @RequestHeader(value = "X-Business-Id", required = false) String headerBizId,
            @RequestBody Expense expense) {
        if (expense.getBusinessId() == null || expense.getBusinessId() <= 0) {
            expense.setBusinessId(TenantUtil.resolveBusinessId(null, headerBizId));
        }
        return GenericResponse.success(expenseService.createExpense(expense), "Expense recorded successfully");
    }

    @DeleteMapping("/{id}")
    public GenericResponse<Boolean> deleteExpense(@PathVariable Long id) {
        return GenericResponse.success(expenseService.deleteExpense(id), "Expense removed successfully");
    }
}
