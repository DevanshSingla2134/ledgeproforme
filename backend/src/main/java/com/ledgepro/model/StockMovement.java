package com.ledgepro.model;

public class StockMovement extends BaseEntity {
    private Long businessId;
    private Long productId;
    private String productName;
    private String movementType; // SALE, PURCHASE, RETURN, ADJUSTMENT
    private int quantityChanged; // Negative for sale, positive for purchase
    private int resultingQuantity;
    private String referenceType; // INVOICE, PURCHASE_ORDER, MANUAL
    private Long referenceId;
    private String notes;

    public StockMovement() {
        super();
    }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public int getQuantityChanged() { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged) { this.quantityChanged = quantityChanged; }

    public int getResultingQuantity() { return resultingQuantity; }
    public void setResultingQuantity(int resultingQuantity) { this.resultingQuantity = resultingQuantity; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
