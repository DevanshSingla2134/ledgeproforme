package com.ledgepro.service;

import com.ledgepro.dao.CustomerDAO;
import com.ledgepro.dao.LedgerDAO;
import com.ledgepro.exception.CustomerNotFoundException;
import com.ledgepro.model.Customer;
import com.ledgepro.model.LedgerEntry;
import com.ledgepro.model.Payment;
import com.ledgepro.util.CsvUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Service
public class KhataLedgerService {

    private final LedgerDAO ledgerDAO;
    private final CustomerDAO customerDAO;

    public KhataLedgerService(LedgerDAO ledgerDAO, CustomerDAO customerDAO) {
        this.ledgerDAO = ledgerDAO;
        this.customerDAO = customerDAO;
    }

    public List<LedgerEntry> getCustomerLedgerTimeline(Long customerId) {
        return ledgerDAO.findByCustomerId(customerId);
    }

    public List<LedgerEntry> getRecentLedgerActivity(Long businessId, int limit) {
        return ledgerDAO.findRecentActivity(businessId, limit);
    }

    /**
     * Records a payment against customer khata ledger
     */
    public Payment recordPayment(Long businessId, Long customerId, BigDecimal amount, 
                                 String paymentMethod, String referenceNumber, String notes) throws SQLException {
        return ledgerDAO.recordKhataPayment(businessId, customerId, amount, paymentMethod, referenceNumber, notes);
    }

    /**
     * Records a credit/debit adjustment
     */
    public LedgerEntry recordAdjustment(Long businessId, Long customerId, String entryType, 
                                        BigDecimal amount, String description) throws SQLException {
        return ledgerDAO.recordAdjustment(businessId, customerId, entryType, amount, description);
    }

    /**
     * Generates a downloadable CSV Khata Statement for a customer
     */
    public String generateCustomerStatementCsv(Long customerId) throws IOException {
        Customer customer = customerDAO.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        List<LedgerEntry> entries = ledgerDAO.findByCustomerId(customerId);
        return CsvUtils.exportCustomerStatementCsv(customer, entries);
    }
}
