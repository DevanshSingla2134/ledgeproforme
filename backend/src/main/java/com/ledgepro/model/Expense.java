package com.ledgepro.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Expense extends BaseEntity {
    private Long businessId;
    private String category; // Rent, Utilities, Salaries, Transport, Packaging, Other
    private BigDecimal amount;
    private String description;
    private LocalDateTime expenseDate;
    private String paymentMethod;

    public Expense() {
        super();
        this.amount = BigDecimal.ZERO;
        this.expenseDate = LocalDateTime.now();
        this.paymentMethod = "CASH";
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDateTime expenseDate) { this.expenseDate = expenseDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
