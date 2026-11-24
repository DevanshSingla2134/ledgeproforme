package com.ledgepro.exception;

public class ProductNotFoundException extends LedgeProException {
    public ProductNotFoundException(Long productId) {
        super("PRODUCT_NOT_FOUND", "Product with ID " + productId + " was not found in catalog.");
    }

    public ProductNotFoundException(String skuOrName) {
        super("PRODUCT_NOT_FOUND", "Product with identifier '" + skuOrName + "' was not found.");
    }
}
