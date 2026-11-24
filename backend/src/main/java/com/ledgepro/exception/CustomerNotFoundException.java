package com.ledgepro.exception;

public class CustomerNotFoundException extends LedgeProException {
    public CustomerNotFoundException(Long customerId) {
        super("CUSTOMER_NOT_FOUND", "Customer with ID " + customerId + " was not found in Khata records.");
    }
}
