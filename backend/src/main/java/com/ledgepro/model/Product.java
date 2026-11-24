package com.ledgepro.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product model demonstrating Encapsulation, Comparable interface
 * for sorting, and business helper methods.
 */
public class Product extends BaseEntity implements Comparable<Product> {
    private Long businessId;
    private String sku;
    private String barcode;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private int quantity;
    private int minStockLevel;
    private String description;
    private boolean active;
    private LocalDateTime updatedAt;

    public Product() {
        super();
        this.costPrice = BigDecimal.ZERO;
        this.sellingPrice = BigDecimal.ZERO;
        this.quantity = 0;
        this.minStockLevel = 5;
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public Product(Long id, String sku, String name, BigDecimal costPrice, BigDecimal sellingPrice, int quantity, int minStockLevel) {
        super(id);
        this.sku = sku;
        this.name = name;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return this.quantity <= this.minStockLevel;
    }

    public BigDecimal calculateStockValuation() {
        return this.costPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    @Override
    public int compareTo(Product other) {
        if (other == null) return 1;
        return this.name.compareToIgnoreCase(other.name);
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
