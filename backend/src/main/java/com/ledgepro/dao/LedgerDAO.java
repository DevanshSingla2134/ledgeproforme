package com.ledgepro.dao;

import com.ledgepro.model.LedgerEntry;
import com.ledgepro.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface LedgerDAO {
    List<LedgerEntry> findByCustomerId(Long customerId);
    List<LedgerEntry> findRecentActivity(Long businessId, int limit);
    
    /**
     * Records a payment against customer khata ledger atomically:
     * Decreases customer's current balance, inserts PAYMENT_RECEIVED ledger entry,
     * and logs the settlement in payments table.
     */
    Payment recordKhataPayment(Long businessId, Long customerId, BigDecimal amount, 
                               String paymentMethod, String referenceNumber, String notes) throws SQLException;

    LedgerEntry recordAdjustment(Long businessId, Long customerId, String entryType, 
                                 BigDecimal amount, String description) throws SQLException;

    BigDecimal getTotalPaymentsReceived(Long businessId);
}
