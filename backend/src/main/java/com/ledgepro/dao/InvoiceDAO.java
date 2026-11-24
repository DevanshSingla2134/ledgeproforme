package com.ledgepro.dao;

import com.ledgepro.model.Invoice;
import com.ledgepro.model.InvoiceItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface InvoiceDAO extends GenericDAO<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findRecent(Long businessId, int limit);
    List<Invoice> findByCustomer(Long businessId, Long customerId);
    List<InvoiceItem> findItemsByInvoiceId(Long invoiceId);
    
    /**
     * Executes atomic POS checkout across invoices, invoice_items,
     * stock deduction, stock_movements, ledger_entries, and payments.
     */
    Invoice executeAtomicBillingTransaction(Invoice invoice) throws SQLException;

    int getTotalOrderCount(Long businessId);
    BigDecimal getTotalSalesToday(Long businessId);
    BigDecimal getTotalRevenueThisMonth(Long businessId);
    BigDecimal getEstimatedGrossProfit(Long businessId);
}
