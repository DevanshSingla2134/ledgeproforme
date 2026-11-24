package com.ledgepro.model;

import java.math.BigDecimal;

public class PurchaseItem extends BaseEntity {
    private Long purchaseId;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;

    public PurchaseItem() {
        super();
        this.unitCost = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (this.unitCost != null) {
            this.totalCost = this.unitCost.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
        if (this.unitCost != null) {
            this.totalCost = this.unitCost.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
}
