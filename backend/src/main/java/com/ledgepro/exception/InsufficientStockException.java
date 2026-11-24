package com.ledgepro.exception;

public class InsufficientStockException extends LedgeProException {
    private final int requested;
    private final int available;

    public InsufficientStockException(String productName, int requested, int available) {
        super("INSUFFICIENT_STOCK", "Cannot fulfill request for '" + productName + "'. Requested: " + requested + ", Available in stock: " + available);
        this.requested = requested;
        this.available = available;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
