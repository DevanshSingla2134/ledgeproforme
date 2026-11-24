package com.ledgepro.exception;

public class DatabaseOperationException extends LedgeProException {
    public DatabaseOperationException(String message, Throwable cause) {
        super("DATABASE_ERROR", message, cause);
    }
}
