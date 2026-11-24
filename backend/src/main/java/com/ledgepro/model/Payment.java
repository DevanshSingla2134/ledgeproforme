package com.ledgepro.model;

import java.math.BigDecimal;

public class Payment extends BaseEntity {
    private Long businessId;
    private Long customerId;
    private Long invoiceId;
    private BigDecimal amount;
    private String paymentMethod; // CASH, UPI, CARD, KHATA, SPLIT
    private String referenceNumber;
    private String notes;

    public Payment() {
        super();
        this.amount = BigDecimal.ZERO;
        this.paymentMethod = "CASH";
    }

    public Payment(Long businessId, Long customerId, Long invoiceId, BigDecimal amount, String paymentMethod, String referenceNumber) {
        super();
        this.businessId = businessId;
        this.customerId = customerId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
