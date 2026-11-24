package com.ledgepro.model;

import java.math.BigDecimal;

/**
 * Digital Khata Ledger Entry model representing permanent,
 * audit-proof customer credit/payment transactions.
 */
public class LedgerEntry extends BaseEntity {
    private Long businessId;
    private Long customerId;
    private String customerName;
    private String entryType; // CREDIT_SALE, PAYMENT_RECEIVED, DEBIT_ADJUSTMENT, CREDIT_ADJUSTMENT
    private BigDecimal amount;
    private BigDecimal runningBalance;
    private Long referenceInvoiceId;
    private String description;

    public LedgerEntry() {
        super();
        this.amount = BigDecimal.ZERO;
        this.runningBalance = BigDecimal.ZERO;
    }

    public LedgerEntry(Long businessId, Long customerId, String entryType, BigDecimal amount, BigDecimal runningBalance, Long referenceInvoiceId, String description) {
        super();
        this.businessId = businessId;
        this.customerId = customerId;
        this.entryType = entryType;
        this.amount = amount;
        this.runningBalance = runningBalance;
        this.referenceInvoiceId = referenceInvoiceId;
        this.description = description;
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getRunningBalance() { return runningBalance; }
    public void setRunningBalance(BigDecimal runningBalance) { this.runningBalance = runningBalance; }

    public Long getReferenceInvoiceId() { return referenceInvoiceId; }
    public void setReferenceInvoiceId(Long referenceInvoiceId) { this.referenceInvoiceId = referenceInvoiceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
