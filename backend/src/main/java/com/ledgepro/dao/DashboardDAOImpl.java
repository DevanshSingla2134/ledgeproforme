package com.ledgepro.dao;

import com.ledgepro.model.DashboardStats;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardDAOImpl implements DashboardDAO {

    private final ProductDAO productDAO;
    private final CustomerDAO customerDAO;
    private final InvoiceDAO invoiceDAO;
    private final LedgerDAO ledgerDAO;
    private final ExpenseDAO expenseDAO;

    public DashboardDAOImpl(ProductDAO productDAO, CustomerDAO customerDAO, 
                            InvoiceDAO invoiceDAO, LedgerDAO ledgerDAO, 
                            ExpenseDAO expenseDAO) {
        this.productDAO = productDAO;
        this.customerDAO = customerDAO;
        this.invoiceDAO = invoiceDAO;
        this.ledgerDAO = ledgerDAO;
        this.expenseDAO = expenseDAO;
    }

    @Override
    public DashboardStats getDashboardStats(Long businessId) {
        DashboardStats stats = new DashboardStats();

        stats.setTotalSalesToday(invoiceDAO.getTotalSalesToday(businessId));
        stats.setTotalRevenueThisMonth(invoiceDAO.getTotalRevenueThisMonth(businessId));
        stats.setTotalOrders(invoiceDAO.getTotalOrderCount(businessId));
        stats.setTotalProducts(productDAO.getTotalProductCount(businessId));
        stats.setLowStockCount(productDAO.getLowStockCount(businessId));
        stats.setTotalCustomers(customerDAO.getTotalCustomerCount(businessId));
        stats.setOutstandingKhataBalance(customerDAO.getTotalOutstandingBalance(businessId));
        stats.setPaymentsReceived(ledgerDAO.getTotalPaymentsReceived(businessId));
        stats.setEstimatedGrossProfit(invoiceDAO.getEstimatedGrossProfit(businessId));
        stats.setTotalExpenses(expenseDAO.getTotalExpenses(businessId));

        stats.setRecentInvoices(invoiceDAO.findRecent(businessId, 5));
        stats.setRecentLedgerActivity(ledgerDAO.findRecentActivity(businessId, 5));
        stats.setLowStockProducts(productDAO.findLowStock(businessId));

        return stats;
    }
}
