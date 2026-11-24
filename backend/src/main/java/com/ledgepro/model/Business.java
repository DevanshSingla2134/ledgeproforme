package com.ledgepro.model;

import java.math.BigDecimal;

public class Business extends BaseEntity {
    private Long userId;
    private String name;
    private String tagline;
    private String taxId; // GSTIN
    private String phone;
    private String email;
    private String address;
    private String currency;
    private String currencySymbol;
    private String invoicePrefix;
    private int nextInvoiceNumber;
    private BigDecimal taxRate;
    private int lowStockThreshold;

    public Business() {
        super();
        this.currency = "INR";
        this.currencySymbol = "₹";
        this.invoicePrefix = "LP-";
        this.nextInvoiceNumber = 1001;
        this.taxRate = new BigDecimal("18.00");
        this.lowStockThreshold = 10;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getInvoicePrefix() { return invoicePrefix; }
    public void setInvoicePrefix(String invoicePrefix) { this.invoicePrefix = invoicePrefix; }

    public int getNextInvoiceNumber() { return nextInvoiceNumber; }
    public void setNextInvoiceNumber(int nextInvoiceNumber) { this.nextInvoiceNumber = nextInvoiceNumber; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
}
