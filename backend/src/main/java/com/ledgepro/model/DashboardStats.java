package com.ledgepro.model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStats {
    private BigDecimal totalSalesToday;
    private BigDecimal totalRevenueThisMonth;
    private int totalOrders;
    private int totalProducts;
    private int lowStockCount;
    private int totalCustomers;
    private BigDecimal outstandingKhataBalance;
    private BigDecimal paymentsReceived;
    private BigDecimal estimatedGrossProfit;
    private BigDecimal totalExpenses;

    private List<Invoice> recentInvoices;
    private List<LedgerEntry> recentLedgerActivity;
    private List<Product> lowStockProducts;

    public DashboardStats() {
        this.totalSalesToday = BigDecimal.ZERO;
        this.totalRevenueThisMonth = BigDecimal.ZERO;
        this.outstandingKhataBalance = BigDecimal.ZERO;
        this.paymentsReceived = BigDecimal.ZERO;
        this.estimatedGrossProfit = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
    }

    public BigDecimal getTotalSalesToday() { return totalSalesToday; }
    public void setTotalSalesToday(BigDecimal totalSalesToday) { this.totalSalesToday = totalSalesToday; }

    public BigDecimal getTotalRevenueThisMonth() { return totalRevenueThisMonth; }
    public void setTotalRevenueThisMonth(BigDecimal totalRevenueThisMonth) { this.totalRevenueThisMonth = totalRevenueThisMonth; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

    public int getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(int lowStockCount) { this.lowStockCount = lowStockCount; }

    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

    public BigDecimal getOutstandingKhataBalance() { return outstandingKhataBalance; }
    public void setOutstandingKhataBalance(BigDecimal outstandingKhataBalance) { this.outstandingKhataBalance = outstandingKhataBalance; }

    public BigDecimal getPaymentsReceived() { return paymentsReceived; }
    public void setPaymentsReceived(BigDecimal paymentsReceived) { this.paymentsReceived = paymentsReceived; }

    public BigDecimal getEstimatedGrossProfit() { return estimatedGrossProfit; }
    public void setEstimatedGrossProfit(BigDecimal estimatedGrossProfit) { this.estimatedGrossProfit = estimatedGrossProfit; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public List<Invoice> getRecentInvoices() { return recentInvoices; }
    public void setRecentInvoices(List<Invoice> recentInvoices) { this.recentInvoices = recentInvoices; }

    public List<LedgerEntry> getRecentLedgerActivity() { return recentLedgerActivity; }
    public void setRecentLedgerActivity(List<LedgerEntry> recentLedgerActivity) { this.recentLedgerActivity = recentLedgerActivity; }

    public List<Product> getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(List<Product> lowStockProducts) { this.lowStockProducts = lowStockProducts; }
}
