package com.ledgepro.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer entity representing customer khata accounts,
 * credit limits, and running balances.
 */
public class Customer extends BaseEntity implements Comparable<Customer> {
    private Long businessId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance; // Positive = owes money to store
    private BigDecimal totalPurchases;
    private BigDecimal totalPayments;
    private LocalDateTime lastTransactionDate;

    public Customer() {
        super();
        this.openingBalance = BigDecimal.ZERO;
        this.currentBalance = BigDecimal.ZERO;
        this.totalPurchases = BigDecimal.ZERO;
        this.totalPayments = BigDecimal.ZERO;
    }

    public Customer(Long id, Long businessId, String name, String phone, BigDecimal openingBalance) {
        super(id);
        this.businessId = businessId;
        this.name = name;
        this.phone = phone;
        this.openingBalance = openingBalance != null ? openingBalance : BigDecimal.ZERO;
        this.currentBalance = this.openingBalance;
        this.totalPurchases = BigDecimal.ZERO;
        this.totalPayments = BigDecimal.ZERO;
    }

    public boolean hasOutstandingBalance() {
        return currentBalance != null && currentBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public int compareTo(Customer other) {
        if (other == null) return 1;
        return this.name.compareToIgnoreCase(other.name);
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(BigDecimal totalPurchases) { this.totalPurchases = totalPurchases; }

    public BigDecimal getTotalPayments() { return totalPayments; }
    public void setTotalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; }

    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }
}
