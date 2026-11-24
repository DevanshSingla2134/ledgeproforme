package com.ledgepro.service;

import com.ledgepro.dao.CustomerDAO;
import com.ledgepro.dao.ExpenseDAO;
import com.ledgepro.dao.InvoiceDAO;
import com.ledgepro.dao.ProductDAO;
import com.ledgepro.model.Customer;
import com.ledgepro.model.Expense;
import com.ledgepro.model.Invoice;
import com.ledgepro.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report Analytics Service demonstrating:
 * - Java 21 Stream API & Lambda Expressions
 * - filter(), map(), sorted(), distinct(), limit(), count(), min(), max(), reduce()
 * - Collectors.groupingBy(), Collectors.partitioningBy(), Collectors.summarizingDouble()
 * - Clear separation of Revenue, Estimated Gross Profit, and Expenses
 */
@Service
public class ReportAnalyticsService {

    private final InvoiceDAO invoiceDAO;
    private final ProductDAO productDAO;
    private final CustomerDAO customerDAO;
    private final ExpenseDAO expenseDAO;

    public ReportAnalyticsService(InvoiceDAO invoiceDAO, ProductDAO productDAO, 
                                  CustomerDAO customerDAO, ExpenseDAO expenseDAO) {
        this.invoiceDAO = invoiceDAO;
        this.productDAO = productDAO;
        this.customerDAO = customerDAO;
        this.expenseDAO = expenseDAO;
    }

    /**
     * Top selling products sorted by revenue using Streams & Lambdas
     */
    public List<Map<String, Object>> getTopSellingProducts(Long businessId, int limit) {
        List<Invoice> invoices = invoiceDAO.findAll(businessId);

        // Group invoice items by Product Name and sum revenues using Stream API
        Map<String, Double> productRevenueMap = invoices.stream()
                .filter(inv -> inv.getItems() != null)
                .flatMap(inv -> inv.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProductName() != null ? item.getProductName() : "Unknown Product",
                        Collectors.summingDouble(item -> item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : 0.0)
                ));

        return productRevenueMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("productName", entry.getKey());
                    map.put("totalRevenue", BigDecimal.valueOf(entry.getValue()));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates partitioningBy: splits customers into those with outstanding balance vs clear
     */
    public Map<Boolean, List<Customer>> partitionCustomersByDebt(Long businessId) {
        List<Customer> customers = customerDAO.findAll(businessId);
        return customers.stream()
                .collect(Collectors.partitioningBy(Customer::hasOutstandingBalance));
    }

    /**
     * Group expenses by category using Collectors.groupingBy and summarize
     */
    public Map<String, Double> getExpensesByCategory(Long businessId) {
        List<Expense> expenses = expenseDAO.findAll(businessId);
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(e -> e.getAmount() != null ? e.getAmount().doubleValue() : 0.0)
                ));
    }

    /**
     * Sales performance breakdown by payment method using Stream API
     */
    public Map<String, Double> getSalesByPaymentMethod(Long businessId) {
        List<Invoice> invoices = invoiceDAO.findAll(businessId);
        return invoices.stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getPaymentMethod() != null ? inv.getPaymentMethod().toUpperCase() : "OTHER",
                        Collectors.summingDouble(inv -> inv.getGrandTotal() != null ? inv.getGrandTotal().doubleValue() : 0.0)
                ));
    }

    /**
     * Inventory valuation and stock health report using summarizingDouble and reduce()
     */
    public Map<String, Object> getInventoryHealthMetrics(Long businessId) {
        List<Product> products = productDAO.findAll(businessId);

        DoubleSummaryStatistics priceStats = products.stream()
                .collect(Collectors.summarizingDouble(p -> p.getSellingPrice().doubleValue()));

        // Total inventory valuation calculated with reduce()
        BigDecimal totalValuation = products.stream()
                .map(Product::calculateStockValuation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStockCount = products.stream()
                .filter(Product::isLowStock)
                .count();

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalProducts", products.size());
        metrics.put("totalValuation", totalValuation);
        metrics.put("lowStockCount", lowStockCount);
        metrics.put("averageSellingPrice", priceStats.getAverage());
        metrics.put("highestSellingPrice", priceStats.getMax());
        metrics.put("lowestSellingPrice", priceStats.getMin());

        return metrics;
    }

    /**
     * Executive financial P&L summary
     */
    public Map<String, Object> getFinancialSummary(Long businessId) {
        BigDecimal totalRevenue = invoiceDAO.getTotalRevenueThisMonth(businessId);
        BigDecimal grossProfit = invoiceDAO.getEstimatedGrossProfit(businessId);
        BigDecimal totalExpenses = expenseDAO.getTotalExpenses(businessId);
        BigDecimal netProfit = grossProfit.subtract(totalExpenses);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("monthlyRevenue", totalRevenue);
        summary.put("estimatedGrossProfit", grossProfit);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netProfit", netProfit);
        summary.put("profitMarginPercent", totalRevenue.signum() > 0 ? 
                netProfit.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);

        return summary;
    }
}
