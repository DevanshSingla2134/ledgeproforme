package com.ledgepro.exception;

/**
 * Base checked/runtime exception class demonstrating OOP inheritance hierarchy
 * for Chitkara University Java Programming coursework.
 */
public class LedgeProException extends RuntimeException {
    private final String errorCode;

    public LedgeProException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LedgeProException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
