package com.ledgepro.exception;

public class InvalidPaymentException extends LedgeProException {
    public InvalidPaymentException(String message) {
        super("INVALID_PAYMENT", message);
    }
}
